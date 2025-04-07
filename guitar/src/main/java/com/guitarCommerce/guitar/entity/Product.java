package com.guitarCommerce.guitar.entity;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "short_description", length = 300)
    private String shortDescription;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "stock")
    private int stock;

    @Column(name = "brand")
    private String brand;

    @Column(name = "reviews_count")
    private Integer reviewsCount = 0;


    // Campo transitorio per le immagini aggiuntive
    @Transient
    private List<String> additionalImages = new ArrayList<>();




    // ----- relazioni tra tabelle ---------


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categories_id", nullable = false)
    private Category category; // Deve essere com.guitarCommerce.guitar.entity.Category

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;



    
}
