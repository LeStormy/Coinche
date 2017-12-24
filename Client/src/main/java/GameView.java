
import javax.swing.*;
import java.awt.*;

public class GameView implements IView {

    GameWindow m_window;

    GameView(Runnable t_fct) {
        m_window = new GameWindow(t_fct);
    }

    public void popMessage(String t_message) {
        JOptionPane.showMessageDialog(null, t_message);
    }

    private void setConnectingWindow() {
        m_window.setTitle("JCoinche - Connecting");
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.setBackground(Color.white);

        JLabel label = new JLabel("Entrez votre nom :");
        panel.add(label);
        JTextField jtf = new JTextField("");
        jtf.setFont(new Font("Arial", Font.BOLD, 14));
        jtf.setPreferredSize(new Dimension(150, 30));
        jtf.setForeground(Color.BLACK);
        panel.add(jtf);

        JButton button = new JButton("Connect");
        panel.add(button);
        button.addActionListener(e -> Controller.write(PacketWrapper.buildIdentidicationPacket(jtf.getText())));

        m_window.setContentPane(panel);
        m_window.setVisible(true);
    }

    private void setWaitingWindow() {
        m_window.getContentPane().removeAll();
        m_window.getContentPane().update(m_window.getGraphics());
        m_window.setTitle("JCoinche - Waiting");
        m_window.setSize(640,410);
        m_window.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(new JLabel (new ImageIcon( "../Assets/waiting.gif")));

        m_window.setContentPane(panel);
        m_window.getContentPane().revalidate();
        m_window.getContentPane().repaint();
    }

    private JButton makeButton(String t_text, Runnable t_fct) {
        JButton button = new JButton(t_text);
        button.addActionListener(e -> t_fct.run());
        return button;
    }
    private JButton makeButton(String t_text, String path, Runnable t_fct) {
        JButton button = new JButton(t_text);
        button.setIcon(new ImageIcon(path));
        button.addActionListener(e -> t_fct.run());
        return button;
    }

    private JPanel makeAuctionButtons(Protocol.Trump t_trump, String t_path) {
        JPanel panel = new JPanel();

        panel.add(makeButton("80", () -> Controller.write(PacketWrapper.buildAuctionPacket(Protocol.Bet.EIGHTY, t_trump))));
        panel.add(makeButton("90", () -> Controller.write(PacketWrapper.buildAuctionPacket(Protocol.Bet.NINETY, t_trump))));
        panel.add(makeButton("100", () -> Controller.write(PacketWrapper.buildAuctionPacket(Protocol.Bet.HUNDRED, t_trump))));
        panel.add(makeButton("110", () -> Controller.write(PacketWrapper.buildAuctionPacket(Protocol.Bet.ONETEN, t_trump))));
        panel.add(makeButton("120", () -> Controller.write(PacketWrapper.buildAuctionPacket(Protocol.Bet.ONETWENTY, t_trump))));
        panel.add(makeButton("130", () -> Controller.write(PacketWrapper.buildAuctionPacket(Protocol.Bet.ONETHIRTY, t_trump))));
        panel.add(makeButton("140", () -> Controller.write(PacketWrapper.buildAuctionPacket(Protocol.Bet.ONEFORTY, t_trump))));
        panel.add(makeButton("150", () -> Controller.write(PacketWrapper.buildAuctionPacket(Protocol.Bet.ONEFIFTY, t_trump))));
        panel.add(makeButton("KPO", () -> Controller.write(PacketWrapper.buildAuctionPacket(Protocol.Bet.KPO, t_trump))));
        panel.add(new JLabel (new ImageIcon( t_path)));

        return panel;
    }

    private JPanel makePlayerPanel(GameModel t_gameModel, int t_index) {
        JPanel panel = new JPanel();

        panel.add(new JLabel (new ImageIcon( "../Assets/FRAME.png")));
        panel.add(new JLabel(t_gameModel.getPlayers().get((t_index + t_gameModel.getPosition()) % 4).getName()));
        if (t_gameModel.getPlayers().get((t_index + t_gameModel.getPosition()) % 4).isIsDealer())
            panel.add(new JLabel(new ImageIcon( "../Assets/DEALER.png")));
        if (t_gameModel.getPlayers().get((t_index + t_gameModel.getPosition()) % 4).isIsTurn())
            panel.add(new JLabel(new ImageIcon( "../Assets/TURN.png")));
        if (t_gameModel.getPlayers().get((t_index + t_gameModel.getPosition()) % 4).isIsTaker())
            panel.add(new JLabel(new ImageIcon( "../Assets/TAKER.png")));

        return panel;
    }

