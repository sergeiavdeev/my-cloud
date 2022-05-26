import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private final Logger log = LogManager.getLogger(FileTransferHandler.class);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("Handler added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("Handler removed");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info(msg);
        String in  = (String) msg;
        if (in.startsWith("auth")) {
            ctx.fireChannelRead(msg);
        }
    }


}
