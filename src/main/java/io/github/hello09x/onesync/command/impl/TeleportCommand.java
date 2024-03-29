package io.github.hello09x.onesync.command.impl;

import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.onesync.manager.teleport.PlayerManager;
import io.github.hello09x.onesync.manager.teleport.TeleportManager;
import io.github.hello09x.onesync.repository.constant.TeleportType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class TeleportCommand {

    public final static String ALL_PLAYERS = "-a";

    public final static TeleportCommand instance = new TeleportCommand();

    private final TeleportManager manager = TeleportManager.instance;

    public void tp(@NotNull Player sender, @NotNull CommandArguments args) {
        var receiver = (String) Objects.requireNonNull(args.get("player"));
        if (!receiver.equals(ALL_PLAYERS)) {
            manager.ask(sender, receiver, TeleportType.TP, true);
            return;
        }

        for (var r : PlayerManager.instance.getPlayers()) {
            if (r.equals(sender.getName())) {
                continue;
            }
            manager.ask(sender, r, TeleportType.TP, true);
        }
    }

    public void tpa(@NotNull Player sender, @NotNull CommandArguments args) {
        var receiver = (String) Objects.requireNonNull(args.get("player"));
        if (!receiver.equals(ALL_PLAYERS)) {
            sender.sendMessage(manager.ask(sender, receiver, TeleportType.TP, false));
            return;
        }

        int count = 0;
        for (var r : PlayerManager.instance.getPlayers()) {
            if (r.equals(sender.getName())) {
                continue;
            }
            manager.ask(sender, r, TeleportType.TP, false);
            count++;
        }

        if (count == 0) {
            sender.sendMessage(text("没有发送传送请求给任何玩家", GRAY));
        } else {
            sender.sendMessage(textOfChildren(
                    text("传送请求已发送给 ", GRAY),
                    text(count),
                    text(" 名玩家, 等待他们接受", GRAY))
            );
        }
    }

    public void tphere(@NotNull Player sender, @NotNull CommandArguments args) {
        var receiver = (String) Objects.requireNonNull(args.get("player"));
        if (!receiver.equals(ALL_PLAYERS)) {
            manager.ask(sender, receiver, TeleportType.TPHERE, true);
            return;
        }

        for (var r : PlayerManager.instance.getPlayers()) {
            if (r.equals(sender.getName())) {
                continue;
            }
            manager.ask(sender, r, TeleportType.TPHERE, true);
        }
    }

    public void tpahere(@NotNull Player sender, @NotNull CommandArguments args) {
        var receiver = (String) Objects.requireNonNull(args.get("player"));
        if (!receiver.equals(ALL_PLAYERS)) {
            sender.sendMessage(manager.ask(sender, receiver, TeleportType.TPHERE, false));
            return;
        }

        int count = 0;
        for (var r : PlayerManager.instance.getPlayers()) {
            if (r.equals(sender.getName())) {
                continue;
            }
            manager.ask(sender, r, TeleportType.TPHERE, false);
            count++;
        }

        if (count == 0) {
            sender.sendMessage(text("没有发送传送请求给任何玩家", GRAY));
        } else {
            sender.sendMessage(textOfChildren(
                    text("传送请求已发送给 ", GRAY),
                    text(count),
                    text(" 名玩家, 等待他们接受", GRAY))
            );
        }
    }


    public void tpaccept(@NotNull Player sender, @NotNull CommandArguments args) {
        var requester = (String) args.get("player");
        var message = manager.answer(sender, requester, true);
        sender.sendMessage(message);
    }

    public void tpdeny(@NotNull Player sender, @NotNull CommandArguments args) {
        var requester = (String) args.get("player");
        var message = manager.answer(sender, requester, false);
        sender.sendMessage(message);
    }

    public void tpcancel(@NotNull Player sender, @NotNull CommandArguments args) {
        var receiver = (String) args.get("player");
        var message = manager.cancel(sender, receiver);
        sender.sendMessage(message);
    }

}
