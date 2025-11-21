package evo.developers.ru.jonio.client.core.base;

public interface IClientHasher {
    String computeClientHash(String login, String password);
}
