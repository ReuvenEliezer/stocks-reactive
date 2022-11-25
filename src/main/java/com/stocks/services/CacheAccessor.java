package com.stocks.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface CacheAccessor {
    Mono<String> retrieveTopic(String eventType);
    Flux<String> retrieveTopics(Set<String> eventTypes);

}
