package Handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import services.UserService;
import services.UserServiceImpl;
import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private static final String URI_AUTH = "/user/auth";
    private final Logger logger = LogManager.getLogger(AuthHandler.class);

    private final UserService userService = UserServiceImpl.getInstance();

    HttpRequest request;

    public void channelRead_00(ChannelHandlerContext ctx, Object msg) {

        logger.info(msg);
        //no auth for test
        ctx.fireChannelRead(msg);

        /*
        FullHttpRequest request = (FullHttpRequest)msg;

        if (request.method().equals(HttpMethod.POST) && request.uri().equals(URI_AUTH)) {

            JSONObject ob = new JSONObject(request.content().toString(StandardCharsets.UTF_8));
            User user = userService.auth(ob.getString("login"), ob.getString("password"));
            if (user == null) {
                ctx.writeAndFlush(userService.authErrorResponse());
                return;
            }
            String token = userService.generateToken(user);
            ctx.writeAndFlush(userService.authSuccessResponse(token, user.getUuid()));
            return;
        }

        String token = request.headers().get("token");
        String userId = request.headers().get("userId");
        if (token == null || userService.validateToken(token, userId) == null) {
            ctx.writeAndFlush(userService.authErrorResponse());
            return;
        }

        logger.info("Method: " + request.method().name() + ", URI: " + request.uri() + ", Token: " + token);
        ctx.fireChannelRead(msg);
        */
    }

    /*@Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {

        HttpRequest request = (HttpRequest) httpObject;

        String token = request.headers().get("token");
        String userId = request.headers().get("userId");
        if (token == null || userService.validateToken(token, userId) == null) {
            ctx.writeAndFlush(userService.authErrorResponse());
            ctx.close();
            return;
        }
        ctx.fireChannelRead(httpObject);

    }*/

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object httpObject) throws Exception {


        ctx.fireChannelRead(httpObject);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage());
        JSONObject ob = new JSONObject();
        ob.put("error", cause.getMessage() + " - auth error!");
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST,
                Unpooled.wrappedBuffer(ob.toString().getBytes(StandardCharsets.UTF_8)));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        ctx.writeAndFlush(response);
        ctx.close();
    }
}
