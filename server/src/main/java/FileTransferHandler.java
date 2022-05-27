import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.UserRepository;

import java.nio.charset.Charset;

public class FileTransferHandler extends ChannelInboundHandlerAdapter {

    private final Logger log = LogManager.getLogger(FileTransferHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        log.info("Channel read: " + msg);
        ctx.channel().writeAndFlush("OK\n");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        UserRepository repo = new UserRepository();
        log.info("Handler added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("Handler removed");
    }
}
