package com.ecom.cart.controller;

import com.ecom.cart.dto.CartItemDTO;
import com.ecom.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add/{productId}")
    public CompletableFuture<CartItemDTO> addProductToCart(@PathVariable Long productId,
                                                           @RequestParam int quantity) {
        return cartService.addProductToCart(productId, quantity);
    }

    @PutMapping("/update/{cartItemId}")
    public CompletableFuture<CartItemDTO> updateCartItem(@PathVariable Long cartItemId, @RequestParam int quantity) {
        return cartService.updateCartItem(cartItemId, quantity);
    }

    @DeleteMapping("/delete/{cartItemId}")
    public CompletableFuture<Void> deleteCartItem(@PathVariable Long cartItemId) {
       return cartService.deleteCartItem(cartItemId);
    }

    @GetMapping
    public CompletableFuture<List<CartItemDTO>> getAllCartItems() {
        return cartService.getAllCartItems();
    }
}