package com.stock.services;

import com.stocks.StockApp;
import com.stocks.controllers.RSocketController;
import com.stocks.entities.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@SpringBootTest( classes = StockApp.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RSocketTest {

    private static final Logger logger = LoggerFactory.getLogger(RSocketTest.class);


    @Autowired
    private RSocketRequester requester;

//    private static RSocketRequester requester;

//    @BeforeAll
//    public static void setupOnce(@Autowired RSocketRequester.Builder builder,
//                                 @Value("${spring.rsocket.server.port}") Integer port) {
//        requester = builder
//                .connectTcp("localhost", port)
//                .block();
//    }

//    @AfterAll
//    void tearDownOnce() {
//        Objects.requireNonNull(requester.rsocket()).dispose();
//    }

    @Test
    void testRequestGetsResponse() {
        Flux<Tweet> result = requester
                .route("getTweetsNonBlocking-3")
                .data(new Request("key", "value"))
                .retrieveFlux(Tweet.class);

        List<Tweet> tweets = result.collectList().block();

        Assertions.assertThat(tweets).isNotNull();
        Assertions.assertThat(tweets.size()).isEqualTo(12);
    }

    @Test
    void greetingTest() {
        requester
                .route("greeting/{name}", "Lokesh")
                .data("Hello there!")
                .retrieveMono(String.class)
                .subscribe(response -> logger.info("RSocket response : {}", response));
    }


    @Test
    void stockTest() {
        String stockSymbol = "TSLA";
        Flux<StockPrice> stockPriceFlux = requester
                .route("stock/{symbol}", stockSymbol)
                .retrieveFlux(StockPrice.class)
                .doOnNext(sb -> logger.info("Price of {} : {} (at {})", sb.symbol(), sb.price(), sb.dateTime()));

        Assertions.assertThat(stockPriceFlux.count().block()).isEqualTo(9);
    }

    @Test
    void eventTest() {
        requester
                .route("event")
                .data(new Event(11, "ERROR", LocalDateTime.now()))
                .send()
                .subscribe();
    }

    @Test
    void loanDetailsTest() {
        Flux<LoanDetails> lonaDetailsFlux = Flux.fromArray(new LoanDetails[]{
                        new LoanDetails(10, 5, Duration.ofDays(365)),
                        new LoanDetails(20, 7, Duration.ofDays(30)),
                        new LoanDetails(30, 10, Duration.ofDays(65)),
                        new LoanDetails(40, 8, Duration.ofDays(35)),
                        new LoanDetails(60, 11, Duration.ofDays(150))
                })
                .delayElements(Duration.ofSeconds(2));

        requester
                .route("check-loan-eligibility")
                .data(lonaDetailsFlux)
                .retrieveFlux(Boolean.class)
                .blockLast();
//    subscribe(result -> logger.info("Loan eligibility : {}", result));
    }


}
