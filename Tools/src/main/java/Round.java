
public class Round {
    private Protocol.Bet m_bet;
    private Protocol.Trump m_trump;
    private Player.Team m_takers;
    private Protocol.Players m_taker;
    private Protocol.Bet m_coinche;

    public Protocol.Bet getCoinche() {
        return m_coinche;
    }
    public void setCoinche(Protocol.Bet t_coinche) {
        this.m_coinche = t_coinche;
    }


    public Round(Protocol.Bet t_bet, Protocol.Trump t_trump, Player.Team t_takers, Protocol.Bet t_coinche, Protocol.Players t_taker) {
        this.m_bet = t_bet;
        this.m_trump = t_trump;
        this.m_takers = t_takers;
        this.m_coinche = t_coinche;
        this.m_taker = t_taker;
    }

    public Protocol.Bet getBet() { return m_bet; }
    public void setBet(Protocol.Bet t_bet) { this.m_bet = t_bet; }

    public Protocol.Trump getTrump() { return m_trump; }
    public void setTrump(Protocol.Trump t_trump) { this.m_trump = t_trump; }

    public Player.Team getTakers() { return m_takers; }
    public void setTakers(Player.Team t_takers) { this.m_takers = t_takers; }

    public Protocol.Players getTaker() { return m_taker; }
    public void setTaker(Protocol.Players t_taker) { this.m_taker = t_taker; }
}
