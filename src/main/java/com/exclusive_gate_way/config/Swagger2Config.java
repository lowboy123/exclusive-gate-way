package com.exclusive_gate_way.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger2Config配置
 *
 * @author: Lizq
 * @Date: 2019/12/4 15:37
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Bean
    public Docket config() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select()
                .apis(RequestHandlerSelectors.basePackage("com.exclusive_gate_way.controller"))
                .paths(PathSelectors.any()).build();
    }

    private ApiInfo apiInfo() {
        Contact contact = new Contact("Lizq", "", "1437217811@qq.com");
        return new ApiInfoBuilder()
                .title("flowableTest 了解 flowable")
                .contact(contact)
                .version("1.0.0-SNAPSHOT")
                .build();
    }
}
