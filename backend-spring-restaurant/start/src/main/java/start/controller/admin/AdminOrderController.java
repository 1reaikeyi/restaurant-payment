package start.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.OrderDetailService;
import service.OrderPayService;
import service.OrderService;

@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private OrderPayService orderPayService;
}
