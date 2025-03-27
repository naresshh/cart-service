package com.ecom.cart.controller;

import com.ecom.cart.dto.CartItemDTO;
import com.ecom.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "http://localhost:3000")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public CompletableFuture<CartItemDTO> addProductToCart(@RequestBody CartItemDTO request) {
        CompletableFuture<CartItemDTO> cartItemDTO = cartService.addProductToCart(request.getProductId(), request.getQuantity(),request.getCustomerId());
        return cartItemDTO;
    }

    @PutMapping("/update/{cartItemId}")
    public CompletableFuture<CartItemDTO> updateCartItem(@PathVariable Long cartItemId, @RequestParam int quantity) {
        return cartService.updateCartItem(cartItemId, quantity);
    }

    @DeleteMapping("/delete/{cartItemId}")
    public CompletableFuture<Void> deleteCartItem(@PathVariable Long cartItemId) {
       return cartService.deleteCartItem(cartItemId);
    }

    @GetMapping("/{customerId}")
    public CompletableFuture<List<CartItemDTO>> getAllCartItems(@PathVariable Long customerId) {
        return cartService.getCartItemsByCustomerId(customerId);
    }


    @DeleteMapping("/clear/{customerId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build(); // Returns HTTP 204 No Content
    }
}