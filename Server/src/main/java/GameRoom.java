
import io.netty.channel.ChannelHandlerContext;

import java.util.UUID;
import java.util.Vector;

public class GameRoom {

    private Vector<Player> m_players = new Vector<>();
    private int m_dealer = 0;
    private Deck m_deck = new Deck();
    private Auctioneer m_auctioneer;
    private Trick m_trick;
    private UUID m_uuid;

    public UUID getUuid() { return m_uuid; }
    public Vector<Player> getPlayers() { return m_players; }
    public int getRounds() {
        int m_rounds = 0;
        return m_rounds; }
    public int getDealer() { return m_dealer; }
    public Auctioneer getAuctioneer() { return m_auctioneer; }
    public Trick getTrick() { return m_trick; }

    private void setTeams() {
        m_players.elementAt(0).setTeam(Player.Team.TEAM_ONE);
        m_players.elementAt(1).setTeam(Player.Team.TEAM_TWO);
        m_players.elementAt(2).setTeam(Player.Team.TEAM_ONE);
        m_players.elementAt(3).setTeam(Player.Team.TEAM_TWO);
    }

    public GameRoom(Vector<Player> t_players) {

        assert(t_players.size() == 4);

        m_uuid = UUID.randomUUID();
        m_players = t_players;

        for (Player pl : m_players) {
            pl.setStatus(Player.Status.IN_GAME);
            pl.setUuid(m_uuid);
        }

        setTeams();
        m_deck.Deal();

        m_auctioneer = new Auctioneer(GameRoom::write, m_players, m_dealer);
        m_trick = new Trick(m_auctioneer.getRound(), GameRoom::write, 0, m_players);
    }

    public static int write(ChannelHandlerContext ctx, Protocol.BaseCommand obj) {
        ctx.writeAndFlush(obj);
        return 0;
    }

    public boolean teamHasWon() {
        for (Player pl : m_players)
            if (pl.getTeamScore() >= 1000)
                return true;
        return false;
    }

    private void checkContract() {
        int teamOneScore = m_players.get(0).getScore() + m_players.get(2).getScore();
        int teamTwoScore = m_players.get(1).getScore() + m_players.get(3).getScore();
        int coinche = 1;

        if (m_auctioneer.getRound().getTrump() == Protocol.Trump.WITHOUT) {
            teamOneScore *= (162.0 / 130.0);
            teamTwoScore *= (162.0 / 130.0);
        }
        else if (m_auctioneer.getRound().getTrump() == Protocol.Trump.ALL) {
            teamOneScore *= (162.0 / 258.0);
            teamTwoScore *= (162.0 / 258.0);
        }
        if (m_auctioneer.getRound().getCoinche() == Protocol.Bet.COINCHE) {
            coinche = 2;
        }
        else if (m_auctioneer.getRound().getCoinche() == Protocol.Bet.SURCOINCHE) {
            coinche = 4;
        }
        if (m_auctioneer.getRound().getTakers() == Player.Team.TEAM_ONE && m_auctioneer.getRound().getBet().getNumber() <= teamOneScore)
        {
            m_players.get(0).setTeamScore((teamOneScore + m_auctioneer.getRound().getBet().getNumber()) * coinche);
            m_players.get(2).setTeamScore((teamOneScore + m_auctioneer.getRound().getBet().getNumber()) * coinche);
        }
        else if (m_auctioneer.getRound().getTakers() == Player.Team.TEAM_ONE && m_auctioneer.getRound().getBet().getNumber() > teamOneScore)
        {
            m_players.get(1).setTeamScore((teamTwoScore + m_auctioneer.getRound().getBet().getNumber()) * coinche);
            m_players.get(3).setTeamScore((teamTwoScore + m_auctioneer.getRound().getBet().getNumber()) * coinche);
        }
        else if (m_auctioneer.getRound().getTakers() == Player.Team.TEAM_TWO && m_auctioneer.getRound().getBet().getNumber() <= teamOneScore)
        {
            m_players.get(1).setTeamScore((teamTwoScore + m_auctioneer.getRound().getBet().getNumber()) * coinche);
            m_players.get(3).setTeamScore((teamTwoScore + m_auctioneer.getRound().getBet().getNumber()) * coinche);
        }
        else if (m_auctioneer.getRound().getTakers() == Player.Team.TEAM_TWO && m_auctioneer.getRound().getBet().getNumber() > teamOneScore)
        {
            m_players.get(0).setTeamScore((teamOneScore + m_auctioneer.getRound().getBet().getNumber()) * coinche);
            m_players.get(2).setTeamScore((teamOneScore + m_auctioneer.getRound().getBet().getNumber()) * coinche);
        }
        for (Player pl : m_players)
            pl.setScore(0);

    }

