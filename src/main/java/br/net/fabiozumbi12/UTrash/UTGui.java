package br.net.fabiozumbi12.UTrash;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Colorable;
import sun.reflect.annotation.ExceptionProxy;

import java.util.*;

public class UTGui implements Listener {
    private final Player p;
    private final String guiName;
    private Inventory inv;
    private ItemStack[] items;
    private int runnableID = -1;
    private final boolean isTemp;

    public UTGui(Player p){
        this.p = p;
        guiName = UTrash.instance().toColor(UTrash.instance().getConfig().getString("strings.guiname"));
        isTemp = UTrash.instance().getConfig().getInt("general.temp-trash.time") > 0;
        UTrash.instance().getServer().getPluginManager().registerEvents(this, UTrash.instance());
    }

    void Open(){
        items = new ItemStack[54];
        String[] head = ChatColor.translateAlternateColorCodes('&', UTrash.instance().getConfig().getString("general.temp-trash.stand-by").replace("{sec}", String.valueOf(UTrash.instance().getConfig().getInt("general.temp-trash.time")))).split(";");

        for (int i = 0; i < 9; i++){
            String index;
            String name;
            if (i == 0) {
                index = "clear";
                name = UTrash.instance().toColor(UTrash.instance().getConfig().getString("strings.clear"));
            } else if (i == 8) {
                index = "close";
                name = UTrash.instance().toColor(UTrash.instance().getConfig().getString("strings.close"));
            } else if (isTemp && i == 4){
                index = "header.4";
                name = ChatColor.translateAlternateColorCodes('&', head[head.length-1]);
            } else {
                index = "header."+i;
                name = ChatColor.translateAlternateColorCodes('&',"&0-");
            }


            Material mat = Material.getMaterial(UTrash.instance().getConfig().getString("materials."+index+".material").toUpperCase());
            try {
                if (isTemp && i == 4){
                    try {
                        mat = (Material.getMaterial(UTrash.instance().getConfig().getString("materials.timer")));
                    } catch (Exception ignore){
                        mat = Material.getMaterial("CLOCK");
                    }
                }
                items[i] = new ItemStack(mat);
            } catch (Exception ex){
                UTrash.instance().getLogger().warning("No material for config on materials: "+index);
                return;
            }

            //display name
            ItemMeta meta = items[i].getItemMeta();
            meta.setDisplayName(name);
            if (isTemp && i == 4){
                meta.setLore(Arrays.asList(head));
            }
            items[i].setItemMeta(meta);

            //color (legacy)
            if (items[i].getData() instanceof Colorable){
                DyeColor color;
                try {
                    color = DyeColor.getByDyeData((byte)UTrash.instance().getConfig().getInt("materials."+index+".color"));
                } catch (Exception ex){
                    color = null;
                }
                if (color != null){
                    items[i].setDurability(color.getDyeData());
                }
            }
        }

        this.inv = Bukkit.createInventory(p, 54, this.guiName);
        this.inv.setContents(UTrash.instance().tempTrash.getOrDefault(this.p.getName(), items));
        this.p.openInventory(this.inv);

        if (UTrash.instance().getConfig().getBoolean("general.random-header-colors")){
            //random header colors
            Runnable runnable = () -> {
                try {
                    int colorNum = 0;
                    for(int i = 1; i < 8; i++) {
                        if (isTemp && i == 4) continue;

                        ItemStack is = inv.getItem(i);

                        // Random material
                        if (items[i].getType().name().contains("STAINED")) {
                            Material[] mat = Arrays.stream(Material.values()).filter(m -> m.name().contains("_STAINED_GLASS_PANE")).toArray(Material[]::new);
                            if (mat.length > 0)
                                is.setType(mat[new Random().nextInt(mat.length)]);
                        }

                        // Color (legacy)
                        else if (items[i].getData() instanceof Colorable){
                            DyeColor color;
                            try {
                                color = DyeColor.getByDyeData((byte) (new Random().nextInt(13)+1));
                            } catch (Exception ex){
                                color = null;
                            }
                            if (color != null){
                                is.setDurability(color.getDyeData());
                            }
                        }
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                    if (runnableID != -1) Bukkit.getScheduler().cancelTask(runnableID);
                }
            };
            runnableID = Bukkit.getScheduler().scheduleSyncRepeatingTask(UTrash.instance(), runnable, 5L,5L);
        }
    }

    void Close(){
        //check for itens
        this.p.updateInventory();
        Bukkit.getScheduler().runTaskLater(UTrash.instance(), ()-> this.p.updateInventory(), 1);

        if (isTemp){
            if (!UTrash.instance().tempTrash.containsKey(this.p.getName())){
                new TempTrash(this.p.getName(), this.items).runTaskTimer(UTrash.instance(), 20, 20);
            }
            UTrash.instance().tempTrash.put(this.p.getName(), this.inv.getContents());
        }
        Bukkit.getScheduler().runTask(UTrash.instance(), () -> p.closeInventory());
        if (runnableID != -1) Bukkit.getScheduler().cancelTask(runnableID);
        HandlerList.unregisterAll(this);
        try {
            this.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        if (e.getPlayer().equals(this.p)){
            this.inv = e.getInventory();
            Close();
        }
    }

    @EventHandler
    void onDeath(PlayerDeathEvent e){
        if (e.getEntity().equals(this.p)) {
            Close();
        }
    }

    @EventHandler
    void onPlayerLogout(PlayerQuitEvent e){
        if (e.getPlayer().equals(this.p)) {
            Close();
        }
    }

    @EventHandler
    void onPluginDisable(PluginDisableEvent event){
        Close();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onInventoryClick(InventoryClickEvent e){
        if (e.getWhoClicked().equals(this.p) && e.getClickedInventory() != null && e.getClickedInventory().equals(this.inv)){
            if (e.getRawSlot() == 0){
                e.setCancelled(true);
                e.getClickedInventory().setContents(items);
                ((Player)e.getWhoClicked()).updateInventory();
                UTrash.instance().tempTrash.remove(this.p.getName());
            }
            if (e.getRawSlot() == 8){
                e.setCancelled(true);
                Close();
            }
            if (e.getRawSlot() > 0 && e.getRawSlot() < 8 ){
                e.setCancelled(true);
            }
        }
    }
}