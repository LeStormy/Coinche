
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
        import io.netty.channel.EventLoopGroup;
        import io.netty.channel.nio.NioEventLoopGroup;
        import io.netty.channel.socket.nio.NioSocketChannel;
        import io.netty.handler.ssl.SslContext;
        import io.netty.handler.ssl.SslContextBuilder;
        import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class Client
{
    static String HOST = System.getProperty("host", "127.0.0.1");
    static int PORT = Integer.parseInt(System.getProperty("port", "4242"));

    public static void main(String[] args) throws Exception {

        if (args.length > 1) {
            HOST = args[0];
            PORT = Integer.parseInt(args[1]);
        }
        else
            System.out.println("Using default IP 127.0.0.1 and default port 4242");
        final SslContext sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            Controller ctrl = new Controller();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInit(sslCtx, ctrl));

            b.connect(HOST, PORT).sync().channel();
            ctrl.run();
        } finally {
            group.shutdownGracefully();
        }
    }
}