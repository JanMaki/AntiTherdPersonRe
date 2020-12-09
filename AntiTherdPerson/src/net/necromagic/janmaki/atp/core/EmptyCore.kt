package net.necromagic.janmaki.atp.core

import org.bukkit.entity.Player

open class EmptyCore {
    open fun hide(player: Player, hider: Player){}

    open fun show(player: Player, hider: Player){}

    protected open fun toByte(yaw_pitch: Float): Byte {
        return (yaw_pitch * 256.0f / 360.0f).toInt().toByte()
    }
}