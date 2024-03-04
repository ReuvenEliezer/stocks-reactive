package com.stocks.services;

import com.stocks.entities.Request;
import com.stocks.entities.Tweet;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class SocketClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final RSocketRequester rSocketRequester;

    public SocketClient(RSocketRequester rSocketRequester) {
        this.rSocketRequester = rSocketRequester;
    }


    public Mono<Tweet> getTweet(Request request) {
        return rSocketRequester.route("getTweet")
                .data(request)
                .retrieveMono(Tweet.class);
    }

    public Flux<Tweet> getTweetsNonBlocking(Request request) {
        return rSocketRequester.route("getTweetsNonBlocking-1")
                .data(request)
                .retrieveFlux(Tweet.class);
    }

    @PreDestroy
    void destroy() {
        rSocketRequester.dispose();
        logger.info("Destroying rSocketRequester");
    }

}
