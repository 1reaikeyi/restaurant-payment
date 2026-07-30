package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import pojo.entity.Category;
import repository.CategoryMapper;
import service.ISevcive.CategoryService;


@Repository
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

}