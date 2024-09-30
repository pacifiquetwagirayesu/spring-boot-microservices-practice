package com.pacifique.microservices.core.recommendation.persistance;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends CrudRepository<RecommendationEntity,String> {
    List<RecommendationEntity> findByProductId(int productId);
}
