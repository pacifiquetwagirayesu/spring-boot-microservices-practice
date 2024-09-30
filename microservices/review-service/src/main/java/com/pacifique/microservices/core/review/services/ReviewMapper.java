package com.pacifique.microservices.core.review.services;

import com.pacifique.microservices.api.core.review.Review;
import com.pacifique.microservices.core.review.persistance.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mappings({
            @Mapping(target = "serviceAddress",ignore = true)
    })
    Review entityToApi(ReviewEntity entity);

    @Mappings({
            @Mapping(target = "id",ignore = true),
            @Mapping(target = "version",ignore = true)
    })
    ReviewEntity apiToEntity(Review api);
    List<Review> entityListToApiList(List<ReviewEntity> entities);
    List<ReviewEntity> apiListToEntityList(List<Review> apiList);
}
