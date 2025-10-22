package evo.developers.ru.jonio.client.core.helpers;

import lombok.Getter;

/**
 * Утилита для определения платформы и архитектуры системы
 */
public class PlatformDetector {
    
    @Getter
    public enum OS {
        WINDOWS("windows"),
        LINUX("linux"),
        MACOS("macos"),
        UNKNOWN("unknown");
        
        private final String platformName;
        
        OS(String platformName) {
            this.platformName = platformName;
        }
    }
    
    @Getter
    public enum Architecture {
        X86_64("x86_64"),
        I686("i686"),
        AARCH64("aarch64"),
        UNKNOWN("unknown");
        
        private final String archName;
        
        Architecture(String archName) {
            this.archName = archName;
        }
    }
    
    @Getter
    public static class PlatformInfo {
        private final OS os;
        private final Architecture architecture;
        private final String fullPlatformString;
        
        public PlatformInfo(OS os, Architecture architecture) {
            this.os = os;
            this.architecture = architecture;
            this.fullPlatformString = os.getPlatformName() + "-" + architecture.getArchName();
        }
        
        public boolean isSupported() {
            return os != OS.UNKNOWN && architecture != Architecture.UNKNOWN;
        }
        
        @Override
        public String toString() {
            return fullPlatformString;
        }
    }
    
    /**
     * Определяет текущую операционную систему
     */
    public static OS detectOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("win")) {
            return OS.WINDOWS;
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            return OS.MACOS;
        } else if (osName.contains("nux") || osName.contains("nix") || osName.contains("aix")) {
            return OS.LINUX;
        }
        
        return OS.UNKNOWN;
    }
    
    /**
     * Определяет архитектуру процессора
     */
    public static Architecture detectArchitecture() {
        String arch = System.getProperty("os.arch").toLowerCase();
        
        if (arch.contains("aarch64") || arch.contains("arm64")) {
            return Architecture.AARCH64;
        } else if (arch.contains("amd64") || arch.contains("x86_64")) {
            return Architecture.X86_64;
        } else if (arch.contains("x86") || arch.contains("i386") || arch.contains("i686")) {
            return Architecture.I686;
        }
        
        return Architecture.UNKNOWN;
    }
    
    /**
     * Получает полную информацию о платформе
     */
    public static PlatformInfo detect() {
        return new PlatformInfo(detectOS(), detectArchitecture());
    }
    
    /**
     * Проверяет, поддерживается ли текущая платформа
     */
    public static boolean isCurrentPlatformSupported() {
        PlatformInfo info = detect();
        return info.isSupported();
    }
    
    /**
     * Возвращает человекочитаемое описание платформы
     */
    public static String getPlatformDescription() {
        PlatformInfo info = detect();
        return String.format("%s %s (%s)", 
            info.getOs().name(), 
            info.getArchitecture().name(),
            info.getFullPlatformString());
    }
    
    /**
     * Проверяет, является ли текущая ОС Windows
     */
    public static boolean isWindows() {
        return detectOS() == OS.WINDOWS;
    }
    
    /**
     * Проверяет, является ли текущая ОС macOS
     */
    public static boolean isMacOS() {
        return detectOS() == OS.MACOS;
    }
    
    /**
     * Проверяет, является ли текущая ОС Linux
     */
    public static boolean isLinux() {
        return detectOS() == OS.LINUX;
    }
    
    /**
     * Проверяет, является ли архитектура ARM (Apple Silicon)
     */
    public static boolean isARM() {
        return detectArchitecture() == Architecture.AARCH64;
    }
}

