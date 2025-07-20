package com.okbasalman.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.okbasalman.order_service.model.OrderItemDto;

public interface OrderItemRepository extends JpaRepository<OrderItemDto, Long> {}
