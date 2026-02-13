package com.tridevmc.davincisvessels.common;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

import org.checkerframework.checker.units.qual.C;

import com.mojang.blaze3d.platform.ScreenManager;

public class DavincisUIHooks {

    private static Optional<IElementProvider> lastProvider = Optional.empty();

    //  public static MenuType containerType;

    // public static RegistryObject<MenuType<? extends AbstractContainerMenu>> register(DeferredRegister<MenuType<?>> registry) {
    //     return registry.register("containers", () -> IForgeMenuType.create(getFactory()));
    // }

    public static MenuType<? extends AbstractContainerMenu> register(DeferredRegister<MenuType<?>> registry) {
        MenuType<AbstractContainerMenu> containerType = IForgeMenuType.create(getFactory());
        registry.register("containers", () -> containerType);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MenuScreens.register(containerType, getScreenFactory()));
        return containerType;
    }

    // public static void registerScreens(MenuType containerType) {
    //     MenuScreens.register(containerType, getScreenFactory());
    // }

    private static <C extends AbstractContainerMenu> IContainerFactory<C> getFactory() {
        return (windowId, inv, data) -> {
            UIType type = UIType.byId(data.readByte());
            Level world = inv.player.level();

            switch (type) {
                case TILE:
                    BlockPos pos = data.readBlockPos();
                    BlockEntity tile = world.getBlockEntity(pos);
                    if (tile instanceof IElementProvider) {
                        lastProvider = Optional.of((IElementProvider) tile);
                        return (C) ((IElementProvider) tile).createMenu(windowId, inv, inv.player);
                    }
                case ENTITY:
                    int entityId = data.readVarInt();
                    Entity entity = world.getEntity(entityId);
                    if (entity instanceof IElementProvider) {
                        lastProvider = Optional.of((IElementProvider) entity);
                        return (C) ((IElementProvider) entity).createMenu(windowId, inv, inv.player);
                    }
                default:
                    lastProvider = Optional.empty();
                    return null;
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    private static <C extends AbstractContainerMenu, U extends Screen & MenuAccess<C>> MenuScreens.ScreenConstructor<C,U> getScreenFactory() {
        return (container, inv, name) -> lastProvider.map(eP -> (U) eP.createScreen(container, inv.player)).orElse(null);
    }

    public static void openGui(Player player, IElementProvider provider) {
        if (player instanceof ServerPlayer) {
            if (provider instanceof BlockEntity) {
                openGui((ServerPlayer) player, provider, ((BlockEntity) provider).getBlockPos());
            } else if (provider instanceof Entity) {
                openGui((ServerPlayer) player, provider, ((Entity) provider).getId());
            }
        } else {
            throw new ClassCastException(String.format("Unable to cast type %s to ServerPlayerEntity", player.getClass().getName()));
        }
    }

    public static void openGui(ServerPlayer player, IElementProvider provider, BlockPos pos) {
        NetworkHooks.openScreen(player, provider, packetBuffer -> {
            packetBuffer.writeByte(UIType.TILE.id);
            packetBuffer.writeBlockPos(pos);
        });
    }

    public static void openGui(ServerPlayer player, IElementProvider provider, int entity) {
        NetworkHooks.openScreen(player, provider, packetBuffer -> {
            packetBuffer.writeByte(UIType.TILE.id);
            packetBuffer.writeVarInt(entity);
        });
    }

    private enum UIType {
        TILE(0),
        ENTITY(1),
        OTHER(2);

        private static final UIType[] TYPES;

        static {
            TYPES = new UIType[]{TILE, ENTITY, OTHER};
        }

        private final int id;

        UIType(int id) {
            this.id = id;
        }

        public static UIType byId(int id) {
            return TYPES[id];
        }

        public int getId() {
            return this.id;
        }
    }

}
