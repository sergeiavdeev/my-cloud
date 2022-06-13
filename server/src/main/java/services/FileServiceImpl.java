package services;

import entities.FileResponse;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class FileServiceImpl implements FileService{

    private static FileService instance;
    private static final Logger logger = LogManager.getLogger(FileService.class);

    private FileServiceImpl() {}

    public static FileService getInstance() {

        if (instance == null) {
            instance = new FileServiceImpl();
        }
        return instance;
    }

    @Override
    public FileResponse getFileResponse(String uri, boolean keepAlive) throws IOException {

        File file = new File(Paths.get("").toAbsolutePath() + uri);

        RandomAccessFile raf = new RandomAccessFile(file, "r");
        long fileLength = raf.length();

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        HttpUtil.setContentLength(response, fileLength);
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
        if (!keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }
        return new FileResponse(raf, response);
    }

    @Override
    public HttpResponse creteDirectoryResponse(String uri) {
        File file = new File(Paths.get("").toAbsolutePath() + uri);
        file.mkdir();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        return response;
    }

    @Override
    public HttpResponse directoryListResponse(String uri) {

        File file = new File(Paths.get("").toAbsolutePath() + uri);
        if(file.isDirectory() && file.exists()) {
            String[] files = file.list();

            if (files == null) {
                return errorResponse(NOT_FOUND);
            }

            JSONArray arr = new JSONArray();
            for (String name : files) {
                File child = new File(file.getPath() + "/" + name);
                logger.info(child);
                JSONObject ob = new JSONObject();
                ob.put("path", uri + child.getName());
                ob.put("isDirectory", child.isDirectory());
                ob.put("fileName", child.getName());
                arr.put(ob);
            }

            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                    Unpooled.wrappedBuffer(arr.toString().getBytes(StandardCharsets.UTF_8)));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
            HttpUtil.setContentLength(response, response.content().readableBytes());
            return response;
        }
        return errorResponse(BAD_REQUEST);
    }

    @Override
    public String getDirectoryList(String uri) {

        String dirs = "[]";
        File file = new File(Paths.get("").toAbsolutePath() + uri);

        if(file.isDirectory() && file.exists()) {
            String[] files = file.list();

            if (files == null) {
                return  dirs;
            }

            JSONArray arr = new JSONArray();
            for (String name : files) {
                File child = new File(file.getPath() + "/" + name);
                logger.info(child);
                JSONObject ob = new JSONObject();
                ob.put("path", uri.replaceFirst("/.{36}", "") + child.getName());
                ob.put("isDirectory", child.isDirectory());
                ob.put("fileName", child.getName());
                arr.put(ob);
            }
            dirs = arr.toString();
        }
        return dirs;
    }

    @Override
    public boolean createDirectory(String uri) {

        File file = new File(Paths.get("").toAbsolutePath() + uri);
        return file.mkdir();
    }

    private HttpResponse errorResponse(HttpResponseStatus status) {

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status);
        HttpUtil.setContentLength(response, response.content().readableBytes());
        return response;
    }
}
