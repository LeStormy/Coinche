
import java.util.*;

public class Deck {
    public class Card {
        public Card(int t_sym, int t_val) {
            m_sym = Protocol.Symbols.forNumber(t_sym);
            m_val = Protocol.Values.forNumber(t_val);
        }

        private int getAllTrumpCardPoints() {
            switch (m_val) {
                case V_SEVEN:
                    return 0;
                case V_EIGHT:
                    return 0;
                case V_NINE:
                    return 14;
                case V_TEN:
                    return 10;
                case V_JACK:
                    return 20;
                case V_QUEEN:
                    return 3;
                case V_KING:
                    return 4;
                case V_ACE:
                    return 11;
                default:
                    return 0;
            }
        }

        private int getWithoutTrumpCardPoints() {
            switch (m_val) {
                case V_SEVEN:
                    return 0;
                case V_EIGHT:
                    return 0;
                case V_NINE:
                    return 0;
                case V_TEN:
                    return 10;
                case V_JACK:
                    return 2;
                case V_QUEEN:
                    return 3;
                case V_KING:
                    return 4;
                case V_ACE:
                    return 11;
                default:
                    return 0;
            }
        }

        private int  getTrumpCardPoints(boolean t_isSymTrump) {
            if (t_isSymTrump)
                return getAllTrumpCardPoints();
            return getWithoutTrumpCardPoints();
        }

        public int getCardPoints(Protocol.Trump t_trump) {
            switch (t_trump) {
                case ALL:
                    return getAllTrumpCardPoints();
                case WITHOUT:
                    return getWithoutTrumpCardPoints();
                default:
                    return getTrumpCardPoints(m_sym.getNumber() == t_trump.getNumber());
            }
        }

        public Protocol.Symbols m_sym;
        public Protocol.Values m_val;
    }
    private Vector<Card> m_deck = new Vector<>();

    public Deck() {
        for (int sym = 0 ; sym < 4; ++sym)
            for (int val = 7; val < 15 ; ++val)
                m_deck.add(new Card(sym, val));
        Collections.shuffle(m_deck);
    }

    private void Cut() {
        Random randomGenerator = new Random();
        int cutIndex = randomGenerator.nextInt(m_deck.size() - 1) + 1;
        Vector<Card> tmp = new Vector<>();
        for (int i = cutIndex; i < m_deck.size(); ++i)
            tmp.add(m_deck.get(i));
        for (int i = 0; i < cutIndex; ++i)
            tmp.add(m_deck.get(i));
        m_deck = tmp;
    }

    private void Sort(Vector<Vector<Card>> t_hands) {
        for (Vector<Card> hand : t_hands) {
            hand.sort(Comparator.comparing(o -> o.m_val));
            hand.sort(Comparator.comparing(o -> o.m_sym));
        }
    }

    public Vector<Vector<Card>> Deal() {
        this.Cut();
        Vector<Vector<Card>> hands = new Vector<>();
        for (int i = 0 ; i < 4 ; ++i)
            hands.add(new Vector<>());
        hands.get(0).addAll(Arrays.asList(m_deck.get(0), m_deck.get(1), m_deck.get(2), m_deck.get(12),
                m_deck.get(13), m_deck.get(20), m_deck.get(21), m_deck.get(22)));
        hands.get(1).addAll(Arrays.asList(m_deck.get(3), m_deck.get(4), m_deck.get(5), m_deck.get(14),
                m_deck.get(15), m_deck.get(23), m_deck.get(24), m_deck.get(25)));
        hands.get(2).addAll(Arrays.asList(m_deck.get(6), m_deck.get(7), m_deck.get(8), m_deck.get(16),
                m_deck.get(17), m_deck.get(26), m_deck.get(27), m_deck.get(28)));
        hands.get(3).addAll(Arrays.asList(m_deck.get(9), m_deck.get(10), m_deck.get(11), m_deck.get(18),
                m_deck.get(19), m_deck.get(29), m_deck.get(30), m_deck.get(31)));
        Sort(hands);
        return hands;
    }
}
