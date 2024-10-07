package com.pacifique.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pacifique.microservices.api.core.product.Product;
import com.pacifique.microservices.api.core.product.ProductService;
import com.pacifique.microservices.api.core.recommendation.Recommendation;
import com.pacifique.microservices.api.core.recommendation.RecommendationService;
import com.pacifique.microservices.api.core.review.Review;
import com.pacifique.microservices.api.core.review.ReviewService;
import com.pacifique.microservices.api.event‎.Event;
import com.pacifique.microservices.api.exceptions.InvalidInputException;
import com.pacifique.microservices.api.exceptions.NotFoundException;
import com.pacifique.microservices.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

import static com.pacifique.microservices.api.event‎.Event.Type.CREATE;
import static com.pacifique.microservices.api.event‎.Event.Type.DELETE;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);
    private final WebClient webClient;
    private final ObjectMapper mapper;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;
    private final StreamBridge streamBridge;
    private final Scheduler publishEventScheduler;

    @Autowired
    public ProductCompositeIntegration(
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
            WebClient.Builder webClientBuilder,
            StreamBridge streamBridge,
            ObjectMapper mapper

    ) {
        this.mapper = mapper;
        this.webClient = webClientBuilder.build();
        this.streamBridge = streamBridge;
        this.publishEventScheduler = publishEventScheduler;
        this.productServiceUrl = "http://product";
        this.recommendationServiceUrl = "http://recommendation";
        this.reviewServiceUrl = "http://review";
    }


    @Override
    public Mono<Product> createProduct(Product body) {
        return Mono.fromCallable(() -> {
            sendMessage("products-out-0", new Event(body, body.getProductId(), CREATE));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        String url = productServiceUrl + "/product/" + productId;
        LOG.info("Will call getProduct API on URL {}", url);

        return webClient.get().uri(url).retrieve().bodyToMono(Product.class)
                .log(LOG.getName(), Level.FINE)
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        return Mono.fromRunnable(() -> sendMessage("products-out-0", new Event<>(null, productId, DELETE)))
                .subscribeOn(publishEventScheduler).then();
    }


    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        return Mono.fromCallable(
                () -> {
                    sendMessage("recommendations-out-0", new Event(body, body.getProductId(), CREATE));
                    return body;
                }
        ).subscribeOn(publishEventScheduler);
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        String url = recommendationServiceUrl + "/recommendation?productId=" + productId;
        LOG.debug("Will call getRecommendations API on URL {}", url);
        return webClient.get().uri(url).retrieve().bodyToFlux(Recommendation.class).log(LOG.getName(), Level.FINE).onErrorResume(error -> Flux.empty());
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        return Mono.fromRunnable(() -> sendMessage("recommendations-out-0", new Event<>(null, productId, DELETE)))
                .subscribeOn(publishEventScheduler).then();

    }

    @Override
    public Mono<Review> createReview(Review body) {
        return Mono.fromCallable(() -> {
            sendMessage("reviews-out-0", new Event<>(body, body.getReviewId(), CREATE));
            return body;
        }).subscribeOn(publishEventScheduler);

    }

    @Override
    public Flux<Review> getReviews(int productId) {
        String url = reviewServiceUrl + "/review?productId=" + productId;
        LOG.debug("Will call getReviews API on URL {}", url);
        return webClient.get().uri(url).retrieve().bodyToFlux(Review.class).log(LOG.getName(), Level.FINE).onErrorResume(error -> Flux.empty());
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        return Mono.fromRunnable(() -> sendMessage("reviews-out-0", new Event<>(null, productId, DELETE)))
                .subscribeOn(publishEventScheduler).then();
    }

    public Mono<Health> getProductHealth() {
        return getHealth(productServiceUrl);
    }

    public Mono<Health> getRecommendationHealth() {
        return getHealth(recommendationServiceUrl);
    }

    public Mono<Health> getReviewHealth() {
        return getHealth(reviewServiceUrl);
    }


    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        LOG.debug("Will call the health API on URL {}", url);
        return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log(LOG.getName(), Level.FINE);
    }


    private Throwable handleException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got an unexpected HTTP error: {},will rethrow it", ex.toString());
        }

        WebClientResponseException wcre = (WebClientResponseException) ex;
        switch (Objects.requireNonNull(HttpStatus.resolve(wcre.getStatusCode().value()))) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));

            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(wcre));

            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    private void sendMessage(String bindingName, Event event) {
        LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message<Event> message = MessageBuilder.withPayload(event).setHeader("partitionKey", event.getKey()).build();
        streamBridge.send(bindingName, message);
    }


}
