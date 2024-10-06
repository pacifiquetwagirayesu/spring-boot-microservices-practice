package com.pacifique.microservices.core.review.services;

import com.pacifique.microservices.api.core.review.Review;
import com.pacifique.microservices.api.exceptions.InvalidInputException;
import com.pacifique.microservices.core.review.persistance.ReviewEntity;
import com.pacifique.microservices.core.review.persistance.ReviewRepository;
import com.pacifique.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

public class ReviewInternalRequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewInternalRequestHandler.class);

    protected static Review internalCreateReview(Review body,ReviewRepository repository,ReviewMapper mapper) {
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newReview = repository.save(entity);
            LOG.debug("createReview: created a review entity: {}/{}",body.getProductId(),body.getReviewId());
            return mapper.entityToApi(newReview);

        }catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Review Id:" + body.getReviewId());
        }
    }

    protected static List<Review> internalGetReviews(int productId, ReviewRepository repository, ServiceUtil serviceUtil,ReviewMapper mapper) {
        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(e->e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("Response size: {}", list.size());
        return list;

    }

    protected static void internalDeleteReview(int productId, ReviewRepository repository) {
        LOG.debug("deleteReview: tries to delete reviews for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
