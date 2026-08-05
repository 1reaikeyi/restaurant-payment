package start.controller.admin;

import common.enumOperation.OperationEnum;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.OrderDetailService;
import service.OrderPayService;
import service.OrderService;
import start.aop.OperationLogging;

@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private OrderPayService orderPayService;
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result readByOrderId(@RequestParam("orderId") Long orderId) {
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readAll() {
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result update3() {
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result update4() {
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result update5() {
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result update6() {
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result update7() {
        return Result.success();
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result update8() {
        return Result.success();
    }

}
