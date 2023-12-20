package io.github.hello09x.onesync.config;

import io.github.hello09x.bedrock.config.Config;
import io.github.hello09x.onesync.Main;
import io.github.hello09x.onesync.repository.constant.SnapshotCause;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@ToString
public class OneSyncConfig extends Config<OneSyncConfig> {

    public final static OneSyncConfig instance = new OneSyncConfig(Main.getInstance(), "1");

    private final Synchronize synchronize = new Synchronize();
    private final Snapshot snapshot = new Snapshot();

    public OneSyncConfig(@NotNull Plugin plugin, @Nullable String version) {
        super(plugin, version);
        this.reload(false);
    }

    @Override
    protected void reload(@NotNull FileConfiguration file) {
        this.synchronize.reload(file);
        this.snapshot.reload(file);
    }

    @Getter
    @ToString
    public final static class Synchronize {
        private boolean inventory;
        private boolean pdc;
        private boolean gameMode;
        private boolean op;
        private boolean health;
        private boolean exp;
        private boolean food;
        private boolean advancement;

        public void reload(@NotNull FileConfiguration file) {
            this.inventory = file.getBoolean("synchronize.inventory", true);
            this.pdc = file.getBoolean("synchronize.pdc", false);
            this.gameMode = file.getBoolean("synchronize.profile.game-mode", false);
            this.op = file.getBoolean("synchronize.profile.op", false);
            this.health = file.getBoolean("synchronize.profile.health", false);
            this.exp = file.getBoolean("synchronize.profile.exp", false);
            this.food = file.getBoolean("synchronize.profile.food", false);
            this.advancement = file.getBoolean("synchronize.advancement", false);
        }
    }

    @Getter
    @ToString
    public final static class Snapshot {

        private int capacity;

        private int keepDays;

        private Set<SnapshotCause> when = Collections.emptySet();

        public void reload(@NotNull FileConfiguration file) {
            this.capacity = file.getInt("snapshot.capacity", 45);
            this.keepDays = file.getInt("snapshot.keep-days", 7);
            this.when = file.getStringList("snapshot.when").stream().map(name -> {
                try {
                    return SnapshotCause.valueOf(name);
                } catch (Throwable e) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet());
        }
    }

}