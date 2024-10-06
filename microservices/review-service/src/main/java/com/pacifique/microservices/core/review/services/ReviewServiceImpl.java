package com.pacifique.microservices.core.review.services;

import com.pacifique.microservices.api.core.review.Review;
import com.pacifique.microservices.api.core.review.ReviewService;
import com.pacifique.microservices.api.exceptions.InvalidInputException;
import com.pacifique.microservices.core.review.persistance.ReviewRepository;
import com.pacifique.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import static java.util.logging.Level.FINE;

@RestController
public class ReviewServiceImpl implements ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final Scheduler jdbcScheduler;

    @Autowired
    public ReviewServiceImpl(@Qualifier("jdbcScheduler") Scheduler jdbcScheduler,  ServiceUtil serviceUtil, ReviewRepository repository, ReviewMapper mapper) {
        this.jdbcScheduler = jdbcScheduler;
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Review> createReview(Review body) {
        if (body.getProductId() < 1){
            throw new InvalidInputException("Invalid productId: " +body.getProductId());
        }

        return Mono.fromCallable(()-> ReviewInternalRequestHandler.internalCreateReview(body,repository,mapper))
                .subscribeOn(jdbcScheduler);
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        if (productId <1){
            throw new InvalidInputException("Invalid productId: " +productId);
        }

        return Mono.fromCallable(()-> ReviewInternalRequestHandler.internalGetReviews(productId,repository,serviceUtil,mapper))
                .flatMapMany(Flux::fromIterable)
                .log(LOG.getName(), FINE)
                .subscribeOn(jdbcScheduler);
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        LOG.debug("deleteReviews: tries to delete reviews for the product with  productId: {}", productId);
      return Mono.fromRunnable(() -> ReviewInternalRequestHandler.internalDeleteReview(productId,repository));

    }
}
