package com.neokey.zoneautomessage.manager;

import com.google.gson.*;
import com.neokey.zoneautomessage.zone.Zone;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║         CONFIG MANAGER - Persistencia de Datos en JSON                   ║
 * ║                                                                          ║
 * ║ Responsabilidades:                                                       ║
 * ║ - Cargar configuración desde archivo JSON                               ║
 * ║ - Guardar configuración a archivo JSON                                  ║
 * ║ - Serializar/deserializar zonas                                         ║
 * ║ - Crear directorio de config automáticamente                            ║
 * ║ - Manejo de errores robusto                                             ║
 * ║                                                                          ║
 * ║ Estructura JSON:                                                         ║
 * ║ {                                                                        ║
 * ║   "zones": [                                                             ║
 * ║     {                                                                    ║
 * ║       "id": "uuid",                                                      ║
 * ║       "name": "Zona 1",                                                  ║
 * ║       "min": [0, 0, 0],                                                  ║
 * ║       "max": [100, 100, 100],                                            ║
 * ║       "enterMsg": "Bienvenido",                                          ║
 * ║       "exitMsg": "Adiós"                                                 ║
 * ║     }                                                                    ║
 * ║   ]                                                                      ║
 * ║ }                                                                        ║
 * ║                                                                          ║
 * ║ Autor: NeoKey                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class ConfigManager {

	private static final String CONFIG_DIR = "config/zoneautomessage";
	private static final String CONFIG_FILE = CONFIG_DIR + "/zones.json";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private JsonObject configRoot;
	private JsonArray zonesArray;

	/**
	 * Constructor. Inicializa el ConfigManager.
	 */
	public ConfigManager() {
		System.out.println("[ConfigManager] Inicializando...");
		ensureConfigDirectory();
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// CARGA Y GUARDADO
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Carga la configuración desde el archivo JSON.
	 * Si el archivo no existe, crea uno con estructura vacía.
	 */
	public void loadConfig() {
		try {
			File configFile = new File(CONFIG_FILE);

			if (configFile.exists()) {
				// Leer archivo existente
				String content = new String(Files.readAllBytes(configFile.toPath()));
				configRoot = JsonParser.parseString(content).getAsJsonObject();
				System.out.println("[ConfigManager] ✓ Config cargada desde: " + CONFIG_FILE);
			} else {
				// Crear config vacía
				configRoot = new JsonObject();
				zonesArray = new JsonArray();
				configRoot.add("zones", zonesArray);
				saveConfig();
				System.out.println("[ConfigManager] ✓ Config nueva creada: " + CONFIG_FILE);
			}

			// Asegurar que existe el array de zonas
			if (!configRoot.has("zones")) {
				zonesArray = new JsonArray();
				configRoot.add("zones", zonesArray);
			} else {
				zonesArray = configRoot.getAsJsonArray("zones");
			}

		} catch (Exception e) {
			System.err.println("[ConfigManager] ✗ Error cargando config:");
			e.printStackTrace();
			initializeEmpty();
		}
	}

	/**
	 * Guarda la configuración actual al archivo JSON.
	 */
	public void saveConfig() {
		try {
			ensureConfigDirectory();
			String jsonString = GSON.toJson(configRoot);
			Files.write(Paths.get(CONFIG_FILE), jsonString.getBytes());
			System.out.println("[ConfigManager] ✓ Config guardada");
		} catch (Exception e) {
			System.err.println("[ConfigManager] ✗ Error guardando config:");
			e.printStackTrace();
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// OPERACIONES CON ZONAS
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Añade una zona a la configuración.
	 *
	 * @param zone Zona a guardar
	 */
	public void addZone(Zone zone) {
		try {
			JsonObject zoneJson = zoneToJson(zone);
			zonesArray.add(zoneJson);
			saveConfig();
			System.out.println("[ConfigManager] ✓ Zona guardada: " + zone.getZoneName());
		} catch (Exception e) {
			System.err.println("[ConfigManager] ✗ Error añadiendo zona:");
			e.printStackTrace();
		}
	}

	/**
	 * Elimina una zona de la configuración por ID.
	 *
	 * @param zoneId ID de la zona
	 */
	public void removeZone(String zoneId) {
		try {
			JsonArray newArray = new JsonArray();

			for (JsonElement element : zonesArray) {
				JsonObject zoneJson = element.getAsJsonObject();
				if (!zoneJson.get("id").getAsString().equals(zoneId)) {
					newArray.add(zoneJson);
				}
			}

			configRoot.add("zones", newArray);
			zonesArray = newArray;
			saveConfig();
			System.out.println("[ConfigManager] ✓ Zona eliminada con ID: " + zoneId);
		} catch (Exception e) {
			System.err.println("[ConfigManager] ✗ Error eliminando zona:");
			e.printStackTrace();
		}
	}

	/**
	 * Actualiza una zona existente.
	 *
	 * @param zone Zona actualizada
	 */
	public void updateZone(Zone zone) {
		removeZone(zone.getZoneId());
		addZone(zone);
	}

	/**
	 * Obtiene todas las zonas desde la config y las convierte a objetos Zone.
	 *
	 * @return Lista de zonas
	 */
	public List<Zone> getZonesData() {
		List<Zone> zones = new ArrayList<>();

		try {
			for (JsonElement element : zonesArray) {
				Zone zone = jsonToZone(element.getAsJsonObject());
				if (zone != null) {
					zones.add(zone);
				}
			}
		} catch (Exception e) {
			System.err.println("[ConfigManager] ✗ Error leyendo zonas:");
			e.printStackTrace();
		}

		return zones;
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// CONVERSIÓN JSON ↔ JAVA
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Convierte un objeto Zone a JsonObject.
	 */
	private JsonObject zoneToJson(Zone zone) {
		JsonObject json = new JsonObject();

		json.addProperty("id", zone.getZoneId());
		json.addProperty("name", zone.getZoneName());

		// Coordenadas
		JsonArray min = new JsonArray();
		min.add(zone.getMinX());
		min.add(zone.getMinY());
		min.add(zone.getMinZ());
		json.add("min", min);

		JsonArray max = new JsonArray();
		max.add(zone.getMaxX());
		max.add(zone.getMaxY());
		max.add(zone.getMaxZ());
		json.add("max", max);

		// Mensajes
		json.addProperty("enterMsg", zone.getEnterMessage());
		json.addProperty("exitMsg", zone.getExitMessage());

		return json;
	}

	/**
	 * Convierte un JsonObject a objeto Zone.
	 */
	private Zone jsonToZone(JsonObject json) {
		try {
			String name = json.get("name").getAsString();
			JsonArray min = json.get("min").getAsJsonArray();
			JsonArray max = json.get("max").getAsJsonArray();

			Zone zone = new Zone(
				name,
				min.get(0).getAsDouble(),
				min.get(1).getAsDouble(),
				min.get(2).getAsDouble(),
				max.get(0).getAsDouble(),
				max.get(1).getAsDouble(),
				max.get(2).getAsDouble()
			);

			zone.setEnterMessage(json.get("enterMsg").getAsString());
			zone.setExitMessage(json.get("exitMsg").getAsString());

			return zone;
		} catch (Exception e) {
			System.err.println("[ConfigManager] ✗ Error desserializando zona:");
			e.printStackTrace();
			return null;
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// UTILIDADES
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Asegura que el directorio de config existe.
	 */
	private void ensureConfigDirectory() {
		try {
			Path path = Paths.get(CONFIG_DIR);
			Files.createDirectories(path);
		} catch (Exception e) {
			System.err.println("[ConfigManager] ✗ Error creando directorio:");
			e.printStackTrace();
		}
	}

	/**
	 * Inicializa una configuración vacía.
	 */
	private void initializeEmpty() {
		configRoot = new JsonObject();
		zonesArray = new JsonArray();
		configRoot.add("zones", zonesArray);
		System.out.println("[ConfigManager] ⚠ Config inicializada vacía");
	}
}