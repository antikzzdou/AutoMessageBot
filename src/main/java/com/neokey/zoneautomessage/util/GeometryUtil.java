package com.neokey.zoneautomessage.util;

/**
 * Utilidades para cálculos geométricos 3D.
 */
public class GeometryUtil {

	/**
	 * Calcula la distancia en línea recta entre dos puntos 3D.
	 *
	 * @param x1, y1, z1 Punto 1
	 * @param x2, y2, z2 Punto 2
	 * @return Distancia euclidiana
	 */
	public static double distance3D(double x1, double y1, double z1,
									 double x2, double y2, double z2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double dz = z2 - z1;

		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * Calcula la distancia horizontal (X-Z) entre dos puntos.
	 * Ignora Y (altura).
	 *
	 * @param x1, z1 Punto 1
	 * @param x2, z2 Punto 2
	 * @return Distancia horizontal
	 */
	public static double distanceHorizontal(double x1, double z1, double x2, double z2) {
		double dx = x2 - x1;
		double dz = z2 - z1;

		return Math.sqrt(dx * dx + dz * dz);
	}

	/**
	 * Convierte bloques a chunks.
	 *
	 * @param blocks Número de bloques
	 * @return Número de chunks (1 chunk = 16 bloques)
	 */
	public static double blocksToChunks(double blocks) {
		return blocks / 16.0;
	}

	/**
	 * Convierte chunks a bloques.
	 *
	 * @param chunks Número de chunks
	 * @return Número de bloques
	 */
	public static double chunksToBlocks(double chunks) {
		return chunks * 16.0;
	}
}