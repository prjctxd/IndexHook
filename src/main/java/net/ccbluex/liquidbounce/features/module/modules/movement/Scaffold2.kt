/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */

/*
    Made by: Dg636
    2/12/24
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.client.settings.GameSettings
import net.ccbluex.liquidbounce.features.module.modules.visual.FreeLook
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.ccbluex.liquidbounce.value.*
import kotlin.math.roundToInt

@ModuleInfo(name = "Scaffold2",  category = ModuleCategory.MOVEMENT)
object Scaffold2 : Module() {
    val modeValue = ListValue("Mode", arrayOf("Simple"), "Simple")

    val safewalkValue = ListValue("SafewalkType", arrayOf("Sneak", "Safewalk", "None"), "Safewalk").displayable { modeValue.equals("Simple") }



    private var playerRot = Rotation(0f, 0f)
    private var oldPlayerRot = Rotation(0f, 0f)
    private var lockRotation = Rotation(0f, 0f)
    private var camYaw = 0f
    private var camPitch = 0f

    private var prevSlot = 0

    override fun onEnable() {
        FDPClient.moduleManager[FreeLook::class.java]!!.enable()
        prevSlot = mc.thePlayer.inventory.currentItem
    }

    override fun onDisable() {
        FDPClient.moduleManager[FreeLook::class.java]!!.disable()
        mc.thePlayer.inventory.currentItem = prevSlot
    }


    // universal, block selection
    @EventTarget
    fun onMotion(e: MotionEvent) {
        val blockSlot = InventoryUtils.findAutoBlockBlock()
        if (blockSlot != -1) {
            mc.thePlayer.inventory.currentItem = blockSlot
            mc.rightClickDelayTimer = 1
            mc.gameSettings.keyBindUseItem.pressed = true
        }
    }

    // safewalk events
    @EventTarget
    fun onMove(event: MoveEvent) {
        if (modeValue.equals("Simple")) {
            if (safewalkValue.equals("Safewalk")) {
                event.isSafeWalk = true
            }
        }
    }

    @EventTarget
    fun onUpdate(e: UpdateEvent) {
        camYaw = FreeLook.cameraYaw
        camPitch = FreeLook.cameraPitch

        oldPlayerRot = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

        when (modeValue.get().lowercase()) {
            "simple" -> {

                // Rotation stuff
                var rpitch = 0f
                if (((camYaw / 45).roundToInt()) % 2 == 0) {
                    rpitch = 82f
                } else {
                    rpitch = 78f
                }
                playerRot = Rotation(camYaw, rpitch)
                lockRotation = RotationUtils.limitAngleChange(oldPlayerRot, playerRot, 50f)


                // Controls correction

                mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
                mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
                mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
                mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)

                // eagle
                if (safewalkValue.equals("Sneak")) {
                    mc.gameSettings.keyBindSneak.pressed = (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) || mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).block == Blocks.air)
                }
            }
        }

        lockRotation.toPlayer(mc.thePlayer)
    }


}