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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// da commentare  -  rifattorializzato

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
    private static final String UPLOAD_DIR = "static/products/";

    // Metodo helper per ottenere il percorso della directory del prodotto
    private Path getProductDirPath(int productId) throws IOException {
        String classPath = ResourceUtils.getFile("classpath:").getAbsolutePath();
        return Paths.get(classPath + File.separator + UPLOAD_DIR + productId);
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
        // logger.info("Prodotto: {}, Immagini: {}", product.getName() != null ? product.getName() : "Nome non disponibile", product.getAdditionalImages());
    }

    // creo la lista dei path delle immagini
    public List<String> loadAdditionalImages(int productId) {
        // apro la lista
        List<String> images = new ArrayList<>();
        try {
            // ottengo il percorso della directory associata al prodotto
            Path productDirPath = getProductDirPath(productId);
            File productDir = productDirPath.toFile();
            if (productDir.exists() && productDir.isDirectory()) {
                File[] files = productDir.listFiles((dir, name) -> name.endsWith(".jpg"));
                if (files != null) {
                    for (File file : files) {
                        String imagePath = "/products/" + productId + "/" + file.getName();
                        images.add(imagePath);
                    }
                }
                images.sort((a, b) -> {
                    if (a.contains("main.jpg"))
                        return -1;
                    if (b.contains("main.jpg"))
                        return 1;
                    return a.compareTo(b);
                });
            }
        } catch (IOException e) {
            logger.error("Errore durante il caricamento delle immagini per il prodotto {}: {}", productId,
                    e.getMessage());
        }
        return images;
    }

    // -------------------------------

    @Transactional
    public void renameImagesInFolder(int productId) {
        try {
            Path productDirPath = getProductDirPath(productId);
            File productDir = productDirPath.toFile();
            if (productDir.exists() && productDir.isDirectory()) {
                List<File> imageFiles = new ArrayList<>();
                for (File file : productDir
                        .listFiles((dir, name) -> name.endsWith(".jpg") && !name.equals("main.jpg"))) {
                    imageFiles.add(file);
                }

                imageFiles.sort(Comparator.comparing(File::getName));

                int thumbIndex = 1;
                for (File file : imageFiles) {
                    String newName = "thumb" + thumbIndex++ + ".jpg";
                    if (!file.getName().equals(newName)) {
                        Path oldPath = file.toPath();
                        Path newPath = productDirPath.resolve(newName);
                        Files.move(oldPath, newPath);
                        logger.info("Immagine rinominata: {} -> {}", file.getName(), newName);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Errore durante la rinominazione delle immagini per il prodotto {}: {}", productId,
                    e.getMessage());
        }
    }

    // -------------------------------------

    @Transactional
    public void deleteProductAndImages(Integer id) throws IOException {
        Product product = getProductById(id);
        if (product == null)
            return;

        Path productPath = getProductDirPath(id);
        if (Files.exists(productPath)) {
            Files.walk(productPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            logger.info("Cartella delle immagini eliminata: {}", productPath);
        }

        productRepository.delete(product);
        logger.info("Prodotto eliminato con successo: ID={}", id);
    }

    // -------------------------------------

    private int getNextThumbIndex(List<String> images) {
        int thumbIndex = 1;
        if (!images.isEmpty()) {
            for (String imageUrl : images) {
                String fileName = Paths.get(imageUrl).getFileName().toString();
                if (fileName.startsWith("thumb")) {
                    String indexStr = fileName.replace("thumb", "").replace(".jpg", "");
                    try {
                        int currentIndex = Integer.parseInt(indexStr);
                        thumbIndex = Math.max(thumbIndex, currentIndex + 1);
                    } catch (NumberFormatException e) {
                        logger.warn("Nome file non valido: {}", fileName);
                    }
                }
            }
        }
        return thumbIndex;
    }

    // -------------------------------------
    @Transactional
    public Product updateProductImages(int id, MultipartFile mainImage, MultipartFile[] newAdditionalImages)
            throws IOException {
        Product product = getProductById(id);
        if (product == null)
            return null;

        Path productPath = getProductDirPath(id);
        if (!Files.exists(productPath)) {
            Files.createDirectories(productPath);
            logger.info("Cartella creata: {}", productPath);
        }

        if (mainImage != null && !mainImage.isEmpty()) {
            mainImage.transferTo(productPath.resolve("main.jpg"));
            logger.info("Main image salvata: {}", productPath.resolve("main.jpg"));
        }

        List<String> updatedImages = new ArrayList<>(
                product.getAdditionalImages() != null ? product.getAdditionalImages() : new ArrayList<>());
        int thumbIndex = getNextThumbIndex(updatedImages);

        if (newAdditionalImages != null) {
            for (MultipartFile file : newAdditionalImages) {
                if (file != null && !file.isEmpty()) {
                    String fileName = "thumb" + thumbIndex++ + ".jpg";
                    file.transferTo(productPath.resolve(fileName));
                    logger.info("Immagine aggiuntiva salvata: {}", fileName);
                }
            }
        }

        renameImagesInFolder(id);
        product.setAdditionalImages(loadAdditionalImages(id));
        return productRepository.save(product);
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
        Product product = productRepository.findById(id).orElse(null); // Restituisco null invece di lanciare eccezione per gestione nel controller
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
    
    // carico 
    // public List<Product> searchProductsByName(String name) {
    //     List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
    //     loadImagesForProducts(products);
    //     return products;
    // }
    

    

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

    @Transactional
    public Product createProduct(Product product) {
        logger.info("Creazione del nuovo prodotto: {}", product);
        Product savedProduct = productRepository.save(product);
        logger.info("Prodotto creato con ID: {}", savedProduct.getId());
        return savedProduct;
    }

    @Transactional
    public Product createProductWithImages(Product product, Integer categoryId, MultipartFile mainImage,
            MultipartFile[] newAdditionalImages) throws IOException {
        if (categoryId != null) {
            Category category = categoryService.getCategoryById(categoryId);
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        logger.info("Prodotto salvato con ID: {}", savedProduct.getId());

        Path productPath = getProductDirPath(savedProduct.getId());
        if (!Files.exists(productPath)) {
            Files.createDirectories(productPath);
            logger.info("Cartella creata: {}", productPath);
        }

        if (mainImage != null && !mainImage.isEmpty()) {
            mainImage.transferTo(productPath.resolve("main.jpg"));
            logger.info("Main image salvata: {}", productPath.resolve("main.jpg"));
        }

        int thumbIndex = 1;
        if (newAdditionalImages != null) {
            for (MultipartFile file : newAdditionalImages) {
                if (file != null && !file.isEmpty()) {
                    String fileName = "thumb" + thumbIndex++ + ".jpg";
                    file.transferTo(productPath.resolve(fileName));
                    logger.info("Immagine aggiuntiva salvata: {}", fileName);
                }
            }
        }

        savedProduct.setAdditionalImages(loadAdditionalImages(savedProduct.getId()));
        return productRepository.save(savedProduct);
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