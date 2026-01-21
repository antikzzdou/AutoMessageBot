package com.neokey.zoneautomessage.event;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import com.neokey.zoneautomessage.ZoneAutoMessageMod;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║     STICK INTERACTION HANDLER - Manejo de Clicks con Palo (Stick)        ║
 * ║                                                                          ║
 * ║ Responsabilidades:                                                       ║
 * ║ - Detectar click izquierdo en bloques con palo (punto 1)                ║
 * ║ - Detectar click derecho en bloques con palo (punto 2)                  ║
 * ║ - Cancelar comportamiento normal del palo durante selección             ║
 * ║                                                                          ║
 * ║ Eventos Fabric:                                                          ║
 * ║ - AttackBlockCallback: Click izquierdo                                  ║
 * ║ - UseBlockCallback: Click derecho                                       ║
 * ║                                                                          ║
 * ║ Autor: NeoKey                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class StickInteractionHandler {

    /**
     * Registra los event listeners para clicks con palo.
     */
    public static void register() {
        // Click IZQUIERDO → Seleccionar punto 1
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player.getStackInHand(hand).getItem() == Items.STICK) {
                if (ZoneAutoMessageMod.isModEnabled()) {
                    ZoneAutoMessageMod.getSelectionManager().setPosition1(pos);
                    return ActionResult.SUCCESS; // Cancelar rotura de bloque
                }
            }
            return ActionResult.PASS;
        });

        // Click DERECHO → Seleccionar punto 2
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.getStackInHand(hand).getItem() == Items.STICK) {
                if (ZoneAutoMessageMod.isModEnabled()) {
                    ZoneAutoMessageMod.getSelectionManager().setPosition2(hitResult.getBlockPos());
                    return ActionResult.SUCCESS; // Cancelar uso del bloque
                }
            }
            return ActionResult.PASS;
        });

        System.out.println("[StickInteractionHandler] ✓ Event listeners registrados");
    }
}