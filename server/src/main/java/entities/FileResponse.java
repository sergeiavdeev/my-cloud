package entities;

import io.netty.handler.codec.http.HttpResponse;

import java.io.RandomAccessFile;

public class FileResponse {

    private RandomAccessFile file;
    private HttpResponse response;

    public FileResponse(RandomAccessFile file, HttpResponse response) {
        this.file = file;
        this.response = response;
    }

    public RandomAccessFile getFile() {
        return file;
    }

    public void setFile(RandomAccessFile file) {
        this.file = file;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }
}
