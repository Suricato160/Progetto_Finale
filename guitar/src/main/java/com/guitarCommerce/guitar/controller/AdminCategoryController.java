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
    public String addCategory(
            @RequestParam("name") String name,
            @RequestParam(value = "redirectTo", defaultValue = "/products") String redirectTo,
            Model model) {
        try {
            Category category = new Category();
            category.setName(name);
            Category savedCategory = categoryService.createCategory(category);
            // Aggiungi un messaggio di successo e l'ID della nuova categoria
            return "redirect:" + redirectTo + "?successMessage=Categoria aggiunta con successo&newCategoryId=" + savedCategory.getId();
        } catch (Exception e) {
            // Aggiungi un messaggio di errore
            return "redirect:" + redirectTo + "?errorMessage=Errore durante l'aggiunta della categoria: " + e.getMessage();
        }
    }
}