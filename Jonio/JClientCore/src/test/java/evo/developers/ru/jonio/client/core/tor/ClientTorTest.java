package evo.developers.ru.jonio.client.core.tor;

import evo.developers.ru.jonio.client.core.helpers.PlatformDetector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ClientTor
 */
class ClientTorTest {
    
    private ClientTor torClient;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        torClient = new ClientTor();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (torClient != null && torClient.isRunning()) {
            torClient.stop();
        }
    }
    
    @Test
    void testPlatformDetection() {
        PlatformDetector.PlatformInfo info = PlatformDetector.detect();
        assertNotNull(info);
        assertTrue(info.isSupported(), "Platform should be supported");
        
        System.out.println("Detected platform: " + PlatformDetector.getPlatformDescription());
    }
    
    @Test
    void testTorInitialization() throws Exception {
        // Инициализируем Tor
        torClient.initTorBin(tempDir);
        
        // Проверяем, что флаг initialized установлен
        assertTrue(torClient.isInitialized(), "Tor should be initialized");
        
        // Проверяем, что директория создана
        assertNotNull(torClient.getTorDirectory());
        assertTrue(Files.exists(torClient.getTorDirectory()), "Tor directory should exist");
        
        // Проверяем, что бинарник существует
        Path torBinary = torClient.getTorDirectory().resolve("tor/tor");
        if (PlatformDetector.isWindows()) {
            torBinary = torClient.getTorDirectory().resolve("tor/tor.exe");
        }
        assertTrue(Files.exists(torBinary), "Tor binary should exist");
    }
    
    @Test
    void testTorNotInitializedError() {
        // Попытка запустить Tor без инициализации должна бросить исключение
        assertThrows(IllegalStateException.class, () -> {
            torClient.start();
        });
    }
    
    @Test
    void testDoubleInitialization() throws Exception {
        // Первая инициализация
        torClient.initTorBin(tempDir);
        assertTrue(torClient.isInitialized());
        
        // Вторая инициализация не должна вызвать ошибку
        assertDoesNotThrow(() -> torClient.initTorBin(tempDir));
        assertTrue(torClient.isInitialized());
    }
    
    @Test
    void testGettersSetters() {
        assertEquals(9050, torClient.getSocksPort());
        assertEquals(9051, torClient.getControlPort());
        
        torClient.setSocksPort(9150);
        torClient.setControlPort(9151);
        
        assertEquals(9150, torClient.getSocksPort());
        assertEquals(9151, torClient.getControlPort());
    }
    
    @Test
    void testIsRunning() {
        assertFalse(torClient.isRunning(), "Tor should not be running initially");
    }
    
    // Этот тест можно раскомментировать для полного интеграционного тестирования
    // Он требует больше времени и реальных ресурсов
    /*
    @Test
    void testFullTorLifecycle() throws Exception {
        // Инициализируем
        torClient.initTorBin(tempDir);
        assertTrue(torClient.isInitialized());
        
        // Запускаем
        torClient.start();
        Thread.sleep(10000); // Ждем запуска Tor
        assertTrue(torClient.isRunning());
        
        // Подключаемся
        torClient.connect();
        
        // Получаем новую идентичность
        assertDoesNotThrow(() -> torClient.newIdentity());
        
        // Останавливаем
        torClient.stop();
        assertFalse(torClient.isRunning());
    }
    */
}




