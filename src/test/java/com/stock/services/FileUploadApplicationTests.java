package com.stock.services;

import com.stocks.StockApp;
import com.stocks.utils.Constants;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = StockApp.class)
class FileUploadApplicationTests {

    @Autowired
    private RSocketRequester rSocketRequester;

//    @Value("classpath:input/java_tutorial.pdf")
//    private Resource resource;


    @Value("${output.file.path:src/test/resources/output/Docker Desktop Installer.exe}")
    private Path outputPath;

    @Test
//    @Disabled
    void uploadFileTest() {

        // read input file as 4096 chunks
        Path path = Path.of("C:\\Users\\eliezerr\\Downloads\\Docker Desktop Installer.exe");
        assertThat(path.toFile()).exists();
        Flux<DataBuffer> readFlux = DataBufferUtils.read(path, new DefaultDataBufferFactory(), 4096)
                .doOnNext(s -> System.out.println("Sent"));

        Mono.just(rSocketRequester)
                .map(r -> r.route("upload-file")
                        .metadata(metadataSpec -> {
                            metadataSpec.metadata("Docker Desktop Installer", MimeType.valueOf(Constants.MIME_FILE_NAME));
                            metadataSpec.metadata("exe", MimeType.valueOf(Constants.MIME_FILE_EXTENSION));
                        })
                        .data(readFlux)
                )
                .flatMapMany(r -> r.retrieveFlux(String.class))
                .doOnNext(s -> System.out.println("Upload Status : " + s))
                .blockLast();

        assertThat(outputPath.toFile()).exists();
    }

}