package evo.developers.ru.jonio.client.core.network.p2p.connector.http;

import io.javalin.http.Context;

abstract public class JOnioAPI {

    protected void index(Context ctx)
    {
        ctx.result("It is tor connect " + System.currentTimeMillis());
    }
}
