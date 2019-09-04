import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.DefaultHttpResponse
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpObject
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.HttpVersion
import io.netty.util.CharsetUtil
import org.junit.jupiter.api.Test

class API01 {
    @Test
    void m01() {
        EventLoopGroup boss = new NioEventLoopGroup()
        EventLoopGroup worker = new NioEventLoopGroup()
        try {
            ServerBootstrap server = new ServerBootstrap().group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("httpServerCodec", new HttpServerCodec())
                            socketChannel.pipeline().addLast("serverHandler", new SimpleChannelInboundHandler<HttpObject>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
                                    if (httpObject instanceof HttpRequest) {
                                        def content = Unpooled.copiedBuffer("hello netty!", CharsetUtil.UTF_8);
                                        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content)
                                        resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain")
                                        resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes())
                                        ctx.writeAndFlush(resp)
                                        ctx.channel().close()
                                    }
                                }
                            })
                        }
                    })
            ChannelFuture channelFuture = server.bind(8081).sync()
            channelFuture.channel().closeFuture().sync()
        } finally {
            boss.shutdownGracefully()
            worker.shutdownGracefully()
        }

    }
}
