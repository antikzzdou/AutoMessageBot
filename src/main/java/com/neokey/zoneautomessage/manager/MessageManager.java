package com.neokey.zoneautomessage.manager;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║    MESSAGE MANAGER - Procesamiento Avanzado de Mensajes y Formatos       ║
 * ║                                                                          ║
 * ║ Formatos soportados:                                                     ║
 * ║ - Códigos legacy: &c, §c                                                 ║
 * ║ - Hex colors: &#rrggbb                                                   ║
 * ║ - MiniMessage: <gradient:#ff0000:#00ff00>texto</gradient>               ║
 * ║ - TextPlaceholderAPI: %placeholder%                                      ║
 * ║                                                                          ║
 * ║ Placeholders internos:                                                   ║
 * ║ - [nickname] - Nombre del jugador                                       ║
 * ║ - [zona_name] - Nombre de la zona                                       ║
 * ║ - [time] - Hora actual                                                  ║
 * ║ - [date] - Fecha actual                                                 ║
 * ║ - [coords] - Coordenadas del jugador                                    ║
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

	// Patrones regex para formatos avanzados
	private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");
	private static final Pattern MINIMESSAGE_GRADIENT = Pattern.compile(
		"<gradient:(#[0-9a-fA-F]{6}):(#[0-9a-fA-F]{6})>(.*?)</gradient>"
	);

	/**
	 * Envía un mensaje a UN JUGADOR ESPECÍFICO mediante /msg.
	 * 
	 * IMPORTANTE: Este método envía el mensaje usando el comando /msg,
	 * lo que significa que el mensaje se envía solo al jugador especificado.
	 *
	 * @param targetPlayer Nombre del jugador destinatario
	 * @param rawMessage Mensaje con placeholders y códigos de color
	 * @param zoneName Nombre de la zona (para placeholders)
	 */
	public static void sendPrivateZoneMessage(String targetPlayer, String rawMessage, String zoneName) {
		if (rawMessage == null || rawMessage.isEmpty()) {
			return;
		}

		if (CLIENT.player == null) {
			System.err.println("[MessageManager] ✗ Cliente sin jugador");
			return;
		}

		try {
			// 1. Procesar placeholders internos
			String processedMessage = processInternalPlaceholders(rawMessage, zoneName);

			// 2. Procesar formatos avanzados (Hex, MiniMessage)
			Text finalText = parseAdvancedFormats(processedMessage);

			// 3. Enviar usando /msg <jugador> <mensaje>
			// ACTUALIZADO PARA MC 1.21.8: Usar sendChatMessage en lugar de sendCommand
			String command = String.format("msg %s %s", targetPlayer, processedMessage);
			
			if (CLIENT.player != null && CLIENT.player.networkHandler != null) {
				CLIENT.player.networkHandler.sendChatCommand(command);
				System.out.println("[MessageManager] ✓ Mensaje enviado a: " + targetPlayer);
			}

		} catch (Exception e) {
			System.err.println("[MessageManager] ✗ Error al enviar mensaje:");
			e.printStackTrace();
		}
	}

	/**
	 * Envía un mensaje al chat del jugador LOCAL (sin usar /msg).
	 * Útil para mensajes de sistema o debug.
	 *
	 * @param rawMessage Mensaje con placeholders y códigos
	 * @param zoneName Nombre de la zona
	 */
	public static void sendZoneMessage(String rawMessage, String zoneName) {
		if (rawMessage == null || rawMessage.isEmpty()) {
			return;
		}

		if (CLIENT.player == null) {
			return;
		}

		try {
			String processedMessage = processInternalPlaceholders(rawMessage, zoneName);
			Text finalText = parseAdvancedFormats(processedMessage);
			CLIENT.player.sendMessage(finalText, false);
			System.out.println("[MessageManager] ✓ Mensaje local enviado");

		} catch (Exception e) {
			System.err.println("[MessageManager] ✗ Error al enviar mensaje:");
			e.printStackTrace();
		}
	}

	/**
	 * Procesa placeholders internos del mod.
	 */
	private static String processInternalPlaceholders(String message, String zoneName) {
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
	 * Procesa formatos avanzados: Hex colors (&#rrggbb) y MiniMessage.
	 * ACTUALIZADO PARA PLACEHOLDER API 2.7.2
	 */
	private static Text parseAdvancedFormats(String message) {
		// 1. Convertir &#rrggbb a formato Minecraft §x§r§r§g§g§b§b
		message = convertHexColors(message);

		// 2. Procesar MiniMessage gradients
		message = processMiniMessageGradients(message);

		// 3. Convertir & a § (códigos legacy)
		message = message.replace("&", "§");

		// 4. Usar TextPlaceholderAPI para parsear el texto final
		// ACTUALIZADO: TextParserUtils está deprecated, usar Placeholders.parseText
		try {
			return Placeholders.parseText(Text.literal(message), PlaceholderContext.of(CLIENT.player));
		} catch (Exception e) {
			// Fallback: crear Text literal si falla el parser
			return Text.literal(message);
		}
	}

	/**
	 * Convierte colores hex &#rrggbb al formato Minecraft §x§r§r§g§g§b§b.
	 * 
	 * Ejemplo: &#ff0000 → §x§f§f§0§0§0§0
	 */
	private static String convertHexColors(String message) {
		Matcher matcher = HEX_PATTERN.matcher(message);
		StringBuffer result = new StringBuffer();

		while (matcher.find()) {
			String hex = matcher.group(1).toLowerCase();
			StringBuilder replacement = new StringBuilder("§x");
			
			for (char c : hex.toCharArray()) {
				replacement.append("§").append(c);
			}

			matcher.appendReplacement(result, replacement.toString());
		}

		matcher.appendTail(result);
		return result.toString();
	}

	/**
	 * Procesa gradientes de MiniMessage.
	 * 
	 * Ejemplo: <gradient:#ff0000:#00ff00>Hola</gradient>
	 */
	private static String processMiniMessageGradients(String message) {
		Matcher matcher = MINIMESSAGE_GRADIENT.matcher(message);
		StringBuffer result = new StringBuffer();

		while (matcher.find()) {
			String startColor = matcher.group(1); // #ff0000
			String endColor = matcher.group(2);   // #00ff00
			String text = matcher.group(3);        // Texto a gradientar

			String gradientText = createGradient(text, startColor, endColor);
			matcher.appendReplacement(result, Matcher.quoteReplacement(gradientText));
		}

		matcher.appendTail(result);
		return result.toString();
	}

	/**
	 * Crea un gradiente entre dos colores hex para un texto.
	 */
	private static String createGradient(String text, String startHex, String endHex) {
		if (text.isEmpty()) {
			return "";
		}

		// Parsear colores hex
		int startR = Integer.parseInt(startHex.substring(1, 3), 16);
		int startG = Integer.parseInt(startHex.substring(3, 5), 16);
		int startB = Integer.parseInt(startHex.substring(5, 7), 16);

		int endR = Integer.parseInt(endHex.substring(1, 3), 16);
		int endG = Integer.parseInt(endHex.substring(3, 5), 16);
		int endB = Integer.parseInt(endHex.substring(5, 7), 16);

		StringBuilder result = new StringBuilder();
		int length = text.length();

		for (int i = 0; i < length; i++) {
			float ratio = (float) i / (length - 1);
			
			int r = (int) (startR + (endR - startR) * ratio);
			int g = (int) (startG + (endG - startG) * ratio);
			int b = (int) (startB + (endB - startB) * ratio);

			String hex = String.format("%02x%02x%02x", r, g, b);
			result.append("&#").append(hex).append(text.charAt(i));
		}

		return result.toString();
	}

	/**
	 * Envía un mensaje de depuración.
	 */
	public static void sendDebugMessage(String message) {
		if (CLIENT.player == null) {
			return;
		}

		Text text = Text.literal("§7[DEBUG] " + message);
		CLIENT.player.sendMessage(text, false);
	}

	/**
	 * Registra placeholders personalizados usando TextPlaceholderAPI.
	 */
	public static void registerCustomPlaceholders() {
		// Ejemplo: %zam:zone_count%
		Placeholders.register(
			Identifier.of("zam", "zone_count"),
			(ctx, arg) -> PlaceholderResult.value(
				String.valueOf(com.neokey.zoneautomessage.ZoneAutoMessageMod.getZoneManager().getZoneCount())
			)
		);

		// Ejemplo: %zam:world%
		Placeholders.register(
			Identifier.of("zam", "world"),
			(ctx, arg) -> PlaceholderResult.value(
				com.neokey.zoneautomessage.ZoneAutoMessageMod.getWorldConfigManager().getCurrentWorldId()
			)
		);

		System.out.println("[MessageManager] ✓ Placeholders personalizados registrados");
	}
}