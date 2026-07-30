package start.config;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import start.interceptor.JwtTokenAdminInterceptor;
import start.interceptor.JwtTokenUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import start.interceptor.LoginInterceptor;

@Configuration
public class WebMVCConfiguration implements WebMvcConfigurer {
    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;
    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //        // 注册登录拦截器，排除注册和登录接口
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .order(0)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employees/register",
                        "/admin/employees/login",
                        "/admin/employees/logout");
        registry.addInterceptor(jwtTokenUserInterceptor)
                .order(0)
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/users/register",
                        "/user/users/login",
                        "/user/users/logout");

        registry.addInterceptor(loginInterceptor)
                .order(10)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employees/register",
                        "/admin/employees/login",
                        "/admin/employees/logout");
        registry.addInterceptor(loginInterceptor)
                .order(10)
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/users/register",
                        "/user/users/login",
                        "/user/users/logout");
    }

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