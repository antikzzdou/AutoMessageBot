package com.neokey.zoneautomessage.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejo avanzado de códigos de color Minecraft.
 */
public class ColorCodeConverter {

	// Mapa de códigos de color
	private static final Map<Character, String> COLOR_MAP = new HashMap<>();

	static {
		// Números para colores
		COLOR_MAP.put('0', "§0"); // Negro
		COLOR_MAP.put('1', "§1"); // Azul oscuro
		COLOR_MAP.put('2', "§2"); // Verde oscuro
		COLOR_MAP.put('3', "§3"); // Cyan
		COLOR_MAP.put('4', "§4"); // Rojo oscuro
		COLOR_MAP.put('5', "§5"); // Púrpura
		COLOR_MAP.put('6', "§6"); // Oro
		COLOR_MAP.put('7', "§7"); // Gris claro
		COLOR_MAP.put('8', "§8"); // Gris oscuro
		COLOR_MAP.put('9', "§9"); // Azul
		COLOR_MAP.put('a', "§a"); // Verde
		COLOR_MAP.put('b', "§b"); // Cyan claro
		COLOR_MAP.put('c', "§c"); // Rojo
		COLOR_MAP.put('d', "§d"); // Magenta
		COLOR_MAP.put('e', "§e"); // Amarillo
		COLOR_MAP.put('f', "§f"); // Blanco

		// Estilos
		COLOR_MAP.put('l', "§l"); // Negrita
		COLOR_MAP.put('o', "§o"); // Itálica
		COLOR_MAP.put('n', "§n"); // Subrayado
		COLOR_MAP.put('m', "§m"); // Tachado
		COLOR_MAP.put('r', "§r"); // Reset
	}

	/**
	 * Convierte códigos & a códigos § (Minecraft).
	 *
	 * @param text Texto con códigos &
	 * @return Texto con códigos §
	 */
	public static String convertAmpersandToSection(String text) {
		if (text == null) {
			return "";
		}

		StringBuilder result = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char current = text.charAt(i);

			if (current == '&' && i + 1 < text.length()) {
				char next = text.charAt(i + 1);
				String colorCode = COLOR_MAP.get(next);

				if (colorCode != null) {
					result.append(colorCode);
					i++; // Saltar el siguiente carácter
				} else {
					result.append(current);
				}
			} else {
				result.append(current);
			}
		}

		return result.toString();
	}

	/**
	 * Verifica si un texto contiene códigos de color válidos.
	 *
	 * @param text Texto a verificar
	 * @return true si todos los códigos son válidos
	 */
	public static boolean isValidColorCodes(String text) {
		if (text == null) {
			return true;
		}

		for (int i = 0; i < text.length(); i++) {
			if ((text.charAt(i) == '&' || text.charAt(i) == '§') && i + 1 < text.length()) {
				char next = text.charAt(i + 1);
				if (!COLOR_MAP.containsKey(next)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Elimina todos los códigos de color de un texto.
	 * Útil para mostrar mensajes sin formato.
	 *
	 * @param text Texto con códigos de color
	 * @return Texto sin códigos de color
	 */
	public static String stripColorCodes(String text) {
		if (text == null) {
			return "";
		}

		return text.replaceAll("[&§][0-9a-fA-Flmnomnr]", "");
	}
}