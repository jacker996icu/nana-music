package com.jacker.plugin.music.manager;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.jacker.plugin.music.constant.DataKeys;
import com.jacker.plugin.music.constant.MusicConstant;
import com.jacker.plugin.music.dto.Playlist;
import com.jacker.plugin.music.dto.PlaylistResponse;
import com.jacker.plugin.music.dto.Profile;
import com.jacker.plugin.music.dto.Track;
import com.jacker.plugin.music.setting.MusicConfig;
import com.jacker.plugin.music.setting.MusicPersistentConfig;
import com.jacker.plugin.music.util.*;
import com.jacker.plugin.music.window.MusicPlayerPanel;
import com.jacker.plugin.music.window.MusicTableCellRenderer;
import com.jacker.plugin.music.window.MusicWindowFactory;
import icons.MusicIcons;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class ViewManager {

    private static JScrollPane musicScrollPane;

    private static JTable musicTable;

    private static MusicPlayerPanel musicPlayerPanel;

    public static void updateMusicPanel(Project project, Profile profile) {
        JPanel groupPanel = MusicWindowFactory.getDataContext(project).getData(DataKeys.GROUP_PANEL);
        if (groupPanel != null) {
            ApplicationManager.getApplication().invokeAndWait(() -> {
                /* --------------顶部开始-------------- */
                // 用户信息
                JPanel userPanel = buildUserPanel(profile);
                // 播放器
                musicPlayerPanel = buildMusicPlayerPanel(project);

                JPanel headerPanel = new JPanel();
                headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
                headerPanel.add(userPanel);
                headerPanel.add(musicPlayerPanel);

                // 歌单列表
                musicTable = new JBTable();

                Long defaultPlaylistId = null;
                PlaylistResponse playlistResponse = CloudMusicUtil.getPlaylist(30, 0);
                if (playlistResponse != null && CollectionUtils.isNotEmpty(playlistResponse.getPlaylist())) {
                    defaultPlaylistId = playlistResponse.getPlaylist().get(0).getId();
                    JComboBox<Playlist> playlistComboBox = new JComboBox<>();
                    for (Playlist playlist : playlistResponse.getPlaylist()) {
                        playlistComboBox.addItem(playlist);
                    }
                    playlistComboBox.setSelectedIndex(0);
                    playlistComboBox.setMaximumRowCount(8);

                    // 点击切换歌曲列表
                    playlistComboBox.addItemListener(e -> {
                        Playlist selectedItem = (Playlist) playlistComboBox.getSelectedItem();
                        if (Objects.nonNull(selectedItem)) {
                            buildSongList(musicTable, musicPlayerPanel, selectedItem.getId());
                        }
                    });

                    headerPanel.add(playlistComboBox);
                }
                groupPanel.add(headerPanel, BorderLayout.PAGE_START, 0);
                /* --------------顶部结束-------------- */

                /* --------------中间开始-------------- */
                musicScrollPane = new JBScrollPane();
                //点击事件
                musicTable.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent evt) {
                        // 监听鼠标双击事件
                        if (evt.getClickCount() == 2) {
                            int selectedIndex = musicTable.getSelectedRow();
                            Track track = (Track) musicTable.getModel().getValueAt(selectedIndex, 0);
                            if (Objects.nonNull(track) && Objects.equals(track.getCanPlay(), false)) {
                                // 没有版权
                                MessageUtils.getInstance(project).showWarnMsg("《" + track.getName() + "》 暂时下架");
                                return;
                            }
                            if (Objects.nonNull(track) && Objects.equals(track.getTryPlay(), true)) {
                                // 试听版本
                                MessageUtils.getInstance(project).showWarnMsg("《" + track.getName() + "》 仅VIP可播");
                                return;
                            }
                            //保存播放序号
                            musicPlayerPanel.setCurrentPlayIndex(selectedIndex);
                            //下载或播放
                            musicPlayerPanel.play(selectedIndex);
                        }
                    }
                });

                musicPlayerPanel.setMusicTable(musicTable);
                musicScrollPane.setViewportView(musicTable);
                groupPanel.add(musicScrollPane, BorderLayout.CENTER, 0);

                buildSongList(musicTable, musicPlayerPanel, defaultPlaylistId);
                /* --------------中间结束-------------- */

                /* --------------底部开始-------------- */
                // 底部占位区域
                JPanel end = new JPanel();
                end.setPreferredSize(new Dimension(400, 50));
                end.setMaximumSize(new Dimension(400, 50));
                end.setVisible(true);
                groupPanel.add(end, BorderLayout.PAGE_END);
                /* --------------顶部结束-------------- */
            });
        }
    }

    /**
     * 刷新歌曲列表
     */
    private static void buildSongList(JTable musicTable, MusicPlayerPanel musicPlayerPanel, Long playListId) {
        if (Objects.isNull(musicTable) || Objects.isNull(musicPlayerPanel) || Objects.isNull(playListId)) {
            return;
        }
        // 歌曲列表
        List<Track> songs = CloudMusicUtil.getSongs(playListId);
        if (CollectionUtils.isNotEmpty(songs)) {
            Vector<String> columns = new Vector<>(Arrays.asList("name", "author", "time"));
            Vector<Vector<Object>> rows = new Vector<>();
            for (Track track : songs) {
                Vector<Object> v = new Vector<>();
                v.add(track);
                v.add(track.getAr().get(0).getName());
                v.add(secondToTime(track.getDt() / 1000));
                rows.add(v);
            }
            DefaultTableModel dtm = new DefaultTableModel(rows, columns) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Track.class : String.class;
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

            };
            musicTable.setModel(dtm);
            MusicTableCellRenderer cellRenderer = new MusicTableCellRenderer();
            TableColumn columnName = musicTable.getColumn(musicTable.getColumnName(0));
            columnName.setCellRenderer(cellRenderer);
            columnName.setPreferredWidth(180);
            TableColumn columnAuthor = musicTable.getColumn(musicTable.getColumnName(1));
            columnAuthor.setCellRenderer(cellRenderer);
            columnAuthor.setPreferredWidth(100);
            TableColumn columnTime = musicTable.getColumn(musicTable.getColumnName(2));
            columnTime.setCellRenderer(cellRenderer);
            columnTime.setPreferredWidth(80);
            columnTime.setMaxWidth(80);

            musicTable.getTableHeader().setVisible(false);
            musicTable.setShowGrid(false);
            musicTable.setFocusable(false);
            musicTable.setRowHeight(30);
            musicPlayerPanel.setMusicListSize(rows.size());
            musicPlayerPanel.setMusicModelList(dtm);
            // 默认从第一个开始播放
            musicPlayerPanel.setCurrentPlayIndex(-1);
            musicPlayerPanel.setPreviousPlayIndex(-1);
            musicTable.scrollRectToVisible(musicTable.getCellRect(0, 0, true));
        }
    }

    public static String secondToTime(long second) {
        long minutes = second / 60;
        second = second % 60;
        return CryptoUtil.zfill(String.valueOf(minutes), 2) + ":" + CryptoUtil.zfill(String.valueOf(second), 2);
    }

    @NotNull
    private static MusicPlayerPanel buildMusicPlayerPanel(Project project) {
        MusicPlayerPanel musicPlayerPanel = new MusicPlayerPanel(project, 430, 70);
        musicPlayerPanel.setPreferredSize(new Dimension(430, 70));
        musicPlayerPanel.setMaximumSize(new Dimension(430, 70));
        return musicPlayerPanel;
    }

    @NotNull
    private static JPanel buildUserPanel(Profile profile) {
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.LINE_AXIS));
        String avatarFile = getAvatarFile(profile);
        ImageIcon icon = new ImageIcon(avatarFile);
        icon.setImage(icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
        iconLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        userPanel.add(iconLabel);

        JPanel nickAndSigPanel = new JPanel();
        nickAndSigPanel.setLayout(new BoxLayout(nickAndSigPanel, BoxLayout.Y_AXIS));
        JLabel nickname = new JLabel(profile.getNickname());
        nickname.setAlignmentX(Component.LEFT_ALIGNMENT);
        nickname.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 0));
        nickAndSigPanel.add(nickname);
        JTextArea signature = new JTextArea("| " + profile.getSignature(), 3, 15);
        signature.setForeground(JBColor.GRAY);
        signature.setWrapStyleWord(true);
        signature.setLineWrap(true);
        signature.setOpaque(false);
        signature.setEditable(false);
        signature.setFocusable(false);
        signature.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        signature.setAlignmentX(Component.LEFT_ALIGNMENT);
        nickAndSigPanel.add(signature);
        nickAndSigPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        userPanel.add(nickAndSigPanel);
        return userPanel;
    }

    @NotNull
    private static String getAvatarFile(Profile profile) {
        MusicConfig config = MusicPersistentConfig.getInstance().getMusicConfig();
        String filePath = config.getFilePath();
        String dirPath = filePath + File.separator;
        String avatarFile = dirPath + MusicConstant.AVATAR_FILE;
        if (!FileUtil.contain(avatarFile)) {
            DownloadUtil.download(dirPath, profile.getAvatarUrl(), MusicConstant.AVATAR_FILE);
        }
        return avatarFile;
    }

    public static void position() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            int currentPlayIndex = musicPlayerPanel.getCurrentPlayIndex();
            musicTable.scrollRectToVisible(musicTable.getCellRect(currentPlayIndex, 0, true));
        });
    }

    public static JScrollPane getMusicScrollPane() {
        return musicScrollPane;
    }

    public static JTable getMusicTable() {
        return musicTable;
    }

    public static MusicPlayerPanel getMusicPlayerPanel() {
        return musicPlayerPanel;
    }

    public static void stopAndRemove(Project project) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            // 停止播放
            musicPlayerPanel.stop();
            // 移除cookie
            CloudMusicUtil.logout();
            // 清空播放列表
            JPanel groupPanel = MusicWindowFactory.getDataContext(project).getData(DataKeys.GROUP_PANEL);
            if (groupPanel != null) {
                DefaultTableModel tableModel = (DefaultTableModel)musicTable.getModel();
                tableModel.setRowCount(0);
                musicTable.removeAll();
                musicTable = null;
                musicScrollPane.removeAll();
                musicScrollPane = null;
                musicPlayerPanel.removeAll();
                musicPlayerPanel = null;
                groupPanel.removeAll();
                groupPanel.add(buildTipsPane(), BorderLayout.CENTER);
            }
        });

    }

    @NotNull
    public static JTextPane buildTipsPane() {
        JTextPane tipsPane = new JTextPane();
        tipsPane.setOpaque(false);
        String addIconText = "'login'";
        String configIconText = "'config'";
        String message = PropertiesUtils.getInfo("config.load", addIconText, configIconText);
        int addIconMarkerIndex = message.indexOf(addIconText);
        tipsPane.replaceSelection(message.substring(0, addIconMarkerIndex));
        tipsPane.insertIcon(MusicIcons.LOGIN);
        int configIconMarkerIndex = message.indexOf(configIconText);
        tipsPane.replaceSelection(message.substring(addIconMarkerIndex + addIconText.length(), configIconMarkerIndex));
        tipsPane.insertIcon(MusicIcons.CONFIG);
        tipsPane.replaceSelection(message.substring(configIconMarkerIndex + configIconText.length()));
        tipsPane.setEditable(false);
        tipsPane.setFocusable(false);
        return tipsPane;
    }
}
