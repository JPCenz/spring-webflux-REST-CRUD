package com.jpcenz.projects.springwebfluxrestcrud.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpcenz.projects.springwebfluxrestcrud.models.documents.Producto;
import com.jpcenz.projects.springwebfluxrestcrud.service.ProductoService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import lombok.extern.slf4j.Slf4j;
import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/productos")
@Slf4j
public class ProductoController {
    private final ProductoService service;

    @Value("${config.uploads.path}")
    private String path;


    public ProductoController(ProductoService service) {
        this.service = service;

    }

    @GetMapping
    public Mono<ResponseEntity<Flux<Producto>>> listar(){
        log.info("listar productos");
        return Mono.just(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(service.findAll().doOnNext(prod -> log.info(prod.getNombre()))));

    }
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Producto>> verDetalle(@PathVariable(value = "id") String id){
        return service.findById(id).map(p -> ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String,Object>>> crear(@Valid @RequestBody Mono<Producto> productoMono){
        Map<String,Object> response = new HashMap<String, Object>();

        return productoMono.flatMap(producto -> {

            if (producto.getCreateAt() == null){
                producto.setCreateAt(new Date());
            }
            return service.save(producto);
        }).map(p -> {
            response.put("producto", p.getId());
            response.put("mensaje", "Producto creado con éxito");
            response.put("timestamp", new Date());
            response.put("status", HttpStatus.CREATED.value());
            return ResponseEntity.created(URI.create("/api/productos/".concat(p.getId())))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }).onErrorResume(t -> {
            return Mono.just(t).cast(WebExchangeBindException.class)
                    .flatMap(e -> Mono.just(e.getFieldErrors()))
                    .flatMapMany(Flux::fromIterable)
                    .map(fieldError -> "El campo "+fieldError.getField()+" "+fieldError.getDefaultMessage())
                    .collectList()
                    .flatMap(list -> {
                        response.put("errors", list);
                        response.put("status", HttpStatus.BAD_REQUEST.value());
                        response.put("timestamp", new Date());
                        return Mono.just(ResponseEntity.badRequest().body(response));
                    });
        });

    }

    @PostMapping("/v2")
    public Mono<ResponseEntity<Map<String, Object>>> crearV2(@RequestPart(name = "file") FilePart file, @RequestPart(name = "json") String jsonString) throws JsonParseException, IOException {
        //convierte Filepart json a un objeto JSON
        var producto = convertJsonToProducto(jsonString);
        producto.setFoto(fixFilename(file));
        Map<String,Object> response = new HashMap<>();

        // Validate the Producto object
        Set<ConstraintViolation<Producto>> violations = Validation.buildDefaultValidatorFactory().getValidator().validate(producto);
        if (!violations.isEmpty()) {
            var errors = new ArrayList<String>();
            for (ConstraintViolation<Producto> violation : violations) {
                errors.add(violation.getPropertyPath()+" - "+violation.getMessage());
            }
            log.error("ERROR al guardar Producto: "+producto.getNombre()+" - "+errors.toString());
            response.put("errors", errors);
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Error en la validación del Producto");
            response.put("timestamp", new Date());
            return Mono.just(ResponseEntity.badRequest().body(response));
        }

        return Mono.just(producto).flatMap(p -> {
            p.setCreateAt(new Date());
            return file.transferTo(new File(path + p.getFoto()));
        }).then(service.save(producto)).map(p -> {
            response.put("producto", p);
            response.put("status", HttpStatus.CREATED.value());
            response.put("message", "Producto creado con éxito");
            response.put("timestamp", new Date());
            log.info("Producto subido con éxito: "+producto.getFoto());
            return ResponseEntity.created(URI.create("/api/productos/".concat(p.getId())))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }).onErrorResume(t -> {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Error al guardar el Producto");
            response.put("timestamp", new Date());
            response.put("error", t.getMessage());
            log.error("ERROR al guardar Producto: "+producto.getNombre()+" - "+t.getMessage());
            return Mono.just(ResponseEntity.badRequest().body(response));
        });


    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Producto>> editar(@RequestBody Producto producto, @PathVariable String id) {
        return service.findById(id).flatMap(p -> {
            p.setNombre(producto.getNombre());
            p.setPrecio(producto.getPrecio());
            p.setCategoria(producto.getCategoria());
            return service.save(p);
        }).map(p -> ResponseEntity.created(URI.create("/api/productos/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(p))
        .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable String id) {
        return service.findById(id).flatMap(p -> {
            return service.delete(p).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
        }).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity<Producto>> upload(@PathVariable String id, @RequestPart(name = "file") FilePart file){
        return service.findById(id).flatMap(p -> {
            p.setFoto(fixFilename(file));
            return file.transferTo(new File(path + p.getFoto())).then(service.save(p));
        }).map(p -> ResponseEntity.ok(p))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private String fixFilename(FilePart file) {
        return UUID.randomUUID() + "-" + file.filename()
                .replace(" ", "")
                .replace(":", "")
                .replace("\\", "");
    }
    private Producto convertJsonToProducto(String json) throws JsonProcessingException {
        json = new String(json.getBytes(StandardCharsets.UTF_8));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(json);
        Producto p = mapper.readValue(json, Producto.class);
        log.info("JSON : "+actualObj);
        log.info("Producto : "+p);
        return p;
    }
}

