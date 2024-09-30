package com.pacifique.microservices.core.product.persistance;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends PagingAndSortingRepository<ProductEntity,String>, CrudRepository<ProductEntity, String> {
    Optional<ProductEntity> findByProductId(int productId);
}
