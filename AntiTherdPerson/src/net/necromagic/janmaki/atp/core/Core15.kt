package net.necromagic.janmaki.atp.core

import net.minecraft.server.v1_15_R1.*
import org.bukkit.craftbukkit.v1_15_R1.CraftEquipmentSlot
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot

class Core15 : EmptyCore() {
    override fun hide(player: Player, hider: Player) {
        if (player is CraftPlayer && hider is CraftPlayer){
            //パケットにてプレイヤーを消滅
            val packet = PacketPlayOutEntityDestroy(hider.handle.id)
            player.handle.playerConnection.sendPacket(packet)
        }
    }

    override fun show(player: Player, hider: Player) {
        if (!(player is CraftPlayer && hider is CraftPlayer)){
            return;
        }
        val entityPlayer = player.handle;
        val entityHider = player.handle;
        val packets = mutableListOf<Packet<PacketListenerPlayOut>>()
        //スポーン
        packets.add(PacketPlayOutSpawnEntity(entityHider))
        //顔の向き
        packets.add(PacketPlayOutEntity.PacketPlayOutEntityLook(entityHider.id, toByte(hider.location.yaw), toByte(hider.location.pitch), true))
        packets.add(PacketPlayOutEntityHeadRotation(entityHider, toByte(hider.location.yaw)))
        //アイテム
        val items = HashMap<EquipmentSlot, ItemStack>()
        items[EquipmentSlot.HAND] = CraftItemStack.asNMSCopy(hider.inventory.itemInMainHand)
        items[EquipmentSlot.OFF_HAND] = CraftItemStack.asNMSCopy(hider.inventory.itemInOffHand)
        items[EquipmentSlot.HEAD] = CraftItemStack.asNMSCopy(hider.inventory.helmet)
        items[EquipmentSlot.CHEST] = CraftItemStack.asNMSCopy(hider.inventory.chestplate)
        items[EquipmentSlot.LEGS] = CraftItemStack.asNMSCopy(hider.inventory.leggings)
        items[EquipmentSlot.FEET] = CraftItemStack.asNMSCopy(hider.inventory.boots)
        for (slot in EquipmentSlot.values()){
            packets.add(PacketPlayOutEntityEquipment(entityHider.id, CraftEquipmentSlot.getNMS(slot), items[slot]))
        }
        //その他情報
        packets.add(PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityHider))
        packets.add(PacketPlayOutEntityMetadata(entityHider.id, entityHider.dataWatcher, true))
        //送信
        for (packet in packets){
            entityPlayer.playerConnection.sendPacket(packet)
        }
    }
}