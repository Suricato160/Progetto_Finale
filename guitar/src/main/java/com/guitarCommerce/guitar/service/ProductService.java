package com.guitarCommerce.guitar.service;

import com.guitarCommerce.guitar.entity.Category;
import com.guitarCommerce.guitar.entity.Product;
import com.guitarCommerce.guitar.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    // Trova tutti i prodotti
    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        products.forEach(this::loadProductImages); // Carica le immagini per ogni prodotto
        return products;
    }

    // Trova un prodotto per ID
    public Product getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        loadProductImages(product); // Carica le immagini per il prodotto
        return product;
    }

    // Trova prodotti per categoria
    public List<Product> getProductsByCategory(Integer categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        products.forEach(this::loadProductImages); // Carica le immagini
        return products;
    }

    // Cerca prodotti per nome
    public List<Product> searchProductsByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        products.forEach(this::loadProductImages); // Carica le immagini
        return products;
    }

    // Metodo per caricare le immagini dalla cartella
    private void loadProductImages(Product product) {
        List<String> imageFiles = new ArrayList<>();
        try {
            // Costruisci il percorso della cartella (es. classpath:static/products/004/)
            String folderPath = "classpath:static" + product.getImageUrl();
            File folder = ResourceUtils.getFile(folderPath);

            if (folder.exists() && folder.isDirectory()) {
                // Elenca tutti i file nella cartella
                File[] files = folder.listFiles((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return lowerName.endsWith(".jpg") || lowerName.endsWith(".png") || lowerName.endsWith(".jpeg");
                });
                if (files != null) {
                    Arrays.sort(files); // Ordina i file per nome
                    for (File file : files) {
                        // Aggiungi il nome del file al percorso base (es. /products/004/immagine1.jpg)
                        imageFiles.add(product.getImageUrl() + "/" + file.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento delle immagini per " + product.getImageUrl() + ": " + e.getMessage());
        }
        product.setAdditionalImages(imageFiles);
    }

    // Crea un nuovo prodotto
    @Transactional
    public Product createProduct(Product product, Integer categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        product.setCategory(category);
        return productRepository.save(product);
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
        return productRepository.save(product);
    }

    // Elimina un prodotto
    @Transactional
    public void deleteProduct(Integer id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}