package com.stocks.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@Component
public class CacheAccessorImpl implements CacheAccessor {

    private static final Logger logger = LoggerFactory.getLogger(CacheAccessorImpl.class);
    private static final String COMPONENT = "ingestion";
    private static final int MAX_RETRY = 3;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String curatorKafkaUrl;
    private final String allEventTypesUrl;

    private static final Duration RETRIEVE_KAFKA_TOPICS_EXPIRED_DURATION = Duration.ofMinutes(5);
    private static final Cache<Set<String>, Flux<String>> eventTypesToTopicsMap = Caffeine.newBuilder()
            .expireAfterAccess(RETRIEVE_KAFKA_TOPICS_EXPIRED_DURATION)
            .build();

    private static final Cache<String, Mono<String>> eventTypeToTopicMap = Caffeine.newBuilder()
            .expireAfterAccess(RETRIEVE_KAFKA_TOPICS_EXPIRED_DURATION)
            .build();

    @Autowired
    public CacheAccessorImpl(final WebClient webClient, final ObjectMapper objectMapper, @Value("${config.curatorEndpoint}") final String curatorEndpoint) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.curatorKafkaUrl = String.format("%s/kafka/topics/component/%s", curatorEndpoint, COMPONENT);
        this.allEventTypesUrl = String.format("%s/all_event_types", curatorKafkaUrl);
    }

    @Override
    public Flux<String> retrieveTopics(@NotNull final Set<String> eventTypes) {
        return eventTypesToTopicsMap.get(eventTypes, v -> {
            String url = buildUrl(eventTypes);
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retry(MAX_RETRY)
                    .map(t -> extractTopics(eventTypes, t))
                    .doOnError(e -> {
                        String message = String.format("failed to retrieveTopics on url %s", url);
                        logger.error(message);
                        throw new RuntimeException(e);
                    })
                    .doOnRequest(t -> logger.info("doOnRequest to retrieveTopics {}", url))
                    .cache(e -> RETRIEVE_KAFKA_TOPICS_EXPIRED_DURATION, // when value return cache connection forever
                            e -> Duration.ZERO, // on error don't cache
                            () -> Duration.ZERO)
                    .flatMapMany(Flux::fromIterable);
        });
    }

    @Override
    public Mono<String> retrieveTopic(@NotNull final String eventType) {
        return eventTypeToTopicMap.get(eventType, v -> {
            String url = buildUrl(Set.of(eventType));
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retry(MAX_RETRY)
                    .map(t -> extractTopics(Set.of(eventType), t).get(0))
                    .doOnError(e -> {
                        String message = String.format("failed to retrieve topics on url %s", url);
                        logger.error(message);
                        throw new RuntimeException(message);
                    })
                    .doOnRequest(e -> logger.info("doOnRequest to retrieveTopics {}", url))
                    .cache(e -> RETRIEVE_KAFKA_TOPICS_EXPIRED_DURATION, // when value return cache connection forever
                            e -> Duration.ZERO, // on error don't cache
                            () -> Duration.ZERO);
        });
    }


    private List<String> extractTopics(Set<String> eventType, String topics) {
        List<String> topicList;
        try {
            topicList = objectMapper.readValue(topics.getBytes(StandardCharsets.UTF_8), List.class);
        } catch (IOException ex) {
            String message = String.format("failed to read topics %s", topics);
            logger.error(message);
            throw new RuntimeException(message, ex);
        }
        if (topicList.isEmpty()) {
            String message = String.format("eventType '%s' does not exists in db", eventType);
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
        return topicList;
    }

    private String buildUrl(Set<String> eventTypes) {
        if (CollectionUtils.isEmpty(eventTypes)) {
            logger.warn("List of eventTypes is empty, going to subscribe to all the topics in the component {}", COMPONENT);
            return allEventTypesUrl;
        } else {
            return UriComponentsBuilder.fromHttpUrl(curatorKafkaUrl).queryParam("event_types", eventTypes).toUriString();
        }
    }

}
