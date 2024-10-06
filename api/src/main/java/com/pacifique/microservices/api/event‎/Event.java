package com.pacifique.microservices.api.event‎;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import lombok.Getter;

import java.time.ZonedDateTime;

public class Event <K,T> {

    public enum Type {
        CREATE,
        DELETE
    }

    @Getter
    private final Type eventType;
    @Getter
    private final K key;
    @Getter
    private final T data;
    private final ZonedDateTime eventCreatedAt;

    public Event() {
        this.eventType = null;
        this.key = null;
        this.data = null;
        this.eventCreatedAt = null;
    }

    public Event(T data, K key, Type eventType) {
        this.data = data;
        this.key = key;
        this.eventType = eventType;
        this.eventCreatedAt = ZonedDateTime.now();
    }

    @JsonSerialize(using= ZonedDateTimeSerializer.class)
    public ZonedDateTime getEventCreatedAt() {
        return eventCreatedAt;
    }

}
