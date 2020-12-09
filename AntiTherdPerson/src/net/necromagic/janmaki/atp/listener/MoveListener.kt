package net.necromagic.janmaki.atp.listener

import net.necromagic.janmaki.atp.AntiThirdPerson
import net.necromagic.janmaki.atp.Util
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

class MoveListener : Listener {
    private val locationMap = HashMap<Player, HashMap<Way, Double>>();

    @EventHandler
    fun onMove(event: PlayerMoveEvent){
        val player = event.player;
        val location = player.location;
        if (locationMap.containsKey(player)){
            val map = locationMap[player]
            if (map != null && map[Way.X] == location.x && map[Way.Y] == location.y && map[Way.Z] == location.z){
                return;
            }
        }
        putLocation(player, location)
        AntiThirdPerson.instance.function(player)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent){
        Util.laterAsync({
            val player = event.player;
            putLocation(player, player.location)
            AntiThirdPerson.instance.function(player)
        }, 1)
    }

    private fun putLocation(player: Player, location: Location){
        val map = HashMap<Way, Double>()
        map[Way.X] = location.x
        map[Way.Y] = location.y
        map[Way.Z] = location.z
        locationMap[player] = map
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent){
        locationMap.remove(event.player)
    }

    enum class Way{
        X,Y,Z;
    }
}