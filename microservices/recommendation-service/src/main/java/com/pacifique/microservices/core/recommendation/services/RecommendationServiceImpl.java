package com.pacifique.microservices.core.recommendation.services;

import com.mongodb.DuplicateKeyException;
import com.pacifique.microservices.api.core.recommendation.Recommendation;
import com.pacifique.microservices.api.core.recommendation.RecommendationService;
import com.pacifique.microservices.api.exceptions.InvalidInputException;
import com.pacifique.microservices.core.recommendation.persistance.RecommendationEntity;
import com.pacifique.microservices.core.recommendation.persistance.RecommendationRepository;
import com.pacifique.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

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
    public Recommendation createRecommendation(Recommendation body) {
        RecommendationEntity entity = mapper.apiToEntity(body);

        try {
            entity = repository.save(entity);
        }catch (DuplicateKeyException dke){
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() +", Recommendation Id: " + body.getRecommendationId());
        }

        LOG.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());

        return mapper.entityToApi(entity);
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        if (productId < 1) {
            throw new  InvalidInputException("Invalid productId: "+productId);
        }

        List<RecommendationEntity> entityList = repository.findByProductId(productId);
        List<Recommendation> list = mapper.entityListToApiList(entityList);
        list.forEach(e->e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getRecommendation: response size {}", list.size());

        return list;
    }

    @Override
    public void deleteRecommendations(int productId) {
    LOG.debug("deleteRecommendations: tries to delete recommendation for product with productId: {}", productId);
    repository.deleteAll(repository.findByProductId(productId));
    }
}
