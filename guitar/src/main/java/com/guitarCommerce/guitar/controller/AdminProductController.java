package com.guitarCommerce.guitar.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


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
import com.guitarCommerce.guitar.service.CategoryService;
import com.guitarCommerce.guitar.service.ProductService;

import jakarta.validation.Valid;

// da commentare  -  rifattorializzato


@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class);
    private static final String UPLOAD_DIR = "static/products/";

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    // =========================================================================

    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable("id") int id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            model.addAttribute("errorMessage", "Prodotto non trovato.");
            return "redirect:/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        logger.info("Immagini aggiuntive caricate per il prodotto {}: {}", id, product.getAdditionalImages());
        return "products/productEdit";
    }

    // =========================================================================
    // ---------- UPDATE ------------------------------

    // aggiornamenti dettagli testuali prodotti
    @PostMapping("/update-details")
    public String updateProductDetails(
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            @RequestParam("categoryId") Integer categoryId,
            Model model) {
        logger.info("Richiesta POST ricevuta per /admin/products/update-details");

        if (product.getPrice().compareTo(BigDecimal.valueOf(0.01)) < 0 ||
                product.getPrice().compareTo(BigDecimal.valueOf(99999999.99)) > 0) {
            bindingResult.rejectValue("price", "error.price", "Il prezzo deve essere compreso tra 0.01 e 99999999.99.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Errore nei dati inseriti. Controlla i campi obbligatori.");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/productEdit";
        }

        try {
            Product updatedProduct = productService.updateProductDetails(product.getId(), product, categoryId);
            if (updatedProduct == null) {
                model.addAttribute("errorMessage", "Prodotto non trovato.");
            } else {
                model.addAttribute("successMessage", "Dettagli del prodotto aggiornati con successo.");
            }
            model.addAttribute("product", updatedProduct != null ? updatedProduct : productService.getProductById(product.getId()));
            model.addAttribute("categories", categoryService.getAllCategories());
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento: " + e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("product", productService.getProductById(product.getId()));
        }
        return "products/productEdit";
    }

    // --------------------------------------------------------------------------
    // aggiornamento immagini
    @PostMapping("/update-images/{id}")
    public String updateProductImages(
            @PathVariable("id") int id,
            @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestParam(value = "newAdditionalImages", required = false) MultipartFile[] newAdditionalImages,
            Model model) {
        logger.info("Richiesta POST ricevuta per /admin/products/update-images/{}", id);

        try {
            Product updatedProduct = productService.updateProductImages(id, mainImage, newAdditionalImages);
            if (updatedProduct == null) {
                model.addAttribute("errorMessage", "Prodotto non trovato.");
            } else {
                model.addAttribute("successMessage", "Immagini aggiornate con successo.");
            }
            model.addAttribute("product", updatedProduct != null ? updatedProduct : productService.getProductById(id));
            model.addAttribute("categories", categoryService.getAllCategories());
        } catch (IOException e) {
            logger.error("Errore durante il salvataggio delle immagini: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento delle immagini: " + e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("product", productService.getProductById(id));
        }
        return "products/productEdit";
    }

    // =========================================================================
    // -------------- DELETE --------------------

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") int id, Model model) {
        logger.info("Richiesta POST ricevuta per /admin/products/delete/{}", id);
        try {
            productService.deleteProductAndImages(id);
            model.addAttribute("successMessage", "Prodotto eliminato con successo.");
        } catch (IOException e) {
            logger.error("Errore durante l'eliminazione delle immagini: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'eliminazione delle immagini: " + e.getMessage());
        }
        return "redirect:/products";
    }

    // ----------------------------------------------------------------------
    @PostMapping("/{id}/delete-image")
    public String deleteProductImage(@PathVariable("id") int id, @RequestParam("imageUrl") String imageUrl,
            Model model) {
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
            if (product != null) {
                product.setAdditionalImages(productService.loadAdditionalImages(id));
            }
            model.addAttribute("product", product);
            return "products/productEdit";
        }
    }

    // =========================================================================
    // ---------------------- CREATE ---------------------

    @GetMapping("/create")
    public String showCreateProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products/productCreate";
    }

    // =========================================================================
    // ---------------------- SAVE -----------------------

    @PostMapping("/save")
    public String saveProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestParam(value = "newAdditionalImages", required = false) MultipartFile[] newAdditionalImages,
            Model model) {
        logger.info("Richiesta POST ricevuta per /admin/products/save");

        if (product.getPrice().compareTo(BigDecimal.valueOf(0.01)) < 0 ||
                product.getPrice().compareTo(BigDecimal.valueOf(99999999.99)) > 0) {
            bindingResult.rejectValue("price", "error.price", "Il prezzo deve essere compreso tra 0.01 e 99999999.99.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Errore nei dati inseriti. Controlla i campi obbligatori.");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/productCreate";
        }

        try {
            Product savedProduct = productService.createProductWithImages(product, categoryId, mainImage, newAdditionalImages);
            model.addAttribute("successMessage", "Prodotto creato con successo.");
            return "redirect:/products";
        } catch (IOException e) {
            logger.error("Errore durante il salvataggio delle immagini: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante il salvataggio delle immagini: " + e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products/productCreate";
        }
    }
}