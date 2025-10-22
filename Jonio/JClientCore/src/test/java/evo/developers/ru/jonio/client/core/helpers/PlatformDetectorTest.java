package evo.developers.ru.jonio.client.core.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для PlatformDetector
 */
class PlatformDetectorTest {
    
    @Test
    void testDetectOS() {
        PlatformDetector.OS os = PlatformDetector.detectOS();
        assertNotNull(os);
        assertNotEquals(PlatformDetector.OS.UNKNOWN, os, 
            "OS should be detected on current platform");
        
        System.out.println("Detected OS: " + os.name());
    }
    
    @Test
    void testDetectArchitecture() {
        PlatformDetector.Architecture arch = PlatformDetector.detectArchitecture();
        assertNotNull(arch);
        assertNotEquals(PlatformDetector.Architecture.UNKNOWN, arch,
            "Architecture should be detected on current platform");
        
        System.out.println("Detected Architecture: " + arch.name());
    }
    
    @Test
    void testDetect() {
        PlatformDetector.PlatformInfo info = PlatformDetector.detect();
        
        assertNotNull(info);
        assertNotNull(info.getOs());
        assertNotNull(info.getArchitecture());
        assertNotNull(info.getFullPlatformString());
        
        System.out.println("Full platform info: " + info.toString());
    }
    
    @Test
    void testIsCurrentPlatformSupported() {
        boolean supported = PlatformDetector.isCurrentPlatformSupported();
        assertTrue(supported, "Current platform should be supported");
    }
    
    @Test
    void testGetPlatformDescription() {
        String description = PlatformDetector.getPlatformDescription();
        assertNotNull(description);
        assertFalse(description.isEmpty());
        
        System.out.println("Platform description: " + description);
    }
    
    @Test
    void testPlatformFlags() {
        // Хотя бы один из флагов должен быть true
        boolean isWindows = PlatformDetector.isWindows();
        boolean isMacOS = PlatformDetector.isMacOS();
        boolean isLinux = PlatformDetector.isLinux();
        
        assertTrue(isWindows || isMacOS || isLinux, 
            "At least one OS flag should be true");
        
        // Только один должен быть true
        int trueCount = (isWindows ? 1 : 0) + (isMacOS ? 1 : 0) + (isLinux ? 1 : 0);
        assertEquals(1, trueCount, "Exactly one OS flag should be true");
        
        System.out.println("Windows: " + isWindows);
        System.out.println("macOS: " + isMacOS);
        System.out.println("Linux: " + isLinux);
        System.out.println("ARM: " + PlatformDetector.isARM());
    }
    
    @Test
    void testPlatformInfoIsSupported() {
        PlatformDetector.PlatformInfo supported = new PlatformDetector.PlatformInfo(
            PlatformDetector.OS.LINUX, 
            PlatformDetector.Architecture.X86_64
        );
        assertTrue(supported.isSupported());
        
        PlatformDetector.PlatformInfo unsupported = new PlatformDetector.PlatformInfo(
            PlatformDetector.OS.UNKNOWN,
            PlatformDetector.Architecture.UNKNOWN
        );
        assertFalse(unsupported.isSupported());
    }
    
    @Test
    void testPlatformInfoToString() {
        PlatformDetector.PlatformInfo info = new PlatformDetector.PlatformInfo(
            PlatformDetector.OS.MACOS,
            PlatformDetector.Architecture.AARCH64
        );
        
        assertEquals("macos-aarch64", info.toString());
        assertEquals("macos-aarch64", info.getFullPlatformString());
    }
}

