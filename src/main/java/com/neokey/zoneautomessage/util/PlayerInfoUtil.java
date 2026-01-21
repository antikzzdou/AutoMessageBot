package com.neokey.zoneautomessage.util;

import net.minecraft.client.MinecraftClient;

/**
 * Información del jugador actual.
 */
public class PlayerInfoUtil {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	/**
	 * Obtiene el nombre del jugador.
	 */
	public static String getPlayerName() {
		if (CLIENT.player == null) return "Unknown";
		return CLIENT.player.getName().getString();
	}

	/**
	 * Obtiene la dimensión actual del jugador.
	 */
	public static String getDimensionName() {
		if (CLIENT.world == null) return "Unknown";
		var dimensionKey = CLIENT.world.getDimensionKey();
		return dimensionKey.getValue().toString();
	}

	/**
	 * Obtiene las coordenadas del jugador redondeadas.
	 */
	public static int[] getPlayerCoordinates() {
		if (CLIENT.player == null) {
			return new int[]{0, 0, 0};
		}

		return new int[]{
			(int) Math.floor(CLIENT.player.getX()),
			(int) Math.floor(CLIENT.player.getY()),
			(int) Math.floor(CLIENT.player.getZ())
		};
	}

	/**
	 * Obtiene el chunk actual del jugador.
	 */
	public static long getPlayerChunk() {
		if (CLIENT.player == null) return -1;

		int chunkX = ((int) CLIENT.player.getX()) >> 4;
		int chunkZ = ((int) CLIENT.player.getZ()) >> 4;

		return (long) chunkX << 32 | (chunkZ & 0xFFFFFFFFL);
	}
}