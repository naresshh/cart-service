package com.ecom.cart.service;

import com.ecom.cart.dto.CartItemDTO;
import com.ecom.cart.dto.ProductDTO;
import com.ecom.cart.exception.ProductNotFoundException;
import com.ecom.cart.exception.CartItemNotFoundException;
import com.ecom.cart.mapper.CartItemMapper;
import com.ecom.cart.model.CartItem;
import com.ecom.cart.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;
    private final RestClient restClient;

    @Value("${product-service.base-url}")
    private String productServiceBaseUrl;

    @Async
    public CompletableFuture<CartItemDTO> addProductToCart(Long productId, int quantity, Long customerId) {
        System.out.println("Thread Info Add: " + Thread.currentThread());

        try {
            ProductDTO product = restClient.get()
                    .uri(productServiceBaseUrl + "/" + productId)
                    .retrieve()
                    .body(ProductDTO.class);

            if (product == null) {
                throw new ProductNotFoundException("Product with ID " + productId + " returned null from Product Service.");
            }

            CartItem cartItem = CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(quantity)
                    .customerId(customerId)
                    .build();
            CartItem existingItem = cartItemRepository.findByProductNameAndCustomerId(product.getName(),customerId);
            if(existingItem!=null && existingItem.getCustomerId().equals(customerId)){
                cartItem.setId(existingItem.getId());
                cartItem.setQuantity(existingItem.getQuantity()+quantity);
            }
            CartItem savedItem = cartItemRepository.save(cartItem);
            CartItemDTO savedDto = cartItemMapper.toDTO(savedItem);
            return CompletableFuture.completedFuture(savedDto);

        } catch (HttpStatusCodeException e) {
            // Inspect ProductService response status
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ProductNotFoundException("Product with ID " + productId + " not found in Product Service.");
            }
            // Handle other client-side errors (400, 500, etc.)
            throw new RuntimeException("Error communicating with Product Service: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new RuntimeException("Product Service is unreachable: " + e.getMessage());
        }
    }

    @Async
    public CompletableFuture<CartItemDTO> updateCartItem(Long cartItemId, int quantity) {
        try {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found"));

        item.setQuantity(quantity);
        CartItem updatedItem = cartItemRepository.save(item);

        return CompletableFuture.completedFuture(cartItemMapper.toDTO(updatedItem));
    } catch (CartItemNotFoundException e) {
            throw new ProductNotFoundException("Product with ID " + cartItemId + " not found in Product Service.");
        }
    }

    @Async
    public CompletableFuture<Void> deleteCartItem(Long cartItemId) {
        try {
            boolean exists = cartItemRepository.existsById(cartItemId);
            if (!exists) {
                throw new CartItemNotFoundException("Cart item with ID " + cartItemId + " not found.");
            }

            cartItemRepository.deleteById(cartItemId);
            return CompletableFuture.completedFuture(null);

        } catch (CartItemNotFoundException e) {
            // Re-throw the custom exception with a meaningful message.
            throw new CartItemNotFoundException("Cart item with ID " + cartItemId + " not found.");

        } catch (Exception e) {
            // Handle any unexpected errors (DB connection issue, etc.)
            throw new RuntimeException("An unexpected error occurred while deleting cart item with ID " + cartItemId + ": " + e.getMessage());
        }
    }

    //For Admins to get all products for all customers
    @Async
    public CompletableFuture<List<CartItemDTO>> getAllCartItems() {
        List<CartItemDTO> cartItems = cartItemRepository.findAll()
                .stream()
                .map(cartItemMapper::toDTO)
                .toList();

        return CompletableFuture.completedFuture(cartItems);
    }

    @Async
    public CompletableFuture<List<CartItemDTO>> getCartItemsByCustomerId(Long customerId) {
        List<CartItemDTO> cartItems = cartItemRepository.findByCustomerId(customerId)
                .stream()
                .map(cartItemMapper::toDTO)
                .toList();

        return CompletableFuture.completedFuture(cartItems);
    }

    public void clearCart(Long customerId) {
        // Find and delete all CartItems by customerId
        List<CartItem> cartItems = cartItemRepository.findByCustomerId(customerId);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("No cart items found for customerId: " + customerId);
        }

        // Delete all items
        cartItemRepository.deleteAll(cartItems);
    }


}