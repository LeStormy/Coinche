
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameWindow extends JFrame {

    private Runnable m_fct;

    GameWindow(Runnable t_fct){
        super();
        m_fct = t_fct;
        build();
    }

    private void build(){
        setTitle("JCoinche");
        setSize(320,240);
        setLocationRelativeTo(null);
        setResizable(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
       addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                Controller.write(PacketWrapper.buildDisconnectPacket());
                m_fct.run();
            }
        });
    }
}
