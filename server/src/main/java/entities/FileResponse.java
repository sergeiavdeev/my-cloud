package entities;

import io.netty.handler.codec.http.HttpResponse;

import java.io.RandomAccessFile;

public class FileResponse {

    private final RandomAccessFile file;
    private final HttpResponse response;

    public FileResponse(RandomAccessFile file, HttpResponse response) {
        this.file = file;
        this.response = response;
    }

    public RandomAccessFile getFile() {
        return file;
    }


    public HttpResponse getResponse() {
        return response;
    }


}
