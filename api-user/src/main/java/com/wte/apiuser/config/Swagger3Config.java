package com.wte.apiuser.config;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.setting.dialect.Props;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.wt.wte.wtebase.common.api.ResultCode;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Create by chenglong on 2021/1/14
 */
@Configuration
@EnableOpenApi
@EnableKnife4j
public class Swagger3Config implements WebMvcConfigurer {

    private static Props SWAGGER = Props.getProp("properties/swagger.properties");

    @Bean
    public Docket createRestApi(){
        List<ResponseMessage> responseMessageList = new ArrayList<>();
        Arrays.stream(ResultCode.values()).forEach(errorEnum -> {
            responseMessageList.add(
                    new ResponseMessageBuilder()
                            .code(NumberUtil.parseInt(errorEnum.getCode()))
                            .message(errorEnum.getMessage())
                            .responseModel(
                            new ModelRef(errorEnum.getMessage())).build()
            );
        });
        return new Docket(DocumentationType.SWAGGER_2)
                .globalResponseMessage(RequestMethod.GET, responseMessageList)
                .globalResponseMessage(RequestMethod.POST, responseMessageList)
                .globalResponseMessage(RequestMethod.PUT, responseMessageList)
                .globalResponseMessage(RequestMethod.DELETE, responseMessageList)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.wte.apiuser.controller"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    /**
     * API文档基本信息
     * @return
     */
    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title(SWAGGER.getStr("title"))
                .description(SWAGGER.getStr("description"))
                .contact(new Contact(SWAGGER.getStr("contactName"), SWAGGER.getStr("contactUrl"), SWAGGER.getStr("contactEmail")))
                .version(SWAGGER.getStr("version"))
                .build();
    }

    /**
     * 设置需要登录认证的路径
     * @return
     */
    private List<SecurityContext> securityContexts() {
        List<SecurityContext> securityContexts = new ArrayList<>();
        SecurityContext securityContext = SecurityContext.builder()
                .securityReferences(securityReferences())
                .forPaths(PathSelectors.regex("/api/((?!account).)+/.*")) //除了api/account/路径下的接口不需要登录认证，其他都需要
                .build();
        securityContexts.add(securityContext);
        return securityContexts;
    }

    private List<SecurityReference> securityReferences(){
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{new AuthorizationScope("global", "accessEverything")};
        List<SecurityReference> securityReferences = new ArrayList<>();
        securityReferences.add(new SecurityReference("Authorization",authorizationScopes));
        return securityReferences;
    }

    /**
     * 设置请求头信息
     * @return
     */
    private List<SecurityScheme> securitySchemes() {
        List<SecurityScheme> securitySchemes = new ArrayList<>();
        ApiKey apiKey = new ApiKey("Authorization", "token", SecuritySchemeIn.HEADER.toString());
        securitySchemes.add(apiKey);
        return securitySchemes;
    }

}
