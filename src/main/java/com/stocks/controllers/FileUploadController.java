package com.stocks.controllers;

import com.stocks.entities.Status;
import com.stocks.services.FileUploadService;
import com.stocks.utils.Constants;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Controller
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @MessageMapping("upload-file")
    public Flux<String> upload(
            @Headers Map<String, Object> metadata,
            @Payload Flux<DataBuffer> content) throws IOException {
        var fileName = metadata.get(Constants.FILE_NAME);
        var fileExtn = metadata.get(Constants.FILE_EXTN);
        Path path = Paths.get(fileName + "." + fileExtn);
        return Flux.concat(fileUploadService.uploadFile(path, content), Mono.just(Status.COMPLETED.getName()))
                .onErrorReturn(Status.FAILED.getName());
    }

}
