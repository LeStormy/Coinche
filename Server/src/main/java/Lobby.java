
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;

@ChannelHandler.Sharable
public class Lobby extends SimpleChannelInboundHandler<Protocol.BaseCommand> {

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public Vector<Player> getPlayers() { return m_players; }

    private Vector<Player> m_players = new Vector<>();
    private Vector<GameRoom> m_gameRooms = new Vector<>();

    @FunctionalInterface
    interface MsgInterpretor <A, B, R> { R apply(A a, B b); }

    private Map<Protocol.BaseCommand.CommandType, MsgInterpretor<ChannelHandlerContext, Protocol.BaseCommand, Integer>> m_readMap
            = new HashMap<>();

    private GameRoom findRequestedPlayerRoom(ChannelHandlerContext ctx, Player.RequestType t_rType) {
        for (Player pl : m_players)
            if (pl.getChannelId() == ctx && pl.getRequest() == t_rType)
                for (GameRoom gr : m_gameRooms)
                    if (gr.getUuid() == pl.getUuid())
                        return gr;
        return null;
    }

    private GameRoom findPlayerRoom(ChannelHandlerContext ctx) {
        for (Player pl : m_players)
            if (pl.getChannelId() == ctx)
                for (GameRoom gr : m_gameRooms)
                    if (gr.getUuid() == pl.getUuid())
                        return gr;
        return null;
    }

    private Player findRequestedPlayer(ChannelHandlerContext ctx, Player.RequestType t_rType) {
        for (Player pl : m_players)
            if (pl.getChannelId() == ctx && pl.getRequest() == t_rType)
                return pl;
        return null;
    }

    private Player findPlayer(ChannelHandlerContext ctx) {
        for (Player pl : m_players)
            if (pl.getChannelId() == ctx)
                return pl;
        return null;
    }

    public Lobby() {
        m_readMap.put(Protocol.BaseCommand.CommandType.AUCTION, (ctx, obj) -> {
            GameRoom tmp = this.findRequestedPlayerRoom(ctx, Player.RequestType.AUCTION);
            if (tmp != null) {
                synchronized(tmp.getAuctioneer()) {
                    if (tmp.getAuctioneer().setRound(obj) == Auctioneer.AuctionStatus.AUCTION_OK)
                        tmp.getAuctioneer().notify();
                    else
                        GameRoom.write(ctx, PacketWrapper.buildServRespPacket(Protocol.ServResponse.RespType.ERR, "Auction forbidden"));
                }
            }
            return (0);
        });
        m_readMap.put(Protocol.BaseCommand.CommandType.MOVE, (ctx, obj) -> {
            GameRoom tmp = this.findRequestedPlayerRoom(ctx, Player.RequestType.TRICK);
            Player tmpPlayer = this.findRequestedPlayer(ctx, Player.RequestType.TRICK);
            if (tmp != null && tmpPlayer != null) {
                synchronized(tmp.getTrick()) {
                    if (tmp.getTrick().setMove(obj, tmpPlayer) == Trick.MoveStatus.MOVE_OK)
                        tmp.getTrick().notify();
                    else
                        GameRoom.write(ctx, PacketWrapper.buildServRespPacket(Protocol.ServResponse.RespType.ERR, "Move forbidden"));
                }
            }
            return (0);
        });
        m_readMap.put(Protocol.BaseCommand.CommandType.PLAYER_IDENTIFICATION, (ctx, obj) -> {
            m_players.add(new Player(ctx,
                    obj.getExtension(Protocol.player_identification.cmd).getName(),
                    obj.getExtension(Protocol.player_identification.cmd).getMAC()));
            GameRoom.write(m_players.lastElement().getChannelId(), PacketWrapper.buildServRespPacket(Protocol.ServResponse.RespType.OK_ID, null));
            Vector<Player> awaiting = new Vector<>();
            for (Player pl : m_players) {
                if (pl.getStatus() == Player.Status.WAITING)
                    awaiting.add(pl);
                if (awaiting.size() == 4)
                {
                    m_gameRooms.add(new GameRoom(awaiting));
                    CompletableFuture.runAsync(m_gameRooms.lastElement()::run);
                }
            }
            return (0);
        });
        m_readMap.put(Protocol.BaseCommand.CommandType.DISCONNECT, (ctx, obj) -> {
            GameRoom gr = findPlayerRoom(ctx);
            if (gr != null)
                gr.close(findPlayer(ctx));
            m_gameRooms.remove(gr);
            m_players.remove(findPlayer(ctx));
            Vector<Player> awaiting = new Vector<>();
            for (Player pl : m_players) {
                if (pl.getStatus() == Player.Status.WAITING)
                    awaiting.add(pl);
                if (awaiting.size() == 4)
                {
                    m_gameRooms.add(new GameRoom(awaiting));
                    CompletableFuture.runAsync(m_gameRooms.lastElement()::run);
                }
            }
            ctx.close();
            return (0);
        });
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                (GenericFutureListener<Future<Channel>>) future -> channels.add(ctx.channel()));
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Protocol.BaseCommand obj) throws Exception {
        if (m_readMap.get(obj.getType()) != null)
            m_readMap.get(obj.getType()).apply(ctx, obj);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        GameRoom gr = findPlayerRoom(ctx);
        Player ply = findPlayer(ctx);

        if (gr != null)
            gr.close(ply);
        m_gameRooms.remove(gr);

        Vector<Player> awaiting = new Vector<>();
        for (Player pl : m_players) {
            if (pl.getStatus() == Player.Status.WAITING)
                awaiting.add(pl);
            if (awaiting.size() == 4)
            {
                m_gameRooms.add(new GameRoom(awaiting));
                CompletableFuture.runAsync(m_gameRooms.lastElement()::run);
            }
        }
        ctx.close();
    }
}