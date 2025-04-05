package com.guitarCommerce.guitar.controller;

import java.io.File;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.guitarCommerce.guitar.entity.Product;
import com.guitarCommerce.guitar.service.ProductService;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class);
    private static final String UPLOAD_DIR = "src/main/resources/static/images/products/";

    @Autowired
    private ProductService productService;

    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable("id") int id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            model.addAttribute("errorMessage", "Prodotto non trovato.");
            return "redirect:/products";
        }
        product.setAdditionalImages(productService.loadAdditionalImages(id));
        model.addAttribute("product", product);
        logger.info("Immagini aggiuntive caricate per il prodotto {}: {}", id, product.getAdditionalImages());
        return "products/productEdit";
    }

    @PostMapping("/update")
    public String updateProduct(
            @ModelAttribute("product") Product product,
            @RequestParam("mainImage") MultipartFile mainImage,
            @RequestParam("newAdditionalImages") MultipartFile[] newAdditionalImages,
            @RequestParam(value = "existingImages", required = false) List<String> existingImages,
            Model model) {
        try {
            Product existingProduct = productService.getProductById(product.getId());
            if (existingProduct == null) {
                model.addAttribute("errorMessage", "Prodotto non trovato.");
                return "products/productEdit";
            }

            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setShortDescription(product.getShortDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setStock(product.getStock());
            existingProduct.setBrand(product.getBrand());

            String productDir = UPLOAD_DIR + product.getId() + "/";
            Path productPath = Paths.get(productDir);
            if (!Files.exists(productPath)) {
                Files.createDirectories(productPath);
            }
            if (!mainImage.isEmpty()) {
                mainImage.transferTo(productPath.resolve("main.jpg"));
                existingProduct.setImageUrl("/images/products/" + product.getId());
            }

            List<String> updatedImages = new ArrayList<>(existingImages != null ? existingImages : new ArrayList<>());
            int thumbIndex = updatedImages.size() + 1;
            for (MultipartFile file : newAdditionalImages) {
                if (!file.isEmpty()) {
                    String fileName = "thumb" + thumbIndex++ + ".jpg";
                    file.transferTo(productPath.resolve(fileName));
                    updatedImages.add(existingProduct.getImageUrl() + "/" + fileName);
                }
            }

            productService.updateProduct(existingProduct);
            Product updatedProduct = productService.getProductById(product.getId());
            updatedProduct.setAdditionalImages(productService.loadAdditionalImages(product.getId()));
            model.addAttribute("product", updatedProduct);
            model.addAttribute("successMessage", "Prodotto aggiornato con successo.");
        } catch (IOException e) {
            logger.error("Errore durante il salvataggio delle immagini: {}", e.getMessage());
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento del prodotto.");
            Product productOnError = productService.getProductById(product.getId());
            productOnError.setAdditionalImages(productService.loadAdditionalImages(product.getId()));
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

        // Costruisci il percorso usando ResourceUtils per il classpath
        String filePath = "classpath:static" + imageUrl;
        File fileToDelete = ResourceUtils.getFile(filePath);
        Path path = fileToDelete.toPath();

        if (Files.exists(path)) {
            Files.delete(path);
            logger.info("Immagine eliminata dal filesystem: {}", filePath);
            productService.renameImagesInFolder(id); // Rinomina i file dopo l'eliminazione
            model.addAttribute("successMessage", "Immagine eliminata con successo.");
        } else {
            logger.warn("File non trovato: {}", filePath);
            productService.renameImagesInFolder(id); // Rinomina comunque per sincronizzare
            model.addAttribute("successMessage", "Il file non era presente, ma la lista è stata sincronizzata.");
        }

        product.setAdditionalImages(productService.loadAdditionalImages(id));
        model.addAttribute("product", product);
        return "products/productEdit";
    } catch (IOException e) {
        logger.error("Errore durante l'eliminazione dell'immagine: {}", e.getMessage());
        model.addAttribute("errorMessage", "Errore durante l'eliminazione dell'immagine.");
        Product product = productService.getProductById(id);
        product.setAdditionalImages(productService.loadAdditionalImages(id));
        model.addAttribute("product", product);
        return "products/productEdit";
    }
}
}