package com.stocks.config;

import com.stocks.entities.Status;
import com.stocks.utils.Constants;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
public class WebRestConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    @Bean
    public WebClient webClient() {
        final int size = 16 * 1024 * 1024;
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
                .build();
        return WebClient.builder()
//                .defaultHeaders()
                .exchangeStrategies(strategies)
                .build();
    }


    @Bean
    public RSocketStrategies rSocketStrategies() {
        return RSocketStrategies.builder()
                .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
                .decoders(decoders -> decoders.add(new Jackson2CborDecoder()))
                .metadataExtractorRegistry(metadataExtractorRegistry -> {
//                    metadataExtractorRegistry.metadataToExtract(MimeType.valueOf(Status.CHUNK_COMPLETED.name()), String.class, Status.CHUNK_COMPLETED.name());
//                    metadataExtractorRegistry.metadataToExtract(MimeType.valueOf(Status.COMPLETED.name()), String.class, Status.COMPLETED.name());
//                    metadataExtractorRegistry.metadataToExtract(MimeType.valueOf(Status.FAILED.name()), String.class, Status.FAILED.name());
                    metadataExtractorRegistry.metadataToExtract(MimeType.valueOf(Constants.MIME_FILE_EXTENSION), String.class, Constants.FILE_EXTN);
                    metadataExtractorRegistry.metadataToExtract(MimeType.valueOf(Constants.MIME_FILE_NAME), String.class, Constants.FILE_NAME);
                })
                .build();
    }


//    @Bean
//    public RSocketRequester getRSocketRequester(RSocketRequester.Builder builder, @Value("${spring.rsocket.server.port}") int port){
//        return builder
//                .rsocketConnector(rSocketConnector ->
//                        rSocketConnector.reconnect(Retry.fixedDelay(2, Duration.ofSeconds(2))))
//                .transport(TcpClientTransport.create(port));
//    }

    @Bean
    public RSocketRequester rSocketRequester(RSocketStrategies rSocketStrategies,
                                             @Value("${spring.rsocket.server.port}") int port) {
        return RSocketRequester.builder()
                .rsocketStrategies(rSocketStrategies)
                .rsocketConnector(
                        rSocketConnector ->
                                rSocketConnector.reconnect(Retry.fixedDelay(2, Duration.ofSeconds(2)))
//                                        .keepAlive(Duration.ofSeconds(5), Duration.ofSeconds(30))
                )
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .tcp("localhost", port)
//                .websocket(URI.create("http://localhost:" + port))
                ;
    }


}