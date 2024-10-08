package com.pacifique.microservices.core.recommendation;

import com.pacifique.microservices.core.recommendation.persistance.RecommendationEntity;
import com.pacifique.microservices.core.recommendation.persistance.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
public class PersistenceTests  extends MongoDbTestBase{
    @Autowired
    private RecommendationRepository repository;

    private RecommendationEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();

        RecommendationEntity entity = RecommendationEntity.builder().productId(1).recommendationId(2)
                .author("a").rating(3).content("c").build();
        savedEntity = repository.save(entity).block();
        assertNotNull(savedEntity);

        assertEqualsRecommendation(entity, savedEntity);
    }


    @Test
    void create() {

        RecommendationEntity newEntity = RecommendationEntity.builder()
                .productId(1)
                .recommendationId(3)
                .author("a")
                .rating(3)
                .content("c").build();

        repository.save(newEntity).block();

        RecommendationEntity foundEntity = repository.findById(newEntity.getId()).block();
        assertNotNull(foundEntity);
        assertEqualsRecommendation(newEntity, foundEntity);

        assertEquals(2, repository.count().block());
    }

    @Test
    void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity).block();

        RecommendationEntity foundEntity = repository.findById(savedEntity.getId()).block();
        assertNotNull(foundEntity);
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
    void delete() {
        repository.delete(savedEntity).block();
        assertNotEquals(Boolean.TRUE, repository.existsById(savedEntity.getId()).block());
    }

    @Test
    void getByProductId() {
        List<RecommendationEntity> entityList = repository.findByProductId(savedEntity.getProductId()).collectList().block();

        assertThat(entityList, hasSize(1));
        assertEqualsRecommendation(savedEntity, entityList.get(0));
    }

    @Test
    void duplicateError() {
        assertThrows(DuplicateKeyException.class, () -> {
            RecommendationEntity entity = RecommendationEntity.builder().productId(1).recommendationId(2).author("a").rating(3).content("c").build();
            repository.save(entity).block();
        });
    }

    @Test
    void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        RecommendationEntity entity1 = repository.findById(savedEntity.getId()).block();
        RecommendationEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        assertNotNull(entity1);
        entity1.setAuthor("a1");
        repository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
        assertThrows(OptimisticLockingFailureException.class, () -> {
            assertNotNull(entity2);
            entity2.setAuthor("a2");
            repository.save(entity2).block();
        });

        // Get the updated entity from the database and verify its new sate
        RecommendationEntity updatedEntity = repository.findById(savedEntity.getId()).block();
        assertNotNull(updatedEntity);
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }

    private void assertEqualsRecommendation(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
        assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
        assertEquals(expectedEntity.getAuthor(),           actualEntity.getAuthor());
        assertEquals(expectedEntity.getRating(),           actualEntity.getRating());
        assertEquals(expectedEntity.getContent(),          actualEntity.getContent());
    }
}
