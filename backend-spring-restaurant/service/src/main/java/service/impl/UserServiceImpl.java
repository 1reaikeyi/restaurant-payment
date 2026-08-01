package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.UserMapper;
import org.springframework.stereotype.Service;
import model.entity.User;
import service.UserService;

/**
 * 用户 Service（对应 user 表）
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