    public void run() {
        int j;
        int whoPlay;
        j = -1;
        for (Player pl : m_players)
            write(pl.getChannelId(), PacketWrapper.buildGameStatePacket(new String[] {m_players.get(0).getName(), m_players.get(1).getName(), m_players.get(2).getName(), m_players.get(3).getName()}, ++j));
        while (!teamHasWon())
        {
            while (!m_auctioneer.ready()) {
                j = -1;
                for (Vector<Deck.Card> hand : m_deck.Deal()) {
                    m_players.get(++j).setHand(hand);
                    write(m_players.get(j).getChannelId(), PacketWrapper.buildPlayerStatePacket(m_players.get(j).getHand(),
                            m_players.get(j).getTeamScore(), m_players.get((j + 1) %4).getTeamScore(),null,
                            Protocol.Players.valueOf(m_dealer), m_auctioneer.getRound().getTaker(),
                            Protocol.Players.valueOf((m_dealer + 1) % 4), Protocol.Trump.NONE, (m_dealer + 1) % 4));
                }
                m_auctioneer.run();
                ++m_dealer;
                m_dealer %= 4;
            }
            whoPlay = m_dealer;
            for (Player pl : m_players)
                write(pl.getChannelId(), PacketWrapper.buildSwitchModePacket(Protocol.switch_mode.GameMode.M_TRICKS));
            for (int i = 0; i < 8; ++i) {
                m_trick = new Trick(m_auctioneer.getRound(), GameRoom::write, whoPlay, m_players);
                m_trick.run();
                ScoreCounter score = new ScoreCounter(m_auctioneer.getRound(), whoPlay, m_players, m_trick.getTrick(), i);
                whoPlay = score.getWinnerIndex();
                for (Player pl : m_players)
                    write(pl.getChannelId(), PacketWrapper.buildServRespPacket(Protocol.ServResponse.RespType.TRICK_WON, m_players.get(whoPlay).getName() + " wins " + score.getTrickScore() + " points"));
            }
            checkContract();
            m_auctioneer.reset(m_dealer);
            j = -1;
            for (Player pl : m_players)
                write(pl.getChannelId(), PacketWrapper.buildPlayerStatePacket(pl.getHand(),
                        pl.getTeamScore(), m_players.get((++j + 1) %4).getTeamScore(),null,
                     Protocol.Players.valueOf(m_dealer), m_auctioneer.getRound().getTaker(),
                     Protocol.Players.valueOf((m_dealer + 1) % 4), Protocol.Trump.NONE, (m_dealer + 1) % 4));
        }


    }

    public void close(Player t_player) {
        for (Player pl : m_players)
            if (pl != t_player) {
                pl.reset();
                GameRoom.write(pl.getChannelId(), PacketWrapper.buildServRespPacket(Protocol.ServResponse.RespType.ERR, t_player.getName() + " has left the game room.\nReturning to lobby."));
                GameRoom.write(pl.getChannelId(), PacketWrapper.buildServRespPacket(Protocol.ServResponse.RespType.OK_ID, null));
            }
    }
}
