
import com.google.protobuf.ExtensionRegistry;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;

public class ServerInit extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    private Lobby m_lobby = new Lobby();

    public ServerInit(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        Protocol.registerAllExtensions(registry);

        pipeline.addLast(sslCtx.newHandler(channel.alloc()));

        pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        pipeline.addLast("protobufResponseDecoder", new ProtobufDecoder(Protocol.BaseCommand.getDefaultInstance(), registry));
        pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast("protobufEncoder", new ProtobufEncoder());
        pipeline.addLast("handler", m_lobby);
    }
}