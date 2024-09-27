package com.pacifique.microservices.api.core.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(force = true)
@AllArgsConstructor
@Data
public class Product {
    private final int productId;
    private final String name;
    private final int weight;
    private final String serviceAddress;
}