    private JPanel makeBetPanel(GameModel t_gameModel, int t_index) {
        JPanel panel = new JPanel();

        panel.add(new JLabel(new ImageIcon("../Assets/" + t_gameModel.getPlayers().get((t_index + t_gameModel.getPosition()) % 4).getAuction().getBet().toString() + ".png")));
        panel.add(new JLabel(new ImageIcon("../Assets/" + t_gameModel.getPlayers().get((t_index + t_gameModel.getPosition()) % 4).getAuction().getTrump().toString() + ".png")));

        return panel;
    }

    private JPanel makeSpecialPanel() {
        JPanel panel = new JPanel();

        panel.add(makeButton("COINCHE", () -> Controller.write(PacketWrapper.buildAuctionPacket(Protocol.Bet.COINCHE, Protocol.Trump.NONE))));
        panel.add(makeButton("SURCOINCHE", () -> Controller.write(PacketWrapper.buildAuctionPacket(Protocol.Bet.SURCOINCHE, Protocol.Trump.NONE))));
        panel.add(makeButton("FOLD", () -> Controller.write(PacketWrapper.buildAuctionPacket(Protocol.Bet.FOLD, Protocol.Trump.NONE))));

        return panel;
    }

    private void makeTrickPanel(GameModel t_gameModel, JPanel mainPanel, int t_index) {
        final int trickX[] = {820, 1250, 820, 390};
        final int trickY[] = {400, 200, 0, 200};
        JPanel panel = new JPanel();
        if (t_gameModel.getTrick().size() >= t_index + 1) {
            panel.add(new JLabel(new ImageIcon("../Assets/" + t_gameModel.getTrick().get(t_index).getSym().toString().substring(2) + t_gameModel.getTrick().get(t_index).getVal().getNumber() + ".png")));
            panel.setBounds(trickX[(t_index + 4 + t_gameModel.getStarter() - t_gameModel.getPosition()) % 4], trickY[(t_index + 4 + t_gameModel.getStarter() - t_gameModel.getPosition()) % 4], 165, 250);
            mainPanel.add(panel);
        }
    }

    private void makeHandPanel(JPanel t_handPanel, GameModel t_gameModel, int t_index) {
        if (t_gameModel.getHand().size() >= t_index + 1)
            t_handPanel.add(makeButton("", "../Assets/" + t_gameModel.getHand().get(t_index).getSym().toString().substring(2) + t_gameModel.getHand().get(t_index).getVal().getNumber() + ".png", () -> Controller.write(PacketWrapper.buildMovePacket(t_index))));
    }

