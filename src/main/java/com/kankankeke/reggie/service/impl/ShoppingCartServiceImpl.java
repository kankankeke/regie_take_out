package com.kankankeke.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kankankeke.reggie.entity.ShoppingCart;
import com.kankankeke.reggie.mapper.ShoppingCartMapper;
import com.kankankeke.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
