package net.necromagic.janmaki.atp

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask

class Util {
    companion object{
        fun async(runnable: Runnable) : BukkitTask{
            return  Bukkit.getScheduler().runTaskAsynchronously(AntiThirdPerson.instance, runnable);
        }

        fun laterAsync(runnable: Runnable, tick: Long ): BukkitTask {
            return Bukkit.getScheduler().runTaskLaterAsynchronously(AntiThirdPerson.instance, runnable, tick)
        }
    }
}