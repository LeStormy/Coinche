
import java.util.Vector;

public class ScoreCounter {

    private int m_whoWin;
    private int m_trickScore = 0;

    public int getTrickScore() { return m_trickScore; }
    public int getWinnerIndex() {
        return m_whoWin;
    }

    private int getAllTrumpCount(Vector<Deck.Card> t_trick, int t_trickNb, int t_whoPlay) {
        int count = (t_trickNb == 7) ? 10 : 0;

        for (Deck.Card cd : t_trick)
            count += cd.getCardPoints(Protocol.Trump.ALL);
        return count;
    }

    private int getWithoutTrumpCount(Vector<Deck.Card> t_trick, int t_trickNb, int t_whoPlay) {
        int count = (t_trickNb == 7) ? 10 : 0;

        for (Deck.Card cd : t_trick)
            count += cd.getCardPoints(Protocol.Trump.WITHOUT);
        return count;
    }

    private int getTrumpCount(Protocol.Trump t_trickTrump, Vector<Deck.Card> t_trick, int t_trickNb, int t_whoPlay) {
        int count = (t_trickNb == 7) ? 10 : 0;

        for (Deck.Card cd : t_trick)
            count += cd.getCardPoints(t_trickTrump);
        return count;
    }

    private int getPointsEarned(Protocol.Trump t_trickTrump, Vector<Deck.Card> t_trick,
                                int t_trickNb, int t_whoPlay)
    {
        switch (t_trickTrump) {
            case ALL:
                return getAllTrumpCount(t_trick, t_trickNb, t_whoPlay);
            case WITHOUT:
                return getWithoutTrumpCount(t_trick, t_trickNb, t_whoPlay);
            default:
                 return getTrumpCount(t_trickTrump, t_trick, t_trickNb, t_whoPlay);
        }
    }

    public ScoreCounter(Round t_round, int t_whoPlay, Vector<Player> t_players,
                        Vector<Deck.Card> t_trick, int t_trickNb) {
        m_whoWin = getWinner(t_round.getTrump(), t_trick, t_whoPlay);
        m_trickScore = getPointsEarned(t_round.getTrump(), t_trick, t_trickNb, t_whoPlay);
        t_players.get(m_whoWin).setScore(m_trickScore);
    }

    private int getWinner(Protocol.Trump t_trickTrump, Vector<Deck.Card> t_trick, int t_whoPlay) {
        if (t_trickTrump == Protocol.Trump.ALL || t_trickTrump == Protocol.Trump.WITHOUT)
            return getStrongestAskedColorCard(t_trickTrump, t_trick, t_whoPlay);
        else
        {
            if (isAnyTrumpOnTrick(t_trickTrump, t_trick)) {
                return getStrongestTrumpOnTrick(t_trick, t_trickTrump, t_whoPlay);
            }
            else {
                return getStrongestAskedColorCard(t_trickTrump, t_trick, t_whoPlay);
            }
        }
    }

    private boolean isAnyTrumpOnTrick(Protocol.Trump t_trickTrump, Vector<Deck.Card> t_trick) {
        for (Deck.Card cd : t_trick)
            if (cd.m_sym.getNumber() == t_trickTrump.getNumber())
                return true;
        return false;
    }

    private int getStrongestAskedColorCard(Protocol.Trump t_trickTrump, Vector<Deck.Card> t_trick, int t_whoPlay) {
        Protocol.Symbols askedColor = t_trick.get(0).m_sym;
        int valTmp = t_trick.get(0).getCardPoints(t_trickTrump);
        int i = 0;
        int winnerTmp = 0;

        for (Deck.Card cd : t_trick) {
            if (cd.m_sym == askedColor && cd.getCardPoints(t_trickTrump) > valTmp) {
                valTmp = cd.getCardPoints(t_trickTrump);
                winnerTmp = i;
            }
            ++i;
        }
        return ((winnerTmp + t_whoPlay) % 4);
    }

    private int getStrongestTrumpOnTrick(Vector<Deck.Card> t_trick, Protocol.Trump t_trickTrump, int t_whoPlay) {
        int valTmp= 0;
        int i = 0;
        int winnerTmp = 0;

        for (Deck.Card cd : t_trick) {
            if (cd.m_sym.getNumber() == t_trickTrump.getNumber() && cd.getCardPoints(t_trickTrump) > valTmp) {
                valTmp = cd.getCardPoints(t_trickTrump);
                winnerTmp = i;
            }
            ++i;
        }
        return ((winnerTmp + t_whoPlay) % 4);
    }
}
