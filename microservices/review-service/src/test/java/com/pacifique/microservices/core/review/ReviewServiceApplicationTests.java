package com.pacifique.microservices.core.review;

import com.pacifique.microservices.api.core.review.Review;
import com.pacifique.microservices.core.review.persistance.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReviewServiceApplicationTests extends MySqlTestBase {

    @Autowired
    private WebTestClient client;
    @Autowired
    private ReviewRepository repository;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
    }

    @Test
    void getReviewByProductId() {
        int productId = 1;
        postAndVerifyReview(productId,1,OK);
        postAndVerifyReview(productId,2,OK);
        postAndVerifyReview(productId,3,OK);
        assertEquals(3,repository.findByProductId(productId).size());

        getAndVerifyReviewByProductId(productId,OK)
                .jsonPath("$.length()").isEqualTo(3)
                        .jsonPath("$[2].productId").isEqualTo(productId)
                        .jsonPath("$[2].reviewId").isEqualTo(3);

        client.get()
                .uri("/review?productId=" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].productId").isEqualTo(productId)
                .jsonPath("$.length()").isEqualTo(3);

    }

    @Test
    void duplicateError() {
        int productId = 1;
        int reviewId = 1;
        assertEquals(0,repository.count());

        postAndVerifyReview(productId,reviewId,OK)
                .jsonPath("$.productId").isEqualTo(productId)
                .jsonPath("$.reviewId").isEqualTo(reviewId);

        assertEquals(1,repository.count());
        postAndVerifyReview(productId,reviewId,INTERNAL_SERVER_ERROR)
                .jsonPath("$.path").isEqualTo("/review");
//                .jsonPath("$.message").isEqualTo("Duplicate product, Product Id: 1, Review Id: 1");

    }


    @Test
    void deleteReviews() {
        int productId = 1;
        int reviewId = 1;

        postAndVerifyReview(productId, reviewId, OK);
        assertEquals(1, repository.findByProductId(productId).size());

        deleteAndVerifyReviewByProductId(productId, OK);
        assertEquals(0, repository.findByProductId(productId).size());

        deleteAndVerifyReviewByProductId(productId, OK);
    }

    @Test
    void getReviewsMissingParameter() {
        getAndVerifyReviewsByProductId("", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
    }

    @Test
    void getReviewsInvalidParameter() {
        getAndVerifyReviewsByProductId("?productId=no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    void getReviewsNotFound() {
        getAndVerifyReviewsByProductId("?productId=213", OK)
                .jsonPath("$.length()").isEqualTo(0);

    }


    @Test
    void getReviewInvalidParameterNegativeValue() {
        int productIdInvalid = -1;
        getAndVerifyReviewsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/review")
                .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
    }


    private WebTestClient.BodyContentSpec getAndVerifyReviewByProductId(int productId, HttpStatus expectedStatus) {
        return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus);
    }


    private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productQuery, HttpStatus expectedStatus) {
        return client.get().uri("/review" + productQuery)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyReview(int productId, int reviewId, HttpStatus expectedStatus) {
        Review review = Review.builder().reviewId(reviewId).productId(productId).author("Author " + reviewId)
                .subject("Subject " + reviewId).content("Content " + reviewId).serviceAddress("SA").build();
        return client.post().uri("/review").body(just(review), Review.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();

    }


    private WebTestClient.BodyContentSpec deleteAndVerifyReviewByProductId(int productId, HttpStatus expectedStatus) {
        return client.delete()
                .uri("/review?productId=" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }

}
