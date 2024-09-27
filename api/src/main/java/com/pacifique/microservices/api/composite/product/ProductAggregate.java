package com.pacifique.microservices.api.composite.product;

import com.pacifique.microservices.api.core.review.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ProductAggregate {
    private final int productId;
    private final String nam;
    private final int weight;
    private final List<RecommendationSummary> recommendations;
    private final List<ReviewSummary> reviews;
    private final ServiceAddresses serviceAddresses;

}
