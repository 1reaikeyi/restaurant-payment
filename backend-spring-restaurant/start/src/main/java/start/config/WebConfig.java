package start.config;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 自定义图片映射
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:start/img/");
        // 保留默认映射
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}