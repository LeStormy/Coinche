

import java.util.List;
import java.util.Vector;

public class GameModel {

    GameModel() {
        resetPlayers();
    }

    public class Auction {
        private Protocol.Bet m_bet;
        private Protocol.Trump m_trump;

        Auction(Protocol.Bet t_bet, Protocol.Trump t_trump) {
            this.m_bet = t_bet;
            this.m_trump = t_trump;
        }

        Protocol.Bet getBet() { return m_bet; }
        void setBet(Protocol.Bet t_bet) { this.m_bet = t_bet; }

        Protocol.Trump getTrump() { return m_trump; }
        void setTrump(Protocol.Trump t_trump) { this.m_trump = t_trump; }
    }

    public class Player {
        private String m_name;
        private boolean m_isTurn;
        private boolean m_isDealer;
        private Auction m_auction;
        private boolean m_isTaker;

        Player(String t_name, boolean t_isTurn, boolean t_isDealer, Auction t_auction, boolean t_isTaker) {
            this.m_name = t_name;
            this.m_isTurn = t_isTurn;
            this.m_isDealer = t_isDealer;
            this.m_auction = t_auction;
            this.m_isTaker = t_isTaker;
        }

        String getName() { return m_name; }
        void setName(String t_name) { this.m_name = t_name; }

        boolean isIsTurn() { return m_isTurn; }
        void setIsTurn(boolean t_isTurn) { this.m_isTurn = t_isTurn; }

        boolean isIsDealer() { return m_isDealer; }
        void setIsDealer(boolean t_isDealer) { this.m_isDealer = t_isDealer; }

        Auction getAuction() { return m_auction; }
        public void setAuction(Auction t_auction) { this.m_auction = t_auction; }

        boolean isIsTaker() { return m_isTaker; }
        void setIsTaker(boolean t_isTaker) { this.m_isTaker = t_isTaker; }
    }

    public class Card {
        private Protocol.Values m_val;
        private Protocol.Symbols m_sym;

        Card(Protocol.Values t_val, Protocol.Symbols t_sym) {
            this.m_val = t_val;
            this.m_sym = t_sym;
        }

        Protocol.Values getVal() { return m_val; }
        public void setVal(Protocol.Values t_val) { this.m_val = t_val; }

        Protocol.Symbols getSym() { return m_sym; }
        public void setSym(Protocol.Symbols t_sym) { this.m_sym = t_sym; }
    }

    public enum GameState {
        CONNECTING,
        WAITING,
        AUCTION,
        TRICKS
    }

    private Vector<Card> m_trick = new Vector<>();
    private Vector<Player> m_players = new Vector<>();
    private Vector<Card> m_hand = new Vector<>();
    private GameState m_gameState = GameState.CONNECTING;
    private int m_scoreTeamOne = 0;
    private int m_scoreTeamTwo = 0;
    private Protocol.Trump m_trump = Protocol.Trump.NONE;
    private int m_starter = 0;
    private int m_position = 0;

    int getStarter() { return m_starter; }
    void setStarter(int m_starter) { this.m_starter = m_starter; }

    int getPosition() { return m_position; }
    void setPosition(int t_position) { this.m_position = t_position; }

    private Card protoCardConverter(Protocol.player_state.Card t_card) {
        return (new Card(t_card.getVal(), t_card.getSym()));
    }

    Vector<Player> getPlayers() { return m_players; }
    public void setPlayers(Vector<Player> m_players) { this.m_players = m_players; }
    public void setPlayers(List<Player> t_players) {
        this.m_players.addAll(t_players);
    }
    void setPlayersNames(List<String> t_players) {
        int i = -1;
        for (Player pl : m_players)
            pl.setName(t_players.get(++i));
    }

    Vector<Card> getHand() { return m_hand; }
    public void setHand(Vector<Card> m_hand) { this.m_hand = m_hand; }
    void setHand(List<Protocol.player_state.Card> t_hand) {
        this.m_hand.clear();
        for (Protocol.player_state.Card it : t_hand)
            this.m_hand.add(protoCardConverter(it));
    }

    GameState getGameState() { return m_gameState; }
    void setGameState(GameState m_gameState) { this.m_gameState = m_gameState; }

    int getScoreTeamOne() { return m_scoreTeamOne; }
    void setScoreTeamOne(int m_scoreTeamOne) { this.m_scoreTeamOne = m_scoreTeamOne; }

    int getScoreTeamTwo() { return m_scoreTeamTwo; }
    void setScoreTeamTwo(int m_scoreTeamTwo) { this.m_scoreTeamTwo = m_scoreTeamTwo; }

    Protocol.Trump getTrump() { return m_trump; }
    void setTrump(Protocol.Trump m_trump) { this.m_trump = m_trump; }

    Vector<Card> getTrick() { return m_trick; }
    public void setTrick(Vector<Card> m_trick) { this.m_trick = m_trick; }
    void setTrick(List<Protocol.player_state.Card> t_trick) {
        this.m_trick.clear();
        for (Protocol.player_state.Card it : t_trick)
            this.m_trick.add(protoCardConverter(it));
    }

    private void resetPlayers() {
        m_players.add(new Player("", false, false, new Auction(Protocol.Bet.FOLD, Protocol.Trump.NONE), false));
        m_players.add(new Player("", false, false, new Auction(Protocol.Bet.FOLD, Protocol.Trump.NONE), false));
        m_players.add(new Player("", false, false, new Auction(Protocol.Bet.FOLD, Protocol.Trump.NONE), false));
        m_players.add(new Player("", false, false, new Auction(Protocol.Bet.FOLD, Protocol.Trump.NONE), false));
    }

    void reset() {
        m_players = new Vector<>();
        resetPlayers();
        m_gameState = GameState.WAITING;
        m_hand = new Vector<>();
        m_position = 0;
        m_scoreTeamOne = 0;
        m_scoreTeamTwo = 0;
        m_starter = 0;
        m_trick = new Vector<>();
        m_trump = Protocol.Trump.NONE;
    }
}
