package com.stocks.controllers;

import com.stocks.entities.Request;
import com.stocks.entities.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class RSocketController {

    private static final Logger logger = LoggerFactory.getLogger(RSocketController.class);
    /**
     * https://stackoverflow.com/questions/27047310/path-variables-in-spring-websockets-sendto-mapping
     *
     * @return
     */

    @MessageMapping("getTweet")
    public Mono<Tweet> getTweet(Request req) {
//        if (1<2){
//            throw new RuntimeException("sdjasjdhas");
//        }
        return Mono.just(new Tweet("message", "user"))
                .doOnNext(r -> logger.info("Received request: {}", req));
    }

}
