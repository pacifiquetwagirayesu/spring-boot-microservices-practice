package com.pacifique.microservices.composite.product.services;

import com.pacifique.microservices.api.composite.product.*;
import com.pacifique.microservices.api.core.product.Product;
import com.pacifique.microservices.api.core.recommendation.Recommendation;
import com.pacifique.microservices.api.core.review.Review;
import com.pacifique.microservices.api.exceptions.NotFoundException;
import com.pacifique.microservices.util.http.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public ProductAggregate getProduct(int productId) {
        Product product = integration.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found with id: " + productId);
        }
        List<Recommendation> recommendations = integration.getRecommendations(productId);
        System.out.println(recommendations);
        List<Review> reviews = integration.getReviews(productId);
        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {

        // 1. setup product info
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null : recommendations.stream().map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate())).toList();
        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null : reviews.stream().map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject())).toList();

        // 4. Create info regarding the involved microservices address
        String productServiceAddress = product.getServiceAddress();
        String reviewServiceAddress = (reviews != null && !reviews.isEmpty()) ? reviews.get(0).getServiceAddress():"";
        String recommendationService = (recommendations != null && !recommendations.isEmpty()) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productServiceAddress, reviewServiceAddress, recommendationService);

        return new ProductAggregate(productId,name,weight,recommendationSummaries,reviewSummaries,serviceAddresses);
    }
}
