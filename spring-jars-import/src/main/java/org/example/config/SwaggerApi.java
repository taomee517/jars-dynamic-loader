package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerApi {
	    @Bean
	    public Docket createRestApi() {// 创建API基本信息  
	        return new Docket(DocumentationType.SWAGGER_2)  
	                .apiInfo(apiInfo()) 
	                .enable(true)
	                .select()
	                .apis(RequestHandlerSelectors.basePackage("org.example.controller"))// 扫描该包下的所有需要在Swagger中展示的API，@ApiIgnore注解标注的除外
	                .paths(PathSelectors.any())  
	                .build();  
	    }  
	  
	    private ApiInfo apiInfo() {// 创建API的基本信息，这些信息会在Swagger UI中进行显示  
	        return new ApiInfoBuilder()
	                .title("Jars Loader Api")// API 标题
	                .description("jar包动态加载接口")// API描述
	                .version("1.0")// 版本
	                .build();  
	    } 

}
