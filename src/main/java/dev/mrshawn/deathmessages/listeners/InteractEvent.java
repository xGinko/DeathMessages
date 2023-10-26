package dev.mrshawn.deathmessages.listeners;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.EntityManager;
import dev.mrshawn.deathmessages.api.ExplosionManager;
import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.api.events.DMBlockExplodeEvent;
import dev.mrshawn.deathmessages.enums.MobType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InteractEvent implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInteract(PlayerInteractEvent e) {
		Block b = e.getClickedBlock();
		if (b == null || !e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || b.getType().equals(Material.AIR))
			return; // Dreeam - No NPE
		World.Environment environment = b.getWorld().getEnvironment();
		if (environment.equals(World.Environment.NETHER) || environment.equals(World.Environment.THE_END)) {
			if (b.getType().name().contains("BED") && !b.getType().equals(Material.BEDROCK)) {
				List<UUID> effected = new ArrayList<>();
				for (Player p : e.getClickedBlock().getWorld().getPlayers()) {
					if (p.getLocation().distanceSquared(b.getLocation()) < 100) {
						Optional<PlayerManager> getPlayer = PlayerManager.getPlayer(p);
						getPlayer.ifPresent(effect -> {
							effected.add(p.getUniqueId());
							effect.setLastEntityDamager(e.getPlayer());
						});
					}
				}
				callEvent(e, b, effected);
			}
		} else if (!b.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
			if (DeathMessages.majorVersion() >= 16) {
				if (b.getType().equals(Material.RESPAWN_ANCHOR)) {
					RespawnAnchor anchor = (RespawnAnchor) b.getBlockData();
					if (!(anchor.getCharges() == anchor.getMaximumCharges()) && !e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.GLOWSTONE))
						return;
					List<UUID> effected = new ArrayList<>();
					for (Player p : e.getClickedBlock().getWorld().getPlayers()) {
						if (p.getLocation().distanceSquared(b.getLocation()) < 100) {
							Optional<PlayerManager> getPlayer = PlayerManager.getPlayer(p);
							getPlayer.ifPresent(effect -> {
								effected.add(p.getUniqueId());
								effect.setLastEntityDamager(e.getPlayer());
							});
						}
					}
					callEvent(e, b, effected);
				}
			}
		}
	}

	private void callEvent(PlayerInteractEvent e, Block b, List<UUID> effected) {
		for (Entity ent : e.getClickedBlock().getWorld().getEntities()) {
			if (ent instanceof Player) continue;
			if (ent.getLocation().distanceSquared(b.getLocation()) < 100) {
				Optional<EntityManager> getEntity = EntityManager.getEntity(ent.getUniqueId());
				Optional<PlayerManager> getPlayer = PlayerManager.getPlayer(e.getPlayer());

				getEntity.ifPresentOrElse(em -> {
					effected.add(ent.getUniqueId());
					getPlayer.ifPresent(em::setLastPlayerDamager);
				}, () -> new EntityManager(ent, ent.getUniqueId(), MobType.VANILLA));
			}
			new ExplosionManager(e.getPlayer().getUniqueId(), b.getType(), b.getLocation(), effected);
			DMBlockExplodeEvent explodeEvent = new DMBlockExplodeEvent(e.getPlayer(), b);
			Bukkit.getPluginManager().callEvent(explodeEvent);
		}
	}
}