/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.extrahardmode.features.monsters;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.events.EhmZombieRespawnEvent;
import com.extrahardmode.features.Feature;
import com.extrahardmode.module.PlayerModule;
import com.extrahardmode.task.RespawnZombieTask;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Zombies
 * <p>
 * can resurrect themselves ,
 * make players slow when hit
 * </p>
 */
public class Zombies implements Listener
{
    private final ExtraHardMode plugin;
    private final RootConfig CFG;
    private final PlayerModule playerModule;

    public Zombies (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        playerModule = plugin.getModuleForClass(PlayerModule.class);
    }

    /**
     * When a zombie dies
     *
     * sometimes reanimate the zombie
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        final int zombiesReanimatePercent = CFG.getInt(RootNode.ZOMBIES_REANIMATE_PERCENT, world.getName());

        // FEATURE: zombies may reanimate if not on fire when they die
        if (zombiesReanimatePercent > 0)
        {
            if (entity.getType() == EntityType.ZOMBIE)
            {
                Zombie zombie = (Zombie) entity;

                Player player = null;
                if (zombie.getTarget() instanceof Player)
                    player = (Player) zombie.getTarget();

                EhmZombieRespawnEvent zombieEvent = new EhmZombieRespawnEvent(player, zombie, zombiesReanimatePercent, !plugin.random(zombiesReanimatePercent));
                plugin.getServer().getPluginManager().callEvent(zombieEvent);
                if (!zombie.isVillager() && entity.getFireTicks() < 1 &&! zombieEvent.isCancelled())
                {
                    RespawnZombieTask task = new RespawnZombieTask(plugin, entity.getLocation(), player);
                    int respawnSeconds = plugin.getRandom().nextInt(6) + 3; // 3-8 seconds
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 20L * respawnSeconds); // /20L
                    // ~ 1 second
                }
            }
        }
    }

    /**
     * When an Entity is damaged
     *
     * When a player is damaged by a zombie make him slow
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.getWorld();

        if (entity instanceof Player)
        {
            Player player = (Player) entity;

            final boolean zombiesSlowPlayers = CFG.getBoolean(RootNode.ZOMBIES_DEBILITATE_PLAYERS, world.getName());
            final boolean playerBypasses = playerModule.playerBypasses(player, Feature.MONSTER_ZOMBIES);

            // is this an entity damaged by entity event?
            EntityDamageByEntityEvent damageByEntityEvent = null;
            if (event instanceof EntityDamageByEntityEvent)
            {
                damageByEntityEvent = (EntityDamageByEntityEvent) event;
            }

            // FEATURE: zombies can apply a debilitating effect
            if (zombiesSlowPlayers && player != null &&! playerBypasses)
            {
                if (damageByEntityEvent != null && damageByEntityEvent.getDamager() instanceof Zombie)
                {
                    //TODO EhmZombieSlowEvent
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 10, 3));
                }
            }
        }
    }
}
