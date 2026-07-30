package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import pojo.entity.ShoppingCart;
import service.ISevcive.ShoppingCartService;
import repository.ShoppingCartMapper;

@Repository
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
