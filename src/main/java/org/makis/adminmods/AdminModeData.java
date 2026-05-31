package org.makis.adminmods;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AdminModeData {
    public record PositionData(double x, double y, double z, float yRot, float xRot, ResourceKey<Level> dimension) {
        public static final Codec<PositionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("x").forGetter(PositionData::x),
                Codec.DOUBLE.fieldOf("y").forGetter(PositionData::y),
                Codec.DOUBLE.fieldOf("z").forGetter(PositionData::z),
                Codec.FLOAT.fieldOf("yRot").forGetter(PositionData::yRot),
                Codec.FLOAT.fieldOf("xRot").forGetter(PositionData::xRot),
                ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(PositionData::dimension)
        ).apply(instance, PositionData::new));
    }

    private static final Codec<Inventory> INVENTORY_CODEC = ItemStackWithSlot.CODEC.listOf().xmap(
            AdminModeData::inventoryFromEntries,
            AdminModeData::inventoryToEntries
    );

    private static final Codec<List<MobEffectInstance>> EFFECTS_CODEC = MobEffectInstance.CODEC.listOf();

    public static final AttachmentType<Boolean> isAm = createPersistentAttachment("is_am", () -> false, Codec.BOOL);
    public static final AttachmentType<Inventory> adminInv = createPersistentAttachment("admin_inv", AdminModeData::createEmptyInventory, INVENTORY_CODEC);
    public static final AttachmentType<Inventory> playerInv = createPersistentAttachment("player_inv", AdminModeData::createEmptyInventory, INVENTORY_CODEC);
    public static final AttachmentType<Integer> adminXP = createPersistentAttachment("admin_xp", () -> 0, Codec.INT);
    public static final AttachmentType<Integer> playerXP = createPersistentAttachment("player_xp", () -> 0, Codec.INT);
    public static final AttachmentType<PositionData> adminPos = createPersistentAttachment("admin_pos", () -> null, PositionData.CODEC);
    public static final AttachmentType<PositionData> playerPos = createPersistentAttachment("player_pos", () -> null, PositionData.CODEC);
    public static final AttachmentType<List<MobEffectInstance>> adminEffects = createPersistentAttachment("admin_effects", ArrayList::new, EFFECTS_CODEC);
    public static final AttachmentType<List<MobEffectInstance>> playerEffects = createPersistentAttachment("player_effects", ArrayList::new, EFFECTS_CODEC);
    public static final AttachmentType<Float> adminHealth = createPersistentAttachment("admin_health", () -> 20.0F, Codec.FLOAT);
    public static final AttachmentType<Float> playerHealth = createPersistentAttachment("player_health", () -> 20.0F, Codec.FLOAT);

    public static void init() {
        // This method is called to trigger static initialization of the class
        // and register the attachment types.
    }

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
