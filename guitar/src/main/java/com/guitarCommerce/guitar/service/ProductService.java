package com.guitarCommerce.guitar.service;

import com.guitarCommerce.guitar.entity.Category;
import com.guitarCommerce.guitar.entity.Product;
import com.guitarCommerce.guitar.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// da commentare  -  rifattorializzato--------------------------------

@Service
public class ProductService {

    // dipendenze
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    // ========================================================================
    // logger
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    // classpath per le immagini
    private static final String BASE_DIR = "static/products/"; // Relativo al classpath

    // Restituisce la directory del prodotto come oggetto File usando una stringa
    // base
    private File getProductDirPath(int productId) {
        try {
            String basePath = ResourceUtils.getFile("classpath:" + BASE_DIR).getAbsolutePath();
            File dir = new File(basePath + File.separator + productId);
            logger.debug("Percorso directory prodotto {}: {}", productId, dir.getAbsolutePath());
            return dir;
        } catch (FileNotFoundException e) {
            logger.error("Errore nel trovare il classpath: {}", e.getMessage());
            return new File(BASE_DIR + File.separator + productId); // Fallback
        }
    }

    // ------- utility ------

    // per ogni prodotto carico le immagini
    private void loadImagesForProducts(List<Product> products) {
        for (Product product : products) {
            loadProductImages(product);
        }
    }

    // carico le immagini del prodotto
    private void loadProductImages(Product product) {
        // se l'oggetto prodotto è vuoto
        if (product == null) {
            logger.warn("Prodotto nullo trovato durante il caricamento delle immagini");
            return;
        }
        // carico le immagini aggiuntive del prodotto e le associo
        product.setAdditionalImages(loadAdditionalImages(product.getId()));
        // logger.info("Prodotto: {}, Immagini: {}", product.getName() != null ?
    }

