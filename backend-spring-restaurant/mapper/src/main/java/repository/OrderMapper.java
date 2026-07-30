package repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import pojo.entity.Orders;

@Repository
public interface OrderMapper extends BaseMapper<Orders> {
}
