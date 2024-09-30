package com.pacifique.microservices.core.product.services;

import com.mongodb.DuplicateKeyException;
import com.pacifique.microservices.core.product.persistance.ProductEntity;
import com.pacifique.microservices.core.product.persistance.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.pacifique.microservices.api.core.product.Product;
import com.pacifique.microservices.api.core.product.ProductService;
import com.pacifique.microservices.api.exceptions.InvalidInputException;
import com.pacifique.microservices.api.exceptions.NotFoundException;
import com.pacifique.microservices.util.http.ServiceUtil;


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
    public Product createProduct(Product body) {
        ProductEntity entity = mapper.apiToEntity(body);

        try {
            entity = repository.save(entity);
        }catch (DuplicateKeyException dke){
            throw new InvalidInputException("Duplicated key, Product Id: " + body.getProductId());
        }

        LOG.debug("createProduct: entity created for productId: {}", body.getProductId());
        return mapper.entityToApi(entity);
    }

    @Override
    public Product getProduct(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        ProductEntity entity = repository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

        Product response = mapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());

        LOG.debug("getProduct: found productId: {}", productId);

        return response;
    }

    @Override
    public void deleteProduct(int productId) {
        LOG.debug(" deleteProduct: tries to delete an entity with productId: {}", productId);
        repository.findByProductId(productId).ifPresent(repository::delete);

    }
}
