package repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import pojo.entity.AddressBook;


@Repository
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
