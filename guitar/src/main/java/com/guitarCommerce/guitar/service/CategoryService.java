package com.guitarCommerce.guitar.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.guitarCommerce.guitar.entity.Category;
import com.guitarCommerce.guitar.repository.CategoryRepository;

import org.springframework.transaction.annotation.Transactional;

// ======================================= ok


@Service
public class CategoryService {

    // dependency
    @Autowired
    private CategoryRepository categoryRepository;

    // ==================================================

    // Trova tutte le categorie
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Trova una categoria per nome
    public Category getCategoryByName(String name) {
        Optional<Category> optionalCategory = categoryRepository.findByName(name);
        if (optionalCategory.isPresent()) {
            return optionalCategory.get();
        } else {
            throw new RuntimeException("Category not found with name: " + name);
        }
    }

    // Trova una categoria per ID (aggiunto per evitare di chiamare direttamente la
    // repository)
    public Category getCategoryById(Integer id) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isPresent()) {
            return optionalCategory.get();
        } else {
            throw new RuntimeException("Category not found with id: " + id);
        }
    }

    // Crea una nuova categoria
    @Transactional
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    // Aggiorna una categoria esistente
    @Transactional
    public Category updateCategory(Integer id, Category updatedCategory) {
        Category category = getCategoryById(id); // Usa il metodo definito sopra
        category.setName(updatedCategory.getName());
        return categoryRepository.save(category);
    }

    // Elimina una categoria
    @Transactional
    public void deleteCategory(Integer id) {
        Category category = getCategoryById(id); // Usa il metodo definito sopra
        categoryRepository.delete(category);
    }

    public Category getCategoryById(int id) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isPresent()) {
            return optionalCategory.get();
        } else {
            throw new RuntimeException("Categoria non trovata");
        }
    }

}