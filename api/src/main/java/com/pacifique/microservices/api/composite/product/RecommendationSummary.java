package com.pacifique.microservices.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor(force = true)
public class RecommendationSummary {
    private  int recommendationId;
    private  String author;
    private  int rate;
    private String content;

}
