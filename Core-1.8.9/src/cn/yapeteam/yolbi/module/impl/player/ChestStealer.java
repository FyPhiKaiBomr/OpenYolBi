package cn.yapeteam.yolbi.module.impl.player;

import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.ymixin.utils.Mapper;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.game.EventLoadWorld;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.managers.PacketManager;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.values.impl.BooleanValue;
import cn.yapeteam.yolbi.module.values.impl.ModeValue;
import cn.yapeteam.yolbi.module.values.impl.NumberValue;
import cn.yapeteam.yolbi.utils.misc.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ChestStealer extends Module {
    private final TimerUtil timer = new TimerUtil();
    private final TimerUtil stealTimer = new TimerUtil();
    private boolean isStealing;
    private int first = 0;
    private final ModeValue<String> mode = new ModeValue<>("Mode", "Hypixel", "Hypixel", "Basic");
    private final NumberValue<Float> delayNext = new NumberValue<>("Delay Before Next Item", 2.0F, 0.0F, 5.0F, 0.5F);
    private final NumberValue<Float> delayFirst = new NumberValue<>("Delay Before First Item", 0F, 0.0F, 15.0F, 0.5F);
    private final BooleanValue drop = new BooleanValue("Auto Drop", () -> !mode.is("Hypixel"), false);
    private final BooleanValue close = new BooleanValue("Auto Close", true);
    private final BooleanValue chestAura = new BooleanValue("Auto Open", false);
    private final BooleanValue ignore = new BooleanValue("Ignore Trash Items", true);

    public ChestStealer() {
        super("ChestStealer", ModuleCategory.PLAYER);
        addValues(mode, delayNext, delayFirst, drop, close, chestAura, ignore);
    }


    private final Map<TileEntityChest, Boolean> chestIsEmptyMap = new HashMap<>();

    @Listener
    private void onLoad(EventLoadWorld e) {
        chestIsEmptyMap.clear();
    }

    private static final Field GuiChest_lowerChestInventory;


    static {
        try {
            GuiChest_lowerChestInventory = GuiChest.class.getDeclaredField(Mapper.map("net/minecraft/client/gui/inventory/GuiChest", "lowerChestInventory", null, Mapper.Type.Field));
            GuiChest_lowerChestInventory.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static IInventory getLowerChestInventory(GuiChest guiChest) {
        try {
            return (IInventory) GuiChest_lowerChestInventory.get(guiChest);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getSuffix() {
        return mode.getValue();
    }

    @Listener
    public void onEvent(EventUpdate event) {
        if (!event.isPre()) return;
        if (chestAura.getValue()) {
            if (stealTimer.hasTimePassed(2000) && isStealing) {
                stealTimer.reset();
                isStealing = false;
            }
            for (Object o : mc.theWorld.loadedTileEntityList) {
                if (o instanceof TileEntityChest) {
                    TileEntityChest chest = (TileEntityChest) o;
                    float x = chest.getPos().getX();
                    float y = chest.getPos().getY();
                    float z = chest.getPos().getZ();
                    if (!isStealing && !(chestIsEmptyMap.containsKey(chest) && chestIsEmptyMap.get(chest)) && mc.thePlayer.getDistance(x, y, z) < 4 && stealTimer.hasTimePassed(1000) && mc.currentScreen == null) {
                        isStealing = true;
                        PacketManager.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(chest.getPos(), getFacingDirection(chest.getPos()).getIndex(), mc.thePlayer.getCurrentEquippedItem(), x, y, z));
                        chestIsEmptyMap.put(chest, true);
                        stealTimer.reset();
                        break;
                    }
                }
            }
        }
    }

    @Listener
    private void onPost(EventUpdate e) {
        if (!e.isPost()) return;
        if (mc.currentScreen instanceof GuiChest) {
            GuiChest guiChest = (GuiChest) mc.currentScreen;
            IInventory lowerChestInventory = getLowerChestInventory(guiChest);
            if (lowerChestInventory == null) {
                Logger.exception(new RuntimeException("Failed to get lower chest inventory"));
                return;
            }
            String name = lowerChestInventory.getDisplayName().getUnformattedText().toLowerCase();
            String[] list = new String[]{"menu", "selector", "game", "gui", "server", "inventory", "play", "teleporter", "shop", "melee", "armor",
                    "block", "castle", "mini", "warp", "teleport", "user", "team", "tool", "sure", "trade", "cancel", "accept", "soul", "book", "recipe",
                    "profile", "tele", "port", "map", "kit", "select", "lobby", "vault", "lock", "anticheat", "travel", "settings", "user", "preference",
                    "compass", "cake", "wars", "buy", "upgrade", "ranged", "potions", "utility"};

            for (String str : list)
                if (name.contains(str))
                    return;
            if (!isStealing)
                first = 0;
            first++;
            isStealing = true;
            if (first <= delayFirst.getValue().intValue())
                return;
            boolean full = true;
            ItemStack[] arrayOfItemStack;
            int j = (arrayOfItemStack = mc.thePlayer.inventory.mainInventory).length;
            for (int i = 0; i < j; i++) {
                ItemStack item = arrayOfItemStack[i];
                if (item == null) {
                    full = false;
                    break;
                }
            }
            boolean containsItems = false;
            if (!full) {
                for (int index = 0; index < lowerChestInventory.getSizeInventory(); index++) {
                    ItemStack stack = lowerChestInventory.getStackInSlot(index);
                    if (stack != null && !isBad(stack)) {
                        containsItems = true;
                        break;
                    }
                }
                if (containsItems) {
                    for (int index = 0; index < lowerChestInventory.getSizeInventory(); index++) {
                        ItemStack stack = lowerChestInventory.getStackInSlot(index);
                        int delay = 50 * delayNext.getValue().intValue();
                        if (stack != null && timer.hasTimePassed(delay) && !isBad(stack)) {
                            mc.playerController.windowClick(guiChest.inventorySlots.windowId, index, 0, !mode.is("Hypixel") && drop.getValue() ? 0 : 1, mc.thePlayer);
                            if (mode.is("Hypixel")) {
                                mc.playerController.windowClick(guiChest.inventorySlots.windowId, index, 0, 1, mc.thePlayer);
                                mc.playerController.windowClick(guiChest.inventorySlots.windowId, index, 1, 1, mc.thePlayer);
                            } else if (drop.getValue()) {
                                mc.playerController.windowClick(guiChest.inventorySlots.windowId, index, 0, 0, mc.thePlayer);
                                mc.playerController.windowClick(guiChest.inventorySlots.windowId, -999, 0, 0, mc.thePlayer);
                            }
                            timer.reset();
                        }
                    }
                } else if (close.getValue()) {
                    mc.thePlayer.closeScreen();
                    isStealing = false;
                }
            } else if (close.getValue()) {
                mc.thePlayer.closeScreen();
                isStealing = false;
            }
        } else {
            isStealing = false;
        }
    }

    private boolean isSolidFullCube(Block block) {
        return block.isFullCube() && block.getMaterial().isSolid();
    }

    private EnumFacing getFacingDirection(final BlockPos pos) {
        EnumFacing direction = null;
        if (!isSolidFullCube(mc.theWorld.getBlockState(pos.add(0, 1, 0)).getBlock()))
            direction = EnumFacing.UP;
        final MovingObjectPosition rayResult = mc.theWorld.rayTraceBlocks(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
        if (rayResult != null)
            return rayResult.sideHit;
        return direction;
    }

    private boolean isBad(ItemStack item) {
        if (!ignore.getValue())
            return false;
        ItemStack is = null;
        float lastDamage = -1;
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is1 = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (is1.getItem() instanceof ItemSword && item.getItem() instanceof ItemSword) {
                    if (lastDamage < getDamage(is1)) {
                        lastDamage = getDamage(is1);
                        is = is1;
                    }
                }
            }
        }


        if (is != null && is.getItem() instanceof ItemSword && item.getItem() instanceof ItemSword) {
            float currentDamage = getDamage(is);
            float itemDamage = getDamage(item);
            if (itemDamage > currentDamage) {
                return false;
            }
        }


        return item != null &&
                ((item.getItem().getUnlocalizedName().contains("tnt")) ||
                        (item.getItem().getUnlocalizedName().contains("stick")) ||
                        (item.getItem().getUnlocalizedName().contains("egg") && !item.getItem().getUnlocalizedName().contains("leg")) ||
                        (item.getItem().getUnlocalizedName().contains("string")) ||
                        (item.getItem().getUnlocalizedName().contains("flint")) ||
                        (item.getItem().getUnlocalizedName().contains("compass")) ||
                        (item.getItem().getUnlocalizedName().contains("feather")) ||
                        (item.getItem().getUnlocalizedName().contains("bucket")) ||
                        (item.getItem().getUnlocalizedName().contains("snow")) ||
                        (item.getItem().getUnlocalizedName().contains("fish")) ||
                        (item.getItem().getUnlocalizedName().contains("enchant")) ||
                        (item.getItem().getUnlocalizedName().contains("exp")) ||
                        (item.getItem().getUnlocalizedName().contains("shears")) ||
                        (item.getItem().getUnlocalizedName().contains("anvil")) ||
                        (item.getItem().getUnlocalizedName().contains("torch")) ||
                        (item.getItem().getUnlocalizedName().contains("seeds")) ||
                        (item.getItem().getUnlocalizedName().contains("leather")) ||
                        ((item.getItem() instanceof ItemPickaxe)) ||
                        ((item.getItem() instanceof ItemGlassBottle)) ||
                        ((item.getItem() instanceof ItemTool)) ||
                        (item.getItem().getUnlocalizedName().contains("piston")) ||
                        ((item.getItem().getUnlocalizedName().contains("potion"))
                                && (isBadPotion(item))));
    }

    private boolean isBadPotion(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemPotion) {
            final ItemPotion potion = (ItemPotion) stack.getItem();
            if (ItemPotion.isSplash(stack.getItemDamage())) {
                for (final PotionEffect o : potion.getEffects(stack)) {
                    if (o.getPotionID() == Potion.poison.getId() ||
                            o.getPotionID() == Potion.harm.getId() ||
                            o.getPotionID() == Potion.moveSlowdown.getId() ||
                            o.getPotionID() == Potion.weakness.getId()
                    ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private float getDamage(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemSword)) {
            return 0;
        }
        return EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25f + ((ItemSword) stack.getItem()).getDamageVsEntity();
    }
}
