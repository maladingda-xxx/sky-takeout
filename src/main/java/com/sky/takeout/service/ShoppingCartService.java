package com.sky.takeout.service;

import com.sky.takeout.dto.ShoppingCartItemDTO;
import com.sky.takeout.dto.ShoppingCartUpdateDTO;
import com.sky.takeout.vo.ShoppingCartVO;

import java.util.List;

public interface ShoppingCartService {

    void add(ShoppingCartItemDTO itemDTO);

    List<ShoppingCartVO> list();

    void sub(ShoppingCartItemDTO itemDTO);

    void updateQuantity(ShoppingCartUpdateDTO updateDTO);

    void clean();
}