    private void setAuctionWindow(GameModel t_gameModel) {
        m_window.getContentPane().removeAll();
        m_window.getContentPane().update(m_window.getGraphics());

        JPanel mainPanel = new JPanel();
        JPanel clubPanel = makeAuctionButtons(Protocol.Trump.CLUB, "../Assets/CLUB.png");
        JPanel heartPanel = makeAuctionButtons(Protocol.Trump.HEART, "../Assets/HEART.png");
        JPanel spadePanel = makeAuctionButtons(Protocol.Trump.SPADE, "../Assets/SPADE.png");
        JPanel diamondPanel = makeAuctionButtons(Protocol.Trump.DIAMOND, "../Assets/DIAMOND.png");
        JPanel allTrumpPanel = makeAuctionButtons(Protocol.Trump.ALL, "../Assets/ALL.png");
        JPanel noTrumpPanel = makeAuctionButtons(Protocol.Trump.WITHOUT, "../Assets/WITHOUT.png");
        JPanel specialPanel = makeSpecialPanel();
        JPanel playerPanel1 = makePlayerPanel(t_gameModel, 0);
        JPanel playerPanel2 = makePlayerPanel(t_gameModel, 1);
        JPanel playerPanel3 = makePlayerPanel(t_gameModel, 2);
        JPanel playerPanel4 = makePlayerPanel(t_gameModel, 3);
        JPanel playerBetPanel1 = makeBetPanel(t_gameModel, 0);
        JPanel playerBetPanel2 = makeBetPanel(t_gameModel, 1);
        JPanel playerBetPanel3 = makeBetPanel(t_gameModel, 2);
        JPanel playerBetPanel4 = makeBetPanel(t_gameModel, 3);
        mainPanel.setLayout(null);

        JPanel handPanel = new JPanel();
        for (GameModel.Card cd : t_gameModel.getHand()) {
            handPanel.add(new JLabel (new ImageIcon( "../Assets/" + cd.getSym().toString().substring(2) + cd.getVal().getNumber() + ".png")));
        }

        JPanel infoPanel = new JPanel();
        infoPanel.add(new JLabel("Your team : " + t_gameModel.getScoreTeamOne() + " - Their team : " + t_gameModel.getScoreTeamTwo() + ""));

        clubPanel.setBounds(700, 200, 700, 50);
        mainPanel.add(clubPanel);
        heartPanel.setBounds(700, 250, 700, 50);
        mainPanel.add(heartPanel);
        spadePanel.setBounds(700, 300, 700, 50);
        mainPanel.add(spadePanel);
        diamondPanel.setBounds(700, 350, 700, 50);
        mainPanel.add(diamondPanel);
        allTrumpPanel.setBounds(700, 400, 700, 50);
        mainPanel.add(allTrumpPanel);
        noTrumpPanel.setBounds(700, 450, 700, 50);
        mainPanel.add(noTrumpPanel);
        specialPanel.setBounds(700, 500, 700, 50);
        mainPanel.add(specialPanel);
        infoPanel.setBounds(700, 50, 700, 50);
        mainPanel.add(infoPanel);

        playerBetPanel1.setBounds(270, 510, 120, 50);
        mainPanel.add(playerBetPanel1);
        playerBetPanel2.setBounds(470, 300, 120, 50);
        mainPanel.add(playerBetPanel2);
        playerBetPanel3.setBounds(270, 100, 120, 50);
        mainPanel.add(playerBetPanel3);
        playerBetPanel4.setBounds(70, 300, 120, 50);
        mainPanel.add(playerBetPanel4);

        playerPanel1.setBounds(250, 400, 165, 300);
        mainPanel.add(playerPanel1);
        playerPanel2.setBounds(450, 200, 165, 300);
        mainPanel.add(playerPanel2);
        playerPanel3.setBounds(250, 0, 165, 300);
        mainPanel.add(playerPanel3);
        playerPanel4.setBounds(50, 200, 165, 300);
        mainPanel.add(playerPanel4);

        handPanel.setBounds(0, 700, 1400, 280);
        mainPanel.add(handPanel);

        m_window.setTitle("JCoinche");
        m_window.setSize(1400, 1000);
        m_window.setLocationRelativeTo(null);
        m_window.setContentPane(mainPanel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void setTricksWindow(GameModel t_gameModel) {
        m_window.getContentPane().removeAll();
        m_window.getContentPane().update(m_window.getGraphics());

        JPanel mainPanel = new JPanel();
        JPanel handPanel = new JPanel();
        JPanel playerPanel1 = makePlayerPanel(t_gameModel, 0);
        JPanel playerPanel2 = makePlayerPanel(t_gameModel, 1);
        JPanel playerPanel3 = makePlayerPanel(t_gameModel, 2);
        JPanel playerPanel4 = makePlayerPanel(t_gameModel, 3);
        mainPanel.setLayout(null);

//// TRICK //
        makeTrickPanel(t_gameModel, mainPanel, 0);
        makeTrickPanel(t_gameModel, mainPanel, 1);
        makeTrickPanel(t_gameModel, mainPanel, 2);
        makeTrickPanel(t_gameModel, mainPanel, 3);

//// PLAYER HAND //
        makeHandPanel(handPanel, t_gameModel, 0);
        makeHandPanel(handPanel, t_gameModel, 1);
        makeHandPanel(handPanel, t_gameModel, 2);
        makeHandPanel(handPanel, t_gameModel, 3);
        makeHandPanel(handPanel, t_gameModel, 4);
        makeHandPanel(handPanel, t_gameModel, 5);
        makeHandPanel(handPanel, t_gameModel, 6);
        makeHandPanel(handPanel, t_gameModel, 7);

        JPanel infoPanel = new JPanel();
        infoPanel.add(new JLabel("Your team : " + t_gameModel.getScoreTeamOne() + " - Their team : " + t_gameModel.getScoreTeamTwo()));

        JPanel auctionPanel = new JPanel();
        auctionPanel.add(new JLabel("Trump : "));
        auctionPanel.add(new JLabel(new ImageIcon( "../Assets/" + t_gameModel.getTrump().toString() + ".png")));

        playerPanel1.setBounds(820, 400, 165, 300);
        mainPanel.add(playerPanel1);
        playerPanel2.setBounds(1250, 200, 165, 300);
        mainPanel.add(playerPanel2);
        playerPanel3.setBounds(820, 0, 165, 300);
        mainPanel.add(playerPanel3);
        playerPanel4.setBounds(390, 200, 165, 300);
        mainPanel.add(playerPanel4);

        infoPanel.setBounds(1400, 50, 400, 50);
        mainPanel.add(infoPanel);
        auctionPanel.setBounds(1400, 100, 400, 50);
        mainPanel.add(auctionPanel);

        handPanel.setBounds(0, 700, 1800, 280);
        mainPanel.add(handPanel);

        m_window.setTitle("JCoinche"); //On lui donne un titre
        m_window.setSize(1800, 1000);//On lui donne une taille
        m_window.setLocationRelativeTo(null);
        m_window.setContentPane(mainPanel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void printView(GameModel t_gameModel) {
        switch (t_gameModel.getGameState()) {
            case CONNECTING:
                setConnectingWindow();
                break;
            case WAITING:
                setWaitingWindow();
                break;
            case AUCTION:
                setAuctionWindow(t_gameModel);
                break;
            case TRICKS:
                setTricksWindow(t_gameModel);
                break;
        }
    }



}
