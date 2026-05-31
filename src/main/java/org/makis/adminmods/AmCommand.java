package org.makis.adminmods;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public class AmCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("adminmods")
                .requires(commandSourceStack -> commandSourceStack.isPlayer() && commandSourceStack.getServer().getPlayerList().isOp(Objects.requireNonNull(commandSourceStack.getPlayer()).nameAndId()))
                .executes(AmCommand::execute));
    }
    private static int execute(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        Player player = commandContext.getSource().getPlayer();
        assert player != null;
        Inventory inv = new Inventory(player, new EntityEquipment());
        inv.replaceWith(player.getInventory());
        if (Boolean.TRUE.equals(player.getAttached(AdminModeData.isAm))) {
            player.setAttached(AdminModeData.adminInv, inv);
            player.getInventory().replaceWith(Objects.requireNonNull(player.getAttached(AdminModeData.playerInv)));
            player.setAttached(AdminModeData.isAm, false);
        } else {
            player.setAttached(AdminModeData.isAm, true);
            player.setAttached(AdminModeData.playerInv, inv);
            player.getInventory().replaceWith(Objects.requireNonNull(player.getAttached(AdminModeData.adminInv)));
        }
        return 1;
    }
}
