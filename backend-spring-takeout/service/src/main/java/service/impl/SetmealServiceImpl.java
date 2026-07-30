package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import pojo.entity.Setmeal;
import repository.SetmealMapper;
import service.ISevcive.SetmealService;

@Repository
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

}