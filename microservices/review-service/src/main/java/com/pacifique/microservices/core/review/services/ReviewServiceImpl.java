package com.pacifique.microservices.core.review.services;

import com.pacifique.microservices.api.core.review.Review;
import com.pacifique.microservices.api.core.review.ReviewService;
import com.pacifique.microservices.api.exceptions.InvalidInputException;
import com.pacifique.microservices.core.review.persistance.ReviewEntity;
import com.pacifique.microservices.core.review.persistance.ReviewRepository;
import com.pacifique.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final ReviewRepository repository;
    private final ReviewMapper mapper;

    @Autowired
    public ReviewServiceImpl(ServiceUtil serviceUtil, ReviewRepository repository, ReviewMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Review createReview(Review body) {
        ReviewEntity entity = mapper.apiToEntity(body);

        try {
            entity = repository.save(entity);
        }catch (DuplicateKeyException dke){
            throw new InvalidInputException("Duplicate key, Product Id: "+body.getProductId()+", Review Id: "+body.getReviewId());
        }

        LOG.debug("createReview: created a review entity: {}/{} ", body.getProductId(), body.getReviewId());

        return mapper.entityToApi(entity);
    }

    @Override
    public List<Review> getReviews(int productId) {
        if (productId <1){
            throw new InvalidInputException("Invalid productId: " +productId);
        }

        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(e->e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getReviews: response size: {}", list.size());
        return list;
    }

    @Override
    public void deleteReviews(int productId) {
        LOG.debug("deleteReviews: tries to delete reviews for the product with  productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));

    }
}
