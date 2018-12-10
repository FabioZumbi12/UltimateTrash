package br.net.fabiozumbi12.UTrash;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class TempTrash extends BukkitRunnable {
    private int time;
    private String p;
    public ItemStack[] def;
    TempTrash(String p, ItemStack[] def){
        this.time = UTrash.instance().getConfig().getInt("general.temp-trash.time");
        this.p = p;
        this.def = def;
    }

    @Override
    public void run() {
        if (time > 0 && UTrash.instance().tempTrash.containsKey(p)){
            time--;

            if (Bukkit.getPlayer(this.p) != null){
                Player play = Bukkit.getPlayer(this.p);
                if (play.getOpenInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', UTrash.instance().getConfig().getString("strings.guiname")))){
                    ItemStack watch = new ItemStack(Material.getMaterial("WATCH"), 1);
                    try {
                        watch.setType(Material.getMaterial(UTrash.instance().getConfig().getString("materials.timer")));
                    } catch (Exception ignore){}
                    ItemMeta meta = watch.getItemMeta();
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', UTrash.instance().getConfig().getString("general.temp-trash.watch-msg").replace("{sec}", String.valueOf(time))));
                    watch.setItemMeta(meta);
                    play.getOpenInventory().setItem(4, watch);
                }
            }
        } else {
            if (Bukkit.getPlayer(this.p) != null) {
                Player play = Bukkit.getPlayer(this.p);
                if (play.getOpenInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', UTrash.instance().getConfig().getString("strings.guiname")))) {
                    for (int i = 0; i < 54; i++){
                        play.getOpenInventory().setItem(i, def[i]);
                    }
                }
            }
            UTrash.instance().tempTrash.remove(p);
            this.cancel();
        }
    }
}
