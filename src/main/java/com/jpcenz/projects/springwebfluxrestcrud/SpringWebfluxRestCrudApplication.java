package com.jpcenz.projects.springwebfluxrestcrud;

import com.jpcenz.projects.springwebfluxrestcrud.models.documents.Categoria;
import com.jpcenz.projects.springwebfluxrestcrud.models.documents.Producto;
import com.jpcenz.projects.springwebfluxrestcrud.service.ProductoService;
import com.jpcenz.projects.springwebfluxrestcrud.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.logging.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

@SpringBootApplication
@Slf4j
public class SpringWebfluxRestCrudApplication implements CommandLineRunner {

    private final ProductoService service;
    private final org.springframework.data.mongodb.core.ReactiveMongoTemplate template;

    public SpringWebfluxRestCrudApplication(ProductoService service, ReactiveMongoTemplate template) {
        this.service = service;
        this.template = template;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringWebfluxRestCrudApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        insertarDatos();
    }

    private void insertarDatos() {
        template.dropCollection("productos").subscribe();
        template.dropCollection("categorias").subscribe();
        //crear las categorias
        var electronico = Categoria.builder().nombre("electronico").codigo("001").build();
        var deporte = Categoria.builder().nombre("deporte").codigo("002").build();
        var computacion = Categoria.builder().nombre("computacion").codigo("003").build();
        var muebles = Categoria.builder().nombre("muebles").codigo("004").build();
        testJsonUtil();
        //crear nuevos productos con categoria usando builder de producto

        Flux.just(electronico, deporte, computacion, muebles)
                .flatMap(service::saveCategoria)
                .doOnNext(categoria -> log.info("Insert: " + categoria.getId() + " " + categoria.getNombre()))
                .thenMany(
                        Flux.just(
                                        Producto.builder().nombre("TV panasonic").precio(456.77)
                                                .categoria(electronico).build(),
                                        Producto.builder().nombre("Sony camara")
                                                .precio(1772.77).categoria(electronico).build(),
                                        Producto.builder().nombre("Sony notebook")
                                                .precio(1377.77).categoria(computacion).build(),
                                        Producto.builder().nombre("Sony Tablet")
                                                .precio(1757.77).categoria(computacion).build()
                                )
                                .flatMap(producto -> {
                                    producto.setCreateAt(new java.util.Date());
                                    return service.save(producto);
                                })
                )
                .subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));
    }

    private void testJsonUtil() {
        var p = Producto.builder().nombre("TV panasonic").precio(456.77)
                .categoria(null).build();
        JsonUtil.convertObjectToJsonWithPrefix(p, "DESA_");
        log.info("JSON"+JsonUtil.convertObjectToJsonWithPrefix(p, "DESA_")) ;
    }
}
