package com.guitarCommerce.guitar.service;

import com.guitarCommerce.guitar.entity.Category;
import com.guitarCommerce.guitar.entity.Product;
import com.guitarCommerce.guitar.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    private static final String UPLOAD_DIR = "src/main/resources/static/images/products/";

    // Trova tutti i prodotti
    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        products.forEach(this::loadProductImages);
        return products;
    }

    // Trova un prodotto per ID
    public Product getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        loadProductImages(product);
        return product;
    }

    // Trova prodotti per categoria
    public List<Product> getProductsByCategory(Integer categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        products.forEach(this::loadProductImages);
        return products;
    }

    // Cerca prodotti per nome
    public List<Product> searchProductsByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        products.forEach(this::loadProductImages);
        return products;
    }

    // Carica le immagini dalla cartella
    private void loadProductImages(Product product) {
        List<String> imageFiles = new ArrayList<>();
        try {
            String folderPath = "classpath:static" + product.getImageUrl();
            File folder = ResourceUtils.getFile(folderPath);

            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return lowerName.endsWith(".jpg") || lowerName.endsWith(".png") || lowerName.endsWith(".jpeg");
                });
                if (files != null) {
                    Arrays.sort(files);
                    for (File file : files) {
                        imageFiles.add(product.getImageUrl() + "/" + file.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento delle immagini per " + product.getImageUrl() + ": " + e.getMessage());
        }
        product.setAdditionalImages(imageFiles);
    }

    // Carica le immagini aggiuntive per AdminProductController
    public List<String> loadAdditionalImages(int productId) {
        Product product = getProductById(productId);
        if (product == null || product.getImageUrl() == null) {
            System.err.println("Prodotto o imageUrl nullo per ID: " + productId);
            return new ArrayList<>();
        }
        List<String> images = loadImagesFromFolder(product.getImageUrl());
        System.out.println("Immagini caricate dal filesystem per " + product.getImageUrl() + ": " + images);
        return images;
    }
    
    private List<String> loadImagesFromFolder(String imageUrl) {
        List<String> images = new ArrayList<>();
        try {
            String folderPath = "classpath:static" + imageUrl;
            File folder = ResourceUtils.getFile(folderPath);
    
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return lowerName.endsWith(".jpg") || lowerName.endsWith(".png") || lowerName.endsWith(".jpeg");
                });
                if (files != null) {
                    Arrays.sort(files);
                    for (File file : files) {
                        images.add(imageUrl + "/" + file.getName());
                    }
                } else {
                    System.err.println("Nessun file trovato nella cartella: " + folderPath);
                }
            } else {
                System.err.println("Cartella non trovata o non è una directory: " + folderPath);
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento delle immagini aggiuntive per " + imageUrl + ": " + e.getMessage());
        }
        return images;
    }

    // Metodo per rinominare i file nella cartella
    public void renameImagesInFolder(int productId) {
        Product product = getProductById(productId);
        String folderPath = UPLOAD_DIR + productId + "/";
        Path folder = Paths.get(folderPath);

        try {
            if (!Files.exists(folder) || !Files.isDirectory(folder)) {
                return; // Nessuna cartella, niente da fare
            }

            // Elenca tutti i file immagine
            File[] files = folder.toFile().listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".jpg") || lowerName.endsWith(".png") || lowerName.endsWith(".jpeg");
            });

            if (files == null || files.length == 0) {
                return; // Nessun file, niente da fare
            }

            Arrays.sort(files); // Ordina i file per nome
            List<File> fileList = new ArrayList<>(Arrays.asList(files));

            // Caso 1: Una sola immagine -> Rinomina a main.jpg
            if (fileList.size() == 1) {
                File file = fileList.get(0);
                Path newPath = folder.resolve("main.jpg");
                if (!file.getName().equals("main.jpg")) {
                    Files.move(file.toPath(), newPath);
                    System.out.println("Rinominato " + file.getName() + " in main.jpg");
                }
            } 
            // Caso 2: Più immagini -> Assicurati che ci sia main.jpg e rinomina gli altri
            else if (fileList.size() > 1) {
                boolean hasMain = false;
                File mainFile = null;

                // Cerca main.jpg
                for (File file : fileList) {
                    if (file.getName().equals("main.jpg")) {
                        hasMain = true;
                        mainFile = file;
                        break;
                    }
                }

                // Se non c’è main.jpg, rinomina il primo file
                if (!hasMain) {
                    File firstFile = fileList.get(0);
                    Path newMainPath = folder.resolve("main.jpg");
                    Files.move(firstFile.toPath(), newMainPath);
                    fileList.set(0, newMainPath.toFile());
                    System.out.println("Rinominato " + firstFile.getName() + " in main.jpg");
                }

                // Rinomina le immagini aggiuntive come thumb1.jpg, thumb2.jpg, ecc.
                int thumbIndex = 1;
                for (int i = 0; i < fileList.size(); i++) {
                    File file = fileList.get(i);
                    if (!file.getName().equals("main.jpg")) {
                        String newName = "thumb" + thumbIndex++ + ".jpg";
                        Path newPath = folder.resolve(newName);
                        if (!file.getName().equals(newName)) {
                            Files.move(file.toPath(), newPath);
                            fileList.set(i, newPath.toFile());
                            System.out.println("Rinominato " + file.getName() + " in " + newName);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Errore durante la rinominazione dei file per il prodotto " + productId + ": " + e.getMessage());
        }
    }

    // Crea un nuovo prodotto
    @Transactional
    public Product createProduct(Product product, Integer categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        product.setCategory(category);
        Product savedProduct = productRepository.save(product);
        renameImagesInFolder(savedProduct.getId()); // Rinomina dopo la creazione
        return savedProduct;
    }

    // Aggiorna un prodotto esistente
    @Transactional
    public Product updateProduct(Integer id, Product updatedProduct, Integer categoryId) {
        Product product = getProductById(id);
        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setStock(updatedProduct.getStock());
        product.setImageUrl(updatedProduct.getImageUrl());
        if (categoryId != null) {
            Category category = categoryService.getCategoryById(categoryId);
            product.setCategory(category);
        }
        Product savedProduct = productRepository.save(product);
        renameImagesInFolder(id); // Rinomina dopo l'aggiornamento
        return savedProduct;
    }

    // Elimina un prodotto
    @Transactional
    public void deleteProduct(Integer id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    // Metodo per updateProduct senza categoria
    @Transactional
    public Product updateProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        renameImagesInFolder(product.getId()); // Rinomina dopo l'aggiornamento
        return savedProduct;
    }
}