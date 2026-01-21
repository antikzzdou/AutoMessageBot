package com.neokey.zoneautomessage.util;

/**
 * Validador de datos de zonas antes de guardarlas o usarlas.
 */
public class ZoneValidator {

	/**
	 * Valida si una zona tiene coordenadas válidas.
	 *
	 * @param minX, minY, minZ Coordenadas mínimas
	 * @param maxX, maxY, maxZ Coordenadas máximas
	 * @return true si las coordenadas son válidas
	 */
	public static boolean validateCoordinates(
		double minX, double minY, double minZ,
		double maxX, double maxY, double maxZ) {

		// Verificar que los límites no están invertidos
		if (minX > maxX || minY > maxY || minZ > maxZ) {
			System.err.println("[ZoneValidator] ✗ Coordenadas invertidas");
			return false;
		}

		// Verificar que el volumen es razonable (mínimo 1x1x1)
		if ((maxX - minX) < 1 || (maxY - minY) < 1 || (maxZ - minZ) < 1) {
			System.err.println("[ZoneValidator] ✗ Zona demasiado pequeña");
			return false;
		}

		// Verificar que no son números infinitos o NaN
		if (Double.isNaN(minX) || Double.isNaN(minY) || Double.isNaN(minZ) ||
			Double.isNaN(maxX) || Double.isNaN(maxY) || Double.isNaN(maxZ)) {
			System.err.println("[ZoneValidator] ✗ Coordenadas NaN");
			return false;
		}

		return true;
	}

	/**
	 * Valida si un nombre de zona es válido.
	 *
	 * @param zoneName Nombre a validar
	 * @return true si es válido
	 */
	public static boolean validateZoneName(String zoneName) {
		if (zoneName == null || zoneName.isEmpty()) {
			System.err.println("[ZoneValidator] ✗ Nombre de zona vacío");
			return false;
		}

		if (zoneName.length() > 50) {
			System.err.println("[ZoneValidator] ✗ Nombre muy largo (máx 50 caracteres)");
			return false;
		}

		return true;
	}

	/**
	 * Valida si un mensaje es válido.
	 *
	 * @param message Mensaje a validar
	 * @return true si es válido
	 */
	public static boolean validateMessage(String message) {
		if (message == null || message.isEmpty()) {
			System.err.println("[ZoneValidator] ✗ Mensaje vacío");
			return false;
		}

		if (message.length() > 1000) {
			System.err.println("[ZoneValidator] ✗ Mensaje demasiado largo (máx 1000 caracteres)");
			return false;
		}

		return true;
	}
}