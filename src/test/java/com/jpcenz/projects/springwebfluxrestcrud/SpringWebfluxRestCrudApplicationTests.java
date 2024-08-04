package com.jpcenz.projects.springwebfluxrestcrud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpcenz.projects.springwebfluxrestcrud.models.documents.Categoria;
import com.jpcenz.projects.springwebfluxrestcrud.models.documents.Producto;
import com.jpcenz.projects.springwebfluxrestcrud.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.*;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SpringWebfluxRestCrudApplicationTests {

    @Autowired
    private WebTestClient client;
    @Autowired
    private ProductoService service;

    @Value("${config.base.endpoint-v2}")
    private String uriV2;
    @Value("${config.base.endpoint}")

    private String uriV1;


    @Test
    public void listartTest() {
        client.get().uri(uriV2)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Producto.class)

                .consumeWith(response -> {
                    List<Producto> productos = response.getResponseBody();
                    assertNotNull(productos, "The product list should not be null");
                    assertFalse(productos.isEmpty(), "The product list should not be empty");
                    productos.forEach(producto -> {
                        assertNotNull(producto.getId(), "Product ID should not be null");
                        assertNotNull(producto.getNombre(), "Product name should not be null");
                        assertTrue(producto.getPrecio().compareTo(0.0d) > 0, "Product price should be greater than zero");
                    });
                });
    }

    @Test
    public void verProductoTest() {
        Optional<Producto> prd = service.findProductoByNombre("TV panasonic").blockOptional();
        client.get()
                .uri(uriV2 + "/{id}", Collections.singletonMap("id", prd.get().getId()))
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Producto.class)
                .consumeWith(response -> {
                    Producto producto = response.getResponseBody();
                    assertNotNull(producto, "The product should not be null");
                    assertNotNull(producto.getId(), "Product ID should not be null");
                    assertNotNull(producto.getNombre(), "Product name should not be null");
                    assertTrue(producto.getPrecio().compareTo(0.0d) > 0, "Product price should be greater than zero");
                });
    }

    @Test
    public void crearTest() {
        var categoriaNombre = "electronico";
        var nombreProducto = "Iphone 12 pro";
        var precio = 1200.00;
        Categoria categoria = service.findCategoriaByNombre(categoriaNombre).blockFirst();
        Producto producto = Producto.builder().nombre(nombreProducto).precio(precio).categoria(categoria).build();
        client.post()
                .uri(uriV2)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(producto), Producto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo(nombreProducto)
                .jsonPath("$.precio").isEqualTo(precio)
                .jsonPath("$.categoria.nombre").isEqualTo(categoriaNombre);

    }

    @Test
    public void editarTest() {
        var categoriaNombre = "electronico";
        var nombreProducto = "Iphone 12 pro";
        var precio = 1200.00;
        Categoria categoria = service.findCategoriaByNombre(categoriaNombre).blockFirst();
        Producto producto = service.findProductoByNombre("Sony notebook").block();
        Producto productoEditado = Producto.builder().nombre(nombreProducto).precio(precio).categoria(categoria).build();
        client.put()
                .uri(uriV2 + "/{id}", Collections.singletonMap("id", producto.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(productoEditado), Producto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.nombre").isEqualTo(nombreProducto)
                .jsonPath("$.precio").isEqualTo(precio)
                .jsonPath("$.categoria.nombre").isEqualTo(categoriaNombre);
    }

    @Test
    public void eliminarTest() {
        Producto producto = service.findProductoByNombre("Sony camara").block();
        client.delete()
                .uri(uriV2 + "/{id}", Collections.singletonMap("id", producto.getId()))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .isEmpty();
        assertEquals(Boolean.FALSE, service.findProductoByNombre("Sony camara").hasElement().block());
    }

    //API v1 TESTs

    @Test
    public void listartTestV1() {
        client.get().uri(uriV1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Producto.class)

                .consumeWith(response -> {
                    List<Producto> productos = response.getResponseBody();
                    assertNotNull(productos, "The product list should not be null");
                    assertFalse(productos.isEmpty(), "The product list should not be empty");
                    productos.forEach(producto -> {
                        assertNotNull(producto.getId(), "Product ID should not be null");
                        assertNotNull(producto.getNombre(), "Product name should not be null");
                        assertTrue(producto.getPrecio().compareTo(0.0d) > 0, "Product price should be greater than zero");
                    });
                });
    }

    @Test
    public void crearTestV1() {
        var categoriaNombre = "electronico";
        var nombreProducto = "Iphone 12 pro";
        var precio = 1200.00;

        var msj = "Producto creado con éxito";
        var timestamp ="timestamp";
        var status = "status";
        Categoria categoria = service.findCategoriaByNombre(categoriaNombre).blockFirst();
        Producto producto = Producto.builder().nombre(nombreProducto).precio(precio).categoria(categoria).build();
        client.post()
                .uri(uriV1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(producto), Producto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {
                })
                .consumeWith(response -> {
                    LinkedHashMap<String, Object> responseMap = response.getResponseBody();
                    Producto p =new  ObjectMapper().convertValue(responseMap.get("producto"), Producto.class);
                    assertEquals(msj, responseMap.get("mensaje"));
                    assertEquals(HttpStatus.CREATED.value(), responseMap.get(status));
                    assertNotNull(responseMap.get(timestamp));
                });

    }

}
