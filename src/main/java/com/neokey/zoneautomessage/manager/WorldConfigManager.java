package com.neokey.zoneautomessage.manager;

import com.google.gson.*;
import com.neokey.zoneautomessage.zone.Zone;
import net.minecraft.client.MinecraftClient;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║    WORLD CONFIG MANAGER - Guardado Independiente por Mundo/Servidor      ║
 * ║                                                                          ║
 * ║ Responsabilidades:                                                       ║
 * ║ - Guardar configuración separada para cada mundo/servidor               ║
 * ║ - Detectar automáticamente el mundo actual                              ║
 * ║ - Cargar zonas específicas del mundo al entrar                          ║
 * ║ - Estructura: config/zoneautomessage/worlds/<world_id>/zones.json       ║
 * ║                                                                          ║
 * ║ Identificación de mundos:                                               ║
 * ║ - Singleplayer: Nombre de la carpeta del mundo                          ║
 * ║ - Multiplayer: IP del servidor + puerto                                 ║
 * ║                                                                          ║
 * ║ Autor: NeoKey                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class WorldConfigManager {

    private static final String CONFIG_DIR = "config/zoneautomessage/worlds";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    
    private String currentWorldId = null;
    private JsonObject configRoot;
    private JsonArray zonesArray;

    /**
     * Constructor. Inicializa el gestor.
     */
    public WorldConfigManager() {
        System.out.println("[WorldConfigManager] Inicializando gestor por mundo...");
    }

    /**
     * Detecta el identificador único del mundo actual.
     *
     * @return ID del mundo (carpeta o servidor)
     */
    public String detectCurrentWorld() {
        if (CLIENT.isInSingleplayer()) {
            // Mundo local
            if (CLIENT.getServer() != null && CLIENT.getServer().getSaveProperties() != null) {
                String worldName = CLIENT.getServer().getSaveProperties().getLevelName();
                currentWorldId = sanitizeWorldId(worldName);
                System.out.println("[WorldConfigManager] Mundo detectado (Local): " + currentWorldId);
            } else {
                currentWorldId = "local_unknown";
            }
        } else {
            // Servidor multiplayer
            if (CLIENT.getCurrentServerEntry() != null) {
                String serverAddress = CLIENT.getCurrentServerEntry().address;
                currentWorldId = sanitizeWorldId(serverAddress);
                System.out.println("[WorldConfigManager] Mundo detectado (Server): " + currentWorldId);
            } else {
                currentWorldId = "server_unknown";
            }
        }
        
        return currentWorldId;
    }

    /**
     * Sanitiza el ID del mundo para usarlo como nombre de carpeta.
     *
     * @param rawId ID sin sanitizar
     * @return ID válido para sistema de archivos
     */
    private String sanitizeWorldId(String rawId) {
        return rawId
            .replaceAll("[^a-zA-Z0-9._-]", "_")
            .replaceAll(":", "_")
            .toLowerCase();
    }

    /**
     * Carga la configuración del mundo actual.
     */
    public void loadConfig() {
        detectCurrentWorld();
        
        try {
            String configPath = getConfigFilePath();
            File configFile = new File(configPath);

            if (configFile.exists()) {
                String content = new String(Files.readAllBytes(configFile.toPath()));
                configRoot = JsonParser.parseString(content).getAsJsonObject();
                System.out.println("[WorldConfigManager] ✓ Config cargada: " + configPath);
            } else {
                configRoot = new JsonObject();
                zonesArray = new JsonArray();
                configRoot.add("zones", zonesArray);
                configRoot.addProperty("world_id", currentWorldId);
                configRoot.addProperty("created_at", System.currentTimeMillis());
                saveConfig();
                System.out.println("[WorldConfigManager] ✓ Config nueva creada: " + configPath);
            }

            if (!configRoot.has("zones")) {
                zonesArray = new JsonArray();
                configRoot.add("zones", zonesArray);
            } else {
                zonesArray = configRoot.getAsJsonArray("zones");
            }

        } catch (Exception e) {
            System.err.println("[WorldConfigManager] ✗ Error cargando config:");
            e.printStackTrace();
            initializeEmpty();
        }
    }

    /**
     * Guarda la configuración del mundo actual.
     */
    public void saveConfig() {
        try {
            ensureWorldDirectory();
            String configPath = getConfigFilePath();
            
            // Actualizar timestamp
            configRoot.addProperty("last_modified", System.currentTimeMillis());
            
            String jsonString = GSON.toJson(configRoot);
            Files.write(Paths.get(configPath), jsonString.getBytes());
            System.out.println("[WorldConfigManager] ✓ Config guardada: " + configPath);
        } catch (Exception e) {
            System.err.println("[WorldConfigManager] ✗ Error guardando config:");
            e.printStackTrace();
        }
    }

    /**
     * Añade una zona a la configuración del mundo actual.
     *
     * @param zone Zona a guardar
     */
    public void addZone(Zone zone) {
        try {
            JsonObject zoneJson = zoneToJson(zone);
            zonesArray.add(zoneJson);
            saveConfig();
            System.out.println("[WorldConfigManager] ✓ Zona guardada en mundo: " + currentWorldId);
        } catch (Exception e) {
            System.err.println("[WorldConfigManager] ✗ Error añadiendo zona:");
            e.printStackTrace();
        }
    }

    /**
     * Elimina una zona del mundo actual.
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
            System.out.println("[WorldConfigManager] ✓ Zona eliminada del mundo: " + currentWorldId);
        } catch (Exception e) {
            System.err.println("[WorldConfigManager] ✗ Error eliminando zona:");
            e.printStackTrace();
        }
    }

    /**
     * Obtiene todas las zonas del mundo actual.
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
            System.err.println("[WorldConfigManager] ✗ Error leyendo zonas:");
            e.printStackTrace();
        }

        return zones;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CONVERSIÓN JSON ↔ JAVA
    // ═══════════════════════════════════════════════════════════════════════

    private JsonObject zoneToJson(Zone zone) {
        JsonObject json = new JsonObject();

        json.addProperty("id", zone.getZoneId());
        json.addProperty("name", zone.getZoneName());

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

        json.addProperty("enterMsg", zone.getEnterMessage());
        json.addProperty("exitMsg", zone.getExitMessage());
        json.addProperty("created_at", System.currentTimeMillis());

        return json;
    }

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
            System.err.println("[WorldConfigManager] ✗ Error deserializando zona:");
            e.printStackTrace();
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ═══════════════════════════════════════════════════════════════════════

    private String getConfigFilePath() {
        return CONFIG_DIR + "/" + currentWorldId + "/zones.json";
    }

    private void ensureWorldDirectory() {
        try {
            Path path = Paths.get(CONFIG_DIR + "/" + currentWorldId);
            Files.createDirectories(path);
        } catch (Exception e) {
            System.err.println("[WorldConfigManager] ✗ Error creando directorio:");
            e.printStackTrace();
        }
    }

    private void initializeEmpty() {
        configRoot = new JsonObject();
        zonesArray = new JsonArray();
        configRoot.add("zones", zonesArray);
        configRoot.addProperty("world_id", currentWorldId);
        System.out.println("[WorldConfigManager] ⚠ Config inicializada vacía");
    }

    public String getCurrentWorldId() {
        return currentWorldId;
    }
}