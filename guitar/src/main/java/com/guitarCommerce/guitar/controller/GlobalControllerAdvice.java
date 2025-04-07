package com.guitarCommerce.guitar.controller;

import com.guitarCommerce.guitar.entity.Category;
import com.guitarCommerce.guitar.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

// ======================================= ok

// Aggiunge un elenco di categorie al modello prima di ogni richiesta
// rendendo le categorie disponibili in tutte le viste. 
// Mi serve per il menu di navigazione, che deve essere visibile in tutte le pagine.

@ControllerAdvice // per aggiungere un comportamento globale a tutti i controller dell'applicazione.
public class GlobalControllerAdvice {

    // Dipendenze
    @Autowired
    private CategoryService categoryService;


    @ModelAttribute // indica che questo metodo deve essere eseguito prima di ogni richiesta gestita da un controller
    public void addCategoriesToModel(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
    }
}