package com.neokey.zoneautomessage.zone;

import java.util.UUID;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║            ZONE - Modelo de Datos para Zonas 3D (Cuboides)               ║
 * ║                                                                          ║
 * ║ Responsabilidades:                                                       ║
 * ║ - Almacenar coordenadas del cuboide (X, Y, Z mín/máx)                   ║
 * ║ - Almacenar mensajes de entrada y salida personalizados                 ║
 * ║ - Detectar si el jugador está dentro de la zona                         ║
 * ║ - Aplicar lógica de buffer para evitar spam de mensajes                 ║
 * ║ - Gestionar transiciones de estado (entrada/salida)                     ║
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

	// Mensajes personalizados (soportan placeholders como [nickname], [zona_name], [time])
	private String enterMessage;
	private String exitMessage;

	// Control de estado del jugador
	private boolean playerWasInside = false;

	// Buffer distance = 8 chunks * 16 bloques/chunk = 128 bloques
	// Evita spam cuando el jugador sale/entra del borde
	private static final double BUFFER_DISTANCE = 128.0;

	/**
	 * Constructor principal para crear una nueva zona.
	 *
	 * @param zoneName Nombre descriptivo de la zona
	 * @param minX Coordenada X mínima del cuboide
	 * @param minY Coordenada Y mínima del cuboide
	 * @param minZ Coordenada Z mínima del cuboide
	 * @param maxX Coordenada X máxima del cuboide
	 * @param maxY Coordenada Y máxima del cuboide
	 * @param maxZ Coordenada Z máxima del cuboide
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

		// Mensajes por defecto
		this.enterMessage = "§a✓ Bienvenido a [zona_name]";
		this.exitMessage = "§c✗ Has salido de [zona_name]";
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// DETECCIÓN DE POSICIÓN
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Verifica si una posición XYZ está dentro de este cuboide.
	 * Útil para detección rápida sin cálculo de distancia.
	 *
	 * @param x Coordenada X del jugador
	 * @param y Coordenada Y del jugador
	 * @param z Coordenada Z del jugador
	 * @return true si está dentro, false si no
	 */
	public boolean isPlayerInside(double x, double y, double z) {
		return x >= minX && x <= maxX &&
			   y >= minY && y <= maxY &&
			   z >= minZ && z <= maxZ;
	}

	/**
	 * Calcula la distancia euclidiana mínima desde un punto al cuboide.
	 * 
	 * Fórmula: Se calcula la distancia a los bordes del cuboide.
	 * Si el punto está dentro, retorna 0.
	 * Si está fuera, retorna la distancia al punto más cercano del cuboide.
	 *
	 * @param x Coordenada X del jugador
	 * @param y Coordenada Y del jugador
	 * @param z Coordenada Z del jugador
	 * @return Distancia en bloques
	 */
	public double getDistanceToZone(double x, double y, double z) {
		// Calcular la distancia a cada eje
		double dx = Math.max(minX - x, Math.max(x - maxX, 0));
		double dy = Math.max(minY - y, Math.max(y - maxY, 0));
		double dz = Math.max(minZ - z, Math.max(z - maxZ, 0));

		// Fórmula euclidiana 3D
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// LÓGICA DE ESTADO Y TRANSICIONES
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Actualiza el estado del jugador en relación con esta zona.
	 * Implementa la lógica de buffer para evitar spam de mensajes.
	 *
	 * LÓGICA:
	 * - Si el jugador ENTRA en la zona: retorna 1 (ENTRADA)
	 * - Si el jugador SALE y está a +128 bloques: retorna -1 (SALIDA)
	 * - Si no hay cambio: retorna 0 (SIN CAMBIO)
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
			System.out.println("[ZONE] ► Entrada detectada en: " + zoneName);
		}
		// CASO 2: Transición Dentro → Fuera (con lógica de buffer)
		else if (!isInside && playerWasInside && distanceToZone > BUFFER_DISTANCE) {
			playerWasInside = false;
			result = -1; // SALIDA DETECTADA
			System.out.println("[ZONE] ◄ Salida detectada de: " + zoneName);
		}
		// CASO 3: Dentro → Fuera pero aún en buffer
		// (El jugador está fuera pero cercano, no enviar mensaje aún)
		else if (!isInside && playerWasInside && distanceToZone <= BUFFER_DISTANCE) {
			// Sin cambio, esperar a que se aleje más
			result = 0;
		}

		return result;
	}

	/**
	 * Reinicia el estado de la zona.
	 * Útil cuando se recarga el mod o cambia de mundo.
	 */
	public void resetState() {
		playerWasInside = false;
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

	public static double getBufferDistance() {
		return BUFFER_DISTANCE;
	}

	@Override
	public String toString() {
		return String.format(
			"Zone{id='%s', name='%s', bounds=[%.0f,%.0f,%.0f to %.0f,%.0f,%.0f]}",
			zoneId, zoneName, minX, minY, minZ, maxX, maxY, maxZ
		);
	}
}