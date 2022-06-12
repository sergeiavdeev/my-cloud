import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import javafx.scene.shape.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class FileTransferHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LogManager.getLogger(FileTransferHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        FullHttpRequest request = (FullHttpRequest)msg;
        final boolean keepAlive = HttpUtil.isKeepAlive(request);

        String userPath = request.uri();

        File file = new File(Paths.get("").toAbsolutePath().toString() + userPath);

        if (file.isDirectory() && file.list() != null) {
            directoryListResponse(ctx, file, userPath);
            return;
        }

        RandomAccessFile raf = new RandomAccessFile(file, "r");
        long fileLength = raf.length();

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        HttpUtil.setContentLength(response, fileLength);
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
        if (!keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (request.protocolVersion().equals(HTTP_1_0)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ctx.write(response);

        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;
        sendFileFuture = ctx.write(
                new DefaultFileRegion(raf.getChannel(), 0, fileLength),
                ctx.newProgressivePromise());
            // Write the end marker.
        lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

        if (!keepAlive) {
            // Close the connection when the whole content is written out.
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                if (total < 0) { // total unknown
                    logger.info(" Transfer progress: " + progress);
                } else {
                    logger.info(" Transfer progress: " + progress + " / " + total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) {
                logger.info(" Transfer complete.");
            }
        });
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        logger.info("Handler added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        logger.info("Handler removed");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        logger.error(cause);
        JSONObject ob = new JSONObject();
        ob.put("error", cause.getMessage());
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST,
                Unpooled.wrappedBuffer(ob.toString().getBytes(StandardCharsets.UTF_8)));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        ctx.writeAndFlush(response);

        logger.info(response);
    }

    private void directoryListResponse(ChannelHandlerContext ctx, File file, String userPath) {

        String files[] = file.list();

        JSONArray arr = new JSONArray();
        for (String name : files) {
            File child = new File(file.getPath() + "/" + name);
            logger.info(child);
            JSONObject ob = new JSONObject();
            ob.put("path", userPath + "/" + child.getName());
            ob.put("isDirectory", child.isDirectory());
            ob.put("fileName", child.getName());
            arr.put(ob);
        }

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(arr.toString().getBytes(StandardCharsets.UTF_8)));
        response.headers().set(HttpHeaderNames.WWW_AUTHENTICATE, "Basic");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        ctx.writeAndFlush(response);
        logger.info(response);
    }

    private void sendFile(ChannelHandlerContext ctx, File file) {

    }
}
