package cl.biblioteca.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuracion de la documentacion OpenAPI / Swagger UI (Pasos 2 y 3 de la guia).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bibliotecaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Biblioteca - DSY1104")
                        .version("1.0.0")
                        .description("""
                                API REST para la gestion de libros de una biblioteca.
                                Permite crear, consultar, actualizar, buscar y eliminar libros.
                                Implementada con Spring Boot, Spring Data JPA y H2.
                                """)
                        .contact(new Contact()
                                .name("Desarrollo Full Stack II")
                                .email("estudiante@duocuc.cl"))
                        .license(new License().name("MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Servidor local")));
    }
}
