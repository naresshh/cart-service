package com.ecom.cart.mapper;

import com.ecom.cart.dto.CartItemDTO;
import com.ecom.cart.model.CartItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    CartItemDTO toDTO(CartItem cartItem);

    CartItem toEntity(CartItemDTO cartItemDTO);
}