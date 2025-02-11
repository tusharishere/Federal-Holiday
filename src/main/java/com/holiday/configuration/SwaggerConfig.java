package com.holiday.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI mySwaggerConfig() {
        return new OpenAPI().info(new Info()
                        .title("Federal Holidays USA/Canada/UK")
                        .description("By Tushar"))
                        .servers(Arrays.asList(new io.swagger.v3.oas.models.servers.Server().
                                        url("http://localhost:8080").description("local"),
                                        new Server().url("http://localhost:8081").description("live")));
    }

}
