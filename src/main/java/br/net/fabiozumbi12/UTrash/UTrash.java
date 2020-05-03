package br.net.fabiozumbi12.UTrash;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public class UTrash extends JavaPlugin implements CommandExecutor {

    HashMap<String, ItemStack[]> tempTrash;
    private static UTrash plugin;
    public static UTrash instance(){
        return plugin;
    }

    public void onEnable(){
        plugin = this;
        this.tempTrash = new HashMap<>();
        getCommand("trash").setExecutor(this);

        initConfig();
        getLogger().info(toColor("UltimateTrash Enabled!"));
    }

    private void initConfig() {
        if (!getDataFolder().exists()){
            getDataFolder().mkdir();
        }

        getConfig().addDefault("general.random-header-colors",true);
        getConfig().addDefault("general.blacklist-worlds", new ArrayList<>());

        getConfig().addDefault("general.temp-trash.time",30);
        getConfig().addDefault("general.temp-trash.watch-msg","&c» &4{sec}s &cto auto-clear «");
        getConfig().addDefault("general.temp-trash.stand-by","&3After close, the trash will be;&3cleaned in &6{sec}&3 seconds;&b» Trash Auto-Cleaner «");

        getConfig().addDefault("strings.tag","&4[&7UTrash&4]&r ");
        getConfig().addDefault("strings.reload","&aReload with success!");
        getConfig().addDefault("strings.guiname","&3» Drop Your Trash here!");
        getConfig().addDefault("strings.close","&4» Close Trash!");
        getConfig().addDefault("strings.clear","&a» Clear Trash!");
        getConfig().addDefault("strings.blacklist-world","You can't use trash in this world!");

        getConfig().addDefault("materials.clear.material","YELLOW_WOOL");
        getConfig().addDefault("materials.clear.color",0);

        getConfig().addDefault("materials.close.material","RED_WOOL");
        getConfig().addDefault("materials.close.color",0);
        getConfig().addDefault("materials.timer","CLOCK");

        //header
        getConfig().addDefault("materials.header.1.material","BLACK_STAINED_GLASS_PANE");
        getConfig().addDefault("materials.header.1.color",0);

        getConfig().addDefault("materials.header.2.material","GRAY_STAINED_GLASS_PANE");
        getConfig().addDefault("materials.header.2.color",0);

        getConfig().addDefault("materials.header.3.material","GREEN_STAINED_GLASS_PANE");
        getConfig().addDefault("materials.header.3.color",0);

        getConfig().addDefault("materials.header.4.material","RED_STAINED_GLASS_PANE");
        getConfig().addDefault("materials.header.4.color",0);

        getConfig().addDefault("materials.header.5.material","BLUE_STAINED_GLASS_PANE");
        getConfig().addDefault("materials.header.5.color",0);

        getConfig().addDefault("materials.header.6.material","LIME_STAINED_GLASS_PANE");
        getConfig().addDefault("materials.header.6.color",0);

        getConfig().addDefault("materials.header.7.material","MAGENTA_STAINED_GLASS_PANE");
        getConfig().addDefault("materials.header.7.color",0);

        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void reload(){
        for (Player p: Bukkit.getOnlinePlayers()){
            p.closeInventory();
        }
        reloadConfig();
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, final String[] args) {
        if (args.length == 1){
            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("utrash.reload")){
                reload();
                sender.sendMessage(toColorTag(getConfig().getString("strings.reload")));
                return true;
            }
        }

        if (!(sender instanceof Player)){
            return false;
        }

        Player p = (Player)sender;
        if (args.length == 0){
            if (sender.hasPermission("utrash.use")){
                if (getConfig().getStringList("general.blacklist-worlds").contains(p.getWorld().getName())){
                    sender.sendMessage(toColorTag(getConfig().getString("strings.blacklist-world")));
                    return true;
                }
                new UTGui(p).Open();
                return true;
            }
        }
        return true;
    }

    public String toColor(String msg){
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String toColorTag(String msg){
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("strings.tag")+msg);
    }
}