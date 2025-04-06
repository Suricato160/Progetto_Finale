package com.guitarCommerce.guitar.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
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

import com.guitarCommerce.guitar.entity.Category;
import com.guitarCommerce.guitar.entity.Product;
import com.guitarCommerce.guitar.service.CategoryService;
import com.guitarCommerce.guitar.service.ProductService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class);
    private static final String UPLOAD_DIR = "static/products/";

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable("id") int id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            model.addAttribute("errorMessage", "Prodotto non trovato.");
            return "redirect:/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories()); // Aggiungi le categorie al modello
        logger.info("Immagini aggiuntive caricate per il prodotto {}: {}", id, product.getAdditionalImages());
        return "products/productEdit";
    }

    @PostMapping("/update-details")
    public String updateProductDetails(
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            @RequestParam("categoryId") Integer categoryId,
            Model model) {
        logger.info("Richiesta POST ricevuta per /admin/products/update-details");
        logger.info("Dati ricevuti - Prodotto: ID={}, Nome={}, Prezzo={}, Stock={}, Categoria ID={}",
                product.getId(), product.getName(), product.getPrice(), product.getStock(), categoryId);

        // Validazione personalizzata per il prezzo
        if (product.getPrice().compareTo(BigDecimal.valueOf(0.01)) < 0
                || product.getPrice().compareTo(BigDecimal.valueOf(99999999.99)) > 0) {
            bindingResult.rejectValue("price", "error.price", "Il prezzo deve essere compreso tra 0.01 e 99999999.99.");
        }

        if (bindingResult.hasErrors()) {
            logger.error("Errori di validazione: {}", bindingResult.getAllErrors());
            model.addAttribute("errorMessage", "Errore nei dati inseriti. Controlla i campi obbligatori.");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/productEdit";
        }

        try {
            Product existingProduct = productService.getProductById(product.getId());
            if (existingProduct == null) {
                logger.error("Prodotto non trovato con ID: {}", product.getId());
                model.addAttribute("errorMessage", "Prodotto non trovato.");
                model.addAttribute("categories", categoryService.getAllCategories());
                return "products/productEdit";
            }

            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setShortDescription(product.getShortDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setStock(product.getStock());
            existingProduct.setBrand(product.getBrand());

            if (categoryId != null) {
                Category category = categoryService.getCategoryById(categoryId);
                existingProduct.setCategory(category);
            }

            logger.info("Campi testuali aggiornati: {}", existingProduct);

            productService.updateProduct(existingProduct);
            Product updatedProduct = productService.getProductById(product.getId());
            logger.info("Dettagli prodotto aggiornati con successo: {}", updatedProduct);
            model.addAttribute("product", updatedProduct);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("successMessage", "Dettagli del prodotto aggiornati con successo.");
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento dei dettagli del prodotto: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento dei dettagli: " + e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
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
        String classPath = ResourceUtils.getFile("classpath:").getAbsolutePath();
        String productDir = classPath + File.separator + UPLOAD_DIR + id;
        Path productPath = Paths.get(productDir);
        if (!Files.exists(productPath)) {
            Files.createDirectories(productPath);
            logger.info("Cartella creata: {}", productDir);
        }

        if (mainImage != null && !mainImage.isEmpty()) {
            mainImage.transferTo(productPath.resolve("main.jpg"));
            logger.info("Main image salvata: {}", productPath.resolve("main.jpg"));
        }

        List<String> updatedImages = new ArrayList<>(product.getAdditionalImages() != null ? product.getAdditionalImages() : new ArrayList<>());
        int thumbIndex = 1;
        if (!updatedImages.isEmpty()) {
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

        if (newAdditionalImages != null) {
            for (MultipartFile file : newAdditionalImages) {
                if (file != null && !file.isEmpty()) {
                    String fileName = "thumb" + thumbIndex++ + ".jpg";
                    file.transferTo(productPath.resolve(fileName));
                    logger.info("Immagine aggiuntiva salvata: {}", fileName);
                }
            }
        }

        productService.renameImagesInFolder(id);
        product.setAdditionalImages(productService.loadAdditionalImages(id));
        logger.info("Immagini aggiornate con successo per il prodotto {}: {}", id, product.getAdditionalImages());
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("successMessage", "Immagini aggiornate con successo.");
    } catch (IOException e) {
        logger.error("Errore durante il salvataggio delle immagini: {}", e.getMessage(), e);
        model.addAttribute("errorMessage", "Errore durante l'aggiornamento delle immagini: " + e.getMessage());
        model.addAttribute("categories", categoryService.getAllCategories());
        Product productOnError = productService.getProductById(id);
        model.addAttribute("product", productOnError);
    } catch (Exception e) {
        logger.error("Errore generico durante l'aggiornamento delle immagini: {}", e.getMessage(), e);
        model.addAttribute("errorMessage", "Errore durante l'aggiornamento delle immagini: " + e.getMessage());
        model.addAttribute("categories", categoryService.getAllCategories());
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

        String classPath = ResourceUtils.getFile("classpath:").getAbsolutePath();
        String fileName = Paths.get(imageUrl).getFileName().toString();
        String filePath = classPath + File.separator + UPLOAD_DIR + id + File.separator + fileName;
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
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/productEdit";
    } catch (IOException e) {
        logger.error("Errore durante l'eliminazione dell'immagine: {}", e.getMessage());
        model.addAttribute("errorMessage", "Errore durante l'eliminazione dell'immagine: " + e.getMessage());
        model.addAttribute("categories", categoryService.getAllCategories());
        Product product = productService.getProductById(id);
        product.setAdditionalImages(productService.loadAdditionalImages(id));
        model.addAttribute("product", product);
        return "products/productEdit";
    }
}

    // ===================================================================

    @GetMapping("/create")
    public String showCreateProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/productCreate";
    }

    @PostMapping("/save")
    public String saveProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestParam(value = "newAdditionalImages", required = false) MultipartFile[] newAdditionalImages,
            Model model) {
        logger.info("Richiesta POST ricevuta per /admin/products/save");
        logger.info("Dati ricevuti - Prodotto: Nome={}, Prezzo={}, Stock={}, Categoria ID={}",
                product.getName(), product.getPrice(), product.getStock(), categoryId);

        // Validazione personalizzata per il prezzo
        if (product.getPrice().compareTo(BigDecimal.valueOf(0.01)) < 0 || product.getPrice().compareTo(BigDecimal.valueOf(99999999.99)) > 0) {
            bindingResult.rejectValue("price", "error.price", "Il prezzo deve essere compreso tra 0.01 e 99999999.99.");
        }

        if (bindingResult.hasErrors()) {
            logger.error("Errori di validazione: {}", bindingResult.getAllErrors());
            model.addAttribute("errorMessage", "Errore nei dati inseriti. Controlla i campi obbligatori.");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/productCreate";
        }

        try {
            // Imposta la categoria
            if (categoryId != null) {
                Category category = categoryService.getCategoryById(categoryId);
                product.setCategory(category);
            }

            // Salva il prodotto per ottenere l'ID
            Product savedProduct = productService.createProduct(product);
            logger.info("Prodotto salvato con ID: {}", savedProduct.getId());

            // Gestione immagini
            // Ottieni il percorso base del classpath (target/classes/)
            String classPath = ResourceUtils.getFile("classpath:").getAbsolutePath();
            // Costruisci il percorso della directory del prodotto
            String productDir = classPath + File.separator + UPLOAD_DIR + savedProduct.getId();
            Path productPath = Paths.get(productDir);

            // Crea la directory se non esiste
            if (!Files.exists(productPath)) {
                Files.createDirectories(productPath);
                logger.info("Cartella creata: {}", productDir);
            }

            // Salva l'immagine principale
            if (mainImage != null && !mainImage.isEmpty()) {
                mainImage.transferTo(productPath.resolve("main.jpg"));
                logger.info("Main image salvata: {}", productPath.resolve("main.jpg"));
            }

            // Salva le immagini aggiuntive
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

            // Aggiorna la lista delle immagini
            savedProduct.setAdditionalImages(productService.loadAdditionalImages(savedProduct.getId()));
            productService.updateProduct(savedProduct);

            logger.info("Prodotto creato con successo: {}", savedProduct);
            model.addAttribute("successMessage", "Prodotto creato con successo.");
            return "redirect:/products";
        } catch (IOException e) {
            logger.error("Errore durante il salvataggio delle immagini: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante il salvataggio delle immagini: " + e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/productCreate";
        } catch (Exception e) {
            logger.error("Errore generico durante la creazione del prodotto: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante la creazione del prodotto: " + e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/productCreate";
        }
    }





    // ========================================================================
    @PostMapping("/delete/{id}")
public String deleteProduct(@PathVariable("id") int id, Model model) {
    logger.info("Richiesta POST ricevuta per /admin/products/delete/{}", id);
    try {
        Product product = productService.getProductById(id);
        if (product == null) {
            logger.error("Prodotto non trovato con ID: {}", id);
            model.addAttribute("errorMessage", "Prodotto non trovato.");
            return "redirect:/products";
        }

        // Elimina le immagini associate
        String classPath = ResourceUtils.getFile("classpath:").getAbsolutePath();
        String productDir = classPath + File.separator + UPLOAD_DIR + id;
        Path productPath = Paths.get(productDir);
        if (Files.exists(productPath)) {
            Files.walk(productPath)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
            logger.info("Cartella delle immagini eliminata: {}", productDir);
        }

        // Elimina il prodotto dal database
        productService.deleteProduct(id);
        logger.info("Prodotto eliminato con successo: ID={}", id);
        model.addAttribute("successMessage", "Prodotto eliminato con successo.");
    } catch (IOException e) {
        logger.error("Errore durante l'eliminazione delle immagini: {}", e.getMessage(), e);
        model.addAttribute("errorMessage", "Errore durante l'eliminazione delle immagini: " + e.getMessage());
    } catch (Exception e) {
        logger.error("Errore generico durante l'eliminazione del prodotto: {}", e.getMessage(), e);
        model.addAttribute("errorMessage", "Errore durante l'eliminazione del prodotto: " + e.getMessage());
    }
    return "redirect:/products";
}
}
