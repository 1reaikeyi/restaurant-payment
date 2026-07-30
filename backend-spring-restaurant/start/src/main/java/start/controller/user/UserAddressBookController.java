package start.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import common.constant.JwtClaimsConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pojo.entity.AddressBook;
import service.ISevcive.AddressBookService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/addressBooks")
public class UserAddressBookController {
    @Autowired
    private AddressBookService addressBookService;
    @PostMapping
    public Result createAddressBook(@RequestBody AddressBook AddressBook) {
       Map<String, Object> map = ThreadLocalContextHolder.get();
       String currentUserId = map.get(JwtClaimsConstant.USER_ID).toString();
       Long userId = Long.parseLong(currentUserId);
       AddressBook.setUserId(userId);
       if(AddressBook.getIsDefault() == null){
           AddressBook.setIsDefault(0L);
       }
       addressBookService.save(AddressBook);
       if(AddressBook.getIsDefault() == 1){
           AddressBook.setIsDefault(1L);
       }
       return Result.success("createAddressBook::"+AddressBook.getId());
    }
    @GetMapping
    public Result readAddressBook() {
        Map<String, Object> map = ThreadLocalContextHolder.get();
        String currentUserId = map.get(JwtClaimsConstant.USER_ID).toString();
        Long userId = Long.parseLong(currentUserId);
        LambdaQueryWrapper<AddressBook> Wrapper = new LambdaQueryWrapper<AddressBook>();
        Wrapper.eq(AddressBook::getUserId, userId)
                .orderByDesc(AddressBook::getIsDefault)
                .orderByAsc(AddressBook::getId);
        List<AddressBook> AddressBookList = addressBookService.list(Wrapper);
        if(AddressBookList.isEmpty()){
            return Result.error("没有添加地址，地址为空");
        }
        return Result.success(AddressBookList);
    }
    @GetMapping("/{id}")
    public Result readAddressBookById(@PathVariable("id") Long id) {
        AddressBook AddressBook = addressBookService.getById(id);
        if(AddressBook == null){
            return Result.error("没有这个地址");
        }
        return Result.success(AddressBook);
    }
    @GetMapping("/default")
    public Result readDefultAddressBook() {
        Map<String, Object> map = ThreadLocalContextHolder.get();
        String currentUserId = map.get(JwtClaimsConstant.USER_ID).toString();
        Long userId = Long.parseLong(currentUserId);
        LambdaQueryWrapper<AddressBook> Wrapper = new LambdaQueryWrapper<AddressBook>();
        Wrapper.eq(AddressBook::getIsDefault, 1)
                .eq(AddressBook::getUserId, userId);
        AddressBook addressBook = addressBookService.getOne(Wrapper);
        if(addressBook == null){
            return Result.error("没有默认地址");
        }
        return Result.success(addressBook);
    }
    @PutMapping
    public Result updateAddressBook(@RequestBody AddressBook addressBook) {
        Map<String, Object> map = ThreadLocalContextHolder.get();
        String currentUserId = map.get(JwtClaimsConstant.USER_ID).toString();
        Long userId = Long.parseLong(currentUserId);
        addressBook.setUserId(userId);
        addressBookService.updateById(addressBook);
        return Result.success("updateAddressBook::"+addressBook.getId());
    }
    @PutMapping("/default/{defaultId}")
    public Result updateDefaultAddressBook(@PathVariable("defaultId") Long defaultId) {
        Map<String, Object> map = ThreadLocalContextHolder.get();
        String currentUserId = map.get(JwtClaimsConstant.USER_ID).toString();
        Long userId = Long.parseLong(currentUserId);
//        先检查是否存在default
        AddressBook addressBook = addressBookService.getOne(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getIsDefault,1L)
                .eq(AddressBook::getUserId, userId));
//        不存在
        if(addressBook == null){
            addressBook.setIsDefault(1L);
        }
//        存在
        addressBook.setIsDefault(0L);
        AddressBook defaultAddressBook = addressBookService.getOne(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId)
                .eq(AddressBook::getId, defaultId));
        defaultAddressBook.setIsDefault(1L);
        addressBookService.updateById(defaultAddressBook);
        return Result.success("updateDefaultAddressBook::"+defaultId);
    }
    @DeleteMapping("/{id}")
    public Result deleteAddressBook(@PathVariable("id") Long id) {
        addressBookService.removeById(id);
        return Result.success("deleteAddressBook::"+id);
    }
}
