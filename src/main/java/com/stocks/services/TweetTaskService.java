package com.stocks.services;

import com.stocks.entities.Tweet;

import java.util.List;

public interface TweetTaskService {
    List<Tweet> getAllTweet(List<String> userIdsList);
}
