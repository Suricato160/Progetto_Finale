package com.guitarCommerce.guitar.controller;

import com.guitarCommerce.guitar.entity.Product;
import com.guitarCommerce.guitar.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        // Recupera alcuni prodotti (es. tutti o filtrati)
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "index"; // Nome del template (index.html)
    }
}