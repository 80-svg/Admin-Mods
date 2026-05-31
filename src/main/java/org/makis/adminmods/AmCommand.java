package org.makis.adminmods;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AmCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("adminmode")
                .requires(source -> source.isPlayer() && source.getServer().getPlayerList().isOp(Objects.requireNonNull(source.getPlayer()).nameAndId()))
                .executes(AmCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer player = commandContext.getSource().getPlayerOrException();

        // Save current state
        Inventory currentInv = new Inventory(player, new EntityEquipment());
        currentInv.replaceWith(player.getInventory());
        int currentXp = player.totalExperience;
        AdminModeData.PositionData currentPos = new AdminModeData.PositionData(
                player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.level().dimension()
        );
        List<MobEffectInstance> currentEffects = new ArrayList<>(player.getActiveEffects());

        if (Boolean.TRUE.equals(player.getAttached(AdminModeData.isAm))) {
            // Switching from ADMIN to PLAYER
            player.setAttached(AdminModeData.adminInv, currentInv);
            player.setAttached(AdminModeData.adminXP, currentXp);
            player.setAttached(AdminModeData.adminPos, currentPos);
            player.setAttached(AdminModeData.adminEffects, currentEffects);
            player.setAttached(AdminModeData.adminHealth, player.getHealth());

            applyInventory(player, player.getAttached(AdminModeData.playerInv));
            setPlayerXp(player, player.getAttached(AdminModeData.playerXP));
            restorePosition(player, commandContext.getSource().getServer(), player.getAttached(AdminModeData.playerPos));
            restoreEffects(player, player.getAttached(AdminModeData.playerEffects));
            setPlayerHP(player, player.getAttached(AdminModeData.playerHealth));

            player.setGameMode(GameType.SURVIVAL);
            player.setAttached(AdminModeData.isAm, false);
        } else {
            // Switching from PLAYER to ADMIN
            player.setAttached(AdminModeData.playerInv, currentInv);
            player.setAttached(AdminModeData.playerXP, currentXp);
            player.setAttached(AdminModeData.playerPos, currentPos);
            player.setAttached(AdminModeData.playerEffects, currentEffects);
            player.setAttached(AdminModeData.playerHealth, player.getHealth());

            applyInventory(player, player.getAttached(AdminModeData.adminInv));
            setPlayerXp(player, player.getAttached(AdminModeData.adminXP));
            restorePosition(player, commandContext.getSource().getServer(), player.getAttached(AdminModeData.adminPos));
            restoreEffects(player, player.getAttached(AdminModeData.adminEffects));
            setPlayerHP(player, player.getAttached(AdminModeData.adminHealth));

            player.setGameMode(GameType.CREATIVE);
            player.setAttached(AdminModeData.isAm, true);
        }
        return 1;
    }

    private static void restorePosition(ServerPlayer player, net.minecraft.server.MinecraftServer server, AdminModeData.PositionData pos) {
        if (pos == null) return;
        ServerLevel level = server.getLevel(pos.dimension());
        if (level != null) {
            player.teleportTo(level, pos.x(), pos.y(), pos.z(), Set.of(), pos.yRot(), pos.xRot(), true);
        }
    }

    private static void restoreEffects(ServerPlayer player, List<MobEffectInstance> effects) {
        player.removeAllEffects();
        if (effects != null) {
            for (MobEffectInstance effect : effects) {
                player.addEffect(new MobEffectInstance(effect));
            }
        }
    }

    private static void setPlayerXp(ServerPlayer player, Integer xp) {
        if (xp == null) xp = 0;
        player.experienceLevel = 0;
        player.experienceProgress = 0.0f;
        player.totalExperience = 0;
        player.giveExperiencePoints(xp);
    }

    private static void applyInventory(ServerPlayer player, Inventory source) {
        if (source == null) {
            player.getInventory().clearContent();
            return;
        }

        // If the source inventory has no player (e.g. default or loaded from NBT),
        // we might hit an NPE in replaceWith if it expects a non-null player.
        if (source.player == null) {
            player.getInventory().clearContent();
            for (int i = 0; i < source.getContainerSize() && i < player.getInventory().getContainerSize(); i++) {
                player.getInventory().setItem(i, source.getItem(i).copy());
            }
        } else {
            player.getInventory().replaceWith(source);
        }
    }

    private static void setPlayerHP(ServerPlayer player, Float hp) {
        if (hp == null) hp = 20.0F;
        player.setHealth(hp);
    }
}
