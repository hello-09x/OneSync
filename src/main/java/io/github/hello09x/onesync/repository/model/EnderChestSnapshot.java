package io.github.hello09x.onesync.repository.model;

import io.github.hello09x.bedrock.database.Table;
import io.github.hello09x.bedrock.database.TableField;
import io.github.hello09x.bedrock.database.TableId;
import io.github.hello09x.onesync.api.handler.SnapshotComponent;
import io.github.hello09x.onesync.handler.EnderChestSnapshotHandler;
import io.github.hello09x.onesync.util.ItemStackMapTypeHandler;
import io.github.hello09x.onesync.util.MenuTemplate;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static io.github.hello09x.bedrock.util.Components.noItalic;
import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@Table("ender_chest_snapshot")
public record EnderChestSnapshot(

        @TableId("snapshot_id")
        Long snapshotId,

        @TableField("player_id")
        UUID playerId,

        @TableField(value = "items", typeHandler = ItemStackMapTypeHandler.class)
        Map<Integer, ItemStack> items

) implements SnapshotComponent {

    @Override
    public @NotNull OfflinePlayer owner() {
        return Bukkit.getOfflinePlayer(this.playerId);
    }

    @Override
    public @NotNull MenuItem toMenuItem(@NotNull Player viewer, @NotNull Consumer<InventoryClickEvent> onClickOutside) {
        var item = new ItemStack(Material.ENDER_CHEST);
        item.editMeta(meta -> {
            meta.displayName(noItalic("末影箱", LIGHT_PURPLE));
            meta.lore(List.of(
                    noItalic(textOfChildren(text("物品: ", GRAY), text(this.items.size(), WHITE))),
                    empty(),
                    noItalic("「左键」查看详情", GRAY)
            ));
        });


        var modified = new MutableBoolean();
        return new MenuItem(
                item,
                ignored -> MenuTemplate.openInventoryMenu(
                        viewer,
                        text("末影箱"),
                        InventoryType.ENDER_CHEST.getDefaultSize(),
                        this.items,
                        newItems -> EnderChestSnapshotHandler.instance.updateItems(this.snapshotId, newItems),
                        event -> {
                            if (modified.booleanValue()) {
                                // 先关闭保存数据再打开加载数据
                                event.getWhoClicked().closeInventory();
                            }
                            onClickOutside.accept(event);
                        },
                        modified
                ));
    }
}
