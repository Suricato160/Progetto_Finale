package com.guitarCommerce.guitar.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.guitarCommerce.guitar.entity.Product;
import com.guitarCommerce.guitar.service.ProductService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class);
    private static final String UPLOAD_DIR = "static/products/";

    @Autowired
    private ProductService productService;

    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable("id") int id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            model.addAttribute("errorMessage", "Prodotto non trovato.");
            return "redirect:/products";
        }
        model.addAttribute("product", product);
        logger.info("Immagini aggiuntive caricate per il prodotto {}: {}", id, product.getAdditionalImages());
        return "products/productEdit";
    }

    @PostMapping("/update-details")
    public String updateProductDetails(
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            Model model) {
        logger.info("Richiesta POST ricevuta per /admin/products/update-details");
        logger.info("Dati ricevuti - Prodotto: ID={}, Nome={}, Prezzo={}, Stock={}",
                product.getId(), product.getName(), product.getPrice(), product.getStock());

        // Controlla errori di validazione
        if (bindingResult.hasErrors()) {
            logger.error("Errori di validazione: {}", bindingResult.getAllErrors());
            model.addAttribute("errorMessage", "Errore nei dati inseriti. Controlla i campi obbligatori.");
            return "products/productEdit";
        }

        try {
            Product existingProduct = productService.getProductById(product.getId());
            if (existingProduct == null) {
                logger.error("Prodotto non trovato con ID: {}", product.getId());
                model.addAttribute("errorMessage", "Prodotto non trovato.");
                return "products/productEdit";
            }

            // Aggiorna i campi testuali
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setShortDescription(product.getShortDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setStock(product.getStock());
            existingProduct.setBrand(product.getBrand());
            logger.info("Campi testuali aggiornati: {}", existingProduct);

            productService.updateProduct(existingProduct);
            Product updatedProduct = productService.getProductById(product.getId());
            logger.info("Dettagli prodotto aggiornati con successo: {}", updatedProduct);
            model.addAttribute("product", updatedProduct);
            model.addAttribute("successMessage", "Dettagli del prodotto aggiornati con successo.");
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento dei dettagli del prodotto: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento dei dettagli: " + e.getMessage());
            Product productOnError = productService.getProductById(product.getId());
            model.addAttribute("product", productOnError);
        }
        return "products/productEdit";
    }

    @PostMapping("/update-images/{id}")
    public String updateProductImages(
            @PathVariable("id") int id,
            @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestParam(value = "newAdditionalImages", required = false) MultipartFile[] newAdditionalImages,
            Model model) {
        logger.info("Richiesta POST ricevuta per /admin/products/update-images/{}", id);
        logger.info("Main image ricevuta: {}, vuota: {}", mainImage != null ? mainImage.getOriginalFilename() : "null", mainImage == null || mainImage.isEmpty());
        logger.info("Immagini aggiuntive ricevute: {}", newAdditionalImages != null ? newAdditionalImages.length : 0);
    
        try {
            Product product = productService.getProductById(id);
            if (product == null) {
                logger.error("Prodotto non trovato con ID: {}", id);
                model.addAttribute("errorMessage", "Prodotto non trovato.");
                return "redirect:/products";
            }
    
            // Gestione immagini
            String productDir = ResourceUtils.getFile("classpath:" + UPLOAD_DIR + id + "/").getAbsolutePath();
            Path productPath = Paths.get(productDir);
            if (!Files.exists(productPath)) {
                Files.createDirectories(productPath);
                logger.info("Cartella creata: {}", productDir);
            }
    
            // Carica main.jpg se presente
            if (mainImage != null && !mainImage.isEmpty()) {
                mainImage.transferTo(productPath.resolve("main.jpg"));
                logger.info("Main image salvata: {}", productPath.resolve("main.jpg"));
            }
    
            // Carica nuove immagini aggiuntive
            List<String> updatedImages = new ArrayList<>(product.getAdditionalImages() != null ? product.getAdditionalImages() : new ArrayList<>());
            // Calcola il prossimo indice per le immagini aggiuntive
            int thumbIndex = 1; // Inizia da 1 (thumb1.jpg)
            if (!updatedImages.isEmpty()) {
                // Trova l'indice più alto tra le immagini esistenti (escludendo main.jpg)
                for (String imageUrl : updatedImages) {
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
    
            // Aggiungi nuove immagini con il prossimo indice disponibile
            if (newAdditionalImages != null) {
                for (MultipartFile file : newAdditionalImages) {
                    if (file != null && !file.isEmpty()) {
                        String fileName = "thumb" + thumbIndex++ + ".jpg";
                        file.transferTo(productPath.resolve(fileName));
                        logger.info("Immagine aggiuntiva salvata: {}", fileName);
                    }
                }
            }
    
            // Aggiorna la lista delle immagini
            productService.renameImagesInFolder(id);
            product.setAdditionalImages(productService.loadAdditionalImages(id));
            logger.info("Immagini aggiornate con successo per il prodotto {}: {}", id, product.getAdditionalImages());
            model.addAttribute("product", product);
            model.addAttribute("successMessage", "Immagini aggiornate con successo.");
        } catch (IOException e) {
            logger.error("Errore durante il salvataggio delle immagini: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento delle immagini: " + e.getMessage());
            Product productOnError = productService.getProductById(id);
            model.addAttribute("product", productOnError);
        } catch (Exception e) {
            logger.error("Errore generico durante l'aggiornamento delle immagini: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento delle immagini: " + e.getMessage());
            Product productOnError = productService.getProductById(id);
            model.addAttribute("product", productOnError);
        }
        return "products/productEdit";
    }
    
    @PostMapping("/{id}/delete-image")
    public String deleteProductImage(@PathVariable("id") int id, @RequestParam("imageUrl") String imageUrl, Model model) {
        try {
            Product product = productService.getProductById(id);
            if (product == null) {
                model.addAttribute("errorMessage", "Prodotto non trovato.");
                return "redirect:/admin/products/edit/" + id;
            }
    
            logger.info("Tentativo di eliminare immagine con URL: {}", imageUrl);
    
            String fileName = Paths.get(imageUrl).getFileName().toString();
            String filePath = ResourceUtils.getFile("classpath:" + UPLOAD_DIR + id + "/").getAbsolutePath() + "/" + fileName;
            Path path = Paths.get(filePath);
            logger.info("Percorso file da eliminare: {}", path.toAbsolutePath());
    
            if (Files.exists(path)) {
                Files.delete(path);
                logger.info("Immagine eliminata dal filesystem: {}", filePath);
                productService.renameImagesInFolder(id);
                model.addAttribute("successMessage", "Immagine eliminata con successo.");
            } else {
                logger.warn("File non trovato: {}", filePath);
                productService.renameImagesInFolder(id);
                model.addAttribute("successMessage", "Il file non era presente, ma la lista è stata sincronizzata.");
            }
    
            product.setAdditionalImages(productService.loadAdditionalImages(id));
            model.addAttribute("product", product);
            return "products/productEdit";
        } catch (IOException e) {
            logger.error("Errore durante l'eliminazione dell'immagine: {}", e.getMessage());
            model.addAttribute("errorMessage", "Errore durante l'eliminazione dell'immagine: " + e.getMessage());
            Product product = productService.getProductById(id);
            product.setAdditionalImages(productService.loadAdditionalImages(id));
            model.addAttribute("product", product);
            return "products/productEdit";
        }
    }
}