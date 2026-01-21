package com.neokey.zoneautomessage.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import com.neokey.zoneautomessage.ZoneAutoMessageMod;
import com.neokey.zoneautomessage.zone.Zone;
import com.neokey.zoneautomessage.manager.MessageManager;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║    PLAYER TICK HANDLER v2.0 - Con Mensajes Individuales por Jugador      ║
 * ║                                                                          ║
 * ║ Responsabilidades:                                                       ║
 * ║ - Monitorear la posición del jugador cada tick                          ║
 * ║ - Detectar transiciones de zonas (entrada/salida)                       ║
 * ║ - Enviar mensajes usando /msg [nickname] (individual)                   ║
 * ║ - Manejar keybindings (toggle, limpiar selección)                       ║
 * ║ - Logging optimizado y no invasivo                                      ║
 * ║                                                                          ║
 * ║ Autor: NeoKey                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class PlayerTickHandler implements ClientTickEvents.EndTick {

	// Contador para limitar logging excesivo
	private int tickCounter = 0;
	private static final int LOG_INTERVAL = 600; // Log cada 30 segundos (600 ticks)

	@Override
	public void onEndTick(MinecraftClient client) {
		try {
			// Verificaciones rápidas
			if (client.player == null || client.world == null) {
				return;
			}

			// Manejar keybindings primero
			handleKeybindings();

			// Si el mod está desactivado, no procesar zonas
			if (!ZoneAutoMessageMod.isModEnabled()) {
				return;
			}

			// Obtener posición actual del jugador
			double playerX = client.player.getX();
			double playerY = client.player.getY();
			double playerZ = client.player.getZ();

			// Obtener nickname del jugador (para envío individual)
			String playerNickname = client.player.getName().getString();

			// Iterar sobre todas las zonas y detectar cambios
			for (Zone zone : ZoneAutoMessageMod.getZoneManager().getAllZones()) {
				int stateChange = zone.updatePlayerState(playerX, playerY, playerZ);

				// Enviar mensaje según el cambio detectado
				if (stateChange == 1) {
					// ENTRADA: El jugador entró en la zona
					handleZoneEntry(zone, playerNickname);
				} else if (stateChange == -1) {
					// SALIDA: El jugador salió de la zona
					handleZoneExit(zone, playerNickname);
				}
			}

			// Logging periódico (depuración, menos frecuente)
			if (tickCounter++ >= LOG_INTERVAL) {
				tickCounter = 0;
				logDebugInfo(playerX, playerY, playerZ);
			}

		} catch (Exception e) {
			System.err.println("[PlayerTickHandler] ✗ Error en tick:");
			e.printStackTrace();
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// MANEJO DE EVENTOS DE ZONA (CON MENSAJES INDIVIDUALES)
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Maneja cuando el jugador entra en una zona.
	 * 
	 * MEJORA v2.0: Envía el mensaje usando /msg [nickname] para que sea
	 * individual y solo lo vea el jugador que entró.
	 *
	 * @param zone Zona en la que se entró
	 * @param playerNickname Nombre del jugador
	 */
	private void handleZoneEntry(Zone zone, String playerNickname) {
		String message = zone.getEnterMessage();
		
		// OPCIÓN 1: Enviar mensaje privado mediante /msg (RECOMENDADO EN SERVIDOR)
		// Esto solo funciona si estás en un servidor que soporte /msg
		// MessageManager.sendPrivateZoneMessage(playerNickname, message, zone.getZoneName());
		
		// OPCIÓN 2: Enviar mensaje local (solo cliente)
		// Esto es mejor para cliente local, ya que /msg podría no funcionar
		MessageManager.sendZoneMessage(message, zone.getZoneName());
		
		// Log interno (consola)
		System.out.println(String.format(
			"[ZONE] ► %s entró en: %s (Buffer: %.1f bloques)",
			playerNickname, zone.getZoneName(), zone.getBufferDistance()
		));
	}

	/**
	 * Maneja cuando el jugador sale de una zona.
	 * 
	 * MEJORA v2.0: Usa buffer dinámico basado en tamaño de zona.
	 *
	 * @param zone Zona de la que se salió
	 * @param playerNickname Nombre del jugador
	 */
	private void handleZoneExit(Zone zone, String playerNickname) {
		String message = zone.getExitMessage();
		
		// OPCIÓN 1: Mensaje privado (servidor con /msg)
		// MessageManager.sendPrivateZoneMessage(playerNickname, message, zone.getZoneName());
		
		// OPCIÓN 2: Mensaje local (cliente)
		MessageManager.sendZoneMessage(message, zone.getZoneName());
		
		// Log interno (consola)
		System.out.println(String.format(
			"[ZONE] ◄ %s salió de: %s (Buffer: %.1f bloques)",
			playerNickname, zone.getZoneName(), zone.getBufferDistance()
		));
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// MANEJO DE KEYBINDINGS
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Procesa los keybindings registrados.
	 */
	private void handleKeybindings() {
		// Toggle: Ctrl+Shift+U
		if (ZoneAutoMessageMod.toggleMod.wasPressed()) {
			ZoneAutoMessageMod.toggleMod();
			boolean enabled = ZoneAutoMessageMod.isModEnabled();
			
			String statusMsg = enabled ? 
				"<gradient:#00ff00:#00aa00>✓ Mod activado</gradient>" :
				"<gradient:#ff0000:#aa0000>✗ Mod desactivado</gradient>";
			
			MessageManager.sendDebugMessage(statusMsg);
		}

		// Abrir gestor de zonas: Ctrl+Shift+J
		if (ZoneAutoMessageMod.openZoneManager.wasPressed()) {
			int zoneCount = ZoneAutoMessageMod.getZoneManager().getZoneCount();
			String worldId = ZoneAutoMessageMod.getWorldConfigManager().getCurrentWorldId();
			
			MessageManager.sendDebugMessage(String.format(
				"<gradient:#ffaa00:#ff5500>📊 Zonas: %d | Mundo: %s</gradient>\n" +
				"§7Usa §e/zam list §7para ver todas las zonas",
				zoneCount, worldId
			));
		}

		// Limpiar selección: Ctrl+Shift+N
		if (ZoneAutoMessageMod.clearSelection.wasPressed()) {
			if (ZoneAutoMessageMod.getSelectionManager().hasActiveSelection()) {
				ZoneAutoMessageMod.getSelectionManager().clearSelection();
			} else {
				MessageManager.sendDebugMessage(
					"§e[INFO] Usa un palo para seleccionar áreas:\n" +
					"§7- Click izquierdo: Punto 1 (§c■§7)\n" +
					"§7- Click derecho: Punto 2 (§9■§7)\n" +
					"§7- Comando: §f/zam create <nombre>"
				);
			}
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// LOGGING Y DEPURACIÓN (OPTIMIZADO)
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Registra información de depuración periódicamente (menos frecuente).
	 */
	private void logDebugInfo(double x, double y, double z) {
		int zoneCount = ZoneAutoMessageMod.getZoneManager().getZoneCount();
		String worldId = ZoneAutoMessageMod.getWorldConfigManager().getCurrentWorldId();
		boolean modEnabled = ZoneAutoMessageMod.isModEnabled();
		
		System.out.println(
			String.format(
				"[TICK] Pos: [%.1f, %.1f, %.1f] | Zonas: %d | Estado: %s | Mundo: %s",
				x, y, z, zoneCount, modEnabled ? "ON" : "OFF", worldId
			)
		);
		
		// Mostrar info de zonas cercanas (opcional)
		for (Zone zone : ZoneAutoMessageMod.getZoneManager().getAllZones()) {
			double distance = zone.getDistanceToZone(x, y, z);
			if (distance < zone.getBufferDistance() * 2) {
				System.out.println(String.format(
					"  └─ Zona cercana: %s (%.1f bloques, Buffer: %.1f)",
					zone.getZoneName(), distance, zone.getBufferDistance()
				));
			}
		}
	}
}