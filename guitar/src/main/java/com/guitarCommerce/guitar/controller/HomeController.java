package com.guitarCommerce.guitar.controller;

import com.guitarCommerce.guitar.entity.Product;
import com.guitarCommerce.guitar.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


// ======================================= ok

@Controller
public class HomeController {

    // dipendenze
    @Autowired
    private ProductService productService;

    // =========================================
    // recupero i prodotti per la home page
    @GetMapping("/")
    public String home(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "index"; 
    }
}


