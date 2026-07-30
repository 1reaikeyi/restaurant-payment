package start.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import common.constant.StatusConstant;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pojo.entity.Dish;
import pojo.entity.OrderDetail;
import pojo.entity.Orders;
import pojo.entity.Setmeal;
import pojo.entityenum.OrderStatusEnum;
import pojo.vo.workspaces.*;
import service.ISevcive.DishService;
import service.ISevcive.OrderDetailService;
import service.ISevcive.OrderService;
import service.ISevcive.SetmealService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/workspace")
public class AdminWorkspaceController {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    // sold: 已售 discontinued: 已停售 总体统计
    @PostMapping("/overview/dish")
    public Result<DishOverViewVO> overviewDish() {
        LambdaQueryWrapper<Dish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Dish::getStatus, StatusConstant.ENABLE);
        Long buying = dishService.count(queryWrapper1);
        LambdaQueryWrapper<Dish> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Dish::getStatus, StatusConstant.DISABLE);
        Long stop = dishService.count(queryWrapper2);
        DishOverViewVO vo = DishOverViewVO.builder()
                .sold(buying)
                .discontinued(stop)
                .build();
        return Result.success(vo);
    }
    @PostMapping("/overview/setmeal")
    public Result<SetmealOverViewVO> overviewSetmeal() {
        LambdaQueryWrapper<Setmeal> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Setmeal::getStatus, StatusConstant.ENABLE);
        Long buying = setmealService.count(queryWrapper1);
        LambdaQueryWrapper<Setmeal> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Setmeal::getStatus, StatusConstant.DISABLE);
        Long stop = setmealService.count(queryWrapper2);
        SetmealOverViewVO vo = SetmealOverViewVO.builder()
                .sold(buying)
                .discontinued(stop)
                .build();
        return Result.success(vo);
    }
    // allOrders: 总订单数 completedOrders: 已完成订单数 refundOrders: 已退款订单数 cancelledOrders: 已取消订单数
    @PostMapping("/overview/order")
    public Result<OrderOverViewVO> overviewOrder() {
       OrderOverViewVO vo = new OrderOverViewVO();
       vo.setAllOrders(orderService.count());
       vo.setCompletedOrders(orderService.count(new LambdaQueryWrapper<Orders>()
               .eq(Orders::getStatus, OrderStatusEnum.COMPLETED)));
        vo.setRefundOrders(orderService.count(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, OrderStatusEnum.CANCELLED)));
        return Result.success(vo);
    }
    @PostMapping("/statisticsOrder")
    public Result<OrderOverViewVO> setmealScole(@RequestBody TimeVO timeVO){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startOfDay = LocalDateTime.parse(timeVO.getStartTime() + " 00:00:00", formatter);
        LocalDateTime endOfDay = LocalDateTime.parse(timeVO.getEndTime() + " 23:59:59", formatter);
        // 总订单数
        Long totalOrder = orderService.count(new LambdaQueryWrapper<Orders>()
                .between(Orders::getCheckoutTime,startOfDay, endOfDay));
        // 有效订单数
        Long validOrder = orderService.count(new LambdaQueryWrapper<Orders>()
                .between(Orders::getCheckoutTime, startOfDay, endOfDay)
                .eq(Orders::getStatus, OrderStatusEnum.COMPLETED));
        //退款订单数
        Long refundOrder = orderService.count(new LambdaQueryWrapper<Orders>()
                .between(Orders::getCheckoutTime, startOfDay, endOfDay)
                .eq(Orders::getStatus, OrderStatusEnum.CANCELLED));
        OrderOverViewVO orderOverViewVO = OrderOverViewVO.builder()
                .allOrders(totalOrder)
                .completedOrders(validOrder)
                .refundOrders(refundOrder)
                .build();
        return Result.success(orderOverViewVO);
    }
    // orderCountList: 今日总订单数 validOrderCountList: 今日有效订单数 validRate: 有效率
    @PostMapping("/orderDay")
    public Result<OrderDayVO> dayOrder() {
        OrderDayVO vo = new OrderDayVO();
        // 今日 00:00:00
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        // 今日 23:59:59
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        // 今日总订单数
        Long totalOrder = orderService.count(new LambdaQueryWrapper<Orders>()
                .between(Orders::getCheckoutTime,startOfDay, endOfDay)
        );

        // 今日有效订单数
        Long validOrder = orderService.count(new LambdaQueryWrapper<Orders>()
                .between(Orders::getCheckoutTime, startOfDay, endOfDay)
                .eq(Orders::getStatus, OrderStatusEnum.COMPLETED)
        );

        double validRate = 0.0;
        if (totalOrder > 0) {
            validRate = (double) validOrder / totalOrder * 100; // 转百分比（保留两位小数可自行处理）
        }

        vo.setOrderCountList(totalOrder.toString());       // 总订单
        vo.setValidOrderCountList(validOrder.toString());   // 有效订单
        vo.setValidRate(validRate);                         // 有效率（记得在VO里加这个字段）

        return Result.success(vo);
    }
    // 商品销售统计
    @PostMapping("/dishScole")
    public Result<List<GoodsSalesVO>> dishScole() {
        List<GoodsSalesVO> goodsSalesVOList = new ArrayList<>();
        // 查询所有菜品列表，而不是假设ID从0开始连续
        List<Dish> dishList = dishService.list();
        for(Dish dish : dishList) {
            GoodsSalesVO goodsSalesVO = new GoodsSalesVO();
            goodsSalesVO.setName(dish.getName());
            goodsSalesVO.setNumber(orderDetailService.count(new LambdaQueryWrapper<OrderDetail>()
                    .eq(OrderDetail::getDishId, dish.getId())));
            goodsSalesVOList.add(goodsSalesVO);
        }
        return Result.success(goodsSalesVOList);
    }
    @PostMapping("/setmealScole")
    public Result<List<GoodsSalesVO>> setmealScole() {
        List<GoodsSalesVO> goodsSalesVOList = new ArrayList<>();
        // 查询所有套餐列表，而不是假设ID从0开始连续
        List<Setmeal> setmealList = setmealService.list();
        for(Setmeal setmeal : setmealList) {
            GoodsSalesVO goodsSalesVO = new GoodsSalesVO();
            goodsSalesVO.setName(setmeal.getName());
            goodsSalesVO.setNumber(orderDetailService.count(new LambdaQueryWrapper<OrderDetail>()
                    .eq(OrderDetail::getSetmealId, setmeal.getId())));
            goodsSalesVOList.add(goodsSalesVO);
        }
        return Result.success(goodsSalesVOList);
    }

    // 销量前5个菜品
    @PostMapping("/statisticsTopDish")
    public Result<List<GoodsSalesVO>> topDish(){
        List<GoodsSalesVO> goodsSalesVOList = dishScole().getData();
        // 按销量降序排列
        goodsSalesVOList.sort((a, b) -> b.getNumber().compareTo(a.getNumber()));
        // 取前5个
        goodsSalesVOList = goodsSalesVOList.subList(0, Math.min(5, goodsSalesVOList.size()));
        return Result.success(goodsSalesVOList);
    }
    // 销量前5个套餐
    @PostMapping("/statisticsTopSetmeal")
    public Result<List<GoodsSalesVO>> topSetmeal(){
        List<GoodsSalesVO> goodsSalesVOList = setmealScole().getData();
        // 按销量降序排列
        goodsSalesVOList.sort((a, b) -> b.getNumber().compareTo(a.getNumber()));
        // 取前5个
        goodsSalesVOList = goodsSalesVOList.subList(0, Math.min(5, goodsSalesVOList.size()));
        return Result.success(goodsSalesVOList);
    }
}
