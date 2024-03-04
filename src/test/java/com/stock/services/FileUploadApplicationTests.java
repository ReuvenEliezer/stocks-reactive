package com.stock.services;

import com.stocks.StockApp;
import com.stocks.utils.Constants;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = StockApp.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FileUploadApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadApplicationTests.class);

    private static final String FILE_NAME = "apache-maven-3.9.6-bin";
    private static final String FILE_TYPE = "zip";

    @Autowired
    private RSocketRequester rSocketRequester;

    @Value("${input.file.path:src/test/resources/input/" + FILE_NAME + "." + FILE_TYPE + "}")
    private Path inputPath;

    @Value("${output.file.path:src/test/resources/output/" + FILE_NAME + "." + FILE_TYPE + "}")
    private Path outputPath;


    @BeforeEach
    void setUp() {
        File outputFile = outputPath.toFile();
        if (outputFile.exists()) {
            Assertions.assertThat(outputFile.delete()).isTrue();
        }

    }

    @Test
    void uploadFileTest() {

        // read input file as 4096 chunks
        assertThat(inputPath.toFile()).exists();
        Flux<DataBuffer> readFlux = DataBufferUtils.read(inputPath, new DefaultDataBufferFactory(), 4096)
                .doOnNext(s -> logger.info("Sent"));
        rSocketRequester.route("upload-file")
                .metadata(metadataSpec -> {
                    metadataSpec.metadata(FILE_NAME, MimeType.valueOf(Constants.MIME_FILE_NAME));
                    metadataSpec.metadata(FILE_TYPE, MimeType.valueOf(Constants.MIME_FILE_EXTENSION));
                })
                .data(readFlux)
                .retrieveFlux(String.class)
                .doOnNext(s -> logger.info("Upload Status : {}", s))
                .doOnComplete(() -> logger.info("done to Upload file: from '{}' to '{}'", inputPath, outputPath))
                .blockLast();

        assertThat(outputPath.toFile()).exists();
    }

}