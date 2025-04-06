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

    private static final String BASE_PATH = "/products/";
    private static final String UPLOAD_DIR = "static/products/"; // Relativo al classpath

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
            String folderPath = "classpath:" + UPLOAD_DIR + productId + "/";
            File folder = ResourceUtils.getFile(folderPath);

            System.out.println("Tentativo di caricare immagini dalla cartella: " + folder.getAbsolutePath());
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return lowerName.endsWith(".jpg") || lowerName.endsWith(".png") || lowerName.endsWith(".jpeg");
                });
                if (files != null && files.length > 0) {
                    Arrays.sort(files);
                    for (File file : files) {
                        images.add(BASE_PATH + productId + "/" + file.getName());
                        System.out.println("Immagine trovata: " + file.getName());
                    }
                } else {
                    System.out.println("Nessun file immagine trovato in: " + folderPath);
                }
            } else {
                System.out.println("Cartella non trovata o non è una directory: " + folderPath);
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento delle immagini per il prodotto " + productId + ": " + e.getMessage());
        }
        return images;
    }

    public void renameImagesInFolder(int productId) {
        try {
            String folderPath = ResourceUtils.getFile("classpath:" + UPLOAD_DIR + productId + "/").getAbsolutePath();
            Path folder = Paths.get(folderPath);

            System.out.println("Tentativo di rinominare immagini nella cartella: " + folderPath);
            if (!Files.exists(folder) || !Files.isDirectory(folder)) {
                System.out.println("Cartella non trovata: " + folderPath);
                return;
            }

            File[] files = folder.toFile().listFiles((dir, name) -> {
                String lowerName = name.toLowerCase();
                return lowerName.endsWith(".jpg") || lowerName.endsWith(".png") || lowerName.endsWith(".jpeg");
            });

            if (files == null || files.length == 0) {
                System.out.println("Nessun file trovato nella cartella: " + folderPath);
                return;
            }

            Arrays.sort(files);
            List<File> fileList = new ArrayList<>(Arrays.asList(files));

            if (fileList.size() == 1) {
                File file = fileList.get(0);
                Path newPath = folder.resolve("main.jpg");
                if (!file.getName().equals("main.jpg")) {
                    Files.move(file.toPath(), newPath);
                    System.out.println("Rinominato " + file.getName() + " in main.jpg");
                }
            } else if (fileList.size() > 1) {
                File firstFile = fileList.get(0);
                Path mainPath = folder.resolve("main.jpg");
                if (!firstFile.getName().equals("main.jpg")) {
                    Files.move(firstFile.toPath(), mainPath);
                    System.out.println("Rinominato " + firstFile.getName() + " in main.jpg");
                }
                fileList.remove(0);

                int thumbIndex = 1;
                for (File file : fileList) {
                    String newName = "thumb" + thumbIndex++ + ".jpg";
                    Path newPath = folder.resolve(newName);
                    if (!file.getName().equals(newName)) {
                        Files.move(file.toPath(), newPath);
                        System.out.println("Rinominato " + file.getName() + " in " + newName);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Errore durante la rinominazione dei file per il prodotto " + productId + ": " + e.getMessage());
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
}