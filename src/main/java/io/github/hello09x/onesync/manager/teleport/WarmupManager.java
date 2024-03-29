package io.github.hello09x.onesync.manager.teleport;

import com.google.common.base.Throwables;
import io.github.hello09x.bedrock.util.Folia;
import io.github.hello09x.onesync.Main;
import it.unimi.dsi.fastutil.Function;
import org.apache.commons.lang3.mutable.MutableInt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class WarmupManager {


    public final static WarmupManager instance = new WarmupManager();
    private final static Logger log = Main.getInstance().getLogger();

    private final Map<Player, Warmup> ticker = Folia.isFolia() ? new ConcurrentHashMap<>() : new HashMap<>();

    public WarmupManager() {
        Folia.runTaskTimer(Main.getInstance(), this::tick, 1, 1);
    }

    private void tick() {
        var itr = ticker.entrySet().iterator();
        while (itr.hasNext()) {
            try {
                var entry = itr.next();
                var player = entry.getKey();
                if (!player.isOnline()) {
                    itr.remove();
                    continue;
                }

                var warmup = entry.getValue();
                if (!warmup.valid().apply(player)) {
                    itr.remove();
                    continue;
                }

                if (warmup.remaining.decrementAndGet() <= 0) {
                    warmup.execution().accept(player);
                    itr.remove();
                }
            } catch (Throwable e) {
                log.severe(Throwables.getStackTraceAsString(e));
            }
        }
    }

    public void add(@NotNull Player player, @NotNull Warmup warmup) {
        this.ticker.put(player, warmup);
    }

    public record Warmup(

            @NotNull
            Function<Player, Boolean> valid,

            @NotNull
            Consumer<Player> execution,

            @NotNull
            MutableInt remaining

    ) {

    }

}
