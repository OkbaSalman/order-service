package com.okbasalman.order_service.service;

import com.example.orderservice.grpc.CreateOrderRequest;
import com.example.orderservice.grpc.CreateOrderResponse;
import com.example.orderservice.grpc.DeleteOrderRequest;
import com.example.orderservice.grpc.DeleteOrderResponse;
import com.example.orderservice.grpc.GetAllOrdersResponse;
import com.example.orderservice.grpc.GetMyOrdersRequest;
import com.example.orderservice.grpc.GetMyOrdersResponse;
import com.example.orderservice.grpc.UpdateOrderStatusRequest;
import com.example.orderservice.grpc.UpdateOrderStatusResponse;

public interface OrderService {
    CreateOrderResponse createOrder(CreateOrderRequest request);
    GetAllOrdersResponse getAllOrders();
    GetMyOrdersResponse getMyOrders(GetMyOrdersRequest request);
    DeleteOrderResponse deleteOrder(DeleteOrderRequest request);
    UpdateOrderStatusResponse updateOrderStatus(UpdateOrderStatusRequest request);
}
