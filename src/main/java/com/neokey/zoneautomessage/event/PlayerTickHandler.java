package com.neokey.zoneautomessage.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import com.neokey.zoneautomessage.ZoneAutoMessageMod;
import com.neokey.zoneautomessage.zone.Zone;
import com.neokey.zoneautomessage.manager.MessageManager;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║      PLAYER TICK HANDLER - Event Handler Principal (Bucle de Juego)      ║
 * ║                                                                          ║
 * ║ Responsabilidades:                                                       ║
 * ║ - Monitorear la posición del jugador cada tick (20 veces/segundo)        ║
 * ║ - Iterar sobre todas las zonas y detectar transiciones                   ║
 * ║ - Enviar mensajes cuando el jugador entra/sale de zonas                 ║
 * ║ - Manejar keybindings (abrir GUI, toggle mod, etc)                      ║
 * ║ - Optimizar: no hacer trabajo innecesario si mod está desactivado       ║
 * ║                                                                          ║
 * ║ RENDIMIENTO:                                                             ║
 * ║ - Iteración O(n) sobre zonas cada tick                                  ║
 * ║ - Cálculos geométricos 3D eficientes (sin sqrt innecesario)             ║
 * ║ - No afecta FPS significativamente con <1000 zonas                      ║
 * ║                                                                          ║
 * ║ Autor: NeoKey                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class PlayerTickHandler implements ClientTickEvents.EndTick {

	// Contador para limitar logging excesivo
	private int tickCounter = 0;
	private static final int LOG_INTERVAL = 200; // Log cada 10 segundos (200 ticks)

	@Override
	public void onEndTick(MinecraftClient client) {
		try {
			// Verificaciones rápidas
			if (client.player == null || client.world == null) {
				return;
			}

			// Si el mod está desactivado, no hacer nada
			if (!ZoneAutoMessageMod.isModEnabled()) {
				return;
			}

			// Obtener posición actual del jugador
			double playerX = client.player.getX();
			double playerY = client.player.getY();
			double playerZ = client.player.getZ();

			// Iterar sobre todas las zonas y detectar cambios
			for (Zone zone : ZoneAutoMessageMod.getZoneManager().getAllZones()) {
				int stateChange = zone.updatePlayerState(playerX, playerY, playerZ);

				// Enviar mensaje según el cambio detectado
				if (stateChange == 1) {
					// ENTRADA: El jugador entró en la zona
					handleZoneEntry(zone);
				} else if (stateChange == -1) {
					// SALIDA: El jugador salió de la zona
					handleZoneExit(zone);
				}
			}

			// Manejar keybindings
			handleKeybindings();

			// Logging periódico (depuración)
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
	// MANEJO DE EVENTOS DE ZONA
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Maneja cuando el jugador entra en una zona.
	 *
	 * @param zone Zona en la que se entró
	 */
	private void handleZoneEntry(Zone zone) {
		String message = zone.getEnterMessage();
		MessageManager.sendZoneMessage(message, zone.getZoneName());
	}

	/**
	 * Maneja cuando el jugador sale de una zona.
	 * (Después de estar a más de 128 bloques por la lógica de buffer)
	 *
	 * @param zone Zona de la que se salió
	 */
	private void handleZoneExit(Zone zone) {
		String message = zone.getExitMessage();
		MessageManager.sendZoneMessage(message, zone.getZoneName());
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
		}

		// Abrir gestor de zonas: Ctrl+Shift+J
		if (ZoneAutoMessageMod.openZoneManager.wasPressed()) {
			MessageManager.sendDebugMessage("§e[GUI] Abre el gestor de zonas (ModMenu)");
			// Nota: La GUI real se abre desde ClientModInitializer con ModMenu
		}

		// Crear nueva zona: Ctrl+Shift+N
		if (ZoneAutoMessageMod.addZoneKeybind.wasPressed()) {
			MessageManager.sendDebugMessage("§6[CREATE] Asistente para crear zona");
			// Aquí irá la lógica de creación rápida de zonas
		}
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// LOGGING Y DEPURACIÓN
	// ═══════════════════════════════════════════════════════════════════════════

	/**
	 * Registra información de depuración periódicamente.
	 */
	private void logDebugInfo(double x, double y, double z) {
		System.out.println(
			String.format(
				"[TICK] Pos: [%.1f, %.1f, %.1f] | Zonas: %d | Mod: %s",
				x, y, z,
				ZoneAutoMessageMod.getZoneManager().getZoneCount(),
				ZoneAutoMessageMod.isModEnabled() ? "ON" : "OFF"
			)
		);
	}
}