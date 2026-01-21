package com.neokey.zoneautomessage.manager;

import java.util.*;
import com.neokey.zoneautomessage.zone.Zone;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║         ZONE MANAGER - Gestor Centralizado de Todas las Zonas            ║
 * ║                                                                          ║
 * ║ Responsabilidades:                                                       ║
 * ║ - Crear, eliminar, actualizar y consultar zonas                         ║
 * ║ - Cargar/guardar zonas desde/hacia persistencia                         ║
 * ║ - Iterar eficientemente sobre zonas para detectar cambios                ║
 * ║ - Proporcionar búsquedas rápidas por ID o nombre                        ║
 * ║                                                                          ║
 * ║ Patrón: Manager Centralizado (Singleton implícito)                       ║
 * ║ Autor: NeoKey                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class ZoneManager {

	// Almacenamiento principal de zonas
	// Usamos HashMap para búsqueda O(1) por ID
	private final Map<String, Zone> zonesById = new HashMap<>();

	// Índice secundario para búsquedas por nombre
	private final Map<String, Zone> zonesByName = new HashMap<>();

	// Estadísticas de depuración
	private int totalZonesCreated = 0;

	/**
	 * Constructor. Inicializa el gestor vacío.
	 */
	public ZoneManager() {
		System.out.println("[ZoneManager] Inicializado correctamente");
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// CARGA Y PERSISTENCIA
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Carga una lista de zonas desde datos persistentes (JSON, etc).
	 * Llamado durante la inicialización del mod.
	 *
	 * @param zonesData Lista de objetos Zone ya construidos
	 */
	public void loadZones(List<Zone> zonesData) {
		if (zonesData == null || zonesData.isEmpty()) {
			System.out.println("[ZoneManager] No hay zonas para cargar");
			return;
		}

		for (Zone zone : zonesData) {
			addZoneInternal(zone);
		}

		System.out.println("[ZoneManager] ✓ Cargadas " + zonesById.size() + " zona(s)");
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// OPERACIONES CRUD
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Crea una nueva zona y la añade al gestor.
	 *
	 * @param zoneName Nombre de la zona
	 * @param minX X mínima
	 * @param minY Y mínima
	 * @param minZ Z mínima
	 * @param maxX X máxima
	 * @param maxY Y máxima
	 * @param maxZ Z máxima
	 * @return La zona creada
	 */
	public Zone createZone(String zoneName, double minX, double minY, double minZ,
						  double maxX, double maxY, double maxZ) {
		// Validar que el nombre no esté duplicado
		if (zonesByName.containsKey(zoneName)) {
			System.err.println("[ZoneManager] ✗ Ya existe una zona con el nombre: " + zoneName);
			return null;
		}

		// Crear la zona
		Zone zone = new Zone(zoneName, minX, minY, minZ, maxX, maxY, maxZ);
		
		// Añadir al gestor
		addZoneInternal(zone);

		System.out.println("[ZoneManager] ✓ Zona creada: " + zone);
		totalZonesCreated++;

		return zone;
	}

	/**
	 * Añade una zona ya creada al gestor.
	 * Útil cuando se carga desde config o se crea externamente.
	 *
	 * @param zone Zona a añadir
	 */
	public void createZoneFromObject(Zone zone) {
		if (zonesByName.containsKey(zone.getZoneName())) {
			System.err.println("[ZoneManager] ✗ Ya existe zona: " + zone.getZoneName());
			return;
		}
		addZoneInternal(zone);
		totalZonesCreated++;
	}

	/**
	 * Método interno para añadir una zona a los índices.
	 */
	private void addZoneInternal(Zone zone) {
		zonesById.put(zone.getZoneId(), zone);
		zonesByName.put(zone.getZoneName(), zone);
	}

	/**
	 * Obtiene una zona por su ID único.
	 *
	 * @param zoneId ID de la zona
	 * @return La zona, o null si no existe
	 */
	public Zone getZoneById(String zoneId) {
		return zonesById.get(zoneId);
	}

	/**
	 * Obtiene una zona por su nombre.
	 *
	 * @param zoneName Nombre de la zona
	 * @return La zona, o null si no existe
	 */
	public Zone getZoneByName(String zoneName) {
		return zonesByName.get(zoneName);
	}

	/**
	 * Elimina una zona por su ID.
	 *
	 * @param zoneId ID de la zona a eliminar
	 * @return true si se eliminó, false si no existía
	 */
	public boolean deleteZone(String zoneId) {
		Zone zone = zonesById.get(zoneId);
		if (zone == null) {
			System.err.println("[ZoneManager] ✗ No existe zona con ID: " + zoneId);
			return false;
		}

		zonesById.remove(zoneId);
		zonesByName.remove(zone.getZoneName());

		System.out.println("[ZoneManager] ✓ Zona eliminada: " + zone.getZoneName());
		return true;
	}

	/**
	 * Elimina una zona por su nombre.
	 *
	 * @param zoneName Nombre de la zona a eliminar
	 * @return true si se eliminó, false si no existía
	 */
	public boolean deleteZoneByName(String zoneName) {
		Zone zone = zonesByName.get(zoneName);
		if (zone == null) {
			System.err.println("[ZoneManager] ✗ No existe zona con nombre: " + zoneName);
			return false;
		}

		return deleteZone(zone.getZoneId());
	}

	/**
	 * Renombra una zona.
	 *
	 * @param zoneId ID de la zona
	 * @param nuevoNombre Nuevo nombre
	 * @return true si se renombró, false si falló
	 */
	public boolean renameZone(String zoneId, String nuevoNombre) {
		Zone zone = zonesById.get(zoneId);
		if (zone == null) {
			System.err.println("[ZoneManager] ✗ No existe zona con ID: " + zoneId);
			return false;
		}

		if (zonesByName.containsKey(nuevoNombre)) {
			System.err.println("[ZoneManager] ✗ El nombre ya está en uso: " + nuevoNombre);
			return false;
		}

		String nombreAnterior = zone.getZoneName();
		zonesByName.remove(nombreAnterior);
		zone.setZoneName(nuevoNombre);
		zonesByName.put(nuevoNombre, zone);

		System.out.println("[ZoneManager] ✓ Zona renombrada: " + nombreAnterior + 
			" → " + nuevoNombre);
		return true;
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// ITERACIÓN Y CONSULTAS
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Obtiene todas las zonas como una colección no modificable.
	 * Eficiente para iterar durante el tick del jugador.
	 *
	 * @return Collection de todas las zonas
	 */
	public Collection<Zone> getAllZones() {
		return Collections.unmodifiableCollection(zonesById.values());
	}

	/**
	 * Obtiene la cantidad total de zonas cargadas.
	 *
	 * @return Número de zonas
	 */
	public int getZoneCount() {
		return zonesById.size();
	}

	/**
	 * Obtiene una lista de nombres de todas las zonas.
	 * Útil para GUIs y listas.
	 *
	 * @return Lista de nombres de zonas
	 */
	public List<String> getZoneNames() {
		return new ArrayList<>(zonesByName.keySet());
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// UTILIDADES Y ESTADÍSTICAS
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Obtiene estadísticas del gestor (depuración).
	 *
	 * @return String con información
	 */
	public String getStats() {
		return String.format(
			"ZoneManager Stats: %d zonas cargadas, %d creadas en total",
			zonesById.size(), totalZonesCreated
		);
	}

	/**
	 * Limpia todas las zonas. ¡CUIDADO!
	 */
	public void clearAllZones() {
		zonesById.clear();
		zonesByName.clear();
		System.out.println("[ZoneManager] ⚠ Todas las zonas han sido eliminadas");
	}

	/**
	 * Reinicia el estado de todas las zonas.
	 * Llamar cuando el jugador cambia de mundo o es necesario resetear.
	 */
	public void resetAllZoneStates() {
		for (Zone zone : zonesById.values()) {
			zone.resetState();
		}
		System.out.println("[ZoneManager] ✓ Estados de todas las zonas reiniciados");
	}
}