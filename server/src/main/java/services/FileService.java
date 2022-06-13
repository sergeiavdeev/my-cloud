package services;

import entities.FileResponse;
import io.netty.handler.codec.http.HttpResponse;

import java.io.IOException;

public interface FileService {

    FileResponse getFileResponse(String uri, boolean keepAlive) throws IOException;
    HttpResponse creteDirectoryResponse(String uri);
    HttpResponse directoryListResponse(String uri);

    String getDirectoryList(String uri);

    boolean createDirectory(String uri);
}
