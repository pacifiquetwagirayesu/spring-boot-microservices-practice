package com.pacifique.microservices.core.recommendation.services;

import com.pacifique.microservices.api.core.recommendation.Recommendation;
import com.pacifique.microservices.api.core.recommendation.RecommendationService;
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
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

    private final RecommendationService recommendationService;

    @Autowired
    public MessageProcessorConfig(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Bean
    public Consumer<Event<Integer, Recommendation>> messageProcessor() {
        return event -> {
            LOG.info("Process message created at: {}", event.getEventCreatedAt());
            switch (event.getEventType()) {
                case CREATE:
                    Recommendation recommendation = event.getData();
                    LOG.info("Created recommendation With ID: {}/{}", recommendation.getProductId(), recommendation.getRecommendationId());
                    recommendationService.createRecommendation(recommendation).block();
                    break;
                case DELETE:
                    Integer productId = event.getKey();
                    LOG.info("Deleted recommendation with ID: {}", productId);
                    recommendationService.deleteRecommendations(productId).block();
                    break;
                default:
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);

            }
            LOG.info("Message processing done!");

        };

    }
}
