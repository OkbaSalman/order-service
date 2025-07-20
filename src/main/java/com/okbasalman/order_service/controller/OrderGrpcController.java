package com.okbasalman.order_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import net.devh.boot.grpc.server.service.GrpcService;

import com.example.orderservice.grpc.CreateOrderRequest;
import com.example.orderservice.grpc.CreateOrderResponse;
import com.example.orderservice.grpc.DeleteOrderRequest;
import com.example.orderservice.grpc.DeleteOrderResponse;
import com.example.orderservice.grpc.Empty;
import com.example.orderservice.grpc.GetAllOrdersResponse;
import com.example.orderservice.grpc.GetMyOrdersRequest;
import com.example.orderservice.grpc.GetMyOrdersResponse;
import com.example.orderservice.grpc.UpdateOrderStatusRequest;
import com.example.orderservice.grpc.UpdateOrderStatusResponse;
import com.example.orderservice.grpc.OrderServiceGrpc.OrderServiceImplBase;

import com.okbasalman.order_service.service.OrderService;

import io.grpc.stub.StreamObserver;

@GrpcService
public class OrderGrpcController extends OrderServiceImplBase {

    @Autowired
    private OrderService orderService;

    @Override
    public void createOrder(CreateOrderRequest request, StreamObserver<CreateOrderResponse> responseObserver) {
        CreateOrderResponse response = orderService.createOrder(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllOrders(Empty request, StreamObserver<GetAllOrdersResponse> responseObserver) {
        GetAllOrdersResponse response = orderService.getAllOrders();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getMyOrders(GetMyOrdersRequest request, StreamObserver<GetMyOrdersResponse> responseObserver) {
        GetMyOrdersResponse response = orderService.getMyOrders(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteOrder(DeleteOrderRequest request, StreamObserver<DeleteOrderResponse> responseObserver) {
        DeleteOrderResponse response = orderService.deleteOrder(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

     @Override
    public void updateOrderStatus(UpdateOrderStatusRequest request, StreamObserver<UpdateOrderStatusResponse> responseObserver) {
        UpdateOrderStatusResponse response = orderService.updateOrderStatus(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
