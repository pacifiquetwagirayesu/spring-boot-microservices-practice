package com.pacifique.microservices.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ReviewSummary {
    private  int reviewId;
    private  String author;
    private  String subject;
    private  String content;
}
