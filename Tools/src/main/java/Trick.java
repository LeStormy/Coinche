
import io.netty.channel.ChannelHandlerContext;

import java.util.Vector;

@FunctionalInterface
interface MsgInterpreter<A, B, R> { R apply(A a, B b); }

public class Trick {

    public enum MoveStatus {
        MOVE_OK,
        MOVE_FORBIDDEN
    }

    private Round m_round;
    private MsgInterpreter<ChannelHandlerContext, Protocol.BaseCommand, Integer> m_fct;
    private int m_starter;
    private Vector<Player> m_players;
    private Vector<Deck.Card> m_trick = new Vector<>();

    public void setStarter(int m_starter) { this.m_starter = m_starter; }
    public Vector<Deck.Card> getTrick() { return m_trick; }


    public Trick(Round t_round, MsgInterpreter<ChannelHandlerContext, Protocol.BaseCommand, Integer> t_fct,
                 int t_starter, Vector<Player> t_players) {
        this.m_round = t_round;
        this.m_fct = t_fct;
        this.m_starter = t_starter;
        this.m_players = t_players;
    }

    private MoveStatus checkTrump(Deck.Card t_cardPlayed, Player t_player, int t_cardId) {
        if (m_round.getTrump() == Protocol.Trump.ALL || m_round.getTrump() == Protocol.Trump.WITHOUT
                || m_round.getTrump().getNumber() == t_cardPlayed.m_sym.getNumber())
        {
            m_trick.add(t_cardPlayed);
            t_player.getHand().remove(t_cardId);
            return MoveStatus.MOVE_OK;
        }
        else if (!t_player.hasColor(m_trick.firstElement().m_sym) && !t_player.hasColor(m_round.getTrump()))
        {
            m_trick.add(t_cardPlayed);
            t_player.getHand().remove(t_cardId);
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.MOVE_FORBIDDEN;
    }

    private MoveStatus checkTurnColor(Deck.Card t_cardPlayed, Player t_player, int t_cardId) {
        if (t_player.hasColor(m_trick.firstElement().m_sym) && t_cardPlayed.m_sym != m_trick.firstElement().m_sym)
            return MoveStatus.MOVE_FORBIDDEN;
        else if (!t_player.hasColor(m_trick.firstElement().m_sym))
            return checkTrump(t_cardPlayed, t_player, t_cardId);
        m_trick.add(t_cardPlayed);
        t_player.getHand().remove(t_cardId);
        return MoveStatus.MOVE_OK;
    }

    private void retrievePlayerMove() {
        try {
            synchronized(this) {
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public MoveStatus setMove(Protocol.BaseCommand obj, Player t_player) {
        Deck.Card cardPlayed = t_player.getCard(obj.getExtension(Protocol.Move.cmd).getIndex());
        int cardId = obj.getExtension(Protocol.Move.cmd).getIndex();
        if (m_trick.size() == 0) {
            m_trick.add(cardPlayed);
            t_player.getHand().remove(cardId);
            return MoveStatus.MOVE_OK;
        }
        return checkTurnColor(cardPlayed, t_player, cardId);
    }

    public void run() {
        for (int i = 0; i < 4; ++i) {
            for (Player pl : m_players)
               pl.getChannelId().writeAndFlush(PacketWrapper.buildPlayerStatePacket(pl.getHand(), pl.getTeamScore(), pl.getTeamScore(), this, Protocol.Players.valueOf((4 + m_starter - 1) % 4), m_round.getTaker(), Protocol.Players.valueOf((m_starter + i) % 4), m_round.getTrump(), m_starter));
            m_players.elementAt((m_starter + i) % 4).setRequested(Player.RequestType.TRICK);
            m_fct.apply(m_players.elementAt((m_starter + i) % 4).getChannelId(), PacketWrapper.buildRequestPacket(Protocol.RequestType.R_MOVE));
            retrievePlayerMove();
            m_players.elementAt((m_starter + i) % 4).setRequested(Player.RequestType.NONE);
            for (Player pl : m_players)
                pl.getChannelId().writeAndFlush(PacketWrapper.buildPlayerStatePacket(pl.getHand(), pl.getTeamScore(), pl.getTeamScore(), this, Protocol.Players.valueOf((4 + m_starter - 1) % 4), m_round.getTaker(), Protocol.Players.valueOf((m_starter + i) % 4), m_round.getTrump(), m_starter));
     }
    }
}
