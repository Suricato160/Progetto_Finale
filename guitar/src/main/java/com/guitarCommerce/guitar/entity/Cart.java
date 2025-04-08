package com.guitarCommerce.guitar.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart")
@Data
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private int id;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "added_at")
    private LocalDateTime addedAt;


    // -------- relazioni tra le tabelle -------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "products_id")
    private Product product;

    @Transient // Questa proprietà non viene salvata nel database
    private BigDecimal subtotal;

    // Metodo helper per calcolare il subtotale (opzionale, se vuoi incapsularlo)
    public void calculateSubtotal() {
        if (product != null && quantity > 0) {
            this.subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }
}
