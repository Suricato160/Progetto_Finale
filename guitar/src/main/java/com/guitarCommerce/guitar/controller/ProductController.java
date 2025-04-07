package com.guitarCommerce.guitar.controller;

import com.guitarCommerce.guitar.entity.Product;
import com.guitarCommerce.guitar.service.CategoryService;
import com.guitarCommerce.guitar.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ======================================= ok

@Controller
@RequestMapping("/products")
public class ProductController {


    // dipendenze
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;


    // =========================================================
    // lista di tutti i prodotti
    // lista di tutte le categorie
    @GetMapping
    public String getAllProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories()); // Necessario per il navbar

        return "products/products";
    }

    // =========================================================
    // recupera un prodotto specifico
    @GetMapping("/{id}")
    public String getProductDetail(@PathVariable Integer id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "products/productDetail"; // Nome del template
    }

    // =========================================================
    // Aggiunta al carrello
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Integer productId, @RequestParam Integer quantity) {
        System.out.println("Aggiunto al carrello: Product ID " + productId + ", Quantità: " + quantity);
        return "redirect:/products/" + productId; // Torna alla pagina del prodotto
    }

    // =========================================================
    // Tutti i prodotti di una specifica categoria
    @GetMapping("/category/{id}")
    public String showProductsByCategory(@PathVariable("id") int categoryId, Model model) {
        model.addAttribute("products", productService.getProductsByCategory(categoryId));
        return "products/products";
    }
}