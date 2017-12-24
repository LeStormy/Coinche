import io.netty.channel.socket.SocketChannel;

public class Controller {

    private IView m_gameView;
    private GameModel m_gameModel = new GameModel();
    static private SocketChannel m_socketChan;

    public void setSocketChan(SocketChannel t_socketChan) { Controller.m_socketChan = t_socketChan; }

    public void launchGame() {
        m_gameModel = new GameModel();
        m_gameView = new GameView(this::userQuit);

        m_gameView.printView(m_gameModel);
    }

    public static void write(Protocol.BaseCommand obj) { m_socketChan.writeAndFlush(obj); }

    public void run() throws InterruptedException {
        synchronized(this) {
            wait();
        }
    }

    public void userQuit() {
        System.out.println("This game was brought to you by La Pirogue du Fun! Hope you enjoyed it!\nGoodBye !");
        synchronized(this) {
            notify();
        }
    }

    public void updateModel(GameModel.GameState t_status) {
        if (t_status == GameModel.GameState.WAITING)
            m_gameModel.reset();
        m_gameModel.setGameState(t_status);
        m_gameView.printView(m_gameModel);
    }

    public void updateModel(Protocol.player_state obj) {
        m_gameModel.setHand(obj.getHandList());
        if (obj.getTrickList() != null)
            m_gameModel.setTrick(obj.getTrickList());
        m_gameModel.setScoreTeamOne(obj.getScoreOne());
        m_gameModel.setScoreTeamTwo(obj.getScoreTwo());
        for (GameModel.Player pl : m_gameModel.getPlayers()) {
            pl.setIsDealer(false);
            pl.setIsTurn(false);
            pl.setIsTaker(false);
        }
        m_gameModel.getPlayers().get(obj.getDealer().getNumber()).setIsDealer(true);
        m_gameModel.getPlayers().get(obj.getTurn().getNumber()).setIsTurn(true);
        m_gameModel.setTrump(obj.getTrump());
        m_gameModel.setStarter(obj.getStarter());
        if (obj.getTaker().getNumber() != -1)
            m_gameModel.getPlayers().get(obj.getTaker().getNumber()).setIsTaker(true);
        m_gameView.printView(m_gameModel);
    }

    public void updateModel(Protocol.static_game_state obj) {
        m_gameModel.setPlayersNames(obj.getPlayersNamesList());
        m_gameModel.setPosition(obj.getPosition());
        m_gameView.printView(m_gameModel);
    }

    public void updateModel(String t_message) {
        m_gameView.popMessage(t_message);
        m_gameView.printView(m_gameModel);
    }

    public void updateModel(Protocol.bet_state obj) {
        m_gameModel.getPlayers().get(obj.getBettingPlayer().getNumber()).getAuction().setBet(obj.getBet());
        m_gameModel.getPlayers().get(obj.getBettingPlayer().getNumber()).getAuction().setTrump(obj.getTrump());
        m_gameView.printView(m_gameModel);
    }
}
