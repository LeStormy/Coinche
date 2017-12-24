
import com.google.protobuf.ExtensionRegistry;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;

public class ClientInit extends ChannelInitializer<SocketChannel> {

    private final SslContext m_sslCtx;
    private Controller m_controller;

    public ClientInit(SslContext t_sslCtx, Controller t_ctrl) {
        this.m_controller = t_ctrl;
        this.m_sslCtx = t_sslCtx;
    }

    @Override
    public void initChannel(SocketChannel t_ch) throws Exception {
        ChannelPipeline pipeline = t_ch.pipeline();

        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        Protocol.registerAllExtensions(registry);

        m_controller.setSocketChan(t_ch);
        pipeline.addLast(m_sslCtx.newHandler(t_ch.alloc(), Client.HOST, Client.PORT));

        pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        pipeline.addLast("protobufResponseDecoder", new ProtobufDecoder(Protocol.BaseCommand.getDefaultInstance(), registry));
        pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast("protobufEncoder", new ProtobufEncoder());
        pipeline.addLast("handler", new ControllerNetworkInterface(m_controller));
    }
}