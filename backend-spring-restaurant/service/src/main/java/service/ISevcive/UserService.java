package service.ISevcive;

import com.baomidou.mybatisplus.extension.service.IService;
import pojo.dto.user.UserLogInDTO;
import pojo.entity.User;

public interface UserService extends IService<User> {
    User loadByUsername(String userName);
    User login(UserLogInDTO userLogInDTO);
}
