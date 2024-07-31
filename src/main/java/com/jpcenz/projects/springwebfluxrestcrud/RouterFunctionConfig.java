package com.jpcenz.projects.springwebfluxrestcrud;


import com.jpcenz.projects.springwebfluxrestcrud.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import org.springframework.web.reactive.function.server.RequestPredicates;

@Configuration
public class RouterFunctionConfig {
    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler handler){

        return RouterFunctions.route(RequestPredicates.GET("/api/v2/productos").or(RequestPredicates.GET("/api/v3/productos")), request -> handler.listar(request))
                .andRoute(RequestPredicates.GET("/api/v2/productos/{id}").and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)),handler::ver)
                .andRoute(RequestPredicates.POST("/api/v2/productos").and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)),handler::crear)
                .andRoute(RequestPredicates.PUT("/api/v2/productos/{id}"),handler::editar)
                .andRoute(RequestPredicates.DELETE("/api/v2/productos/{id}"),handler::eliminar)
                .andRoute(RequestPredicates.POST("/api/v2/productos/upload/{id}"),handler::upload)
                .andRoute(RequestPredicates.POST("/api/v2/productos/create-upload/"),handler::crearConUpload);
    }
}