    // ----------------------
    // Carica la lista degli URL delle immagini associate a un prodotto
    public List<String> loadAdditionalImages(int productId) {
        // Creo una lista vuota per gli URL delle immagini
        List<String> images = new ArrayList<>();

        try {
            // Ottengo la directory del prodotto usando la stringa base
            File productDir = getProductDirPath(productId);
            logger.debug("Controllo esistenza directory: {}", productDir.exists());

            // Controllo se la directory esiste ed è una directory
            if (productDir.exists() && productDir.isDirectory()) {
                // Recupero tutti i file nella directory
                File[] files = productDir.listFiles();
                // Se non ci sono file, restituisco la lista vuota
                if (files == null) {
                    logger.warn("Nessun file trovato nella directory: {}", productDir.getAbsolutePath());

                    return images;
                }

                // Itero sui file per trovare le immagini .jpg
                for (File file : files) {
                    String fileName = file.getName();
                    // Aggiungo solo i file che terminano con .jpg
                    if (fileName.endsWith(".jpg")) {
                        // Costruisco l'URL pubblico dell'immagine
                        String imagePath = "/products/" + productId + "/" + fileName;
                        images.add(imagePath);
                        logger.debug("Immagine aggiunta: {}", imagePath);

                    }
                }

                // Metto "main.jpg" al primo posto manualmente
                String mainImage = null;
                for (int i = 0; i < images.size(); i++) {
                    if (images.get(i).contains("main.jpg")) {
                        mainImage = images.get(i);
                        images.remove(i);
                        break;
                    }
                }
                if (mainImage != null) {
                    images.add(0, mainImage);
                    logger.debug("Main image spostata in cima: {}", mainImage);
                }
            } else {
                logger.warn("Directory non esistente o non valida: {}", productDir.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Errore caricamento immagini per prodotto {}: {}", productId, e.getMessage(), e);
        }
        logger.info("Immagini caricate per prodotto {}: {}", productId, images);
        return images;
    }

    // -------------------------------

    // Rinomina le immagini nella directory del prodotto in modo sequenziale
    @Transactional
    public void renameImagesInFolder(int productId) {
        try {
            // Ottengo la directory del prodotto
            File productDir = getProductDirPath(productId);
            // Controllo se la directory esiste ed è una directory
            if (productDir.exists() && productDir.isDirectory()) {
                // Creo una lista per i file immagine escluso "main.jpg"
                List<File> imageFiles = new ArrayList<>();
                for (File file : productDir.listFiles()) {
                    String name = file.getName();
                    if (name.endsWith(".jpg") && !name.equals("main.jpg")) {
                        imageFiles.add(file);
                    }
                }

                // Ordino i file per nome
                imageFiles.sort(Comparator.comparing(File::getName));

                // Rinomino i file con un indice progressivo
                int thumbIndex = 1;
                for (File file : imageFiles) {
                    String newName = "thumb" + thumbIndex++ + ".jpg";
                    if (!file.getName().equals(newName)) {
                        File newFile = new File(productDir, newName);
                        file.renameTo(newFile);
                        logger.info("Immagine rinominata: {} -> {}", file.getName(), newName);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Errore durante la rinominazione delle immagini per il prodotto " + productId + ": "
                    + e.getMessage());
        }
    }

    // -------------------------------------

    // Elimina un prodotto dal database e tutti i file immagine associati
    @Transactional
    public void deleteProductAndImages(Integer id) {
        // Recupero il prodotto dal database
        Product product = getProductById(id);
        if (product == null) {
            return;
        }

        try {
            // Ottengo la directory del prodotto
            File productDir = getProductDirPath(id);
            // Controllo se la directory esiste
            if (productDir.exists() && productDir.isDirectory()) {
                // Elimino tutti i file nella directory
                File[] files = productDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
                // Elimino la directory stessa
                productDir.delete();
                logger.info("Cartella delle immagini eliminata: {}", productDir.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Errore durante l'eliminazione delle immagini per il prodotto " + id + ": " + e.getMessage());
        }

        // Elimino il prodotto dal database
        productRepository.delete(product);
        logger.info("Prodotto eliminato con successo: ID={}", id);
    }

    // -------------------------------------

    
    // Calcola il prossimo indice disponibile per i file "thumbX.jpg" x è l'indice che mi serve

    private int getNextThumbIndex(List<String> images) {
        // Inizializzo l'indice della thumbnail a 1
        int thumbIndex = 1;
    
        // Se la lista delle immagini non è vuota, procedo con l'analisi
        if (!images.isEmpty()) {
            // Itero attraverso ogni URL delle immagini nella lista
            for (String imageUrl : images) {
                // Estaggo il nome del file dall'URL dell'immagine
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    
                // Verifico se il nome del file inizia con "thumb", indicando che è una thumbnail
                if (fileName.startsWith("thumb")) {
                    // Rimuovo il prefisso "thumb" e l'estensione ".jpg" per ottenere l'indice corrente
                    String indexStr = fileName.replace("thumb", "").replace(".jpg", "");
    
                    try {
                        // Converto l'indice corrente in un numero intero
                        int currentIndex = Integer.parseInt(indexStr);
    
                        // Aggiorno l'indice della thumbnail con il massimo tra quello corrente e quello trovato + 1
                        thumbIndex = Math.max(thumbIndex, currentIndex + 1);
                    } catch (NumberFormatException e) {
                        // Se il nome del file non è valido, registro un avviso
                        logger.warn("Nome file non valido: {}", fileName);
                    }
                }
            }
        }
    
        // Restituisco l'indice della prossima thumbnail
        return thumbIndex;
    }
    

    // -------------------------------------
    
    // Aggiorna le immagini di un prodotto esistente
    @Transactional
    public Product updateProductImages(int id, MultipartFile mainImage, MultipartFile[] newAdditionalImages) {
        // Recupero il prodotto
        Product product = getProductById(id);
        if (product == null) {
            return null;
        }

        try {
            // Ottengo la directory del prodotto
            File productDir = getProductDirPath(id);
            // Creo la directory se non esiste
            if (!productDir.exists()) {
                productDir.mkdirs();
                logger.info("Cartella creata: {}", productDir.getAbsolutePath());
            }

            // Salvo la nuova immagine principale, se presente
            if (mainImage != null && !mainImage.isEmpty()) {
                File mainImageFile = new File(productDir, "main.jpg");
                mainImage.transferTo(mainImageFile);
                logger.info("Main image salvata: {}", mainImageFile.getAbsolutePath());
            }

            // Preparo la lista delle immagini esistenti
            List<String> updatedImages = new ArrayList<>(
                    product.getAdditionalImages() != null ? product.getAdditionalImages() : new ArrayList<>());
            int thumbIndex = getNextThumbIndex(updatedImages);

            // Salvo le nuove immagini aggiuntive
            if (newAdditionalImages != null) {
                for (MultipartFile file : newAdditionalImages) {
                    if (file != null && !file.isEmpty()) {
                        String fileName = "thumb" + thumbIndex++ + ".jpg";
                        File targetFile = new File(productDir, fileName);
                        file.transferTo(targetFile);
                        logger.info("Immagine aggiuntiva salvata: {}", fileName);
                    }
                }
            }

            // Rinomino le immagini e aggiorno il prodotto
            renameImagesInFolder(id);
            product.setAdditionalImages(loadAdditionalImages(id));
            return productRepository.save(product);
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento delle immagini per il prodotto " + id + ": " + e.getMessage());
            return null;
        }
    }

    // ========================================================================
    // ------- cerco prodotti ------

    // carico tutta la lista dei prodotti e ci associo le immagini
    public List<Product> getAllProducts() {
        // lista di tutti i prodotti
        List<Product> products = productRepository.findAll();
        // per ogni prodotto associo le immagini
        loadImagesForProducts(products);
        return products;
    }

    // cerco un prodotto con l'id
    public Product getProductById(Integer id) {
        Product product = productRepository.findById(id).orElse(null); // Restituisco null invece di lanciare eccezione
                                                                       // per gestione nel controller
        if (product != null) {
            loadProductImages(product);
        }
        return product;
    }

    // carico una lista di prodotti in base alla categoria
    public List<Product> getProductsByCategory(Integer categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        loadImagesForProducts(products);
        return products;
    }



    // ========================================================================
    // ----- creo prodotti -------------

    @Transactional
    public Product createProduct(Product product, Integer categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        product.setCategory(category);
        Product savedProduct = productRepository.save(product);
        renameImagesInFolder(savedProduct.getId());
        return savedProduct;
    }

  

    // Crea un nuovo prodotto con immagini
    @Transactional
    public Product createProductWithImages(Product product, Integer categoryId, MultipartFile mainImage,
            MultipartFile[] newAdditionalImages) {
        // Associa la categoria, se specificata
        if (categoryId != null) {
            Category category = categoryService.getCategoryById(categoryId);
            product.setCategory(category);
        }

        // Salvo il prodotto per ottenere l'ID
        Product savedProduct = productRepository.save(product);
        logger.info("Prodotto salvato con ID: {}", savedProduct.getId());

        try {
            // Ottengo la directory del prodotto
            File productDir = getProductDirPath(savedProduct.getId());
            // Creo la directory se non esiste
            if (!productDir.exists()) {
                productDir.mkdirs();
                logger.info("Cartella creata: {}", productDir.getAbsolutePath());
            }

            // Salvo l'immagine principale, se presente
            if (mainImage != null && !mainImage.isEmpty()) {
                File mainImageFile = new File(productDir, "main.jpg");
                mainImage.transferTo(mainImageFile);
                logger.info("Main image salvata: {}", mainImageFile.getAbsolutePath());
            }

            // Salvo le immagini aggiuntive con indici progressivi
            int thumbIndex = 1;
            if (newAdditionalImages != null) {
                for (MultipartFile file : newAdditionalImages) {
                    if (file != null && !file.isEmpty()) {
                        String fileName = "thumb" + thumbIndex++ + ".jpg";
                        File targetFile = new File(productDir, fileName);
                        file.transferTo(targetFile);
                        logger.info("Immagine aggiuntiva salvata: {}", fileName);
                    }
                }
            }

            // Aggiorno la lista delle immagini e salvo il prodotto
            savedProduct.setAdditionalImages(loadAdditionalImages(savedProduct.getId()));
            return productRepository.save(savedProduct);
        } catch (Exception e) {
            logger.error("Errore durante la creazione del prodotto con immagini " + savedProduct.getId() + ": "
                    + e.getMessage());
            return null;
        }
    }

    // ========================================================================
    // ----- cancello prodotti -------------

    @Transactional
    public void deleteProduct(Integer id) {
        Product product = getProductById(id);
        if (product != null) {
            productRepository.delete(product);
        }
    }

    // =============================================================================
    // metodi di aggiornamenti

    @Transactional
    public Product updateProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        renameImagesInFolder(product.getId());
        return savedProduct;
    }

    @Transactional
    public Product updateProduct(Integer id, Product updatedProduct, Integer categoryId) {
        Product product = getProductById(id);
        if (product == null)
            return null;
        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setStock(updatedProduct.getStock());
        if (categoryId != null) {
            Category category = categoryService.getCategoryById(categoryId);
            product.setCategory(category);
        }
        Product savedProduct = productRepository.save(product);
        renameImagesInFolder(id);
        return savedProduct;
    }

    @Transactional
    public Product updateProductDetails(Integer id, Product updatedProduct, Integer categoryId) {
        Product product = getProductById(id);
        if (product == null)
            return null;
        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setShortDescription(updatedProduct.getShortDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setStock(updatedProduct.getStock());
        product.setBrand(updatedProduct.getBrand());

        if (categoryId != null) {
            Category category = categoryService.getCategoryById(categoryId);
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        logger.info("Dettagli prodotto aggiornati con successo: {}", savedProduct);
        return savedProduct;
    }

}