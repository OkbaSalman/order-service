package com.okbasalman.order_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.orderservice.grpc.CreateOrderRequest;
import com.example.orderservice.grpc.CreateOrderResponse;
import com.example.orderservice.grpc.DeleteOrderRequest;
import com.example.orderservice.grpc.DeleteOrderResponse;
import com.example.orderservice.grpc.GetAllOrdersResponse;
import com.example.orderservice.grpc.GetMyOrdersRequest;
import com.example.orderservice.grpc.GetMyOrdersResponse;
import com.example.orderservice.grpc.OrderDto;
import com.example.orderservice.grpc.OrderItem;
import com.example.orderservice.grpc.UpdateOrderStatusRequest;
import com.example.orderservice.grpc.UpdateOrderStatusResponse;
import com.okbasalman.grpc.ProductResponse;
import com.okbasalman.grpc.ProductVariantResponse;
import com.okbasalman.order_service.client.ProductClient;
import com.okbasalman.order_service.model.Order;
import com.okbasalman.order_service.model.OrderItemDto;
import com.okbasalman.order_service.model.OrderStatus;
import com.okbasalman.order_service.repository.OrderRepository;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import java.time.ZoneId;


import jakarta.transaction.Transactional;


@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductClient productClient;

    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setEmail(request.getEmail());
        order.setAddress(request.getAddress());
        order.setCreatedAt(java.time.LocalDateTime.now());


        for (OrderItem itemReq : request.getItemsList()) {
            System.out.println("Handling item: productId=" + itemReq.getProductId()
        + ", quantity=" + itemReq.getQuantity());
            ProductResponse product = productClient.getProductById(itemReq.getProductId());

            // CHANGE START: Find the specific variant and check its stock
            Optional<ProductVariantResponse> variant = product.getVariantsList().stream()
                .filter(v -> v.getId() == itemReq.getProductVariantId())
                .findFirst();

            if (variant.isEmpty() || variant.get().getStock() < itemReq.getQuantity()) {
                return CreateOrderResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage( product.getName() + " has insufficient stock")
                        .build();
            }
            // CHANGE END
            
            OrderItemDto item = new OrderItemDto();
            
            item.setProductId(itemReq.getProductId());
            // CHANGE START: Set the product variant ID
            item.setProductVariantId(itemReq.getProductVariantId()); 
            // CHANGE END
            item.setQuantity(itemReq.getQuantity());
            item.setOrder(order);
            order.getItems().add(item);
        }

        Order saved = orderRepository.save(order);

        return CreateOrderResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Order created successfully")
                .setOrderId(saved.getId())
                .build();
    }


    @Override
    @Transactional
    public GetAllOrdersResponse getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        GetAllOrdersResponse.Builder response = GetAllOrdersResponse.newBuilder();

        for (Order order : orders) {
            OrderDto.Builder orderDto = OrderDto.newBuilder();
            orderDto.setOrderId(order.getId());
            orderDto.setUserId(order.getUserId());
            orderDto.setStatus(order.getStatus().name());
            Timestamp createdAtProto = Timestamps.fromMillis(
        order.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    );
    orderDto.setCreatedAt(createdAtProto);

            for (OrderItemDto item : order.getItems()) {
                OrderItem orderItem = OrderItem.newBuilder()
                    .setProductId(item.getProductId())
                    .setProductVariantId(item.getProductVariantId())
                    .setQuantity(item.getQuantity())
                    .build();
            orderDto.addItems(orderItem);
            }

        response.addOrders(orderDto.build());
    }

    return response.build();
}


    @Override
    @Transactional
    public GetMyOrdersResponse getMyOrders(GetMyOrdersRequest request) {
    List<Order> orders = orderRepository.findByUserId(request.getUserId());

    GetMyOrdersResponse.Builder response = GetMyOrdersResponse.newBuilder();

    for (Order order : orders) {
        OrderDto.Builder orderDto = OrderDto.newBuilder()
            .setOrderId(order.getId())
            .setUserId(order.getUserId())
            .setStatus(order.getStatus().name());
            Timestamp createdAtProto = Timestamps.fromMillis(
        order.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    );
    orderDto.setCreatedAt(createdAtProto);

        for (OrderItemDto item : order.getItems()) {
            OrderItem orderItem = OrderItem.newBuilder()
                .setProductId(item.getProductId())
                .setProductVariantId(item.getProductVariantId())
                .setQuantity(item.getQuantity())
                .build();
            orderDto.addItems(orderItem);
        }

        response.addOrders(orderDto.build());
    }

    return response.build();
}

@Override
public DeleteOrderResponse deleteOrder(DeleteOrderRequest request) {
    boolean exists = orderRepository.existsById(request.getOrderId());

    if (!exists) {
        return DeleteOrderResponse.newBuilder()
            .setSuccess(false)
            .setMessage("Order not found")
            .build();
    }

    orderRepository.deleteById(request.getOrderId());

    return DeleteOrderResponse.newBuilder()
        .setSuccess(true)
        .setMessage("Order deleted successfully")
        .build();
}

@Override
@Transactional
public UpdateOrderStatusResponse updateOrderStatus(UpdateOrderStatusRequest request) {
    Order order = orderRepository.findById(request.getOrderId()).orElse(null);

    if (order == null) {
        return UpdateOrderStatusResponse.newBuilder()
            .setSuccess(false)
            .setMessage("Order not found")
            .build();
    }

    OrderStatus newStatus;
    try {
        newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
    } catch (IllegalArgumentException e) {
        return UpdateOrderStatusResponse.newBuilder()
            .setSuccess(false)
            .setMessage("Invalid status")
            .build();
    }

     if (newStatus == OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.CONFIRMED) {
        for (OrderItemDto item : order.getItems()) {
            ProductResponse product = productClient.getProductById(item.getProductId());

            // CHANGE START: Find the specific variant and check its stock
            Optional<ProductVariantResponse> variant = product.getVariantsList().stream()
                .filter(v -> v.getId() == item.getProductVariantId())
                .findFirst();
            
            if (variant.isEmpty() || variant.get().getStock() < item.getQuantity()) {
                return UpdateOrderStatusResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Insufficient stock for product: " + product.getName())
                        .build();
            }
            // CHANGE END

            // CHANGE START: Call decreaseStock with the correct productVariantId
            productClient.decreaseStock(item.getProductVariantId(), item.getQuantity());
            // CHANGE END
        }
    }

    order.setStatus(newStatus);
    orderRepository.save(order);

    return UpdateOrderStatusResponse.newBuilder()
        .setSuccess(true)
        .setMessage("Order status updated successfully")
        .build();
}


}

