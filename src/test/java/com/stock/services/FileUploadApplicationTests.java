package com.stock.services;

import com.stocks.StockApp;
import com.stocks.entities.Status;
import com.stocks.utils.Constants;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeType;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = StockApp.class)
class FileUploadApplicationTests {

    @Autowired
    private RSocketRequester rSocketRequester;

//    @Value("classpath:input/java_tutorial.pdf")
//    private Resource resource;

    @Test
    void uploadFileTest() {

        // read input file as 4096 chunks
        Path path = Path.of("C:\\Users\\eliezerr\\Downloads\\Docker Desktop Installer.exe");
        assertThat(path.toFile()).exists();
        Flux<DataBuffer> readFlux = DataBufferUtils.read(path, new DefaultDataBufferFactory(), 4096)
                .doOnNext(s -> System.out.println("Sent"));

        Mono.just(rSocketRequester)
                .map(r -> r.route("upload-file")
                        .metadata(metadataSpec -> {
                            metadataSpec.metadata("exe", MimeType.valueOf(Constants.MIME_FILE_EXTENSION));
                            metadataSpec.metadata("output", MimeType.valueOf(Constants.MIME_FILE_NAME));
                        })
                        .data(readFlux)
                )
                .flatMapMany(r -> r.retrieveFlux(String.class))
                .doOnNext(s -> System.out.println("Upload Status : " + s))
                .blockLast();

//        Flux<Status> result = rSocketRequester
//                .route("file.upload")
//                .metadata(metadataSpec -> {
//                    metadataSpec.metadata("pdf", MimeType.valueOf(Constants.MIME_FILE_EXTENSION));
//                    metadataSpec.metadata("output", MimeType.valueOf(Constants.MIME_FILE_NAME));
//                })
//                .data(readFlux)
//                .retrieveFlux(Status.class)
//                .doOnNext(s -> System.out.println("Upload Status : " + s));
////                .subscribe();
//
//            result.subscribe();
//        result.blockLast();
    }

}