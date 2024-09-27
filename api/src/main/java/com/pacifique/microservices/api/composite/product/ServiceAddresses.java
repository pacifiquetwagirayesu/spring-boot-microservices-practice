package com.pacifique.microservices.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor(force = true)
@Data
public class ServiceAddresses {
    private final String cmp;
    private final String pro;
    private final String rev;
    private final String rec;
}
