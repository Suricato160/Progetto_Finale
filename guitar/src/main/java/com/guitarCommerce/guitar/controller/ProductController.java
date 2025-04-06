package com.guitarCommerce.guitar.controller;

import com.guitarCommerce.guitar.entity.Product;
import com.guitarCommerce.guitar.service.CategoryService;
import com.guitarCommerce.guitar.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;




// =====================================================================
//              Caricamento pagina


    @GetMapping
    public String getAllProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories()); // Necessario per il navbar

        return "products/products";
    }

// =====================================================================
//   

    @GetMapping("/{id}")
    public String getProductDetail(@PathVariable Integer id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "products/productDetail"; // Nome del template
    }

    // Aggiunta al carrello (logica base, da espandere)
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Integer productId, @RequestParam Integer quantity) {
        // Logica per aggiungere al carrello (es. salvare in sessione o database)
        System.out.println("Aggiunto al carrello: Product ID " + productId + ", Quantità: " + quantity);
        return "redirect:/products/" + productId; // Torna alla pagina del prodotto
    }




    @GetMapping("/category/{id}")
    public String showProductsByCategory(@PathVariable("id") int categoryId, Model model) {
        model.addAttribute("products", productService.getProductsByCategory(categoryId));
        return "products/products";
    }
}