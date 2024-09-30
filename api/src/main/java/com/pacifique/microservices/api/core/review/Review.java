package com.pacifique.microservices.api.core.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor(force = true)
@Data
@Builder
public class Review {
    private int productId;
    private  int reviewId;
    private  String author;
    private  String subject;
    private  String content;
    private  String serviceAddress;
}
