package com.okbasalman.order_service.client;
import org.springframework.stereotype.Service;

import com.okbasalman.grpc.DecreaseStockRequest;
import com.okbasalman.grpc.GetProductByIdRequest;
import com.okbasalman.grpc.ProductResponse;
import com.okbasalman.grpc.ProductServiceGrpc;

import net.devh.boot.grpc.client.inject.GrpcClient;


@Service
public class ProductClient {

    @GrpcClient("productService")
    private ProductServiceGrpc.ProductServiceBlockingStub productStub;

    public ProductResponse getProductById(Long id) {
        return productStub.getProductById(
            GetProductByIdRequest.newBuilder().setId(id).build()
        );
    }

    public ProductResponse decreaseStock(Long productId, int quantity) {
    DecreaseStockRequest request = DecreaseStockRequest.newBuilder()
        .setProductId(productId)
        .setQuantity(quantity)
        .build();

    return productStub.decreaseStock(request);
}

}

