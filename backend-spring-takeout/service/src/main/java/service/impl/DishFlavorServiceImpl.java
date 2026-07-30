package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import pojo.entity.DishFlavor;
import repository.DishFlavorMapper;
import service.ISevcive.DishFlavorService;
@Repository
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService  {
}