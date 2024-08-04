package com.jpcenz.projects.springwebfluxrestcrud.models.dao;


import com.jpcenz.projects.springwebfluxrestcrud.models.documents.Producto;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductoDao extends ReactiveMongoRepository<Producto,String> {

    public Mono<Producto> findByNombre(String nombre);
    @Query("{ 'nombre': ?0 }")
    public Mono<Producto> findByNombreQuery(String nombre);
}
