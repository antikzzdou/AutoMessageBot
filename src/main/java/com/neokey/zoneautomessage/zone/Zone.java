package com.neokey.zoneautomessage.zone;

import java.util.UUID;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║         ZONE - Modelo con Buffer Dinámico Basado en Tamaño              ║
 * ║                                                                          ║
 * ║ MEJORAS v2.0:                                                            ║
 * ║ - Buffer dinámico calculado automáticamente (10% del tamaño de zona)    ║
 * ║ - Mínimo 5 bloques, máximo 200 bloques                                  ║
 * ║ - Evita spam sin ser demasiado restrictivo                              ║
 * ║ - Se adapta automáticamente a zonas pequeñas y grandes                  ║
 * ║                                                                          ║
 * ║ Fórmula del buffer:                                                      ║
 * ║ buffer = max(5, min(200, diagonal_zona * 0.1))                          ║
 * ║                                                                          ║
 * ║ Ejemplos:                                                                ║
 * ║ - Zona 10x10x10: buffer = 5 bloques (mínimo)                            ║
 * ║ - Zona 100x100x100: buffer = 17 bloques                                 ║
 * ║ - Zona 1000x1000x1000: buffer = 200 bloques (máximo)                    ║
 * ║                                                                          ║
 * ║ Autor: NeoKey                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class Zone {

	// Identificadores únicos
	private final String zoneId;
	private String zoneName;

	// Coordenadas del cuboide (delimitadores)
	private double minX, minY, minZ;
	private double maxX, maxY, maxZ;

	// Mensajes personalizados (soportan MiniMessage, Hex, placeholders)
	private String enterMessage;
	private String exitMessage;

	// Control de estado del jugador
	private boolean playerWasInside = false;

	// Buffer dinámico (calculado automáticamente)
	private double bufferDistance;

	// Constantes para buffer
	private static final double MIN_BUFFER = 5.0;
	private static final double MAX_BUFFER = 200.0;
	private static final double BUFFER_MULTIPLIER = 0.10; // 10% del tamaño

	/**
	 * Constructor principal para crear una nueva zona.
	 */
	public Zone(String zoneName, double minX, double minY, double minZ,
			   double maxX, double maxY, double maxZ) {
		this.zoneId = UUID.randomUUID().toString();
		this.zoneName = zoneName;

		// Asegurar que mín < máx
		this.minX = Math.min(minX, maxX);
		this.maxX = Math.max(minX, maxX);
		this.minY = Math.min(minY, maxY);
		this.maxY = Math.max(minY, maxY);
		this.minZ = Math.min(minZ, maxZ);
		this.maxZ = Math.max(minZ, maxZ);

		// Calcular buffer dinámico basado en tamaño
		this.bufferDistance = calculateDynamicBuffer();

		// Mensajes por defecto con placeholders
		this.enterMessage = "<gradient:#00ff00:#00aa00>✓ Bienvenido a [zona_name]</gradient>";
		this.exitMessage = "<gradient:#ff0000:#aa0000>✗ Has salido de [zona_name]</gradient>";

		System.out.println(String.format(
			"[Zone] Creada: %s | Buffer: %.1f bloques | Dimensiones: %.0fx%.0fx%.0f",
			zoneName, bufferDistance, getWidth(), getHeight(), getDepth()
		));
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// CÁLCULO DE BUFFER DINÁMICO
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Calcula el buffer dinámico basado en el tamaño de la zona.
	 * 
	 * El buffer es el 10% de la diagonal 3D de la zona, con un mínimo de 5
	 * bloques y un máximo de 200 bloques.
	 *
	 * Esto evita que zonas pequeñas tengan buffers demasiado grandes,
	 * y que zonas enormes tengan buffers excesivos.
	 *
	 * @return Buffer en bloques
	 */
	private double calculateDynamicBuffer() {
		// Calcular diagonal 3D de la zona
		double width = maxX - minX;
		double height = maxY - minY;
		double depth = maxZ - minZ;
		
		double diagonal = Math.sqrt(width * width + height * height + depth * depth);
		
		// Buffer = 10% de la diagonal
		double buffer = diagonal * BUFFER_MULTIPLIER;
		
		// Aplicar límites (mín: 5, máx: 200)
		buffer = Math.max(MIN_BUFFER, Math.min(MAX_BUFFER, buffer));
		
		return buffer;
	}

	/**
	 * Recalcula el buffer dinámico si cambian las coordenadas.
	 * Llamar después de usar setCoordinates().
	 */
	public void recalculateBuffer() {
		this.bufferDistance = calculateDynamicBuffer();
		System.out.println(String.format(
			"[Zone] %s | Buffer recalculado: %.1f bloques",
			zoneName, bufferDistance
		));
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// DETECCIÓN DE POSICIÓN
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Verifica si una posición XYZ está dentro de este cuboide.
	 */
	public boolean isPlayerInside(double x, double y, double z) {
		return x >= minX && x <= maxX &&
			   y >= minY && y <= maxY &&
			   z >= minZ && z <= maxZ;
	}

	/**
	 * Calcula la distancia euclidiana mínima desde un punto al cuboide.
	 */
	public double getDistanceToZone(double x, double y, double z) {
		double dx = Math.max(minX - x, Math.max(x - maxX, 0));
		double dy = Math.max(minY - y, Math.max(y - maxY, 0));
		double dz = Math.max(minZ - z, Math.max(z - maxZ, 0));

		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// LÓGICA DE ESTADO Y TRANSICIONES
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Actualiza el estado del jugador en relación con esta zona.
	 * 
	 * MEJORA: Ahora usa buffer dinámico calculado automáticamente.
	 *
	 * @param x Coordenada X del jugador
	 * @param y Coordenada Y del jugador
	 * @param z Coordenada Z del jugador
	 * @return 1=entrada, -1=salida (con buffer), 0=sin cambio
	 */
	public int updatePlayerState(double x, double y, double z) {
		boolean isInside = isPlayerInside(x, y, z);
		double distanceToZone = getDistanceToZone(x, y, z);

		int result = 0;

		// CASO 1: Transición Fuera → Dentro
		if (isInside && !playerWasInside) {
			playerWasInside = true;
			result = 1; // ENTRADA DETECTADA
			System.out.println(String.format(
				"[ZONE] ► Entrada en: %s (Buffer: %.1f bloques)",
				zoneName, bufferDistance
			));
		}
		// CASO 2: Transición Dentro → Fuera (con buffer dinámico)
		else if (!isInside && playerWasInside && distanceToZone > bufferDistance) {
			playerWasInside = false;
			result = -1; // SALIDA DETECTADA
			System.out.println(String.format(
				"[ZONE] ◄ Salida de: %s (Distancia: %.1f > Buffer: %.1f)",
				zoneName, distanceToZone, bufferDistance
			));
		}
		// CASO 3: Dentro → Fuera pero aún en buffer
		else if (!isInside && playerWasInside && distanceToZone <= bufferDistance) {
			// Sin cambio, esperar a que se aleje más
			result = 0;
		}

		return result;
	}

	/**
	 * Reinicia el estado de la zona.
	 */
	public void resetState() {
		playerWasInside = false;
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// GETTERS PARA DIMENSIONES
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Obtiene el ancho de la zona (eje X).
	 */
	public double getWidth() {
		return maxX - minX;
	}

	/**
	 * Obtiene la altura de la zona (eje Y).
	 */
	public double getHeight() {
		return maxY - minY;
	}

	/**
	 * Obtiene la profundidad de la zona (eje Z).
	 */
	public double getDepth() {
		return maxZ - minZ;
	}

	/**
	 * Obtiene el volumen de la zona en bloques cúbicos.
	 */
	public double getVolume() {
		return getWidth() * getHeight() * getDepth();
	}

	/**
	 * Obtiene la diagonal 3D de la zona.
	 */
	public double getDiagonal() {
		double w = getWidth();
		double h = getHeight();
		double d = getDepth();
		return Math.sqrt(w * w + h * h + d * d);
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// GETTERS Y SETTERS
	// ═══════════════════════════════════════════════════════════════════════════

	public String getZoneId() {
		return zoneId;
	}

	public String getZoneName() {
		return zoneName;
	}

	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}

	// Coordenadas
	public double getMinX() { return minX; }
	public double getMinY() { return minY; }
	public double getMinZ() { return minZ; }
	public double getMaxX() { return maxX; }
	public double getMaxY() { return maxY; }
	public double getMaxZ() { return maxZ; }

	public void setCoordinates(double minX, double minY, double minZ,
							   double maxX, double maxY, double maxZ) {
		this.minX = Math.min(minX, maxX);
		this.maxX = Math.max(minX, maxX);
		this.minY = Math.min(minY, maxY);
		this.maxY = Math.max(minY, maxY);
		this.minZ = Math.min(minZ, maxZ);
		this.maxZ = Math.max(minZ, maxZ);
		
		// Recalcular buffer automáticamente
		recalculateBuffer();
	}

	// Mensajes
	public String getEnterMessage() {
		return enterMessage;
	}

	public void setEnterMessage(String msg) {
		this.enterMessage = msg;
	}

	public String getExitMessage() {
		return exitMessage;
	}

	public void setExitMessage(String msg) {
		this.exitMessage = msg;
	}

	// Estado
	public boolean isPlayerWasInside() {
		return playerWasInside;
	}

	public void setPlayerWasInside(boolean state) {
		this.playerWasInside = state;
	}

	public double getBufferDistance() {
		return bufferDistance;
	}

	@Override
	public String toString() {
		return String.format(
			"Zone{id='%s', name='%s', bounds=[%.0f,%.0f,%.0f to %.0f,%.0f,%.0f], buffer=%.1f}",
			zoneId, zoneName, minX, minY, minZ, maxX, maxY, maxZ, bufferDistance
		);
	}
}