import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import services.UserService;
import services.UserServiceImpl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private final String HTTP_POST = "POST";
    private final String URI_AUTH = "/auth";
    private final Logger logger = LogManager.getLogger(AuthHandler.class);

    //private final UserService userService = new UserServiceImpl();
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("Handler added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        logger.info("Handler removed");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        logger.info(msg);

        FullHttpRequest request = (FullHttpRequest)msg;

        if (request.method().name().equals(HTTP_POST) && request.uri().equals(URI_AUTH)) {

            JSONObject ob = new JSONObject(request.content().toString(StandardCharsets.UTF_8));
            authResponse(ctx);
            return;
        }

        String token = request.headers().get("token");
        if (token == null) {
            authErrorResponse(ctx);
            return;
        }

        logger.info("Method: " + request.method().name() + ", URI: " + request.uri() + ", Token: " + token);
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage());
        JSONObject ob = new JSONObject();
        ob.put("error", cause.getMessage());
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST,
                Unpooled.wrappedBuffer(ob.toString().getBytes(StandardCharsets.UTF_8)));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        ctx.writeAndFlush(response);
    }

    private void authResponse(ChannelHandlerContext ctx) {

        JSONObject ob = new JSONObject();
        ob.put("token", "lklsdfkahldlkfhSSDF");
        ob.put("userId", "kjhsdfkkjhsdfjhsdf");
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(ob.toString().getBytes(StandardCharsets.UTF_8)));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        ctx.writeAndFlush(response);
        logger.info(response);
    }

    private void authErrorResponse(ChannelHandlerContext ctx) {

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, UNAUTHORIZED);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        ctx.writeAndFlush(response);
        logger.info(response);
    }
}
