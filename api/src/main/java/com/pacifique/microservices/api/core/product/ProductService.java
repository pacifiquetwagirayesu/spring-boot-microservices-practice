package com.pacifique.microservices.api.core.product;

import org.springframework.web.bind.annotation.*;

public interface ProductService {

    /**
     * Sample usage, see below.
     *
     * curl -X POST $HOST:$PORT/product \
     *     -H "Content-Type: application/json" --data \
     *     '{"productId: 123", "name":"product 123", "weight": 123}'
     *
     * @param body A JSON representation of the new product
     * @return A JSON representation of the newly created product
     */
    @PostMapping(
            path = "/product",
            consumes = "application/json",
            produces = "application/json")
    Product createProduct(@RequestBody Product body);

    /**
     * Sample usage:  "curl $HOST:$PORT/product/1"
     * @param productId  Id of the product
     * @return the product if found else null
    */
    @GetMapping(
            path = "/product/{productId}",
            produces = "application/json")
    Product getProduct( @PathVariable int productId);

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/product/1
     * @ PathVariable productId
     */
    @DeleteMapping(path = "/product/{productId}")
    void deleteProduct(@PathVariable int productId);
}
