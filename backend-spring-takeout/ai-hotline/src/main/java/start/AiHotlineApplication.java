package start;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"start","graph","config","service"})
@MapperScan("service.tool.mapper")
@Slf4j
public class AiHotlineApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiHotlineApplication.class, args);
        log.info(">>>flow");
    }

}
