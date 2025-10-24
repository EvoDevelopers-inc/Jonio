package evo.developers.ru.jonio.client.core.network.p2p.connector;

import com.google.gson.Gson;
import evo.developers.ru.jonio.client.core.network.p2p.connector.http.JOnioAPI;
import evo.developers.ru.jonio.client.core.tor.ClientTor;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

@Slf4j
public class JOnioConnector extends JOnioAPI {

    private static JOnioConnector instance;

    public static JOnioConnector getInstance(ClientTor tor) throws IOException {
        if (instance == null)
            instance = new JOnioConnector(tor);

        return instance;
    }

    @Getter
    private Javalin app;
    
    @Getter
    private ClientTor clientTor;

    private JOnioConnector(ClientTor clientTor) {
        this.clientTor = clientTor;

        Gson gson = new Gson();
        
        app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            // Настраиваем Gson как JSON mapper
            config.jsonMapper(new JsonMapper() {
                @NotNull
                @Override
                public String toJsonString(@NotNull Object obj, @NotNull Type type) {
                    return gson.toJson(obj, type);
                }

                @NotNull
                @Override
                public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
                    return gson.fromJson(json, targetType);
                }
            });
        }).start(clientTor.getSOCKS_PROXY_HOST(), 8080);
        

        app.get("/", this::index);
        app.post("/connect", this::handleConnect);
        app.post("/message", this::handleMessage);
        app.get("/peers", this::listPeers);
        
        log.info("Javalin server started on {}:8080", clientTor.getSOCKS_PROXY_HOST());
    }

    public void stop() {
        if (app != null) {
            app.stop();
            log.info("Javalin server stopped");
        }
    }
}
