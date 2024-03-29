package io.github.hello09x.onesync.repository;

import io.github.hello09x.bedrock.database.Repository;
import io.github.hello09x.bedrock.database.typehandler.JsonTypeHandler;
import io.github.hello09x.bedrock.util.Exceptions;
import io.github.hello09x.onesync.Main;
import io.github.hello09x.onesync.repository.model.AdvancementSnapshot;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.Statement;

public class AdvancementSnapshotRepository extends Repository<AdvancementSnapshot> {

    public final static AdvancementSnapshotRepository instance = new AdvancementSnapshotRepository(Main.getInstance());

    public AdvancementSnapshotRepository(@NotNull Plugin plugin) {
        super(plugin);
    }

    public int insert(@NotNull AdvancementSnapshot snapshot) {
        var sql = "insert into advancement_snapshot (snapshot_id, player_id, advancements) values (?, ?, ?)";
        return execute(connection -> {
            try (PreparedStatement stm = connection.prepareStatement(sql)) {
                stm.setLong(1, snapshot.snapshotId());
                stm.setString(2, snapshot.playerId().toString());
                stm.setString(3, JsonTypeHandler.gson.toJson(snapshot.advancements()));
                return stm.executeUpdate();
            }
        });
    }

    @Override
    protected void initTables() {
        execute(connection -> {
            Statement stm = connection.createStatement();
            stm.executeUpdate("""
                    create table if not exists advancement_snapshot
                    (
                        snapshot_id  bigint   not null comment '快照 ID'
                            primary key,
                        player_id    char(36) not null comment '玩家 ID',
                        advancements json     not null comment '成就数据'
                    );
                    """);
            Exceptions.noException(() -> {
                stm.executeUpdate("""
                        create index advancement_snapshot_player_id_index
                            on advancement_snapshot (player_id);
                        """);
            });
        });
    }
}
