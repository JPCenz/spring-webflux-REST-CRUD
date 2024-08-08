# Spring Webflux REST CRUD

This project is a Spring Webflux-based RESTful CRUD application for managing products and categories.

## Prerequisites

- Java 17 
- Spring Boot 3.3.2
- Maven 3.6.3 or higher
- MongoDB

## Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/JPCenz/spring-webflux-rest-crud.git
    cd spring-webflux-rest-crud
    ```

2. Build the project using Maven:
    ```sh
    mvn clean install
    ```

3. Run the application:
    ```sh
    mvn spring-boot:run
    ```

## Usage

### Endpoints

- **List all products**
    ```http
    GET /api/productos
    ```

- **Get product details by ID**
    ```http
    GET /api/productos/{id}
    ```

- **Create a new product**
    ```http
    POST /api/productos
    Content-Type: application/json
    {
        "nombre": "Product Name",
        "precio": 100.0,
        "categoria": {
            "id": "category_id"
        }
    }
    ```

- **Update a product by ID**
    ```http
    PUT /api/productos/{id}
    Content-Type: application/json
    {
        "nombre": "Updated Product Name",
        "precio": 150.0,
        "categoria": {
            "id": "updated_category_id"
        }
    }
    ```

- **Delete a product by ID**
    ```http
    DELETE /api/productos/{id}
    ```

- **Upload a file for a product**
    ```http
    POST /api/productos/upload/{id}
    Content-Type: multipart/form-data
    {
        "file": "file_to_upload"
    }
    ```

- **Create a new product with file upload (v2)**
    ```http
    POST /api/productos/v2
    Content-Type: multipart/form-data
    {
        "file": "file_to_upload",
        "json": "{\"nombre\": \"Product Name\", \"precio\": 100.0, \"categoria\": {\"id\": \"category_id\"}}"
    }
    ```

  # API V2

### Ejemplos de Solicitudes

- **Listar todos los productos**
    ```http
    GET /api/v2/productos
    ```

- **Ver detalles de un producto por ID**
    ```http
    GET /api/v2/productos/{id}
    Content-Type: application/json
    ```

- **Crear un nuevo producto**
    ```http
    POST /api/v2/productos
    Content-Type: application/json
    {
        "nombre": "Nombre del Producto",
        "precio": 100.0,
        "categoria": {
            "id": "id_de_categoria"
        }
    }
    ```

- **Actualizar un producto por ID**
    ```http
    PUT /api/v2/productos/{id}
    Content-Type: application/json
    {
        "nombre": "Nombre Actualizado del Producto",
        "precio": 150.0,
        "categoria": {
            "id": "id_de_categoria_actualizada"
        }
    }
    ```

- **Eliminar un producto por ID**
    ```http
    DELETE /api/v2/productos/{id}
    ```

- **Subir un archivo para un producto**
    ```http
    POST /api/v2/productos/upload/{id}
    Content-Type: multipart/form-data
    {
        "file": "archivo_a_subir"
    }
    ```

- **Crear un nuevo producto con subida de archivo**
    ```http
    POST /api/v2/productos/create-upload/
    Content-Type: multipart/form-data
    {
        "file": "archivo_a_subir",
        "json": "{\"nombre\": \"Nombre del Producto\", \"precio\": 100.0, \"categoria\": {\"id\": \"id_de_categoria\"}}"
    }
    ```

    POSTMAN LINK https://elements.getpostman.com/redirect?entityId=24549653-cb5d493f-294d-4ab7-8c3d-31bc0dec6339&entityType=collection
## License

This project is licensed under the MIT License.
