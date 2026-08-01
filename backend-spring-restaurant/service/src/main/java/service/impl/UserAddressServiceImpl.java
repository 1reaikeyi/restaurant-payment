package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.UserAddressMapper;
import org.springframework.stereotype.Service;
import model.entity.UserAddress;
import service.UserAddressService;

/**
 * 用户地址簿 Service（对应 user_address 表）
 */
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {
}
