package org.makis.adminmods;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AdminModeData {
    private static final Codec<Inventory> INVENTORY_CODEC = ItemStackWithSlot.CODEC.listOf().xmap(
            AdminModeData::inventoryFromEntries,
            AdminModeData::inventoryToEntries
    );

    public static final AttachmentType<Boolean> isAm = createPersistentAttachment("is_am", () -> false, Codec.BOOL);
    public static final AttachmentType<Inventory> adminInv = createPersistentAttachment("admin_inv", AdminModeData::createEmptyInventory, INVENTORY_CODEC);
    public static final AttachmentType<Inventory> playerInv = createPersistentAttachment("player_inv", AdminModeData::createEmptyInventory, INVENTORY_CODEC);
    public static final AttachmentType<Integer> adminXP = createPersistentAttachment("admin_xp", () -> 0, Codec.INT);
    public static final AttachmentType<Integer> playerXP = createPersistentAttachment("player_xp", () -> 0, Codec.INT);

    private static <T> AttachmentType<T> createPersistentAttachment(String path, Supplier<T> initializer, Codec<T> codec) {
        return AttachmentRegistry.create(
                Identifier.fromNamespaceAndPath(Adminmods.MOD_ID, path),
                        builder -> builder.initializer(initializer).persistent(codec)
        );
    }

    private static Inventory createEmptyInventory() {
        return new Inventory(null, new EntityEquipment());
    }

    private static Inventory inventoryFromEntries(List<ItemStackWithSlot> entries) {
        Inventory inventory = createEmptyInventory();
        for (ItemStackWithSlot entry : entries) {
            if (entry.isValidInContainer(inventory.getContainerSize())) {
                inventory.setItem(entry.slot(), entry.stack());
            }
        }
        return inventory;
    }

    private static List<ItemStackWithSlot> inventoryToEntries(Inventory inventory) {
        List<ItemStackWithSlot> entries = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty()) {
                entries.add(new ItemStackWithSlot(slot, stack));
            }
        }
        return entries;
    }
}
