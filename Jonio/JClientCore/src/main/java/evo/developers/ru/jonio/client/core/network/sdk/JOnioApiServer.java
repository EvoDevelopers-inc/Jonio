package evo.developers.ru.jonio.client.core.network.sdk;

import com.google.gson.Gson;
import evo.developers.ru.jonio.client.core.dto.RequestAuthJwt;
import evo.developers.ru.jonio.client.core.dto.ResponseAuthJwt;
import evo.developers.ru.jonio.client.core.exceptions.JOnioClientSdkError;
import evo.developers.ru.jonio.client.core.helpers.httpclient.TorHttpClient;
import evo.developers.ru.jonio.client.core.model.Settings;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;

@AllArgsConstructor
public class JOnioApiServer {
    private Settings settings;
    private TorHttpClient torHttpClient;
    private final Gson gson = new Gson();

    public ResponseAuthJwt auth(RequestAuthJwt requestAuthJwt) {

        try{

            if (requestAuthJwt.getPubKeyBase64() == null || requestAuthJwt.getHashClient() == null){
                throw new JOnioClientSdkError("Missing required parameters");
            }

            String responseString = torHttpClient.post(settings.getUrlServer(), gson.toJson(requestAuthJwt));

            return gson.fromJson(responseString, ResponseAuthJwt.class);

        }catch (Exception e){
            throw new JOnioClientSdkError(e.getMessage());
        }
    }
}
