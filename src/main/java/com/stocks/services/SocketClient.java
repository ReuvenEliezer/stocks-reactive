package com.stocks.services;

import com.stocks.entities.Request;
import com.stocks.entities.Tweet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SocketClient {

    @Autowired
    private RSocketRequester rSocketRequester;


    public Mono<Tweet> getTweet(Request request) {
        return rSocketRequester.route("getTweet")
                .data(request)
                .retrieveMono(Tweet.class);
    }

}
