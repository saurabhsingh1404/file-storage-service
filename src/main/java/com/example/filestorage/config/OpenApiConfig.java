package com.example.filestorage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl("https://freightfox-file-storage-service-f901d.containers.snapdeploy.app");
        server.setDescription("SnapDeploy Production Server");

        return new OpenAPI()
                .addServersItem(server);
    }
}