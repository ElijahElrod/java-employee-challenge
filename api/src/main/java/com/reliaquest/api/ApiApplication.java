package com.reliaquest.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info =
                @Info(
                        title = "Employee API",
                        version = "1.0.0",
                        description =
                                "This API provides endpoints to create, delete, and perform various read queries against a 3rd party employee API."),
        servers = @Server(url = "http://localhost:8111", description = "Dev Server (uses test data via mock API"))
@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
