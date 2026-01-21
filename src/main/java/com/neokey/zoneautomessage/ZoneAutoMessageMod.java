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
import com.neokey.zoneautomessage.command.ZoneCommands;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║          ZONE AUTO MESSAGE - Punto de Entrada Principal                  ║
 * ║                                                                          ║
 * ║ Características:                                                         ║
 * ║ - Selección de áreas con palo (click izq/der)                           ║
 * ║ - Guardado independiente por mundo/servidor                             ║
 * ║ - Sistema de comandos completo (/zam)                                   ║
 * ║ - Detección automática de entrada/salida de zonas                       ║
 * ║ - Keybindings personalizables                                           ║
 * ║                                                                          ║
 * ║ Autor: NeoKey | Versión: 1.0.0 | MC: 1.21.8                            ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class ZoneAutoMessageMod implements ClientModInitializer {

	// Identificadores del mod
	public static final String MOD_ID = "zoneautomessage";
	public static final String MOD_NAME = "Zone Auto Message";
	public static final String MOD_VERSION = "1.0.0";

	// Keybindings globales
	public static KeyBinding openZoneManager;
	public static KeyBinding toggleMod;
	public static KeyBinding addZoneKeybind;

	// Managers singleton
	private static ZoneManager zoneManager;
	private static WorldConfigManager worldConfigManager;
	private static SelectionManager selectionManager;
	private static boolean modEnabled = true;

	@Override
	public void onInitializeClient() {
		System.out.println("═══════════════════════════════════════════════════════════");
		System.out.println("[" + MOD_NAME + " v" + MOD_VERSION + "] Inicializando...");
		System.out.println("═══════════════════════════════════════════════════════════");

		try {
			// 1. Inicializar WorldConfigManager (detecta mundo actual)
			worldConfigManager = new WorldConfigManager();
			worldConfigManager.loadConfig();
			System.out.println("[INIT] ✓ WorldConfigManager inicializado");

			// 2. Inicializar ZoneManager (carga zonas del mundo actual)
			zoneManager = new ZoneManager();
			zoneManager.loadZones(worldConfigManager.getZonesData());
			System.out.println("[INIT] ✓ ZoneManager inicializado con " + 
				zoneManager.getZoneCount() + " zona(s)");

			// 3. Inicializar SelectionManager (sistema de selección con palo)
			selectionManager = new SelectionManager();
			System.out.println("[INIT] ✓ SelectionManager inicializado");

			// 4. Registrar keybindings
			registerKeybindings();
			System.out.println("[INIT] ✓ Keybindings registrados");

			// 5. Registrar event handlers
			ClientTickEvents.END_CLIENT_TICK.register(new PlayerTickHandler());
			StickInteractionHandler.register();
			System.out.println("[INIT] ✓ Event handlers registrados");

			// 6. Registrar comandos
			ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
				ZoneCommands.register(dispatcher);
			});
			System.out.println("[INIT] ✓ Comandos registrados");

			System.out.println("═══════════════════════════════════════════════════════════");
			System.out.println("[" + MOD_NAME + "] ✓ Mod cargado exitosamente");
			System.out.println("Mundo actual: " + worldConfigManager.getCurrentWorldId());
			System.out.println("═══════════════════════════════════════════════════════════");

		} catch (Exception e) {
			System.err.println("[ERROR] Falló la inicialización del mod:");
			e.printStackTrace();
		}
	}

	/**
	 * Registra todos los keybindings del mod.
	 * 
	 * Keybindings:
	 * - Ctrl + Shift + J: Abre el gestor de zonas
	 * - Ctrl + Shift + U: Activa/desactiva el mod
	 * - Ctrl + Shift + N: Limpia la selección actual
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
		addZoneKeybind = KeyBindingHelper.registerKeyBinding(
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
}