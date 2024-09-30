package com.pacifique.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pacifique.microservices.api.core.product.Product;
import com.pacifique.microservices.api.core.product.ProductService;
import com.pacifique.microservices.api.core.recommendation.Recommendation;
import com.pacifique.microservices.api.core.recommendation.RecommendationService;
import com.pacifique.microservices.api.core.review.Review;
import com.pacifique.microservices.api.core.review.ReviewService;
import com.pacifique.microservices.api.exceptions.InvalidInputException;
import com.pacifique.microservices.api.exceptions.NotFoundException;
import com.pacifique.microservices.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpMethod.GET;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService , ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(RestTemplate restTemplate,
                                       ObjectMapper mapper,
                                       @Value("${app.product-service.host}") String productServiceHost,
                                       @Value("${app.product-service.port}") int productServicePort,
                                       @Value("${app.recommendation-service.host}") String recommendationServiceHost,
                                       @Value("${app.recommendation-service.port}") int recommendationServicePort,
                                       @Value("${app.review-service.host}") String reviewServiceHost,
                                       @Value("${app.review-service.port}") int reviewServicePort

    ) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product";
        this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
        this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
    }


    @Override
    public Product createProduct(Product body) {
        String url = productServiceUrl;
        LOG.debug("Will post a new product to URL: {}", url);

        try {

            ResponseEntity<Product> responseEntity = restTemplate.postForEntity(url, body, Product.class);
            Product product = responseEntity.getBody();

            System.out.println("created product: " + product);
            LOG.debug("Created a product with id: {}", product.getProductId());

            return product;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Product getProduct(int productId) {
        String url = productServiceUrl +"/" + productId;
        LOG.info("Will call getProduct API on URL {}", url);
        Product product;

        try {
            product = restTemplate.getForObject(url, Product.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }

        LOG.debug("Found product with id: {}", Optional.of(Objects.requireNonNull(product).getProductId()));

        return product;
    }

    @Override
    public void deleteProduct(int productId) {
        String url = productServiceUrl +"/" + productId;
        LOG.debug("Will call the deleteProduct API on URL {}", url);

        try {
            restTemplate.delete(url);
        }catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }


    }


    @Override
    public Recommendation createRecommendation(Recommendation body) {
        Recommendation recommendation;

        try {
            recommendation = restTemplate.postForObject(recommendationServiceUrl, body, Recommendation.class);
        }catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }

        LOG.debug("Created a recommendation with id: {} ", Optional.of(Objects.requireNonNull(recommendation).getRecommendationId()));
        return recommendation;
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        String url = recommendationServiceUrl +"?productId="+ productId;
        LOG.debug("Will call getRecommendations API on URL {}", url);
        List<Recommendation> recommendations;

        try {
           recommendations = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {
            }).getBody();

        }catch (Exception ex) {
            LOG.warn("Got an exception while requesting recommendation, return zero recommendations: {}", ex.getMessage());
            return new ArrayList<>();
        }

        LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(),productId);
        return recommendations;
    }

    @Override
    public void deleteRecommendations(int productId) {
        String url = recommendationServiceUrl +"?productId="+ productId;
        LOG.debug("Will call the deleteRecommendations API on URL {}", url);

        try {
            restTemplate.delete(url);
        }catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Review createReview(Review body) {
        String url = reviewServiceUrl;
        LOG.debug("Will post a new review to URL {}", url);
        Review review;

        try {
            review = restTemplate.postForObject(url, body, Review.class);
        }catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }

        LOG.debug("Created a review with id: {} ", Optional.of(Objects.requireNonNull(review).getReviewId()));
        return review;
    }

    @Override
    public List<Review> getReviews(int productId) {
        String url = reviewServiceUrl +"?productId=" + productId;
        LOG.debug("Will call getReviews API on URL {}", url);
        List<Review> reviews;

        try {
           reviews = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {
            }).getBody();

        }catch (Exception ex) {
            LOG.warn("Got an exception while requesting review, return zero reviews: {}", ex.getMessage());
            return new ArrayList<>();
        }

        LOG.debug("Found {} reviews for a product with id: {}", reviews.size(),productId);
        return reviews;
    }

    @Override
    public void deleteReviews(int productId) {
        String url = reviewServiceUrl +"?productId=" + productId;
        LOG.debug("Will call the deleteReviews API on URL {}", url);

        try {
            restTemplate.delete(url);
        }catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }

    }


    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        switch (Objects.requireNonNull(HttpStatus.resolve(ex.getStatusCode().value()))) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(ex));

            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(ex));

            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                return ex;
        }
    }


    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }



}
