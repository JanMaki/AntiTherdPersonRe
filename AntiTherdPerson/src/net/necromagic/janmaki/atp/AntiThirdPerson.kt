package net.necromagic.janmaki.atp

import net.necromagic.janmaki.atp.core.Core15
import net.necromagic.janmaki.atp.core.EmptyCore
import net.necromagic.janmaki.atp.listener.MoveListener
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.math.cos
import kotlin.math.sin

class AntiThirdPerson : JavaPlugin() {
    companion object{
        lateinit var instance: AntiThirdPerson
    }

    init {
        instance = this
        instance = this
    }

    private var core = EmptyCore()

    override fun onEnable(){
        val systemVersion = Bukkit.getServer().version
        if (systemVersion.contains("1.15")){
            this.core = Core15()
        }else {
            Bukkit.getConsoleSender().sendMessage("${ChatColor.RED}This plugin is not supported in this server's version.")
            return
        }
        getCommand("AntiThirdPerson")?.setExecutor(Command())
        val pluginManager = Bukkit.getPluginManager()
        pluginManager.registerEvents(MoveListener(), this)
        saveDefaultConfig()
        if (!config.contains("corners")){
            config.set("corners", 12)
        }
        saveConfig()
    }

    //以下メイン処理
    fun function(player: Player){
        for(player2 in Bukkit.getOnlinePlayers()){
            if (player == player2){
                continue
            }
            invUpdate(player, player2)
            invUpdate(player2, player)
        }
    }

    private val tasks =  HashMap<List<UUID>, BukkitTask>()
    private val hideMap = HashMap<Player, MutableSet<Player>>()

    private fun invUpdate(player: Player, hider: Player){
        //hideMapの初期代入
        if (!hideMap.containsKey(player)){
            hideMap[player] = HashSet()
        }
        //タスクの確認
        val uuid = listOf(player.uniqueId, hider.uniqueId)
        for (key in tasks.keys){
            if (key[0] == uuid[0] && key[1] == uuid[1]){
                val task = tasks[key]
                task?.cancel()
                tasks.remove(key)
                break
            }
        }
        //ゲームモードの確認
        if (player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR){
            if (hideMap.containsKey(player)){
                for (p in hideMap[player]!!){
                    core.show(player, p)
                }
                hideMap.remove(player)
            }
            return
        }
        //ワールドの確認
        if (player.world != hider.world){
            return
        }
        val eyeLocation = player.eyeLocation;
        val targetLocation = hider.location.clone().add(0.5, .0, 0.5)
        val task = Util.async(runnable = {
            val mainResult = checkLine(eyeLocation, targetLocation)
            var height = 1.8
            if (hider.isSneaking) {
                height = 1.6
            }
            for (i in 1..(height * 10).toInt()) {
                targetLocation.add(.0, 0.1, .0)
                mainResult == mainResult || checkLine(eyeLocation, targetLocation)
                if (mainResult) break
            }
            val s = hideMap[player]
            if (mainResult) {
                if (!hider.isDead) {
                    s?.remove(hider)
                    core.show(player, hider)
                }
            } else {
                s?.add(hider)
                core.hide(player, hider)
            }
            hideMap[player] = s!!
        })
        tasks[uuid] = task
    }

    private fun  checkLine(from: Location, to: Location) : Boolean{
        var outResult = false
        val acc = config.getInt("corners", 12)
        var c = 365
        while (c > 0){
            val xAdd = 0.3 * cos(Math.toRadians(c.toDouble()))
            val zAdd = 0.3 * sin(Math.toRadians(c.toDouble()))
            var result = true
            val location = Location(to.world, to.x + xAdd, to.y, to.z + zAdd)
            val pos1 = from.toVector()
            val pos2 = location.toVector()
            val vector = pos2.clone().subtract(pos1).normalize().multiply(0.1)
            val distance = from.distance(to)
            if (distance > 50){
                continue
            }
            var coverd = 0.0
            while (coverd < distance){
                coverd += 0.1
                val block = Location(from.world, pos1.x, pos1.y, pos1.z).block
                if (block.isLiquid || block.type.isTransparent || !block.type.isOccluding){
                    continue
                }
                if (block.type != Material.AIR){
                    result = false
                    break
                }
                pos1.add(vector)
            }
            outResult = outResult || result
            c -= 365/acc
        }
        return outResult
    }
}