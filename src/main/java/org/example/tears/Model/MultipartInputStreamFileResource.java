package org.example.tears.Model;

import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;

public class MultipartInputStreamFileResource extends InputStreamResource {

    private final String filename;
    private final long size;

    public MultipartInputStreamFileResource(InputStream inputStream, String filename, long size) {
        super(inputStream);
        this.filename = filename;
        this.size = size;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public long contentLength() {
        return size;
    }
}
