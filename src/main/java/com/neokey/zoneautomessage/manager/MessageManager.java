package com.neokey.zoneautomessage.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║        MESSAGE MANAGER - Procesamiento y Envío de Mensajes               ║
 * ║                                                                          ║
 * ║ Responsabilidades:                                                       ║
 * ║ - Procesar placeholders en mensajes ([nickname], [zona_name], [time])   ║
 * ║ - Convertir códigos de color § y & a formato Minecraft                  ║
 * ║ - Enviar mensajes al chat del jugador                                   ║
 * ║ - Aplicar formato y estilos de texto                                    ║
 * ║                                                                          ║
 * ║ Placeholders soportados:                                                ║
 * ║ - [nickname]: Nombre del jugador                                        ║
 * ║ - [zona_name]: Nombre de la zona                                        ║
 * ║ - [time]: Hora actual (HH:mm:ss)                                        ║
 * ║ - [date]: Fecha actual (dd/MM/yyyy)                                     ║
 * ║ - [coords]: Coordenadas actuales del jugador                            ║
 * ║                                                                          ║
 * ║ Autor: NeoKey                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class MessageManager {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final DateTimeFormatter TIME_FORMATTER = 
		DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final DateTimeFormatter DATE_FORMATTER = 
		DateTimeFormatter.ofPattern("dd/MM/yyyy");

	/**
	 * Envía un mensaje procesado al chat del jugador.
	 * 
	 * El mensaje se procesa para:
	 * 1. Reemplazar placeholders
	 * 2. Convertir códigos de color (§ y &)
	 * 3. Crear componente de texto
	 *
	 * @param rawMessage Mensaje con placeholders y códigos de color
	 * @param zoneName Nombre de la zona (para placeholders)
	 */
	public static void sendZoneMessage(String rawMessage, String zoneName) {
		if (rawMessage == null || rawMessage.isEmpty()) {
			return;
		}

		if (CLIENT.player == null) {
			System.err.println("[MessageManager] ✗ Cliente sin jugador");
			return;
		}

		try {
			// 1. Procesar placeholders
			String processedMessage = processPlaceholders(rawMessage, zoneName);

			// 2. Convertir códigos de color
			String coloredMessage = convertColorCodes(processedMessage);

			// 3. Crear Text component y enviar
			Text textComponent = Text.literal(coloredMessage);
			CLIENT.player.sendMessage(textComponent, false);

			System.out.println("[MessageManager] ✓ Mensaje enviado: " + zoneName);

		} catch (Exception e) {
			System.err.println("[MessageManager] ✗ Error al enviar mensaje:");
			e.printStackTrace();
		}
	}

	/**
	 * Procesa placeholders dinámicos en el mensaje.
	 *
	 * @param message Mensaje con placeholders
	 * @param zoneName Nombre de la zona
	 * @return Mensaje con placeholders reemplazados
	 */
	private static String processPlaceholders(String message, String zoneName) {
		if (CLIENT.player == null) {
			return message;
		}

		String result = message;

		// [nickname] - Nombre del jugador
		result = result.replace("[nickname]", CLIENT.player.getName().getString());

		// [zona_name] - Nombre de la zona
		result = result.replace("[zona_name]", zoneName);

		// [time] - Hora actual
		result = result.replace("[time]", LocalDateTime.now().format(TIME_FORMATTER));

		// [date] - Fecha actual
		result = result.replace("[date]", LocalDateTime.now().format(DATE_FORMATTER));

		// [coords] - Coordenadas actuales
		int x = (int) Math.floor(CLIENT.player.getX());
		int y = (int) Math.floor(CLIENT.player.getY());
		int z = (int) Math.floor(CLIENT.player.getZ());
		result = result.replace("[coords]", x + ", " + y + ", " + z);

		return result;
	}

	/**
	 * Convierte códigos de color Minecraft (§ y &) en el formato correcto.
	 *
	 * Soporta:
	 * - §0 a §f para colores básicos
	 * - §l para negrita
	 * - §o para itálica
	 * - §n para subrayado
	 * - §m para tachado
	 * - & también funciona (se convierte a §)
	 *
	 * @param message Mensaje con códigos de color
	 * @return Mensaje procesado
	 */
	private static String convertColorCodes(String message) {
		// Convertir & a § (Minecraft color code)
		return message.replace("&", "§");
	}

	/**
	 * Envía un mensaje de depuración al chat.
	 *
	 * @param message Mensaje de debug
	 */
	public static void sendDebugMessage(String message) {
		if (CLIENT.player == null) {
			return;
		}

		Text text = Text.literal("§7[DEBUG] " + message);
		CLIENT.player.sendMessage(text, false);
	}
}