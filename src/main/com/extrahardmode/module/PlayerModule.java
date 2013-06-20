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

package com.extrahardmode.module;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.features.Feature;
import com.extrahardmode.service.EHMModule;
import org.apache.commons.lang.Validate;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Player centric actions
 *
 * @author Max
 */
public class PlayerModule extends EHMModule
{
    private final RootConfig CFG;
    private final BlockModule blockModule;
    /**
     * Constructor
     *
     * @param plugin
     */
    public PlayerModule (ExtraHardMode plugin)
    {
        super(plugin);
        CFG = plugin.getModuleForClass(RootConfig.class);
        blockModule = plugin.getModuleForClass(BlockModule.class);
    }

    public boolean playerBypasses(Player player, Feature feature)
    {
        Validate.notNull(player, "We can't check if a Player bypasses if there is no Player!");

        final boolean bypassPermsEnabled = CFG.getBoolean(RootNode.BYPASS_PERMISSION, player.getWorld().getName());
        final boolean opsBypass = CFG.getBoolean(RootNode.BYPASS_OPS, player.getWorld().getName());
        final boolean creativeBypasses = CFG.getBoolean(RootNode.BYPASS_CREATIVE, player.getWorld().getName());

        boolean bypasses = false;

        if (bypassPermsEnabled)
            bypasses = player.hasPermission(feature.getBypassNode().getNode());
        if (!bypasses && opsBypass)
            bypasses = player.isOp();
        if (!bypasses && creativeBypasses)
            bypasses = player.getGameMode().equals(GameMode.CREATIVE);

        return bypasses;
    }

    /**
     * Is the player currently on a ladder?
     * @param player
     * @return
     */
    public boolean isPlayerOnLadder(Player player)
    {
        return player.getLocation().getBlock().getType().equals(Material.LADDER);
    }

    /**
     * Calculates the weight of the players inventory with the given amount of weight per item
     * @param player
     * @param armorPoints Points per piece of worn armor
     * @param inventoryPoints Points per full stack of one item
     * @param toolPoints Points per tool (which doesn't stack)
     * @return
     */
    public float inventoryWeight (Player player, float armorPoints, float inventoryPoints, float toolPoints)
    {
        // count worn clothing
        PlayerInventory inventory = player.getInventory();
        float weight = 0.0F;
        ItemStack[] armor = inventory.getArmorContents();
        for (ItemStack armorPiece : armor)
        {
            if (armorPiece != null && armorPiece.getType() != Material.AIR)
            {
                weight += armorPoints;
            }
        }

        // count contents
        for (ItemStack itemStack : inventory.getContents())
        {
            if (itemStack != null && itemStack.getType() != Material.AIR)
            {
                float addWeight = 0.0F;
                if (blockModule.isTool(itemStack.getType()))
                {
                    addWeight += toolPoints;
                }
                else
                {
                    //take stackSize into consideration
                    addWeight = inventoryPoints * itemStack.getAmount() / itemStack.getMaxStackSize();
                }
                weight += addWeight;
            }
        }
        return weight;
    }

    /**
     * Counts the number of items of a specific type
     * @param inv to count in
     * @param toCount the Material to count
     * @return the number of items as Integer
     */
    public static int countInvItem (PlayerInventory inv, Material toCount)
    {
        int counter = 0;
        for (ItemStack stack : inv.getContents())
        {
            if (stack != null && stack.getType().equals(toCount))
            {
                counter += stack.getAmount();
            }
        }
        return counter;
    }

    /**
     * Get the percentage of how much less damage a player will take.
     *
     * @param player to check the armor contents for
     * @return the percentage as double. Example 0.8 when full armor is worn
     */
    public double getArmorPoints(final Player player)
    {
        double points = 0.0;
        for (ItemStack armor : player.getInventory().getArmorContents())
        {
            switch (armor.getType())
            {
                case LEATHER_HELMET:
                case LEATHER_BOOTS:
                case GOLD_BOOTS:
                case CHAINMAIL_BOOTS:
                    points += 0.04; break;
                case GOLD_HELMET:
                case CHAINMAIL_HELMET:
                case IRON_HELMET:
                case IRON_BOOTS:
                case LEATHER_LEGGINGS:
                    points += 0.08; break;
                case DIAMOND_HELMET:
                case DIAMOND_BOOTS:
                case GOLD_LEGGINGS:
                case LEATHER_CHESTPLATE:
                    points += 0.12; break;
                case CHAINMAIL_LEGGINGS:
                    points += 0.16; break;
                case IRON_LEGGINGS:
                case GOLD_CHESTPLATE:
                case CHAINMAIL_CHESTPLATE:
                    points += 0.2; break;
                case DIAMOND_LEGGINGS:
                case IRON_CHESTPLATE:
                    points += 0.24; break;
                case DIAMOND_CHESTPLATE:
                    points += 0.32; break;
                case AIR:
                    break;
                default:
                    throw new UnsupportedOperationException("There seems to be a new armor type name: " + armor.getType().name() + " id: " + armor.getType().getId());
            }
        }
        return points;
    }

    @Override
    public void starting() {
    }

    @Override
    public void closing() {
    }
}
