# Использование Tor в JOnio Client

## Описание

Модуль `ClientTor` обеспечивает автоматическую инициализацию, запуск и управление Tor для всех поддерживаемых платформ.

## Поддерживаемые платформы

- **Linux**: x86_64, i686
- **macOS**: x86_64 (Intel), aarch64 (Apple Silicon)
- **Windows**: x86_64, i686

## Основные возможности

### 1. Автоматическое определение платформы
Система автоматически определяет ОС и архитектуру процессора, выбирая правильный бинарник Tor из ресурсов.

### 2. Распаковка в папку сессии
Tor распаковывается в подпапку `tor` внутри папки сессии, указанной в настройках.

### 3. Управление процессом Tor
- Запуск/остановка процесса
- Подключение к control port
- Получение новой идентичности (смена IP)
- Проверка статуса

## Примеры использования

### Базовое использование

```java
import evo.developers.ru.jonio.client.core.tor.ClientTor;
import java.nio.file.Paths;

// Создаем экземпляр
ClientTor torClient = new ClientTor();

// Инициализируем (распаковываем бинарник)
Path sessionFolder = Paths.get(System.getProperty("user.home"), ".jonio", "session");
torClient.initTorBin(sessionFolder);

// Запускаем Tor
torClient.start();

// Подключаемся к контроллеру
torClient.connect();

// Используем SOCKS прокси
System.out.println("SOCKS proxy: 127.0.0.1:" + torClient.getSocksPort());

// Получаем новую идентичность (новый IP)
torClient.newIdentity();

// Останавливаем
torClient.stop();
```

### Использование через JOnioClient

```java
import evo.developers.ru.jonio.client.core.JOnioClient;
import evo.developers.ru.jonio.client.core.model.Settings;

// Создаем настройки
Settings settings = new Settings("/path/to/session");

// Создаем клиент
JOnioClient client = new JOnioClient(settings);

// Инициализируем (включая Tor)
client.initialize();

// Получаем доступ к Tor
ClientTor torClient = client.getTorClient();
System.out.println("SOCKS: " + torClient.getSocksPort());

// Завершаем работу
client.shutdown();
```

### HTTP запросы через Tor

```java
import evo.developers.ru.jonio.client.core.helpers.httpclient.TorHttpClient;

// Создаем HTTP клиент для работы через Tor
TorHttpClient httpClient = new TorHttpClient("127.0.0.1", torClient.getSocksPort());

// Проверяем IP
String ipInfo = httpClient.checkIP();
System.out.println("Current IP: " + ipInfo);

// Проверяем, работает ли Tor
boolean isWorking = httpClient.isTorWorking();
System.out.println("Tor is working: " + isWorking);

// Выполняем GET запрос
String response = httpClient.get("https://example.com");
```

## Конфигурация Tor

Параметры по умолчанию:
- **SOCKS порт**: 9050
- **Control порт**: 9051
- **Cookie Authentication**: включена

## Структура файлов

После инициализации в папке сессии создается следующая структура:

```
session/
├── tor/
│   ├── tor/              # Бинарники Tor
│   │   ├── tor           # (или tor.exe на Windows)
│   │   └── ...
│   ├── data/             # Данные Tor (consensus, keys, etc.)
│   └── torrc             # Файл конфигурации
└── jo-session.json       # Данные сессии приложения
```

## API ClientTor

### Основные методы

- `initTorBin(Path sessionFolder)` - инициализирует и распаковывает Tor
- `start()` - запускает процесс Tor
- `connect()` - подключается к control port
- `stop()` - останавливает Tor
- `newIdentity()` - получает новую идентичность (новый IP)
- `isRunning()` - проверяет, запущен ли Tor
- `getSocksPort()` - возвращает порт SOCKS прокси
- `getControlPort()` - возвращает порт управления

## Логирование

Модуль использует SLF4J для логирования. Настройте `logback.xml` для управления уровнем детализации:

```xml
<logger name="evo.developers.ru.jonio.client.core.tor" level="INFO"/>
```

## Обработка ошибок

```java
try {
    torClient.initTorBin(sessionFolder);
    torClient.start();
} catch (IOException e) {
    System.err.println("Failed to initialize Tor: " + e.getMessage());
}
```

## Зависимости

Модуль использует следующие библиотеки:
- `net.freehaven.tor.control:jtorctl:0.4` - для управления Tor
- `org.apache.commons:commons-compress:1.24.0` - для распаковки tar.gz

## Примечания

1. При первом запуске Tor может занять до 30 секунд для установки соединения с сетью
2. Бинарники Tor версии 14.5.8 встроены в JAR-файл
3. Повторная инициализация пропускается, если Tor уже распакован
4. SOCKS прокси доступен на `127.0.0.1:9050` после запуска




