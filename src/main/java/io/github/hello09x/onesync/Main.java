package io.github.hello09x.onesync;

import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import io.github.hello09x.bedrock.menu.ChestMenuRegistry;
import io.github.hello09x.bedrock.util.AnyLevelLoggerHandler;
import io.github.hello09x.bedrock.util.BungeeCord;
import io.github.hello09x.onesync.api.handler.SnapshotHandler;
import io.github.hello09x.onesync.command.SynchronizeCommandRegistry;
import io.github.hello09x.onesync.command.TeleportCommandRegistry;
import io.github.hello09x.onesync.config.OneSyncConfig;
import io.github.hello09x.onesync.listener.BatonListener;
import io.github.hello09x.onesync.listener.SnapshotListener;
import io.github.hello09x.onesync.listener.SynchronizeListener;
import io.github.hello09x.onesync.listener.TeleportListener;
import io.github.hello09x.onesync.manager.synchronize.LockingManager;
import io.github.hello09x.onesync.manager.synchronize.SynchronizeManager;
import io.github.hello09x.onesync.manager.synchronize.handler.*;
import io.github.hello09x.onesync.manager.teleport.PlayerManager;
import io.github.hello09x.onesync.manager.teleport.ServerManager;
import io.github.hello09x.onesync.manager.teleport.TeleportManager;
import io.github.hello09x.onesync.repository.constant.SnapshotCause;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;


public final class Main extends JavaPlugin {

    @Getter
    private final static Gson gson = new Gson();
    @Getter
    private static Main instance;
    @Getter
    private static ChestMenuRegistry chestMenuRegistry;

    @Override
    public void onLoad() {
        instance = this;

        super.getLogger().addHandler(new AnyLevelLoggerHandler(this));
        if (OneSyncConfig.instance.isDebug()) {
            super.getLogger().setLevel(Level.CONFIG);
        }
        OneSyncConfig.instance.addListener(config -> {
            if (config.isDebug()) {
                super.getLogger().setLevel(Level.CONFIG);
            } else {
                super.getLogger().setLevel(Level.INFO);
            }
        });
    }

    @Override
    public void onEnable() {
        try {
            chestMenuRegistry = new ChestMenuRegistry(this);
            SynchronizeCommandRegistry.register();

            {
                var sm = Bukkit.getServicesManager();
                sm.register(SnapshotHandler.class, EnderChestSnapshotHandler.instance, this, ServicePriority.Highest);
                sm.register(SnapshotHandler.class, InventorySnapshotHandler.instance, this, ServicePriority.Highest);
                sm.register(SnapshotHandler.class, ProfileSnapshotHandler.instance, this, ServicePriority.Normal);
                sm.register(SnapshotHandler.class, AdvancementSnapshotHandler.instance, this, ServicePriority.Normal);
                sm.register(SnapshotHandler.class, PDCSnapshotHandler.instance, this, ServicePriority.Normal);
                sm.register(SnapshotHandler.class, PotionEffectSnapshotHandler.instance, this, ServicePriority.Normal);
                sm.register(SnapshotHandler.class, VaultSnapshotHandler.instance, this, ServicePriority.Normal);
            }

            {
                var pm = super.getServer().getPluginManager();
                pm.registerEvents(SynchronizeListener.instance, this);
                pm.registerEvents(SnapshotListener.instance, this);
                pm.registerEvents(TeleportListener.instance, this);
                pm.registerEvents(BatonListener.instance, this);
            }

            {
                var messenger = getServer().getMessenger();
                messenger.registerIncomingPluginChannel(this, BungeeCord.CHANNEL, LockingManager.instance);
                messenger.registerOutgoingPluginChannel(this, BungeeCord.CHANNEL);

                messenger.registerIncomingPluginChannel(this, BungeeCord.CHANNEL, ServerManager.instance);
                messenger.registerOutgoingPluginChannel(this, BungeeCord.CHANNEL);

                messenger.registerIncomingPluginChannel(this, BungeeCord.CHANNEL, PlayerManager.instance);
                messenger.registerOutgoingPluginChannel(this, BungeeCord.CHANNEL);

                messenger.registerIncomingPluginChannel(this, BungeeCord.CHANNEL, TeleportManager.instance);
                messenger.registerOutgoingPluginChannel(this, BungeeCord.CHANNEL);
            }

            {
                var mgr = ProtocolLibrary.getProtocolManager();
                mgr.addPacketListener(SynchronizeListener.instance);
            }

            LockingManager.instance.releaseAll();           // 崩服重启, 释放上一次会话的锁
            LockingManager.instance.acquireAll();           // 热重载

            if (OneSyncConfig.instance.getTeleport().isEnabled()) {
                // 命令不支持 reload
                TeleportCommandRegistry.register();
            }
        } catch (Throwable e) {
            getLogger().severe("加载插件失败, 为了数据安全, 关闭当前服务器!\n" + Throwables.getStackTraceAsString(e));
            Bukkit.shutdown();
            throw e;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        SynchronizeManager.instance.saveAndUnlockAll(SnapshotCause.PLUGIN_DISABLE);  // 关闭服务器不会调用 PlayerQuitEvent 事件, 因此需要全量保存一次

        {
            var messenger = getServer().getMessenger();
            messenger.unregisterIncomingPluginChannel(this);
            messenger.unregisterOutgoingPluginChannel(this);
        }

        {
            Bukkit.getServicesManager().unregisterAll(this);
        }
    }
}
