package com.stocks.controllers;

import com.stocks.entities.*;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

@Controller
public class RSocketController {

    private static final Logger logger = LoggerFactory.getLogger(RSocketController.class);
    /**
     * https://stackoverflow.com/questions/27047310/path-variables-in-spring-websockets-sendto-mapping
     * https://howtodoinjava.com/spring-boot/rsocket-tutorial/
     *
     * @return
     */

    private final WebClient webClient;
    private final int appPort;

    public RSocketController(WebClient webClient, @Value("${server.port}") int appPort) {
        this.webClient = webClient;
        this.appPort = appPort;
    }

    @MessageMapping("getTweet")
    public Mono<Tweet> getTweet(@Payload Request req) {
//        if (true){
//            throw new RuntimeException("sdjasjdhas");
//        }
        return Mono.just(new Tweet("message", "user"))
                .doOnNext(r -> logger.info("Received request: {}", req));
    }

    @MessageMapping("getTweetsNonBlocking-3")
    public Flux<Tweet> getTweetsNonBlocking(@Payload Mono<Request> req) {
        logger.info("Starting NON-BLOCKING Controller!");
        List<Integer> userIdList = IntStream.range(1, 5).boxed().toList();
        Flux<Tweet> tweetList = Flux.fromIterable(userIdList)
                .flatMap(collectData());

//        Disposable subscribe = TweetFlux.subscribe(Tweet -> logger.info(Tweet.toString()));
//        disposableList.add(subscribe);

        logger.info("done");
        return tweetList;
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
                .doOnComplete(() -> logger.info("Complete to get Tweet data for user-id {}", userId))
                .doOnNext(tweet -> logger.info("doOnNext starting to get Tweet data {} for user-id {}", tweet, userId));
    }


    @MessageMapping("greeting/{name}")
    public Mono<String> greet(@DestinationVariable("name") String name,
                              @Payload Mono<String> greetingMono) {

        return greetingMono
                .doOnNext(greeting ->
                        logger.info("Received a greeting from {} : {}", name, greeting))
                .map(greeting -> "Hello " + name + "!");
    }


    /**
     * Request-Stream
     *
     * @param symbol
     * @return
     */

    private static final Duration RETRIEVE_DATA_MAX_DURATION = Duration.ofSeconds(10);

    @MessageMapping("stock/{symbol}")
    public Flux<StockPrice> getStockPrice(@DestinationVariable("symbol") String symbol) {
        LocalDateTime start = LocalDateTime.now();
        logger.info("getStockPrice");

        return Flux
                .interval(Duration.ofSeconds(1))
                .takeWhile(time -> Duration.between(start, LocalDateTime.now()).minus(RETRIEVE_DATA_MAX_DURATION).isNegative())
                .map(i -> {
//                    logger.info("starting at {}. passed time {}", start, Duration.between(start, LocalDateTime.now()));
                    Double price = Double.valueOf(Math.random() * 10);
                    return new StockPrice(symbol, price, LocalDateTime.now());
                });
    }

    /**
     * Fire-and-Forget
     *
     * @param alertMono
     * @return
     */
    @MessageMapping("event")
    public Mono<Void> setAlert(@Payload Mono<Event> alertMono) {
        return alertMono
                .doOnNext(event ->
                        logger.info("Event Id '{}' occurred of type '{}' at '{}'",
                                event.id(),
                                event.type(),
                                event.dateTime())
                )
                .thenEmpty(Mono.empty());
    }

    @MessageMapping("check-loan-eligibility")
    public Flux<Boolean> calculate(@Payload Flux<LoanDetails> loanDetails) {
        return loanDetails
                .doOnNext(ld -> logger.info("Calculating eligibility:  {}", ld))
                .map(ld -> ld.rate() > 9);
//                 .map(ld -> {
        //            if(ld.getRate() > 9)
        //                return true;
        //            else
        //                return false;
        //        });
    }

}
