package com.jpcenz.projects.springwebfluxrestcrud.models.dao;

import com.jpcenz.projects.springwebfluxrestcrud.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria,String> {
    Flux<Categoria> findAllByNombre(String nombre);
}
