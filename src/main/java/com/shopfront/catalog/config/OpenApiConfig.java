package com.shopfront.catalog.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Catalog API", version = "2.1", description = "Products, categories and prices."),
        servers = @Server(url = "/"))
public class OpenApiConfig {
}
