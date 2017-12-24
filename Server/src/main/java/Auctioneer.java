
import io.netty.channel.ChannelHandlerContext;

import java.util.Vector;


public class Auctioneer {
    private Round m_round = new Round(Protocol.Bet.FOLD, Protocol.Trump.NONE, Player.Team.TEAM_ONE, Protocol.Bet.FOLD, Protocol.Players.PLAYER_NONE);
    private boolean m_ready = false;
    private Lobby.MsgInterpretor<ChannelHandlerContext, Protocol.BaseCommand, Integer> m_fct;
    private int m_dealer;
    private Vector<Player> m_players;
    private int m_folds = 0;
    private Protocol.Bet m_lastRoundBet;
    private Protocol.Trump m_lastRoundTrump;

    public enum AuctionStatus {
        AUCTION_OK,
        AUCTION_FORBIDDEN
    }

    public Round getRound() { return m_round; }

    public boolean isTeamConflicting(Protocol.BaseCommand obj, Player pl) {
        return pl.getRequest() == Player.RequestType.AUCTION
                && pl.getTeam() == m_round.getTakers()
                && obj.getExtension(Protocol.Auction.cmd).getBet() == Protocol.Bet.COINCHE;
    }

    public AuctionStatus setRound(Protocol.BaseCommand obj) {
        Player.Team teamTmp = Player.Team.TEAM_NONE;
        Protocol.Players playerTmp = Protocol.Players.PLAYER_NONE;
        if ((m_round.getCoinche() == Protocol.Bet.COINCHE && (obj.getExtension(Protocol.Auction.cmd).getBet() != Protocol.Bet.FOLD
                && obj.getExtension(Protocol.Auction.cmd).getBet() != Protocol.Bet.SURCOINCHE))
            || (obj.getExtension(Protocol.Auction.cmd).getBet().getNumber() >= Protocol.Bet.COINCHE.getNumber() && m_round.getBet() == Protocol.Bet.FOLD)
            || (obj.getExtension(Protocol.Auction.cmd).getBet().getNumber() == Protocol.Bet.SURCOINCHE.getNumber() && m_round.getCoinche() != Protocol.Bet.COINCHE)) {
            return AuctionStatus.AUCTION_FORBIDDEN;
        }
        if (obj.getExtension(Protocol.Auction.cmd).getBet() == Protocol.Bet.FOLD)
        {
            ++m_folds;
            m_lastRoundBet = obj.getExtension(Protocol.Auction.cmd).getBet();
            m_lastRoundTrump = obj.getExtension(Protocol.Auction.cmd).getSymbol();
            return AuctionStatus.AUCTION_OK;
        }
        else if (obj.getExtension(Protocol.Auction.cmd).getBet().getNumber() <= m_round.getBet().getNumber())
            return AuctionStatus.AUCTION_FORBIDDEN;
        else {
            int i = 0;
            for (Player pl : m_players) {
                if (isTeamConflicting(obj, pl))
                    return AuctionStatus.AUCTION_FORBIDDEN;
                else if (pl.getRequest() == Player.RequestType.AUCTION) {
                    if (obj.getExtension(Protocol.Auction.cmd).getBet() == Protocol.Bet.COINCHE)
                        m_round.setCoinche(Protocol.Bet.COINCHE);
                    else {
                        playerTmp = Protocol.Players.valueOf(i);
                        teamTmp = pl.getTeam();
                    }
                }
                ++i;
            }
        }
        if (obj.getExtension(Protocol.Auction.cmd).getBet().getNumber() >= Protocol.Bet.COINCHE.getNumber())
            m_round.setCoinche(obj.getExtension(Protocol.Auction.cmd).getBet());
        else {
            m_round.setBet(obj.getExtension(Protocol.Auction.cmd).getBet());
            m_round.setTrump(obj.getExtension(Protocol.Auction.cmd).getSymbol());
        }
        if (teamTmp != Player.Team.TEAM_NONE) {
            m_round.setTaker(playerTmp);
            m_round.setTakers(teamTmp);
        }
        m_folds = 0;
        m_lastRoundBet = obj.getExtension(Protocol.Auction.cmd).getBet();
        m_lastRoundTrump = obj.getExtension(Protocol.Auction.cmd).getSymbol();
        return AuctionStatus.AUCTION_OK;
    }

    public boolean ready() {
        if (m_ready)
            return true;
        reset(m_dealer);
        return m_ready;
    }

    public Auctioneer(Lobby.MsgInterpretor<ChannelHandlerContext, Protocol.BaseCommand, Integer> t_fct, Vector<Player> t_players, int t_dealer) {
        m_fct = t_fct;
        m_players = t_players;
        m_dealer = t_dealer;
    }

    public void reset(int t_dealer) {
        m_round.setBet(Protocol.Bet.FOLD);
        m_round.setTrump(Protocol.Trump.NONE);
        m_round.setTakers(Player.Team.TEAM_ONE);
        m_round.setCoinche(Protocol.Bet.FOLD);
        m_round.setTaker(Protocol.Players.PLAYER_NONE);
        m_ready = false;
        m_folds = 0;
        m_dealer = t_dealer;
    }

    private void retrievePlayerBet() {
        try {
            synchronized(this) {
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean foldChecker() {
        if (m_folds == 4)
            return true;
        else if ((m_folds == 3 && m_round.getBet() != Protocol.Bet.FOLD)
                || m_round.getCoinche() == Protocol.Bet.SURCOINCHE) {
            m_ready = true;
            return true;
        }
        return false;
    }

    public void run() {
        Protocol.BaseCommand cmd = PacketWrapper.buildSwitchModePacket(Protocol.switch_mode.GameMode.M_AUCTION);
        for (Player pl : m_players)
            m_fct.apply(pl.getChannelId(), cmd);

        int i = 1;
        while (!foldChecker())
        {
            m_players.elementAt((m_dealer + i) % 4).setRequested(Player.RequestType.AUCTION);
            m_fct.apply(m_players.elementAt((m_dealer + i) % 4).getChannelId(), PacketWrapper.buildRequestPacket(Protocol.RequestType.R_AUCTION));
            retrievePlayerBet();
            m_players.elementAt((m_dealer + i) % 4).setRequested(Player.RequestType.NONE);
            int j = -1;
            for (Player pl : m_players) {
                GameRoom.write(pl.getChannelId(), PacketWrapper.buildPlayerStatePacket(pl.getHand(), pl.getTeamScore(), m_players.get((++j + 1) % 4).getTeamScore(),
                        null, Protocol.Players.valueOf(m_dealer), getRound().getTaker(), Protocol.Players.valueOf((1 + m_dealer + i) % 4), m_round.getTrump(), (m_dealer + 1) % 4));
                GameRoom.write(pl.getChannelId(), PacketWrapper.buildBetStatePacket(m_lastRoundTrump, m_lastRoundBet, Protocol.Players.valueOf((m_dealer + i) % 4)));
            }
            ++i;
        }

    }
}
