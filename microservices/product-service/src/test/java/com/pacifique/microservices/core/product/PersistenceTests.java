package com.pacifique.microservices.core.product;

import com.pacifique.microservices.core.product.persistance.ProductEntity;
import com.pacifique.microservices.core.product.persistance.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;


import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
public class PersistenceTests extends MongoDbTestBase {

    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;

    @BeforeEach
    void setupDb() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();

        ProductEntity entity = ProductEntity.builder().productId(1).name("n").weight(1).build();
        StepVerifier.create(repository.save(entity))
                .expectNextMatches(createdEntity -> {
                    savedEntity = createdEntity;
                    return areProductEqual(entity, createdEntity);
                }).verifyComplete();

    }


    @Test
    void create() {

        ProductEntity newEntity =  ProductEntity.builder().productId(2).name( "n").weight(2).build();

        StepVerifier.create(repository.save(newEntity))
                .expectNextMatches(createdEntity -> newEntity.getProductId() == createdEntity.getProductId())
                .verifyComplete();

        StepVerifier.create(repository.findById(newEntity.getId()))
                .expectNextMatches(foundEntity -> areProductEqual(newEntity, foundEntity))
                .verifyComplete();

        StepVerifier.create(repository.count()).expectNext(2L).verifyComplete();

    }

    @Test
    void update() {
        savedEntity.setName("n2");
        StepVerifier.create(repository.save(savedEntity))
                .expectNextMatches(updateEntity -> Objects.equals(updateEntity.getName(), "n2")).verifyComplete();

        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity -> foundEntity.getVersion() == 1 && foundEntity.getName().equals("n2")).verifyComplete();
    }

    @Test
    void delete() {
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
        StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
    }

    @Test
    void getByProductId() {
        StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
                .expectNextMatches(foundEntity -> areProductEqual(savedEntity, foundEntity))
                .verifyComplete();
    }

    @Test
    void duplicateError() {
        ProductEntity entity = ProductEntity.builder().productId(savedEntity.getProductId()).name("n").weight(1).build();
        StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
    }

    @Test
    void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        ProductEntity entity1 = repository.findById(savedEntity.getId()).block();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        assertNotNull(entity1);
        entity1.setName("n1");
        repository.save(entity1).block();

        // Update the entity using the second entity object.
        // This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
        assertNotNull(entity2);
        StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();
//        assertThrows(OptimisticLockingFailureException.class, () -> {
//            entity2.setName("n2");
//            repository.save(entity2);
//        });

        // Get the updated entity from the database and verify its new sate
        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity -> foundEntity.getVersion() == 1 && foundEntity.getName().equals("n1")).verifyComplete();
//        ProductEntity updatedEntity = repository.findById(savedEntity.getId()).block();
//        assertEquals(1, (int)updatedEntity.getVersion());
//        assertEquals("n1", updatedEntity.getName());
    }


    private boolean areProductEqual(ProductEntity expectedEntity, ProductEntity actualEntity) {
        return (Objects.equals(expectedEntity.getId(), actualEntity.getId()))
                && (Objects.equals(expectedEntity.getVersion(), actualEntity.getVersion()))
                && (Objects.equals(expectedEntity.getProductId(), actualEntity.getProductId()))
                && (Objects.equals(expectedEntity.getName(), actualEntity.getName()))
                && (Objects.equals(expectedEntity.getWeight(), actualEntity.getWeight()));
    }
}
