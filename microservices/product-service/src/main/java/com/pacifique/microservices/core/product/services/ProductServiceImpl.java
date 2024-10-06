package com.pacifique.microservices.core.product.services;

import com.pacifique.microservices.core.product.persistance.ProductEntity;
import com.pacifique.microservices.core.product.persistance.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import com.pacifique.microservices.api.core.product.Product;
import com.pacifique.microservices.api.core.product.ProductService;
import com.pacifique.microservices.api.exceptions.InvalidInputException;
import com.pacifique.microservices.api.exceptions.NotFoundException;
import com.pacifique.microservices.util.http.ServiceUtil;
import reactor.core.publisher.Mono;

import static java.util.logging.Level.FINE;


@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil, ProductRepository repository, ProductMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        if (body.getProductId() < 1){
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }
        ProductEntity entity = mapper.apiToEntity(body);

        return repository.save(entity)
                .log(LOG.getName(), FINE)
                        .onErrorMap(DuplicateKeyException.class,
                                ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()))
                                .map(mapper::entityToApi);

    }

    @Override
    public Mono<Product> getProduct(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        LOG.info("Will get product info for id={}", productId);

        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: "+productId)))
                .log(LOG.getName(),FINE)
                .map(mapper::entityToApi)
                .map(this::setServiceAddress);

    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }
        LOG.debug(" deleteProduct: tries to delete an entity with productId: {}", productId);
        return repository.findByProductId(productId).log(LOG.getName(),FINE).map(repository::delete).flatMap(e->e);

    }

    private Product setServiceAddress(Product e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}
