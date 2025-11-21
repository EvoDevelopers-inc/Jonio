package evo.developers.ru.jonio.client.core.base;

import com.google.gson.Gson;
import evo.developers.ru.jonio.client.core.JOnioClient;
import evo.developers.ru.jonio.client.core.exceptions.JOnioClientExists;
import evo.developers.ru.jonio.client.core.exceptions.JOnioClientNotInit;
import evo.developers.ru.jonio.client.core.model.Settings;
import lombok.Getter;
import lombok.Setter;

abstract public class JClient {
    private static JClient jonioClient;
    private static final Gson gsonHelper = new Gson();

    @Setter
    @Getter
    private IJwkPairFactory jwkPairFactory;

    @Setter
    @Getter
    private IClientHasher clientHasher;

    public static JOnioClient getInstance(){

        if(jonioClient == null)
            throw new JOnioClientNotInit();

        return (JOnioClient) jonioClient;
    }

    public static JOnioClient init(Settings settings)
    {
        if (jonioClient != null)
            throw new JOnioClientExists();

        return (JOnioClient) (jonioClient = new JOnioClient(settings));

    }

    public static boolean unInit(){
        if (jonioClient == null)
            throw new JOnioClientNotInit();

        jonioClient = null;

        return true; // TODO remove log and auth jwt client
    }

    protected String toJson(Object object){
        return gsonHelper.toJson(object);
    }

    protected <T> T fromJson(String json, Class<T> classOfT){
        return gsonHelper.fromJson(json, classOfT);
    }
}
