package com.stocks.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
//@JsonSubTypes(
//        JsonSubTypes.Type(value = Response.Accepted::class, name = "Accepted"),
//        JsonSubTypes.Type(value = Response.Rejected::class, name = "Rejected")
//        )
@Data
@AllArgsConstructor
//@NoArgsConstructor //for serializer
public class Response {

    @Data
    @AllArgsConstructor
//    @NoArgsConstructor //for serializer
    public static class Accepted{
        String reqId;
    }

    @Data
    @AllArgsConstructor
//    @NoArgsConstructor //for serializer
    public static class Rejected{
        String message;
    }

}
