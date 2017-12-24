
import com.google.protobuf.GeneratedMessage;

import java.util.UUID;
import java.util.Vector;

public class PacketWrapper {
    static <Type> Protocol.BaseCommand wrap(Protocol.BaseCommand.CommandType type, GeneratedMessage.GeneratedExtension<Protocol.BaseCommand, Type> extension, Type cmd) {
        return Protocol.BaseCommand.newBuilder().setType(type).setExtension(extension, cmd).build();
    }

    static public Protocol.BaseCommand buildAuctionPacket(Protocol.Bet t_bet, Protocol.Trump t_trump) {
        Protocol.Auction mode = Protocol.Auction.newBuilder().setBet(t_bet).setSymbol(t_trump).build();
        return wrap(Protocol.BaseCommand.CommandType.AUCTION, Protocol.Auction.cmd, mode);
    }

    static public Protocol.BaseCommand buildServRespPacket(Protocol.ServResponse.RespType t_resp, String t_message) {
        Protocol.ServResponse.Builder mode = Protocol.ServResponse.newBuilder().setRType(t_resp);
        if (t_message != null)
            mode.setMessage(t_message);
        return wrap(Protocol.BaseCommand.CommandType.SERV_RESP, Protocol.ServResponse.cmd, mode.build());
    }

    static public Protocol.BaseCommand buildMovePacket(int t_index) {
        Protocol.Move mode = Protocol.Move.newBuilder().setIndex(t_index).build();
        return wrap(Protocol.BaseCommand.CommandType.MOVE, Protocol.Move.cmd, mode);
    }

    static public Protocol.BaseCommand buildIdentidicationPacket(String name) {
        Protocol.player_identification mode = Protocol.player_identification.newBuilder().setName(name).setMAC(UUID.randomUUID().toString()).build();
        return wrap(Protocol.BaseCommand.CommandType.PLAYER_IDENTIFICATION, Protocol.player_identification.cmd, mode);
    }

    static public Protocol.BaseCommand buildBetStatePacket(Protocol.Trump t_trump, Protocol.Bet t_bet, Protocol.Players t_player) {
        Protocol.bet_state mode = Protocol.bet_state.newBuilder().setTrump(t_trump).setBet(t_bet).setBettingPlayer(t_player).build();
        return wrap(Protocol.BaseCommand.CommandType.BET_STATE, Protocol.bet_state.cmd, mode);
    }

    static public Protocol.BaseCommand buildDisconnectPacket() {
        Protocol.Disconnect mode = Protocol.Disconnect.newBuilder().build();
        return wrap(Protocol.BaseCommand.CommandType.DISCONNECT, Protocol.Disconnect.cmd, mode);
    }

    static private Protocol.player_state.Card buildCard(Deck.Card cd) {
        return Protocol.player_state.Card.newBuilder().setSym(cd.m_sym).setVal(cd.m_val).build();
    }

    static public Protocol.BaseCommand buildGameStatePacket(String[] t_names, int t_pos) {
        Protocol.static_game_state.Builder mode = Protocol.static_game_state.newBuilder();
        for (String name : t_names)
            mode.addPlayersNames(name);
        mode.setPosition(t_pos);

        return wrap(Protocol.BaseCommand.CommandType.STATIC_GAME_STATE, Protocol.static_game_state.cmd, mode.build());
    }

    static public Protocol.BaseCommand buildPlayerStatePacket(Vector<Deck.Card> t_hand, int t_scoreOne, int t_scoreTwo, Trick t_trick,
                                                              Protocol.Players t_dealer, Protocol.Players t_taker, Protocol.Players t_turn,
                                                              Protocol.Trump t_trump, int t_starter) {
        Protocol.player_state.Builder mode = Protocol.player_state.newBuilder();
        for (Deck.Card cd : t_hand) {
            mode.addHand(buildCard(cd));
        }
        if (t_trick != null)
            for (int i = 0 ; i < t_trick.getTrick().size(); ++i)
                 mode.addTrick(buildCard(t_trick.getTrick().get(i)));
        mode.setScoreOne(t_scoreOne).setScoreTwo(t_scoreTwo).setDealer(t_dealer).setTaker(t_taker).setTurn(t_turn).setTrump(t_trump).setStarter(t_starter);

        return wrap(Protocol.BaseCommand.CommandType.PLAYER_STATE, Protocol.player_state.cmd, mode.build());
    }

    static public Protocol.BaseCommand buildSwitchModePacket(Protocol.switch_mode.GameMode t_mode) {
        Protocol.switch_mode mode = Protocol.switch_mode.newBuilder().setMode(t_mode).build();
        return wrap(Protocol.BaseCommand.CommandType.SWITCH_MODE, Protocol.switch_mode.cmd, mode);
    }

    static public Protocol.BaseCommand buildRequestPacket(Protocol.RequestType t_mode) {
        Protocol.request mode = Protocol.request.newBuilder().setRequestT(t_mode).build();
        return wrap(Protocol.BaseCommand.CommandType.REQUEST, Protocol.request.cmd, mode);
    }

}
