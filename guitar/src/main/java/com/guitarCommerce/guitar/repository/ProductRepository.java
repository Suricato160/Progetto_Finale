package com.guitarCommerce.guitar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.guitarCommerce.guitar.entity.Product;
import java.util.List;
import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>{

    //query per trovare prodotti per categoria
    List<Product> findByCategoryId(Integer categoryId);

    //query per trovare prodotti per nome (ricerca parziale)
    List<Product> findByNameContainingIgnoreCase(String name);

    //query per trovare prodotti per nome
    Optional<Product> findByName(String name);

    //query per trovare prodotti per range di prezzo


    
} 