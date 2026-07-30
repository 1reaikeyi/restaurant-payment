import org.junit.jupiter.api.Test;
import pojo.entityenum.OrderStatusEnum;

import java.time.LocalDateTime;

public class Get {
    @Test
    public void test() {
        System.out.println(LocalDateTime.now().toString());

        System.out.println(OrderStatusEnum.COMPLETED);
        // 今日 00:00:00
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        System.out.println("startOfDay = " + startOfDay);
        // 今日 23:59:59
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        System.out.println("endOfDay = " + endOfDay);
    }
}
