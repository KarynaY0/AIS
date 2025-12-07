package ais.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Admin mappings
        registry.addViewController("/admin-dashboard").setViewName("admin-dashboard");
        registry.addViewController("/admin-students").setViewName("admin-students");
        registry.addViewController("/admin-teachers").setViewName("admin-teachers");
        registry.addViewController("/admin-subjects").setViewName("admin-subjects");
        registry.addViewController("/admin-groups").setViewName("admin-groups");
        registry.addViewController("/admin-assignments").setViewName("admin-assignments");


        // Teacher mappings
        registry.addViewController("/teacher-dashboard").setViewName("teacher-dashboard");
        registry.addViewController("/teacher-grades").setViewName("teacher-grades");

        // Root redirect
        registry.addViewController("/").setViewName("redirect:/login");
    }
}