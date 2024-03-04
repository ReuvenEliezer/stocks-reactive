package com.stocks.services;

import com.stocks.entities.Status;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Path;

public interface FileUploadService {
    Flux<String> uploadFile(Path path, Flux<DataBuffer> bufferFlux) throws IOException;
}
