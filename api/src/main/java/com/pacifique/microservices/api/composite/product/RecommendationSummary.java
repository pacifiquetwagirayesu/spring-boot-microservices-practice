package com.pacifique.microservices.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor(force = true)
public class RecommendationSummary {
    private final int recommendationId;
    private final String author;
    private final int rate;

}
