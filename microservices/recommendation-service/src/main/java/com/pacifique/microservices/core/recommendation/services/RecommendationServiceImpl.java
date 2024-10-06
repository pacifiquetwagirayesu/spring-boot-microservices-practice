package com.pacifique.microservices.core.recommendation.services;

import com.pacifique.microservices.api.core.recommendation.Recommendation;
import com.pacifique.microservices.api.core.recommendation.RecommendationService;
import com.pacifique.microservices.api.exceptions.InvalidInputException;
import com.pacifique.microservices.core.recommendation.persistance.RecommendationEntity;
import com.pacifique.microservices.core.recommendation.persistance.RecommendationRepository;
import com.pacifique.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.logging.Level.FINE;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;

    @Autowired
    public RecommendationServiceImpl(ServiceUtil serviceUtil, RecommendationRepository repository, RecommendationMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }

        RecommendationEntity entity = mapper.apiToEntity(body);

        return repository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap(DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id: " +
                                body.getRecommendationId()))
                .map(mapper::entityToApi);
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.info("Will get recommendation for product with id: {}", productId);
        return repository.findByProductId(productId)
                .log(LOG.getName(), FINE)
                .map(mapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.debug("deleteRecommendations: tries to delete recommendation for product with productId: {}", productId);
        return repository.deleteAll(repository.findByProductId(productId));
    }

    private Recommendation setServiceAddress(Recommendation e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}
