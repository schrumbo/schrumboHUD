package schrumbo.schrumbohud.Utils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import schrumbo.schrumbohud.SchrumboHUDClient;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class SkyblockerItemBackgrounds {

    private static final MethodHandle DRAW = resolve();

    private SkyblockerItemBackgrounds() {}

    private static MethodHandle resolve() {
        if (!FabricLoader.getInstance().isModLoaded("skyblocker")) return null;
        try {
            Class<?> manager = Class.forName("de.hysky.skyblocker.skyblock.item.background.ItemBackgroundManager");
            return MethodHandles.publicLookup().findStatic(manager, "drawBackgrounds",
                    MethodType.methodType(void.class, ItemStack.class, GuiGraphicsExtractor.class, int.class, int.class));
        } catch (Throwable t) {
            SchrumboHUDClient.LOGGER.warn("Skyblocker item-background bridge unavailable: {}", t.toString());
            return null;
        }
    }

    public static boolean available() {
        return DRAW != null;
    }

    public static void draw(ItemStack stack, GuiGraphicsExtractor context, int x, int y) {
        if (DRAW == null || stack == null || stack.isEmpty()) return;
        try {
            DRAW.invokeExact(stack, context, x, y);
        } catch (Throwable ignored) {}
    }
}
