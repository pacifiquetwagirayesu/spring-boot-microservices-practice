package com.pacifique.microservices.api.core.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Recommendation {
    private  int productId;
    private  int recommendationId;
    private  String author;
    private  int rate;
    private  String content;
    private  String serviceAddress;

}
