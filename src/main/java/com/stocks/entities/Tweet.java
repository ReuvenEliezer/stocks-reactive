package com.stocks.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor//for serialization
@Getter
@ToString
public class Tweet implements Serializable {
    private String message;
    private String user;
}
