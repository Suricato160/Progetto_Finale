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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.util.Comparator;


@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    private static final String UPLOAD_DIR = "static/products/"; // Relativo al classpath

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);


    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        products.forEach(this::loadProductImages);
        return products;
    }

    public Product getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        loadProductImages(product);
        return product;
    }

    public List<Product> getProductsByCategory(Integer categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        products.forEach(this::loadProductImages);
        return products;
    }

    public List<Product> searchProductsByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        products.forEach(this::loadProductImages);
        return products;
    }

    private void loadProductImages(Product product) {
        product.setAdditionalImages(loadAdditionalImages(product.getId()));
    }

    public List<String> loadAdditionalImages(int productId) {
        List<String> images = new ArrayList<>();
        try {
            String classPath = ResourceUtils.getFile("classpath:").getAbsolutePath();
            String productDirPath = classPath + File.separator + UPLOAD_DIR + productId;
            File productDir = new File(productDirPath);
            if (productDir.exists() && productDir.isDirectory()) {
                File[] files = productDir.listFiles((dir, name) -> name.endsWith(".jpg"));
                if (files != null) {
                    for (File file : files) {
                        String imagePath = "/products/" + productId + "/" + file.getName();
                        images.add(imagePath);
                    }
                }
                images.sort((a, b) -> {
                    if (a.contains("main.jpg")) return -1;
                    if (b.contains("main.jpg")) return 1;
                    return a.compareTo(b);
                });
            }
        } catch (IOException e) {
            logger.error("Errore durante il caricamento delle immagini per il prodotto {}: {}", productId, e.getMessage());
        }
        return images;
    }

    public void renameImagesInFolder(int productId) {
        try {
            String classPath = ResourceUtils.getFile("classpath:").getAbsolutePath();
            String productDirPath = classPath + File.separator + UPLOAD_DIR + productId;
            File productDir = new File(productDirPath);
            if (productDir.exists() && productDir.isDirectory()) {
                List<File> imageFiles = new ArrayList<>();
                for (File file : productDir.listFiles((dir, name) -> name.endsWith(".jpg") && !name.equals("main.jpg"))) {
                    imageFiles.add(file);
                }

                imageFiles.sort(Comparator.comparing(File::getName));

                int thumbIndex = 1;
                for (File file : imageFiles) {
                    String newName = "thumb" + thumbIndex++ + ".jpg";
                    if (!file.getName().equals(newName)) {
                        Path oldPath = file.toPath();
                        Path newPath = productDir.toPath().resolve(newName);
                        Files.move(oldPath, newPath);
                        logger.info("Immagine rinominata: {} -> {}", file.getName(), newName);
                    }
                }

                File mainImage = new File(productDir, "main.jpg");
                if (mainImage.exists()) {
                    List<String> updatedImages = new ArrayList<>();
                    updatedImages.add("/products/" + productId + "/main.jpg");
                    for (int i = 1; i < thumbIndex; i++) {
                        updatedImages.add("/products/" + productId + "/thumb" + i + ".jpg");
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Errore durante la rinominazione delle immagini per il prodotto {}: {}", productId, e.getMessage());
        }
    }

    @Transactional
    public Product createProduct(Product product, Integer categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        product.setCategory(category);
        Product savedProduct = productRepository.save(product);
        renameImagesInFolder(savedProduct.getId());
        return savedProduct;
    }

    @Transactional
    public Product updateProduct(Integer id, Product updatedProduct, Integer categoryId) {
        Product product = getProductById(id);
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
    public void deleteProduct(Integer id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    @Transactional
    public Product updateProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        renameImagesInFolder(product.getId());
        return savedProduct;
    }


     // Crea un nuovo prodotto
     @Transactional
     public Product createProduct(Product product) {
         logger.info("Creazione del nuovo prodotto: {}", product);
         Product savedProduct = productRepository.save(product);
         logger.info("Prodotto creato con ID: {}", savedProduct.getId());
         return savedProduct;
     }


     public List<Product> getProductsByCategory(int categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
}