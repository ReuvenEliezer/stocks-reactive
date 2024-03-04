package com.stocks.controllers;

import com.stocks.entities.Request;
import com.stocks.entities.Tweet;
import com.stocks.services.SocketClient;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/")
public class RoutingController {

    private static final Logger logger = LoggerFactory.getLogger(RoutingController.class);
    private static final int MAX_RETRY = 3;
    private final WebClient webClient;
    private final RestClient restClient;
    private final SocketClient socketClient;


    private final RSocketRequester rSocketRequester;
    private final int appPort;

    public RoutingController(WebClient webClient,
                             RestClient restClient,
                             SocketClient socketClient,
                             RSocketRequester rSocketRequester,
                             @Value("${server.port}") int appPort) {
        this.webClient = webClient;
        this.restClient = restClient;
        this.socketClient = socketClient;
        this.rSocketRequester = rSocketRequester;
        this.appPort = appPort;
    }

//
//    @GetMapping(value = "getTweetsNonBlocking")
//    public List<Tweet> getTweetsNonBlocking() {
//        logger.info("Starting NON-BLOCKING Controller!");
//        List<Integer> userIdList = IntStream.range(1, 5).boxed().toList();
//        List<Tweet> tweetList = Flux.fromIterable(userIdList)
//                .flatMap(collectData()
//                        , 100, 1)
//                .collectList()
//                .block();
//
//
////        Disposable subscribe = TweetFlux.subscribe(Tweet -> logger.info(Tweet.toString()));
////        disposableList.add(subscribe);
//
//        logger.info("Exiting NON-BLOCKING Controller!");
//        return tweetList;
//    }

    @GetMapping("/tweets-blocking")
    public List<Tweet> getTweetsBlocking() {
        logger.info("Starting BLOCKING Controller!");
        List<Tweet> tweets = restClient.get()
                .uri("http://localhost:" + appPort + "/slow-service-Tweets/1")
                .retrieve()
                .body(List.class);
//        tweets.forEach(tweet -> logger.info(tweet.toString()));
        logger.info("Exiting BLOCKING Controller!");
        return tweets;
    }

    @GetMapping(value = "/tweets-non-blocking", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> getTweetsNonBlocking() {
        logger.info("Starting NON-BLOCKING Controller!");
        Flux<Tweet> tweetFlux = WebClient.create()
                .get()
                .uri("http://localhost:" + appPort + "/slow-service-Tweets/1")
                .retrieve()
                .bodyToFlux(Tweet.class);

        tweetFlux.subscribe(tweet -> logger.info(tweet.toString()));
        logger.info("Exiting NON-BLOCKING Controller!");
        return tweetFlux;
    }

    private Function<Integer, Publisher<? extends Tweet>> collectData() {
        return userId -> webClient
                .get()
                .uri("http://localhost:" + appPort + "/slow-service-Tweets/" + userId)
                .retrieve()
                .bodyToFlux(Tweet.class)
//                                .retry(MAX_RETRY)
//                                .doOnError(e -> logger.error("failed to get data for user-id {}", userIdList.get(i)))
                .onErrorResume(e -> {
                    logger.error("failed to get data for user-id {} ERROR {}", userId, e);
                    return Flux.empty();
                })
                .doOnNext(tweet -> logger.info("doOnNext starting to get Tweet data {} for user-id {}", tweet, userId))
                .doOnComplete(() -> logger.info("Complete to get Tweet data for user-id {}", userId));
    }

    @GetMapping(value = "getTweetsNonBlocking-by-rsocket")
    public Flux<Tweet> getTweetsNonBlockingRSocket() {
        return rSocketRequester
                .route("getTweetsNonBlocking-3")
                .data(new Request("key", "value"))
                .retrieveFlux(Tweet.class);
    }

    @GetMapping(value = "getTweetsNonBlocking-0")
    public Flux<Tweet> getTweetsNonBlocking0() {
        logger.info("Starting NON-BLOCKING Controller!");

        return Flux.fromIterable(IntStream.range(1, 5).boxed().toList())
                .flatMap(collectData()
                        , 100, 1)
                .doOnNext(tweetList -> logger.info("Exiting NON-BLOCKING Controller!"));
    }

    @GetMapping(value = "getTweetsNonBlocking-1")
    public Flux<Tweet> getTweetsNonBlocking1() {
        logger.info("Starting NON-BLOCKING Controller!");
        List<Integer> userIdList = IntStream.range(1, 5).boxed().toList();
        Flux<Tweet> tweetList = Flux.fromIterable(userIdList)
                .flatMap(collectData());

//        Disposable subscribe = TweetFlux.subscribe(Tweet -> logger.info(Tweet.toString()));
//        disposableList.add(subscribe);

        logger.info("done");
        return tweetList;
    }

//        int count = 0;
//        for (Disposable subscribe : disposableList) {
//            while (!subscribe.isDisposed() && count < 100) {
//                Thread.sleep(400);
//                count++;
//                System.out.println("Waiting......");
//            }
//        }

    @GetMapping("slow-service-Tweets/{userId}")
    private List<Tweet> getAllTweets(@PathVariable int userId) throws Exception {
        logger.info("getAllTweets of user {}", userId);
//        if (userId == 1) {
//            throw new RuntimeException("failed for user id: " + userId);
//        }
        Thread.sleep(5000L); // delay
        return List.of(
                new Tweet("RestTemplate rules", userId + "@gmail.com"),
                new Tweet("WebClient is better", userId + "@gmail.com"),
                new Tweet("OK, both are useful", userId + "@gmail.com")
        );
    }

    /**
     * https://stackoverflow.com/questions/27047310/path-variables-in-spring-websockets-sendto-mapping
     *
     * @return
     */
    @GetMapping("get-tweet")
    public Mono<Tweet> getTweet() {
        Mono<Tweet> tweetResponse = socketClient.getTweet(new Request("key", "value"));
        return tweetResponse;
    }

    @GetMapping("print-params")
    public void printParams(@RequestParam(value = "ids") final String[] ids) {
        logger.info("accountIds {}", Arrays.toString(ids));
    }

    @PostMapping("print-params1")
    public void printParams1(@RequestParam(value = "ids") final String[] ids) {
        logger.info("accountIds {}", Arrays.toString(ids));
    }

    @PostMapping("print-params2")
    public void printParams2(@RequestBody List<String> ids) {
        logger.info("accountIds {}", Arrays.toString(ids.toArray()));
    }

    @PostMapping("print-params3")
    public void printParams3(@RequestBody String[] ids) {
        logger.info("accountIds {}", ids);
    }

}
