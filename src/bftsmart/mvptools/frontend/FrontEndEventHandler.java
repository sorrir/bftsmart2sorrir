package bftsmart.mvptools.frontend;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.core.messages.TOMMessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

class FrontEndEventHandler extends SimpleChannelInboundHandler<String> {

    AsynchServiceProxy serviceProxy;

    public FrontEndEventHandler(AsynchServiceProxy serviceProxy) {
        this.serviceProxy = serviceProxy;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {

       // System.out.println("FrontEndEventHandler received: " + msg);
       // System.out.println("FrontEndEventHandler received msg length: " + msg.length());

        // Forward request to replica set, use invoke() to achieve total order

        byte[] request = msg.getBytes();

        //System.out.println("FrontEndEventhandler request bytes " + request.length + " " + request);

        // Todo buffer sending messages while not yet connected?
        this.serviceProxy.invokeAsynchRequest(request, new ReplyListener() {

            @Override
            public void reset() {
            }

            @Override
            public void replyReceived(RequestContext context, TOMMessage reply) {
                System.out.println(reply);
            }
        }, TOMMessageType.ORDERED_REQUEST);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}