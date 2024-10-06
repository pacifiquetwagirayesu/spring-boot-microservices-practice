package com.pacifique.microservices.composite.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pacifique.microservices.api.core.product.Product;
import com.pacifique.microservices.api.event‎.Event;
import org.junit.jupiter.api.Test;

import static com.pacifique.microservices.api.event‎.Event.Type.CREATE;
import static com.pacifique.microservices.api.event‎.Event.Type.DELETE;
import static com.pacifique.microservices.composite.product.IsSameEvent.sameEventExceptCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;

public class IsSameEventTests {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    void testEventObjectCompare() throws JsonProcessingException {
        // Event #1 and #2 are the same event, but occurs as different times
        // Event #3 and #4 are different events

        Event<Integer, Product> event1 = new Event<>(new Product(1,"name",1,null),1, CREATE);
        Event<Integer, Product> event2 = new Event<>(new Product(1,"name",1,null),1, CREATE);
        Event<Integer, Product> event3 = new Event<>(null,1, DELETE);
        Event<Integer, Product> event4 = new Event<>(new Product(2,"name",1,null),1, CREATE);

        String event1Json = mapper.writeValueAsString(event1);

        assertThat(event1Json, is(sameEventExceptCreatedAt(event2)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event3)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event4)));
    }
}
