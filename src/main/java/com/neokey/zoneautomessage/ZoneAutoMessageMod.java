package com.neokey.zoneautomessage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import com.neokey.zoneautomessage.event.PlayerTickHandler;
import com.neokey.zoneautomessage.event.StickInteractionHandler;
import com.neokey.zoneautomessage.manager.ZoneManager;
import com.neokey.zoneautomessage.manager.WorldConfigManager;
import com.neokey.zoneautomessage.manager.SelectionManager;
import com.neokey.zoneautomessage.manager.MessageManager;
import com.neokey.zoneautomessage.command.ZoneCommands;
import com.neokey.zoneautomessage.render.ZoneRenderer;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║       ZONE AUTO MESSAGE v2.0 - Punto de Entrada Principal                ║
 * ║                                                                          ║
 * ║ NUEVAS CARACTERÍSTICAS:                                                  ║
 * ║ - ✨ Renderizado visual 3D de zonas y selecciones                        ║
 * ║ - 💬 Mensajes individuales por jugador (/msg)                            ║
 * ║ - 📏 Buffer dinámico basado en tamaño de zona                            ║
 * ║ - 🎨 Soporte MiniMessage y colores hex (&#rrggbb)                        ║
 * ║ - 📊 Integración con TextPlaceholderAPI                                  ║
 * ║ - 🖼️ GUI visual mejorada                                                 ║
 * ║                                                                          ║
 * ║ Formatos soportados:                                                     ║
 * ║ - Legacy: &c, §c                                                         ║
 * ║ - Hex: &#ff0000                                                          ║
 * ║ - MiniMessage: <gradient:#ff0000:#00ff00>texto</gradient>               ║
 * ║ - Placeholders: [nickname], [zona_name], %zam:zone_count%               ║
 * ║                                                                          ║
 * ║ Autor: NeoKey | Versión: 2.0.0 | MC: 1.21.8                            ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class ZoneAutoMessageMod implements ClientModInitializer {

	// Identificadores del mod
	public static final String MOD_ID = "zoneautomessage";
	public static final String MOD_NAME = "Zone Auto Message";
	public static final String MOD_VERSION = "2.0.0";

	// Keybindings globales
	public static KeyBinding openZoneManager;
	public static KeyBinding toggleMod;
	public static KeyBinding clearSelection;

	// Managers singleton
	private static ZoneManager zoneManager;
	private static WorldConfigManager worldConfigManager;
	private static SelectionManager selectionManager;
	private static boolean modEnabled = true;

	@Override
	public void onInitializeClient() {
		printHeader();

		try {
			// 1. Inicializar WorldConfigManager (detecta mundo actual)
			worldConfigManager = new WorldConfigManager();
			worldConfigManager.loadConfig();
			logSuccess("WorldConfigManager inicializado");

			// 2. Inicializar ZoneManager (carga zonas del mundo actual)
			zoneManager = new ZoneManager();
			zoneManager.loadZones(worldConfigManager.getZonesData());
			logSuccess("ZoneManager inicializado con " + 
				zoneManager.getZoneCount() + " zona(s)");

			// 3. Inicializar SelectionManager (sistema de selección con palo)
			selectionManager = new SelectionManager();
			logSuccess("SelectionManager inicializado");

			// 4. Registrar keybindings
			registerKeybindings();
			logSuccess("Keybindings registrados");

			// 5. Registrar event handlers
			ClientTickEvents.END_CLIENT_TICK.register(new PlayerTickHandler());
			StickInteractionHandler.register();
			logSuccess("Event handlers registrados");

			// 6. Registrar comandos
			ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
				ZoneCommands.register(dispatcher);
			});
			logSuccess("Comandos registrados");

			// 7. Registrar renderizador 3D (NUEVA CARACTERÍSTICA)
			ZoneRenderer.register();
			logSuccess("Renderizador 3D activado");

			// 8. Registrar placeholders personalizados (NUEVA CARACTERÍSTICA)
			MessageManager.registerCustomPlaceholders();
			logSuccess("Placeholders personalizados registrados");

			printFooter();

		} catch (Exception e) {
			System.err.println("[ERROR] Falló la inicialización del mod:");
			e.printStackTrace();
		}
	}

	/**
	 * Registra todos los keybindings del mod.
	 */
	private void registerKeybindings() {
		// Keybinding 1: Abrir gestor de zonas
		openZoneManager = KeyBindingHelper.registerKeyBinding(
			new KeyBinding(
				"key.zoneautomessage.open_manager",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_J,
				"category.zoneautomessage.main"
			)
		);

		// Keybinding 2: Toggle del mod (activar/desactivar)
		toggleMod = KeyBindingHelper.registerKeyBinding(
			new KeyBinding(
				"key.zoneautomessage.toggle",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_U,
				"category.zoneautomessage.main"
			)
		);

		// Keybinding 3: Limpiar selección
		clearSelection = KeyBindingHelper.registerKeyBinding(
			new KeyBinding(
				"key.zoneautomessage.clear_selection",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_N,
				"category.zoneautomessage.main"
			)
		);

		System.out.println("  └─ Ctrl+Shift+J: Abrir gestor de zonas");
		System.out.println("  └─ Ctrl+Shift+U: Activar/Desactivar mod");
		System.out.println("  └─ Ctrl+Shift+N: Limpiar selección");
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// GETTERS Y SETTERS GLOBALES
	// ═══════════════════════════════════════════════════════════════════════════

	public static ZoneManager getZoneManager() {
		if (zoneManager == null) {
			throw new IllegalStateException("ZoneManager no ha sido inicializado");
		}
		return zoneManager;
	}

	public static WorldConfigManager getWorldConfigManager() {
		if (worldConfigManager == null) {
			throw new IllegalStateException("WorldConfigManager no ha sido inicializado");
		}
		return worldConfigManager;
	}

	public static SelectionManager getSelectionManager() {
		if (selectionManager == null) {
			throw new IllegalStateException("SelectionManager no ha sido inicializado");
		}
		return selectionManager;
	}

	public static boolean isModEnabled() {
		return modEnabled;
	}

	public static void setModEnabled(boolean enabled) {
		modEnabled = enabled;
		System.out.println("[MOD] Estado: " + (enabled ? "ACTIVADO" : "DESACTIVADO"));
	}

	public static void toggleMod() {
		setModEnabled(!modEnabled);
	}

	// ═══════════════════════════════════════════════════════════════════════════
	// UTILIDADES DE LOGGING
	// ═══════════════════════════════════════════════════════════════════════════

	private void printHeader() {
		System.out.println("═══════════════════════════════════════════════════════════");
		System.out.println("[" + MOD_NAME + " v" + MOD_VERSION + "] Inicializando...");
		System.out.println("═══════════════════════════════════════════════════════════");
	}

	private void printFooter() {
		System.out.println("═══════════════════════════════════════════════════════════");
		System.out.println("[" + MOD_NAME + "] ✓ Mod cargado exitosamente");
		System.out.println("Mundo actual: " + worldConfigManager.getCurrentWorldId());
		System.out.println("Zonas cargadas: " + zoneManager.getZoneCount());
		System.out.println("═══════════════════════════════════════════════════════════");
		System.out.println("NUEVAS CARACTERÍSTICAS v2.0:");
		System.out.println("  ✨ Renderizado 3D visual de zonas");
		System.out.println("  💬 Mensajes individuales por jugador");
		System.out.println("  📏 Buffer dinámico automático");
		System.out.println("  🎨 Soporte MiniMessage y hex colors");
		System.out.println("  📊 Placeholders avanzados");
		System.out.println("═══════════════════════════════════════════════════════════");
	}

	private void logSuccess(String message) {
		System.out.println("[INIT] ✓ " + message);
	}
}