
import io.netty.channel.ChannelHandlerContext;

import java.util.UUID;
import java.util.Vector;

public class Player {

    public enum RequestType {
        NONE,
        AUCTION,
        TRICK
    }

    private UUID m_roomUuid;
    private RequestType m_request;

    public void setRequested(RequestType t_type) { m_request = t_type; }
    public RequestType getRequest() { return m_request; }

    public UUID getUuid() { return m_roomUuid; }
    public void setUuid(UUID t_uuid) { this.m_roomUuid = t_uuid; }

    public ChannelHandlerContext getChannelId() { return m_channel; }

    public Status getStatus() { return m_status; }
    public void setStatus(Status t_status) { this.m_status = t_status; }

    public Team getTeam() { return m_team; }
    public void setTeam(Team t_team) { this.m_team = t_team; }

    public int getScore() { return m_score; }
    public void setScore(int t_score) { this.m_score += t_score; }

    public int getTeamScore() { return m_teamScore; }
    public void setTeamScore(int t_score) { this.m_teamScore += t_score; }

    public String getName() { return m_name.substring(0, m_name.indexOf('|')); }

    public enum Status {
        WAITING,
        IN_GAME
    }

    public enum Team {
        TEAM_NONE,
        TEAM_ONE,
        TEAM_TWO
    }

    private ChannelHandlerContext m_channel;
    private Status m_status;
    private Team m_team;
    private int m_score = 0;
    private int m_teamScore = 0;
    private final String m_name;
    private Vector<Deck.Card> m_hand;

    public Vector<Deck.Card> getHand() { return m_hand; }
    public void setHand(Vector<Deck.Card> m_hand) { this.m_hand = m_hand; }

    public Deck.Card getCard(int t_index) {
        return m_hand.get(t_index);
    }

    public boolean hasColor(Protocol.Symbols t_color) {
        for (Deck.Card cd : m_hand)
            if (cd.m_sym == t_color)
                return true;
        return false;
    }
    public boolean hasColor(Protocol.Trump t_color) {
        for (Deck.Card cd : m_hand)
            if (cd.m_sym.getNumber() == t_color.getNumber())
                return true;
        return false;
    }

    public Player(ChannelHandlerContext t_channel, String t_name, String t_MAC) {
        m_channel = t_channel;
        m_name = t_name + "|" + t_MAC;
        m_status = Status.WAITING;
    }

    public void reset() {
        m_status = Status.WAITING;
        m_team = Team.TEAM_NONE;
        m_score = 0;
        m_teamScore = 0;
        m_hand = new Vector<>();
    }


}
