package me.zetastormy.akropolis.module.modules.hotbar.items;

import me.zetastormy.akropolis.module.modules.hotbar.HotbarItem;
import me.zetastormy.akropolis.module.modules.hotbar.HotbarManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderButt extends HotbarItem {
    private final Map<UUID, EnderPearl> pearls = new HashMap<>();
    private final Map<UUID, Boolean> playerDismounting = new HashMap<>();

    public EnderButt(HotbarManager hotbarManager, ItemStack item, int slot, String keyValue) {
        super(hotbarManager, item, slot, keyValue);
    }

    @Override
    protected void onInteract(Player player) {
        if (player.hasCooldown(Material.ENDER_PEARL)) return;
        UUID uuid = player.getUniqueId();

        if (!pearls.containsKey(uuid)) {
            playerDismounting.put(uuid, false);
            EnderPearl pearl = player.getWorld().spawn(player.getEyeLocation(), EnderPearl.class, enderPearl -> {
                enderPearl.addPassenger(player);
                enderPearl.setVelocity(player.getLocation().getDirection().multiply(1.5));
                enderPearl.setShooter(player);
            });
            pearls.put(uuid, pearl);

            player.playSound(player, Sound.ENTITY_ENDER_PEARL_THROW, 0.7f, 1.1f);
            player.setCooldown(Material.ENDER_PEARL, 50);
        } else {
            removePearl(uuid);
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!pearls.containsKey(uuid)) return;

        playerDismounting.put(uuid, event.isSneaking());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDismount(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        EnderPearl pearl = pearls.get(uuid);
        if (!event.getDismounted().equals(pearl)) return;

        if (playerDismounting.getOrDefault(uuid, false)) {
            removePearl(uuid);
        } else {
            if (!pearl.isValid()) return;
            event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (event.getAction().isLeftClick()) return;
        if (!event.getItem().equals(getItem())) return;

        event.setCancelled(true);
    }
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof EnderPearl pearl)) return;
        if (!(pearl.getShooter() instanceof Player player)) return;

        removePearl(player.getUniqueId());

        Location loc = player.getLocation();
        loc.getWorld().spawnParticle(Particle.PORTAL, loc, 20);
        player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 0.7f, 1f);

        player.setCooldown(Material.ENDER_PEARL, 0);
    }

    private void removePearl(UUID uuid) {
        EnderPearl pearl = pearls.remove(uuid);
        if (pearl != null) pearl.remove();
        playerDismounting.remove(uuid);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removePearl(event.getPlayer().getUniqueId());
    }

}
