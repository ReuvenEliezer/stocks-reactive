package com.stocks.services;

import com.stocks.entities.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class TweetTaskServiceImpl implements TweetTaskService {

    private static final Logger logger = LoggerFactory.getLogger(TweetTaskServiceImpl.class);
    private static final String UPDATE_SERVICE_DEFAULTS_URL = "http://localhost:8085/slow-service-Tweets/";
    private Map<String, AtomicInteger> serviceIdToRetryTimesMap = new ConcurrentHashMap<>();
    private DecimalFormat progressFormat = new DecimalFormat("#.#####");
    private ScheduledExecutorService executorService;
    @Value("${scheduler.thread.pool.size}")
    private Integer schedulerPoolSize;
    @Autowired
    private RestTemplate restTemplate;


    @PostConstruct()
    void init() {
        executorService = Executors.newScheduledThreadPool(schedulerPoolSize);
    }

    @Override
    public List<Tweet> getAllTweet(List<String> userIdsList) {
        List<String> allServiceIdToUpdate = userIdsList;
        final List<CompletableFuture<List<Tweet>>> futures = executeTasks(allServiceIdToUpdate);
        List<Tweet> tweets = waitUntilFinishedAllTasksExecutions(futures);
        logger.info("done to get all tweets {}", Arrays.toString(tweets.toArray()));
        return tweets;
    }

    private List<CompletableFuture<List<Tweet>>> executeTasks(List<String> allUserIdToUpdate) {
        AtomicInteger updaterCounter = new AtomicInteger();
        return allUserIdToUpdate.stream().map(userId -> CompletableFuture
                        .supplyAsync(() -> updateServiceDefaults(userId), executorService)
                        .handle((res, ex) -> {
                            printProgress(updaterCounter, allUserIdToUpdate.size());
                            if (ex != null) {
                                return doOnError(userId, ex.getMessage());
                            }
                            return res;
                        }))
                .collect(Collectors.toList());
    }

    private void printProgress(AtomicInteger updaterCounter, int totalServicesToUpdate) {
        String progressPercentage = progressFormat.format(((double) updaterCounter.incrementAndGet() / totalServicesToUpdate) * 100);
        logger.info("progressPercentage: {}%", progressPercentage);
    }

    private List<Tweet> doOnError(final String serviceId, String errorMessage) {
        logger.error("failed to update service id {}. exception: {}", serviceId, errorMessage);
        return new ArrayList<>();
    }


    private List<Tweet> waitUntilFinishedAllTasksExecutions(final List<CompletableFuture<List<Tweet>>> futureResultList) {
        List<Tweet> allTweet = new ArrayList<>();
        for (CompletableFuture<List<Tweet>> future : futureResultList) {
            try {
                final List<Tweet> tweets = future.get();
                logger.info("done to update tweets {}", Arrays.toString(tweets.toArray()));
                allTweet.addAll(tweets);
            } catch (Exception e) {
                logger.error("failed duration updating ", e);
            }
        }
        return allTweet;
    }

    private List<Tweet> updateServiceDefaults(final String serviceId) {
        LocalDateTime start = LocalDateTime.now();
        List<Tweet> tweets = restTemplate.getForObject(UPDATE_SERVICE_DEFAULTS_URL + serviceId, List.class);
        logger.info("serviceId '{}' update successfully. total time {}", serviceId, Duration.between(start, LocalDateTime.now()));
        return tweets;
    }

    @PreDestroy
    private void destroy() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

}
