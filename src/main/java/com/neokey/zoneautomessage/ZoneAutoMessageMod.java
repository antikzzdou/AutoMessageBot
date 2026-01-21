package com.neokey.zoneautomessage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import com.neokey.zoneautomessage.event.PlayerTickHandler;
import com.neokey.zoneautomessage.manager.ZoneManager;
import com.neokey.zoneautomessage.manager.ConfigManager;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║          ZONE AUTO MESSAGE - Punto de Entrada Principal                  ║
 * ║                                                                          ║
 * ║ Responsabilidades:                                                       ║
 * ║ - Inicializar todos los componentes del mod                              ║
 * ║ - Registrar keybindings y event handlers                                 ║
 * ║ - Proporcionar acceso global a managers                                  ║
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
	private static ConfigManager configManager;
	private static boolean modEnabled = true;

	@Override
	public void onInitializeClient() {
		System.out.println("═══════════════════════════════════════════════════════════");
		System.out.println("[" + MOD_NAME + " v" + MOD_VERSION + "] Inicializando...");
		System.out.println("═══════════════════════════════════════════════════════════");

		try {
			// 1. Inicializar ConfigManager (carga archivo JSON)
			configManager = new ConfigManager();
			configManager.loadConfig();
			System.out.println("[INIT] ✓ ConfigManager inicializado");

			// 2. Inicializar ZoneManager (carga zonas desde config)
			zoneManager = new ZoneManager();
			zoneManager.loadZones(configManager.getZonesData());
			System.out.println("[INIT] ✓ ZoneManager inicializado con " + 
				zoneManager.getZoneCount() + " zona(s)");

			// 3. Registrar keybindings
			registerKeybindings();
			System.out.println("[INIT] ✓ Keybindings registrados");

			// 4. Registrar event handlers (ClientTick)
			ClientTickEvents.END_CLIENT_TICK.register(new PlayerTickHandler());
			System.out.println("[INIT] ✓ Event handlers registrados");

			System.out.println("═══════════════════════════════════════════════════════════");
			System.out.println("[" + MOD_NAME + "] Mod cargado exitosamente");
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
	 * - Ctrl + Shift + J: Abre el gestor de zonas (ModMenu)
	 * - Ctrl + Shift + U: Activa/desactiva el mod
	 * - Ctrl + Shift + N: Inicia asistente para crear nueva zona
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

		// Keybinding 3: Crear nueva zona
		addZoneKeybind = KeyBindingHelper.registerKeyBinding(
			new KeyBinding(
				"key.zoneautomessage.add_zone",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_N,
				"category.zoneautomessage.main"
			)
		);

		System.out.println("  └─ Ctrl+Shift+J: Abrir gestor de zonas");
		System.out.println("  └─ Ctrl+Shift+U: Activar/Desactivar mod");
		System.out.println("  └─ Ctrl+Shift+N: Crear nueva zona");
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

	public static ConfigManager getConfigManager() {
		if (configManager == null) {
			throw new IllegalStateException("ConfigManager no ha sido inicializado");
		}
		return configManager;
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