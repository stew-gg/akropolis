package me.zetastormy.akropolis.module.modules.hotbar.items;

import me.zetastormy.akropolis.module.modules.hotbar.HotbarItem;
import me.zetastormy.akropolis.module.modules.hotbar.HotbarManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EnderButt extends HotbarItem {
    private EnderPearl pearl = null;

    public EnderButt(HotbarManager hotbarManager, ItemStack item, int slot, String keyValue) {
        super(hotbarManager, item, slot, keyValue);
    }

    @Override
    protected void onInteract(Player player) {
        if (player.hasCooldown(Material.ENDER_PEARL)) return;

        if (pearl == null) {
            pearl = player.launchProjectile(EnderPearl.class);
            pearl.setVelocity(pearl.getVelocity().multiply(1.1));
            pearl.addPassenger(player);

            player.playSound(player, Sound.ENTITY_ENDER_PEARL_THROW, 0.7f, 1.1f);
            player.setCooldown(Material.ENDER_PEARL, 50);
        } else {
            pearl.remove();
            pearl = null;
        }
    }

    @EventHandler
    public void onPlayerDismount(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!event.getDismounted().equals(pearl)) return;

        pearl.remove();
        pearl = null;
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (!event.getItem().equals(getItem())) return;

        event.setCancelled(true);
    }
}
