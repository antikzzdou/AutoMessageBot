package com.neokey.zoneautomessage.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import com.neokey.zoneautomessage.zone.Zone;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║       SELECTION MANAGER - Gestor de Selección de Áreas con Palo          ║
 * ║                                                                          ║
 * ║ Responsabilidades:                                                       ║
 * ║ - Gestionar selección de punto 1 y punto 2 con palo (stick)             ║
 * ║ - Validar selecciones antes de crear zonas                              ║
 * ║ - Proporcionar feedback visual al jugador                                ║
 * ║ - Crear zonas desde selecciones activas                                 ║
 * ║                                                                          ║
 * ║ Uso:                                                                     ║
 * ║ 1. Click izquierdo con palo → Selecciona punto 1                        ║
 * ║ 2. Click derecho con palo → Selecciona punto 2                          ║
 * ║ 3. /zam create <nombre> → Crea zona desde selección                     ║
 * ║                                                                          ║
 * ║ Autor: NeoKey                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class SelectionManager {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    
    // Posiciones seleccionadas
    private BlockPos position1 = null;
    private BlockPos position2 = null;
    
    // Estado de selección
    private boolean hasActiveSelection = false;
    
    /**
     * Establece el punto 1 de la selección (click izquierdo).
     *
     * @param pos Posición del bloque
     */
    public void setPosition1(BlockPos pos) {
        position1 = pos;
        hasActiveSelection = (position2 != null);
        
        sendFeedback(String.format(
            "§a✓ Punto 1 establecido: §7[%d, %d, %d]",
            pos.getX(), pos.getY(), pos.getZ()
        ));
        
        if (hasActiveSelection) {
            showSelectionInfo();
        }
    }
    
    /**
     * Establece el punto 2 de la selección (click derecho).
     *
     * @param pos Posición del bloque
     */
    public void setPosition2(BlockPos pos) {
        position2 = pos;
        hasActiveSelection = (position1 != null);
        
        sendFeedback(String.format(
            "§a✓ Punto 2 establecido: §7[%d, %d, %d]",
            pos.getX(), pos.getY(), pos.getZ()
        ));
        
        if (hasActiveSelection) {
            showSelectionInfo();
        }
    }
    
    /**
     * Muestra información sobre la selección actual.
     */
    private void showSelectionInfo() {
        if (!hasActiveSelection) {
            return;
        }
        
        int volume = getSelectionVolume();
        int[] dimensions = getSelectionDimensions();
        
        sendFeedback(String.format(
            "§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "§6📦 Selección Completa\n" +
            "§7Dimensiones: §f%dx%dx%d bloques\n" +
            "§7Volumen: §f%,d bloques³\n" +
            "§7Usa §a/zam create <nombre> §7para crear la zona\n" +
            "§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            dimensions[0], dimensions[1], dimensions[2], volume
        ));
    }
    
    /**
     * Crea una zona desde la selección actual.
     *
     * @param zoneName Nombre de la zona a crear
     * @return La zona creada, o null si la selección no es válida
     */
    public Zone createZoneFromSelection(String zoneName) {
        if (!hasActiveSelection) {
            sendError("§c✗ Selección incompleta. Necesitas seleccionar dos puntos.");
            return null;
        }
        
        if (!isValidSelection()) {
            sendError("§c✗ Selección inválida. Los puntos deben formar un volumen válido.");
            return null;
        }
        
        // Obtener coordenadas normalizadas (min/max)
        double minX = Math.min(position1.getX(), position2.getX());
        double minY = Math.min(position1.getY(), position2.getY());
        double minZ = Math.min(position1.getZ(), position2.getZ());
        
        double maxX = Math.max(position1.getX(), position2.getX());
        double maxY = Math.max(position1.getY(), position2.getY());
        double maxZ = Math.max(position1.getZ(), position2.getZ());
        
        // Crear la zona
        Zone zone = new Zone(zoneName, minX, minY, minZ, maxX, maxY, maxZ);
        
        int volume = getSelectionVolume();
        sendFeedback(String.format(
            "§a✓ Zona creada: §f%s\n" +
            "§7Volumen: §f%,d bloques³",
            zoneName, volume
        ));
        
        // Limpiar selección
        clearSelection();
        
        return zone;
    }
    
    /**
     * Limpia la selección actual.
     */
    public void clearSelection() {
        position1 = null;
        position2 = null;
        hasActiveSelection = false;
        sendFeedback("§7Selección limpiada");
    }
    
    /**
     * Obtiene las dimensiones de la selección [X, Y, Z].
     *
     * @return Array con dimensiones [ancho, alto, profundidad]
     */
    public int[] getSelectionDimensions() {
        if (!hasActiveSelection) {
            return new int[]{0, 0, 0};
        }
        
        int width = Math.abs(position1.getX() - position2.getX()) + 1;
        int height = Math.abs(position1.getY() - position2.getY()) + 1;
        int depth = Math.abs(position1.getZ() - position2.getZ()) + 1;
        
        return new int[]{width, height, depth};
    }
    
    /**
     * Calcula el volumen de la selección.
     *
     * @return Volumen en bloques cúbicos
     */
    public int getSelectionVolume() {
        if (!hasActiveSelection) {
            return 0;
        }
        
        int[] dims = getSelectionDimensions();
        return dims[0] * dims[1] * dims[2];
    }
    
    /**
     * Valida que la selección es correcta.
     *
     * @return true si es válida
     */
    private boolean isValidSelection() {
        if (position1 == null || position2 == null) {
            return false;
        }
        
        int[] dims = getSelectionDimensions();
        return dims[0] > 0 && dims[1] > 0 && dims[2] > 0;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════════════════
    
    public boolean hasActiveSelection() {
        return hasActiveSelection;
    }
    
    public BlockPos getPosition1() {
        return position1;
    }
    
    public BlockPos getPosition2() {
        return position2;
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // UTILIDADES DE FEEDBACK
    // ═══════════════════════════════════════════════════════════════════════
    
    private void sendFeedback(String message) {
        if (CLIENT.player != null) {
            CLIENT.player.sendMessage(Text.literal(message), false);
        }
    }
    
    private void sendError(String message) {
        if (CLIENT.player != null) {
            CLIENT.player.sendMessage(Text.literal(message), false);
        }
    }
}