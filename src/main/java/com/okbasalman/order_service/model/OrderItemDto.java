package com.okbasalman.order_service.model;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "order_items")
public class OrderItemDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private Long productVariantId; 
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    
}
