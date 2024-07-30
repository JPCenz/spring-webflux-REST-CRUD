package com.jpcenz.projects.springwebfluxrestcrud.models.documents;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "categorias")
public class Categoria {

    @Id
    @NotEmpty
    private String id;
    private String nombre;
    private String descripcion;
    private String estado;
    private String createAt;
    private String codigo;
}
