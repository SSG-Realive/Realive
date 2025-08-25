package com.realive.service.cart.view;

import com.realive.dto.cart.CartListResponseDTO;

public interface CartViewService {

    CartListResponseDTO getCart(Long customerId);
}