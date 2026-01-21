package com.neokey.zoneautomessage.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import com.neokey.zoneautomessage.ZoneAutoMessageMod;
import com.neokey.zoneautomessage.zone.Zone;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║           ZONE COMMANDS - Sistema de Comandos del Mod                     ║
 * ║                                                                          ║
 * ║ Comandos disponibles:                                                    ║
 * ║ /zam create <nombre> - Crear zona desde selección                       ║
 * ║ /zam delete <nombre> - Eliminar zona                                    ║
 * ║ /zam list - Listar todas las zonas                                      ║
 * ║ /zam info <nombre> - Ver información de una zona                        ║
 * ║ /zam setenter <nombre> <mensaje> - Cambiar mensaje de entrada          ║
 * ║ /zam setexit <nombre> <mensaje> - Cambiar mensaje de salida            ║
 * ║ /zam clear - Limpiar selección actual                                   ║
 * ║ /zam toggle - Activar/desactivar mod                                    ║
 * ║                                                                          ║
 * ║ Autor: NeoKey                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class ZoneCommands {

    /**
     * Registra todos los comandos del mod.
     *
     * @param dispatcher Dispatcher de comandos de Fabric
     */
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("zam")
            .then(ClientCommandManager.literal("create")
                .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                    .executes(ZoneCommands::createZone)))
            
            .then(ClientCommandManager.literal("delete")
                .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                    .executes(ZoneCommands::deleteZone)))
            
            .then(ClientCommandManager.literal("list")
                .executes(ZoneCommands::listZones))
            
            .then(ClientCommandManager.literal("info")
                .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                    .executes(ZoneCommands::showZoneInfo)))
            
            .then(ClientCommandManager.literal("setenter")
                .then(ClientCommandManager.argument("name", StringArgumentType.string())
                    .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(ZoneCommands::setEnterMessage))))
            
            .then(ClientCommandManager.literal("setexit")
                .then(ClientCommandManager.argument("name", StringArgumentType.string())
                    .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(ZoneCommands::setExitMessage))))
            
            .then(ClientCommandManager.literal("clear")
                .executes(ZoneCommands::clearSelection))
            
            .then(ClientCommandManager.literal("toggle")
                .executes(ZoneCommands::toggleMod))
            
            .executes(ZoneCommands::showHelp)
        );

        System.out.println("[ZoneCommands] ✓ Comandos registrados: /zam");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // IMPLEMENTACIÓN DE COMANDOS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * /zam create <nombre>
     * Crea una zona desde la selección actual.
     */
    private static int createZone(CommandContext<FabricClientCommandSource> context) {
        String zoneName = StringArgumentType.getString(context, "name");

        Zone zone = ZoneAutoMessageMod.getSelectionManager().createZoneFromSelection(zoneName);
        
        if (zone != null) {
            ZoneAutoMessageMod.getZoneManager().createZoneFromObject(zone);
            ZoneAutoMessageMod.getWorldConfigManager().addZone(zone);
            
            sendFeedback(context, String.format(
                "§a✓ Zona creada exitosamente: §f%s\n" +
                "§7Usa §e/zam setenter %s <mensaje> §7para personalizar el mensaje de entrada",
                zoneName, zoneName
            ));
        }

        return 1;
    }

    /**
     * /zam delete <nombre>
     * Elimina una zona.
     */
    private static int deleteZone(CommandContext<FabricClientCommandSource> context) {
        String zoneName = StringArgumentType.getString(context, "name");

        Zone zone = ZoneAutoMessageMod.getZoneManager().getZoneByName(zoneName);
        
        if (zone == null) {
            sendError(context, "§c✗ No existe una zona con ese nombre");
            return 0;
        }

        ZoneAutoMessageMod.getZoneManager().deleteZone(zone.getZoneId());
        ZoneAutoMessageMod.getWorldConfigManager().removeZone(zone.getZoneId());
        
        sendFeedback(context, "§a✓ Zona eliminada: §f" + zoneName);
        return 1;
    }

    /**
     * /zam list
     * Lista todas las zonas del mundo actual.
     */
    private static int listZones(CommandContext<FabricClientCommandSource> context) {
        var zones = ZoneAutoMessageMod.getZoneManager().getAllZones();
        
        if (zones.isEmpty()) {
            sendFeedback(context, "§7No hay zonas creadas en este mundo");
            return 1;
        }

        StringBuilder sb = new StringBuilder("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append(String.format("§6📋 Zonas en: §f%s\n", 
            ZoneAutoMessageMod.getWorldConfigManager().getCurrentWorldId()));
        sb.append("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        int index = 1;
        for (Zone zone : zones) {
            int[] dims = {
                (int)(zone.getMaxX() - zone.getMinX()),
                (int)(zone.getMaxY() - zone.getMinY()),
                (int)(zone.getMaxZ() - zone.getMinZ())
            };
            
            sb.append(String.format(
                "§7%d. §f%s §7(%dx%dx%d bloques)\n",
                index++, zone.getZoneName(), dims[0], dims[1], dims[2]
            ));
        }

        sb.append("§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sendFeedback(context, sb.toString());
        return 1;
    }

    /**
     * /zam info <nombre>
     * Muestra información detallada de una zona.
     */
    private static int showZoneInfo(CommandContext<FabricClientCommandSource> context) {
        String zoneName = StringArgumentType.getString(context, "name");
        Zone zone = ZoneAutoMessageMod.getZoneManager().getZoneByName(zoneName);

        if (zone == null) {
            sendError(context, "§c✗ No existe una zona con ese nombre");
            return 0;
        }

        String info = String.format(
            "§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "§6📦 Información de Zona\n" +
            "§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "§7Nombre: §f%s\n" +
            "§7ID: §8%s\n" +
            "§7Punto 1: §f[%.0f, %.0f, %.0f]\n" +
            "§7Punto 2: §f[%.0f, %.0f, %.0f]\n" +
            "§7Mensaje entrada: §f%s\n" +
            "§7Mensaje salida: §f%s\n" +
            "§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            zone.getZoneName(),
            zone.getZoneId(),
            zone.getMinX(), zone.getMinY(), zone.getMinZ(),
            zone.getMaxX(), zone.getMaxY(), zone.getMaxZ(),
            zone.getEnterMessage(),
            zone.getExitMessage()
        );

        sendFeedback(context, info);
        return 1;
    }

    /**
     * /zam setenter <nombre> <mensaje>
     * Cambia el mensaje de entrada de una zona.
     */
    private static int setEnterMessage(CommandContext<FabricClientCommandSource> context) {
        String zoneName = StringArgumentType.getString(context, "name");
        String message = StringArgumentType.getString(context, "message");

        Zone zone = ZoneAutoMessageMod.getZoneManager().getZoneByName(zoneName);

        if (zone == null) {
            sendError(context, "§c✗ No existe una zona con ese nombre");
            return 0;
        }

        zone.setEnterMessage(message);
        ZoneAutoMessageMod.getWorldConfigManager().removeZone(zone.getZoneId());
        ZoneAutoMessageMod.getWorldConfigManager().addZone(zone);

        sendFeedback(context, String.format(
            "§a✓ Mensaje de entrada actualizado:\n§f%s", message
        ));
        return 1;
    }

    /**
     * /zam setexit <nombre> <mensaje>
     * Cambia el mensaje de salida de una zona.
     */
    private static int setExitMessage(CommandContext<FabricClientCommandSource> context) {
        String zoneName = StringArgumentType.getString(context, "name");
        String message = StringArgumentType.getString(context, "message");

        Zone zone = ZoneAutoMessageMod.getZoneManager().getZoneByName(zoneName);

        if (zone == null) {
            sendError(context, "§c✗ No existe una zona con ese nombre");
            return 0;
        }

        zone.setExitMessage(message);
        ZoneAutoMessageMod.getWorldConfigManager().removeZone(zone.getZoneId());
        ZoneAutoMessageMod.getWorldConfigManager().addZone(zone);

        sendFeedback(context, String.format(
            "§a✓ Mensaje de salida actualizado:\n§f%s", message
        ));
        return 1;
    }

    /**
     * /zam clear
     * Limpia la selección actual.
     */
    private static int clearSelection(CommandContext<FabricClientCommandSource> context) {
        ZoneAutoMessageMod.getSelectionManager().clearSelection();
        return 1;
    }

    /**
     * /zam toggle
     * Activa/desactiva el mod.
     */
    private static int toggleMod(CommandContext<FabricClientCommandSource> context) {
        ZoneAutoMessageMod.toggleMod();
        
        boolean enabled = ZoneAutoMessageMod.isModEnabled();
        sendFeedback(context, enabled ? 
            "§a✓ Mod activado" : "§c✗ Mod desactivado");
        
        return 1;
    }

    /**
     * /zam (sin argumentos)
     * Muestra ayuda.
     */
    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        String help = 
            "§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "§6Zone Auto Message - Comandos\n" +
            "§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "§7/zam create <nombre> §f- Crear zona\n" +
            "§7/zam delete <nombre> §f- Eliminar zona\n" +
            "§7/zam list §f- Listar zonas\n" +
            "§7/zam info <nombre> §f- Info de zona\n" +
            "§7/zam setenter <nombre> <msg> §f- Mensaje entrada\n" +
            "§7/zam setexit <nombre> <msg> §f- Mensaje salida\n" +
            "§7/zam clear §f- Limpiar selección\n" +
            "§7/zam toggle §f- Activar/desactivar\n" +
            "§e━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "§7Selección: Click izq/der con palo";

        sendFeedback(context, help);
        return 1;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILIDADES
    // ═══════════════════════════════════════════════════════════════════════

    private static void sendFeedback(CommandContext<FabricClientCommandSource> context, String msg) {
        context.getSource().sendFeedback(Text.literal(msg));
    }

    private static void sendError(CommandContext<FabricClientCommandSource> context, String msg) {
        context.getSource().sendError(Text.literal(msg));
    }
}