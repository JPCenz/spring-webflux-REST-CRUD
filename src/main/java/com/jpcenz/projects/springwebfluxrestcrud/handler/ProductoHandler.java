package com.jpcenz.projects.springwebfluxrestcrud.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpcenz.projects.springwebfluxrestcrud.models.documents.Producto;
import com.jpcenz.projects.springwebfluxrestcrud.service.ProductoService;
import jakarta.validation.ConstraintViolation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.BeanProperty;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class ProductoHandler {
    @Autowired
    ProductoService service;
    @Value("${config.uploads.path}")
    String path;
    @Autowired
    Validator validator;

    public Mono<ServerResponse> listar(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.findAll(), Producto.class);
    }

    public Mono<ServerResponse> ver(ServerRequest request) {
        String id = request.pathVariable("id");
        return service.findById(id).flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crear(ServerRequest request) {
        Mono<Producto> producto = request.bodyToMono(Producto.class);
        var response = new HashMap<String, Object>();
        var result = new BeanPropertyBindingResult(producto, Producto.class.getName());
        return producto.flatMap(p -> {
            validator.validate(p, result);
            if (result.hasErrors()) {
                return Flux.fromIterable(result.getFieldErrors())
                        .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                        .collectList()
                        .flatMap(list -> {
                            response.put("errors", list);
                            return ServerResponse.badRequest().body(BodyInserters.fromValue(response));
                        });
            }
            if (p.getCreateAt() == null) {
                p.setCreateAt(new Date());
            }
            return service.save(p)
                    .flatMap(pp -> ServerResponse.created(URI.create("/api/v2/productos/".concat(pp.getId())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(pp)).onErrorResume(error -> {
                        response.put("error", error.getMessage());
                        return ServerResponse.badRequest().body(BodyInserters.fromValue(response));

                    });
        });
    }

    public Mono<ServerResponse> editar(ServerRequest request) {
        Mono<Producto> producto = request.bodyToMono(Producto.class);
        String id = request.pathVariable("id");
        var response = new HashMap<String, Object>();
        log.info("Producto a editar: " + id);
        var result = new BeanPropertyBindingResult(producto, Producto.class.getName());

        return producto.flatMap(p -> {
             validator.validate(p,result);
            if (result.hasErrors()) {
                return Flux.fromIterable(result.getFieldErrors())
                        .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                        .collectList()
                        .flatMap(list -> {
                            response.put("errors", list);
                            return ServerResponse.badRequest().body(BodyInserters.fromValue(response));
                        });
            }
            return service.findById(id).zipWith(Mono.just(p), (db, req) -> {
                        db.setNombre(req.getNombre());
                        db.setPrecio(req.getPrecio());
                        db.setCategoria(req.getCategoria());
                        log.info("Producto a editar: " + db.getNombre() + " - " + db.getPrecio() + " - " + db.getCategoria());
                        return db;
                    }).flatMap(pp -> ServerResponse.created(URI.create("/api/v2/productos/".concat(pp.getId())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(service.save(pp), Producto.class)).onErrorResume(error -> {
                        response.put("error", error.getMessage());
                        return ServerResponse.badRequest().body(BodyInserters.fromValue(response));
                    })
                    .switchIfEmpty(ServerResponse.notFound().build());

        });
    }

    public Mono<ServerResponse> eliminar(ServerRequest request) {

        String id = request.pathVariable("id");
        log.info("Producto a eliminar: " + id);
        return service.findById(id).flatMap(p -> service.delete(p).then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build()).onErrorResume(error -> ServerResponse.badRequest().build());
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");
        var response = new HashMap<String, Object>();
        return request.body(BodyExtractors.toMultipartData()).flatMap(parts -> {
            var part = parts.toSingleValueMap().get("file");
            if(part == null ){
                return Mono.error(new Exception("No se encontró el archivo file"));
            }
            var filePart = (FilePart) part;
            var filename = fixFilename(filePart);
            return filePart.transferTo(new java.io.File(path + filename))
                    .then(service.findById(id))
                    .flatMap(p -> {
                        p.setFoto(filename);
                        return service.save(p);
                    }).switchIfEmpty(Mono.error(new Exception("Producto no encontrado")))   ;
        }).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(p)).onErrorResume(error -> {
            response.put("error", error.getMessage());
            return ServerResponse.badRequest().body(BodyInserters.fromValue(response));
        });


    }

    public Mono<ServerResponse> crearConUpload (ServerRequest request) {
        var response = new HashMap<String, Object>();


        return request.body(BodyExtractors.toMultipartData()).flatMap(parts -> {
            var part = parts.toSingleValueMap().get("file");
            var json = parts.toSingleValueMap().get("json");
            if (part == null || json == null) {
                var strError = "No se encontró en el Multipart" + (part == null ? " file" : "") + (json == null ? " json" : "");
                return Mono.error(new Exception(strError));
            }

            var filePart = (FilePart) part;
            var filename = fixFilename(filePart);
            var jsonPart = (FormFieldPart) json;
            return convertPartToString(jsonPart).flatMap(j -> {
                Producto producto;
                try {
                    producto = convertJsonToProducto(j);
                    var result = new BeanPropertyBindingResult(producto, Producto.class.getName());
                    validator.validate(producto, result);
                    if (result.hasErrors()) {
                        log.info("Errores en la validación"+result.getFieldErrors());
                        return Flux.fromIterable(result.getFieldErrors())
                                .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                                .collectList()
                                .flatMap(list -> {
                                    response.put("errors", list);
                                    return ServerResponse.badRequest().body(BodyInserters.fromValue(response));
                                });
                    }
                } catch (JsonProcessingException e) {
                    return Mono.error(new Exception("Error al convertir JSON a Producto"));
                }
                return filePart.transferTo(new java.io.File(path + filename))
                        .then(service.save(producto))
                        .flatMap(p -> {
                            p.setFoto(filename);
                            return service.save(p);
                        }).switchIfEmpty(Mono.error(new Exception("Producto no encontrado")))
                        .flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(p));
            });
        }).onErrorResume(error -> {
                            log.error(error.getMessage());
                    response.put("error", error.getMessage());
                    return ServerResponse.badRequest().body(BodyInserters.fromValue(response));
                });

    }

    private String fixFilename(FilePart file) {
        return UUID.randomUUID() + "-" + file.filename()
                .replace(" ", "")
                .replace(":", "")
                .replace("\\", "");
    }

    private Producto convertJsonToProducto(String json) throws JsonProcessingException{
        json = new String(json.getBytes(StandardCharsets.UTF_8));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(json);
        Producto p = mapper.readValue(json, Producto.class);
        log.info("JSON : "+actualObj);
        log.info("Producto : "+p);
        return p;
    }

    public Mono<String> convertPartToString(Part part) {
        return part.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return new String(bytes, StandardCharsets.UTF_8);
                })
                .reduce(String::concat);
    }
}
