package com.guitarCommerce.guitar.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.guitarCommerce.guitar.entity.Category;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer>{

    //Query per trovare una categoria per nome
    Optional<Category> findByName(String name);


    
} 

    
