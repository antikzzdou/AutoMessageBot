package com.neokey.zoneautomessage.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import com.neokey.zoneautomessage.ZoneAutoMessageMod;
import com.neokey.zoneautomessage.zone.Zone;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║      ZONE RENDERER - Renderizado Visual 3D de Zonas y Selección          ║
 * ║                                                                          ║
 * ║ Características:                                                         ║
 * ║ - Renderiza cuboides 3D en el mundo                                     ║
 * ║ - Muestra selecciones activas en tiempo real                            ║
 * ║ - Diferentes colores para zonas/selecciones                             ║
 * ║ - Renderiza cajas con líneas                                            ║
 * ║ - Sistema optimizado para no afectar FPS                                ║
 * ║                                                                          ║
 * ║ SIMPLIFICADO PARA MC 1.21.8 - Solo líneas, sin caras                    ║
 * ║ Autor: NeoKey                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class ZoneRenderer {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    
    // Colores para renderizado (RGBA)
    private static final int ZONE_COLOR = 0x4000FF00; // Verde semitransparente
    private static final int SELECTION_COLOR = 0x60FF0000; // Rojo semitransparente
    private static final int POINT1_COLOR = 0xFFFF0000; // Rojo brillante
    private static final int POINT2_COLOR = 0xFF0000FF; // Azul brillante
    
    /**
     * Registra el renderizador en el event loop de Fabric.
     */
    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(ZoneRenderer::renderZonesAndSelection);
        System.out.println("[ZoneRenderer] ✓ Renderizador 3D registrado");
    }

    /**
     * Renderiza todas las zonas y la selección actual.
     */
    private static void renderZonesAndSelection(WorldRenderContext context) {
        if (CLIENT.player == null || CLIENT.world == null) {
            return;
        }

        if (!ZoneAutoMessageMod.isModEnabled()) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();

        // Preparar renderizado
        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // Renderizar todas las zonas guardadas
        for (Zone zone : ZoneAutoMessageMod.getZoneManager().getAllZones()) {
            renderZone(matrices, zone);
        }

        // Renderizar selección activa (si existe)
        renderActiveSelection(matrices);

        matrices.pop();
    }

    /**
     * Renderiza una zona específica.
     */
    private static void renderZone(MatrixStack matrices, Zone zone) {
        double minX = zone.getMinX();
        double minY = zone.getMinY();
        double minZ = zone.getMinZ();
        double maxX = zone.getMaxX() + 1;
        double maxY = zone.getMaxY() + 1;
        double maxZ = zone.getMaxZ() + 1;

        renderBox(matrices, minX, minY, minZ, maxX, maxY, maxZ, ZONE_COLOR);
    }

    /**
     * Renderiza la selección activa del jugador (con palo).
     */
    private static void renderActiveSelection(MatrixStack matrices) {
        var selectionManager = ZoneAutoMessageMod.getSelectionManager();
        
        BlockPos pos1 = selectionManager.getPosition1();
        BlockPos pos2 = selectionManager.getPosition2();

        // Renderizar punto 1 (si existe)
        if (pos1 != null) {
            renderPoint(matrices, pos1, POINT1_COLOR);
        }

        // Renderizar punto 2 (si existe)
        if (pos2 != null) {
            renderPoint(matrices, pos2, POINT2_COLOR);
        }

        // Renderizar caja de selección (si ambos puntos existen)
        if (selectionManager.hasActiveSelection()) {
            double minX = Math.min(pos1.getX(), pos2.getX());
            double minY = Math.min(pos1.getY(), pos2.getY());
            double minZ = Math.min(pos1.getZ(), pos2.getZ());
            double maxX = Math.max(pos1.getX(), pos2.getX()) + 1;
            double maxY = Math.max(pos1.getY(), pos2.getY()) + 1;
            double maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1;

            renderBox(matrices, minX, minY, minZ, maxX, maxY, maxZ, SELECTION_COLOR);
        }
    }

    /**
     * Renderiza un punto específico (bloque destacado).
     */
    private static void renderPoint(MatrixStack matrices, BlockPos pos, int color) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        renderBox(matrices, x, y, z, x + 1, y + 1, z + 1, color);
    }

    /**
     * Renderiza un cubo 3D usando WorldRenderer.drawBox.
     * SIMPLIFICADO: Usa el método nativo de Minecraft para dibujar cajas.
     */
    private static void renderBox(MatrixStack matrices, double minX, double minY, double minZ,
                                   double maxX, double maxY, double maxZ, int color) {
        
        // Extraer componentes ARGB del color
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        // Usar el método nativo de Minecraft para dibujar líneas de caja
        WorldRenderer.drawBox(
            matrices,
            Tessellator.getInstance().begin(
                VertexFormat.Mode.DEBUG_LINES,
                VertexFormats.POSITION_COLOR
            ),
            minX, minY, minZ,
            maxX, maxY, maxZ,
            r, g, b, a
        );
    }
}