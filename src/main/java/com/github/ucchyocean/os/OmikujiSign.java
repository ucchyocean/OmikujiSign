/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.os;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * おみくじサイン
 * @author ucchy
 */
public class OmikujiSign extends JavaPlugin implements Listener {

    private static final String FIRST_LINE = "[omikuji]";
    private static final String SLOT_META_NAME = "omikujislot";

    private static final int MIN_ROLL_TIMES = 15;
    private static final int MAX_ROLL_TIMES = 25;
    private static final String[] ITEMS =
        {"§4大吉", "§5吉", "§2小吉", "§3末吉", "§9凶", "§8大凶"};

    private static JavaPlugin instance;

    /**
     * プラグインが起動した時に呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        instance = this;

        // イベントリスナーの登録
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * プレイヤーがクリックした時に呼び出されるメソッド
     * @param event
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        // ブロック左クリックでもブロック右クリックでもなければ無視する
        if ( event.getAction() != Action.RIGHT_CLICK_BLOCK &&
                event.getAction() != Action.LEFT_CLICK_BLOCK ) {
            return;
        }

        // クリック先が看板でなければ無視する
        Block block = event.getClickedBlock();
        if ( block == null || (block.getType() != Material.SIGN &&
                block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) ) {
            return;
        }

        // 1行目が[omikuji]でなければ無視する
        final Sign sign = (Sign)block.getState();
        if ( !sign.getLine(0).equals(FIRST_LINE) ) {
            return;
        }

        // 既にスロットが動作中なら無視する
        if ( sign.hasMetadata(SLOT_META_NAME) ) {
            return;
        }

        // おみくじスロット開始！

        // おみくじ項目を準備する
        final ArrayList<String> items = new ArrayList<String>();
        for ( String i : ITEMS ) {
            items.add(i);
        }
        Collections.shuffle(items); // シャッフル！

        // メタデータを入れて、スロットをクリック禁止にする
        sign.setMetadata(SLOT_META_NAME, new FixedMetadataValue(this, true));

        // 同期タイマータスクを開始
        new BukkitRunnable() {

            private int count = MIN_ROLL_TIMES +
                    (int)(Math.random() * (MAX_ROLL_TIMES - MIN_ROLL_TIMES));
            private int index = 0;

            @Override
            public void run() {

                count--;

                if ( count > 0 ) {
                    // スロット回転中

                    index++;
                    if ( index >= items.size() ) {
                        index = 0;
                    }

                    // 看板に表示
                    sign.setLine(1, items.get(index));
                    sign.update();

                } else if ( -13 < count && count <= 0 ) {
                    // スロット回転終了、結果を点滅させる

                    if ( count % 2 == 0 ) {
                        switch ( (int)(count / 2) ) {
                        case 0:
                        case -2:
                        case -4:
                            sign.setLine(1, items.get(index));
                            sign.update();
                            break;
                        case -1:
                        case -3:
                        case -5:
                            sign.setLine(1, " ");
                            sign.update();
                            break;
                        }
                    }

                } else {
                    //

                    sign.setLine(1, items.get(index));
                    sign.update();

                    // メタデータ除去
                    sign.removeMetadata(SLOT_META_NAME, instance);

                    // タイマー終了
                    cancel();
                }
            }

        }.runTaskTimer(this, 3, 3);
    }
}
