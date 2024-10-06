package com.pacifique.microservices.core.review.services;

import com.pacifique.microservices.api.core.review.Review;
import com.pacifique.microservices.api.core.review.ReviewService;
import com.pacifique.microservices.api.event‎.Event;
import com.pacifique.microservices.api.exceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {
    private final static Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);
    private final ReviewService reviewService;

    @Autowired
    public MessageProcessorConfig(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Bean
    public Consumer<Event<Integer, Review>> messageProcessor() {
        return event -> {
            LOG.info("Process message created at {}...", event.getEventCreatedAt());
            switch (event.getEventType()) {
                case CREATE:
                    Review review = event.getData();
                    LOG.info("Create review with ID: {}/{}", review.getProductId(), review.getReviewId());
                    reviewService.createReview(review).block();
                    break;
                case DELETE:
                    int productId = event.getKey();
                    LOG.info("Delete productID: {}", event.getKey());
                    reviewService.deleteReviews(productId).block();
                    break;
                default:
                    String errorMessage = "Incorrect event type: " + event.getEventType();
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
            }
            LOG.info("Message processing done!");
        };
    }

}
