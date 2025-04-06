package com.guitarCommerce.guitar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.guitarCommerce.guitar.entity.Category;
import com.guitarCommerce.guitar.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/{id}")
    public Category getCategory(@PathVariable int id) {
        return categoryService.getCategoryById(id);
    }

    @PostMapping("/add")
    public String addCategory(@RequestParam("name") String name, Model model) {
        try {
            Category category = new Category();
            category.setName(name);
            categoryService.createCategory(category);
            model.addAttribute("successMessage", "Categoria aggiunta con successo.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore durante l'aggiunta della categoria: " + e.getMessage());
        }
        // Reindirizza alla pagina corrente (dovrebbe essere gestito dal client, ma per ora reindirizziamo a /products)
        return "redirect:/products";
    }
}