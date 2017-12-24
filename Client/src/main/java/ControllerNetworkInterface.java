
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.Map;

public class ControllerNetworkInterface extends SimpleChannelInboundHandler<Protocol.BaseCommand> {

    private Controller m_controller;

    @FunctionalInterface
    interface MsgInterpreter<A, B> { void apply (A a, B b); }

    private Map<Protocol.BaseCommand.CommandType, MsgInterpreter<ChannelHandlerContext, Protocol.BaseCommand>> m_map = new HashMap<>();

    public ControllerNetworkInterface(Controller t_controller) {
        m_controller = t_controller;

        m_map.put(Protocol.BaseCommand.CommandType.PLAYER_STATE, (ctx, obj) -> m_controller.updateModel(obj.getExtension(Protocol.player_state.cmd)));
        m_map.put(Protocol.BaseCommand.CommandType.BET_STATE, (ctx, obj) -> m_controller.updateModel(obj.getExtension(Protocol.bet_state.cmd)));
        m_map.put(Protocol.BaseCommand.CommandType.STATIC_GAME_STATE, (ctx, obj) -> m_controller.updateModel(obj.getExtension(Protocol.static_game_state.cmd)));
        m_map.put(Protocol.BaseCommand.CommandType.REQUEST, (ctx, obj) -> m_controller.updateModel("Your turn !"));
        m_map.put(Protocol.BaseCommand.CommandType.SWITCH_MODE, (ctx, obj) -> {
            if (obj.getExtension(Protocol.switch_mode.cmd).getMode() == Protocol.switch_mode.GameMode.M_AUCTION)
                m_controller.updateModel(GameModel.GameState.AUCTION);
            else
                m_controller.updateModel(GameModel.GameState.TRICKS);
        });
        m_map.put(Protocol.BaseCommand.CommandType.SERV_RESP, (ctx, obj) -> {
            switch (obj.getExtension(Protocol.ServResponse.cmd).getRType()) {
                case OK_ID:
                    m_controller.updateModel(GameModel.GameState.WAITING);
                    break;
                case TRICK_WON:
                    m_controller.updateModel(obj.getExtension(Protocol.ServResponse.cmd).getMessage());
                    break;
                case ERR:
                    m_controller.updateModel(obj.getExtension(Protocol.ServResponse.cmd).getMessage());
                    break;
            }
        });
        m_controller.launchGame();
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, Protocol.BaseCommand obj) throws Exception {
        if (m_map.get(obj.getType()) != null)
            m_map.get(obj.getType()).apply(ctx, obj);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}