# Реализация интеграции Tor в JOnio Client

## Обзор

Была реализована полная интеграция Tor в JOnio Client с автоматическим определением платформы, распаковкой бинарников и управлением через библиотеку jtorctl.

## Реализованные компоненты

### 1. ClientTor (`evo.developers.ru.jonio.client.core.tor.ClientTor`)

Основной класс для работы с Tor, который обеспечивает:

**Функционал:**
- ✅ Автоматическое определение ОС и архитектуры процессора
- ✅ Выбор правильного бинарника из ресурсов (tar.gz архивы)
- ✅ Распаковка в папку сессии (`sessionFolder/tor/`)
- ✅ Установка прав на исполнение (Unix-системы)
- ✅ Запуск и остановка процесса Tor
- ✅ Подключение к control port через jtorctl
- ✅ Получение новой идентичности (смена IP)
- ✅ Проверка статуса работы
- ✅ Настраиваемые SOCKS и Control порты

**Методы:**
```java
void initTorBin(Path sessionFolder)  // Инициализация и распаковка
void start()                         // Запуск Tor
void connect()                       // Подключение к контроллеру
void stop()                          // Остановка Tor
void newIdentity()                   // Новая идентичность
boolean isRunning()                  // Проверка статуса
int getSocksPort()                   // Получить SOCKS порт (9050)
int getControlPort()                 // Получить Control порт (9051)
```

### 2. PlatformDetector (`evo.developers.ru.jonio.client.core.helpers.PlatformDetector`)

Утилитный класс для определения платформы:

**Функционал:**
- ✅ Определение ОС (Windows, macOS, Linux)
- ✅ Определение архитектуры (x86_64, i686, aarch64)
- ✅ Проверка поддержки платформы
- ✅ Человекочитаемое описание платформы

**Поддерживаемые платформы:**
- Linux: x86_64, i686
- macOS: x86_64 (Intel), aarch64 (Apple Silicon)
- Windows: x86_64, i686

**API:**
```java
OS detectOS()
Architecture detectArchitecture()
PlatformInfo detect()
boolean isCurrentPlatformSupported()
String getPlatformDescription()
boolean isWindows()
boolean isMacOS()
boolean isLinux()
boolean isARM()
```

### 3. TorHttpClient (`evo.developers.ru.jonio.client.core.helpers.httpclient.TorHttpClient`)

HTTP клиент для работы через Tor SOCKS прокси:

**Функционал:**
- ✅ Выполнение HTTP GET запросов через Tor
- ✅ Проверка текущего IP адреса
- ✅ Проверка работоспособности Tor

**Методы:**
```java
String get(String url)        // GET запрос через Tor
String checkIP()              // Проверка IP через check.torproject.org
boolean isTorWorking()        // Проверка работы Tor
```

### 4. Интеграция с JOnioClient

`JOnioClient` был расширен методами:

```java
void initialize()      // Инициализирует клиент + Tor
void shutdown()        // Останавливает клиент + Tor
ClientTor getTorClient() // Получить экземпляр ClientTor
```

## Зависимости

Добавлены в `build.gradle.kts`:

```kotlin
implementation("net.freehaven.tor.control:jtorctl:0.4")           // Управление Tor
implementation("org.apache.commons:commons-compress:1.24.0")      // Распаковка tar.gz
```

## Структура файлов после инициализации

```
sessionFolder/
├── tor/
│   ├── tor/              # Распакованные бинарники Tor
│   │   ├── tor           # Основной бинарник (или tor.exe на Windows)
│   │   └── ...           # Другие файлы из bundle
│   ├── data/             # Данные Tor (consensus, keys, state)
│   └── torrc             # Конфигурационный файл
└── jo-session.json       # Сессия приложения
```

## Конфигурация Tor

По умолчанию создается `torrc` со следующими параметрами:

```
SocksPort 9050
ControlPort 9051
HashedControlPassword [hash]
DataDirectory [path]/tor/data
CookieAuthentication 1
```

## Примеры использования

### Базовый пример

```java
ClientTor torClient = new ClientTor();
Path sessionFolder = Paths.get(System.getProperty("user.home"), ".jonio", "session");

torClient.initTorBin(sessionFolder);
torClient.start();
torClient.connect();

System.out.println("SOCKS: 127.0.0.1:" + torClient.getSocksPort());

torClient.newIdentity();  // Новый IP

torClient.stop();
```

### Через JOnioClient

```java
Settings settings = new Settings("/path/to/session");
JOnioClient client = new JOnioClient(settings);

client.initialize();  // Инициализирует всё, включая Tor

ClientTor torClient = client.getTorClient();
// Используем Tor...

client.shutdown();
```

### HTTP через Tor

```java
TorHttpClient httpClient = new TorHttpClient("127.0.0.1", torClient.getSocksPort());

String ipInfo = httpClient.checkIP();
boolean isWorking = httpClient.isTorWorking();
String response = httpClient.get("https://example.com");
```

## Тесты

Созданы unit-тесты:

1. **ClientTorTest** - тесты для основного функционала Tor
2. **PlatformDetectorTest** - тесты определения платформы

Запуск тестов:
```bash
./gradlew test
```

## Примеры кода

Создан класс `TorExample` с различными сценариями использования:
- Базовая инициализация
- HTTP запросы через Tor
- Смена идентичности
- Обработка ошибок
- Полный жизненный цикл

## Логирование

Все компоненты используют SLF4J для логирования.

Конфигурация в `logback.xml`:
```xml
<logger name="evo.developers.ru.jonio.client.core.tor" level="INFO"/>
```

Уровни логирования:
- INFO: основные операции (запуск, остановка, смена IP)
- DEBUG: детальный вывод Tor
- ERROR: ошибки

## Безопасность

- Cookie authentication включена по умолчанию
- Пароль control port настраивается (по умолчанию хешированный)
- Tor запускается локально только для текущего пользователя
- DataDirectory изолирована в папке сессии

## Производительность

- Первая инициализация: ~2-5 секунд (распаковка)
- Последующие запуски: используется уже распакованный Tor
- Запуск Tor: ~5-30 секунд (до Bootstrapped 100%)
- Смена идентичности: ~10 секунд

## Ограничения

- Требуется около 50-100 MB для распакованного Tor
- Tor bundle версии 14.5.8 (можно обновить через константу TOR_VERSION)
- SOCKS прокси по умолчанию на 127.0.0.1:9050
- Control port по умолчанию на 127.0.0.1:9051

## Дальнейшие улучшения

Возможные направления развития:

1. **Bridge support** - поддержка мостов для обхода блокировок
2. **Circuit control** - управление цепями Tor
3. **Hidden services** - создание onion сервисов
4. **Bandwidth monitoring** - мониторинг трафика
5. **Auto-update** - автоматическое обновление Tor
6. **Multiple instances** - запуск нескольких экземпляров Tor
7. **Custom exit nodes** - выбор exit нод

## Ресурсы

- [Tor Project](https://www.torproject.org/)
- [jtorctl documentation](https://github.com/guardianproject/jtorctl)
- [Tor Control Protocol](https://gitweb.torproject.org/torspec.git/tree/control-spec.txt)

## Автор реализации

Реализовано для проекта Jonio Client
Дата: 2025-10-22




