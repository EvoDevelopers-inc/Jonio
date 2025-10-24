import evo.developers.ru.jonio.client.core.JOnioClient;
import evo.developers.ru.jonio.client.core.model.Settings;
import evo.developers.ru.jonio.client.core.tor.ClientTor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {

        Settings settings = new Settings("./session");
        JOnioClient.init(settings);
        JOnioClient.getInstance().initialize();





    }
}
