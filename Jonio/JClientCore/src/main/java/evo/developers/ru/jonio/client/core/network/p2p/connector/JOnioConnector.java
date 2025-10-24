package evo.developers.ru.jonio.client.core.network.p2p.connector;

import evo.developers.ru.jonio.client.core.tor.ClientTor;
import evo.developers.ru.jonio.client.core.tor.TorControlConnection;
import io.javalin.Javalin;

import java.io.IOException;

public class JOnioConnector extends JOnioAPI {

    private static JOnioConnector instance;

    public static JOnioConnector getInstance(ClientTor tor) throws IOException {
        if (instance == null)
            instance = new JOnioConnector(tor);

        return instance;
    }

    private Javalin app;
    private ClientTor clientTor;

    private JOnioConnector(ClientTor clientTor) {

        this.clientTor = clientTor;

        app = Javalin.create().start(clientTor.getSOCKS_PROXY_HOST(), 8080);
        app.get("/", this::index);
    }


}
