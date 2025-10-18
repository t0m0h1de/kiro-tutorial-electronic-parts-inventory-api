package com.example.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger設定クラス
 * API仕様書の自動生成設定
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI electronicPartsInventoryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("電子部品在庫管理API")
                        .description("電子部品の在庫管理を行うREST APIシステム。" +
                                   "Java Spring BootとMariaDBを使用して、電子部品の登録、更新、削除、検索、在庫数量の管理を提供します。")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("開発チーム")
                                .email("dev-team@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("開発環境"),
                        new Server()
                                .url("https://api.example.com")
                                .description("本番環境")));
    }
}