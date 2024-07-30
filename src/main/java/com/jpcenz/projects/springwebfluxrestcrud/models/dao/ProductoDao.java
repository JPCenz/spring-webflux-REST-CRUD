package com.jpcenz.projects.springwebfluxrestcrud.models.dao;


import com.jpcenz.projects.springwebfluxrestcrud.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoDao extends ReactiveMongoRepository<Producto,String> {
}
