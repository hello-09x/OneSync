package io.github.hello09x.onesync.config;

import io.github.hello09x.bedrock.config.Config;
import io.github.hello09x.onesync.Main;
import io.github.hello09x.onesync.repository.constant.SnapshotCause;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@ToString
public class OneSyncConfig extends Config<OneSyncConfig> {

    public final static OneSyncConfig instance = new OneSyncConfig(Main.getInstance(), "4");

    private final SynchronizeConfig synchronize = new SynchronizeConfig();
    private final SnapshotConfig snapshot = new SnapshotConfig();
    private final TeleportConfig teleport = new TeleportConfig();
    private boolean debug;

    @Setter
    private String serverId = UUID.randomUUID().toString();

    public OneSyncConfig(@NotNull Plugin plugin, @Nullable String version) {
        super(plugin, version);
        this.reload(false);
    }

    private static @NotNull String getNonBlankString(@NotNull FileConfiguration file, @NotNull String path, @NotNull String def) {
        var value = file.getString(path, def);
        if (value.isBlank()) {
            return def;
        }
        return value;
    }

    @Override
    protected void reload(@NotNull FileConfiguration file) {
        this.debug = file.getBoolean("debug", true);
        this.synchronize.reload(file);
        this.snapshot.reload(file);
        this.teleport.reload(file);
        Optional.ofNullable(file.getString("server-id")).filter(StringUtils::isNotBlank).ifPresent(this::setServerId);
    }

    @Getter
    @ToString
    public final static class SynchronizeConfig {

        private Enabled inventory;
        private Enabled enderChest;
        private Enabled pdc;
        private Enabled gameMode;
        private Enabled op;
        private Enabled health;
        private Enabled exp;
        private Enabled food;
        private Enabled air;
        private Enabled advancements;
        private Enabled potionEffects;
        private Enabled vault;

        public void reload(@NotNull FileConfiguration file) {
            this.inventory = Enabled.ofValue(file.getString("synchronize.inventory", "true"));
            this.enderChest = Enabled.ofValue(file.getString("synchronize.ender-chest", "true"));
            this.pdc = Enabled.ofValue(file.getString("synchronize.pdc", "false"));
            this.gameMode = Enabled.ofValue(file.getString("synchronize.profile.game-mode", "false"));
            this.op = Enabled.ofValue(file.getString("synchronize.profile.op", "false"));
            this.health = Enabled.ofValue(file.getString("synchronize.profile.health", "false"));
            this.exp = Enabled.ofValue(file.getString("synchronize.profile.exp", "false"));
            this.food = Enabled.ofValue(file.getString("synchronize.profile.food", "false"));
            this.air = Enabled.ofValue(file.getString("synchronize.profile.air", "false"));
            this.advancements = Enabled.ofValue(file.getString("synchronize.advancements", "false"));
            this.potionEffects = Enabled.ofValue(file.getString("synchronize.potion-effects", "false"));
            this.vault = Enabled.ofValue(file.getString("synchronize.vault", "false"));
        }
    }

    @Getter
    @ToString
    public final static class SnapshotConfig {

        /**
         * 每个玩家最大快照数
         */
        private int capacity;

        /**
         * 每个玩家多少天内每天至少保留一份最后的快照
         */
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

    @Getter
    @ToString
    public final static class TeleportConfig {

        private final Map<String, String> commands = new HashMap<>();
        private boolean enabled;
        private int warmup;
        private Duration expiresIn;
        private boolean particle;
        private boolean sound;

        public void reload(@NotNull FileConfiguration file) {
            this.enabled = file.getBoolean("teleport.enabled", false);
            this.expiresIn = Duration.ofSeconds(file.getInt("teleport.expires-in", 60));
            this.warmup = file.getInt("teleport.warmup", 3);
            this.particle = file.getBoolean("teleport.particle", true);
            this.sound = file.getBoolean("teleport.sound", true);

            this.commands.clear();
            this.commands.put("stpa", getNonBlankString(file, "teleport.commands.stpa", "tpa"));
            this.commands.put("stpahere", getNonBlankString(file, "teleport.commands.stpahere", "tpahere"));
            this.commands.put("stpaccept", getNonBlankString(file, "teleport.commands.stpaccept", "tpaccept"));
            this.commands.put("stpdeny", getNonBlankString(file, "teleport.commands.stpdeny", "tpdeny"));
            this.commands.put("stpcacel", getNonBlankString(file, "teleport.commands.stpcacel", "tpcacel"));

            this.commands.put("stp", getNonBlankString(file, "teleport.commands.stp", "tp"));
            this.commands.put("stphere", getNonBlankString(file, "teleport.commands.stphere", "tphere"));
            this.commands.put("stphereall", getNonBlankString(file, "teleport.commands.stphereall", "tphereall"));
            this.commands.put("stpahereall", getNonBlankString(file, "teleport.commands.stpahereall", "tpahereall"));
        }
    }

}
