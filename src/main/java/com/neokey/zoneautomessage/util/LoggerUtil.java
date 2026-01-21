package com.neokey.zoneautomessage.util;

/**
 * Utilidades de logging para depuración consistente.
 */
public class LoggerUtil {

	public enum LogLevel {
		DEBUG("§7[DEBUG]"),
		INFO("§a[INFO]"),
		WARN("§e[WARN]"),
		ERROR("§c[ERROR]");

		private final String prefix;

		LogLevel(String prefix) {
			this.prefix = prefix;
		}

		public String getPrefix() {
			return prefix;
		}
	}

	/**
	 * Registra un mensaje en la consola.
	 *
	 * @param level Nivel de logging
	 * @param component Componente (ZoneManager, ConfigManager, etc)
	 * @param message Mensaje
	 */
	public static void log(LogLevel level, String component, String message) {
		String formatted = String.format(
			"%s §7[%s] §r%s",
			level.getPrefix(),
			component,
			message
		);
		System.out.println(formatted);
	}

	/**
	 * Registra un error con excepción.
	 *
	 * @param component Componente
	 * @param message Mensaje
	 * @param exception Excepción
	 */
	public static void logError(String component, String message, Exception exception) {
		log(LogLevel.ERROR, component, message);
		exception.printStackTrace();
	}

	/**
	 * Registra información de depuración.
	 *
	 * @param component Componente
	 * @param message Mensaje
	 */
	public static void debug(String component, String message) {
		log(LogLevel.DEBUG, component, message);
	}
}