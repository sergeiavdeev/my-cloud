package Handlers;

import entities.FileResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import services.FileService;
import services.FileServiceImpl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class FileHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger logger = LogManager.getLogger(FileHandler.class);

    private HttpRequest request;
    boolean keepAlive;
    //String userPath;
    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if size exceed

    private HttpPostRequestDecoder decoder;
    private QueryStringDecoder uriDecoder;
    private String requestUri;
    private final FileService fileService = FileServiceImpl.getInstance();
    FileOutputStream outputStream;

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
        // on exit (in normal
        // exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
        // exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) {

        if (httpObject instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) httpObject;

            uriDecoder = new QueryStringDecoder(request.uri());
            requestUri = "/" + request.headers().get("userId") + normalizeUri(uriDecoder.path());
            decoder = new HttpPostRequestDecoder(factory, request);

            keepAlive = HttpUtil.isKeepAlive(request);

            if (request.method().equals(HttpMethod.GET)) {
                httpGetHandler(ctx.channel());
            }
            return;

        } else if (httpObject instanceof HttpContent){ //content
            HttpContent content = (HttpContent) httpObject;

            if (request.method().equals(HttpMethod.GET))return;

            if (decoder.isMultipart()) {
                try {
                    multipartContentHandler(ctx, content);
                } catch (IOException e) {
                    writeErrorResponse(ctx.channel(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                }

                return;
            }
            httpPostHandler(ctx.channel());
            return;
        }
        writeErrorResponse(ctx.channel());
    }

    private void httpPostHandler(Channel channel) {

        if(fileService.createDirectory(requestUri)) {
            writeResponse(channel);
        } else {
            writeErrorResponse(channel, HttpResponseStatus.CONFLICT);
        }
    }

    private void httpGetHandler(Channel channel) {

        boolean list = uriDecoder.parameters().get("list") != null;
        String fileName = uriDecoder.parameters().getOrDefault("file", Collections.singletonList("")).get(0);

        if (list) {
            writeResponse(channel, fileService.getDirectoryList(requestUri));
            return;
        }

        if (!fileName.isEmpty()) {

            try {
                FileResponse fileResponse = fileService.getFileResponse(requestUri + fileName, keepAlive);
                channel.write(fileResponse.getResponse());
                RandomAccessFile raf = fileResponse.getFile();
                channel.write(new DefaultFileRegion(raf.getChannel(), 0, raf.length()));

                ChannelFuture lastContentFuture = channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

                if (!keepAlive) {
                    lastContentFuture.addListener(ChannelFutureListener.CLOSE);
                }
            } catch (IOException e) {
                writeErrorResponse(channel, HttpResponseStatus.NOT_FOUND);
            }
            return;
        }
        writeResponse(channel);
    }

    private void writeResponse(Channel channel) {

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, 0);

        ChannelFuture future = channel.writeAndFlush(response);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void writeResponse(Channel channel, String body) {

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(body.getBytes(StandardCharsets.UTF_8)));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());

        ChannelFuture future = channel.writeAndFlush(response);

        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void writeErrorResponse(Channel channel) {

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, 0);

        ChannelFuture future = channel.writeAndFlush(response);
        // Close the connection after the write operation is done if necessary.
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void writeErrorResponse(Channel channel, HttpResponseStatus status) {

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, 0);

        ChannelFuture future = channel.writeAndFlush(response);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void multipartContentHandler(ChannelHandlerContext ctx, HttpContent content) throws IOException {

        decoder.offer(content);

        if (content instanceof LastHttpContent) {
            if (outputStream != null) outputStream.close();
            outputStream = null;
            MixedFileUpload d = (MixedFileUpload)decoder.getBodyHttpData("file");

            FileUpload fileUpload = d.copy();

            Path path = Paths.get(Paths.get("").toAbsolutePath() + requestUri + fileUpload.getFilename());
            fileUpload.renameTo(path.toFile());
            logger.info("Download file {} size {} bytes", d.getFilename(), d.length());

            writeResponse(ctx.channel());

            request = null;
            decoder.destroy();
            decoder = null;
        }
    }

    private String normalizeUri(String uri) {

        if (uri.endsWith("/")) {
            return uri;
        }
        return uri + "/";
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage());
    }
}
