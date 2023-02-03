package com.jacker.plugin.music.window;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.jacker.plugin.music.constant.ModeEnum;
import com.jacker.plugin.music.dto.Track;
import com.jacker.plugin.music.player.MusicPlayer;
import com.jacker.plugin.music.player.audio.player.Player;
import com.jacker.plugin.music.player.model.TrackData;
import com.jacker.plugin.music.setting.MusicConfig;
import com.jacker.plugin.music.setting.MusicPersistentConfig;
import com.jacker.plugin.music.statusbar.MusicStatusChangeListener;
import com.jacker.plugin.music.util.*;
import icons.MusicIcons;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 播放器面板
 */
public class MusicPlayerPanel extends JPanel implements MouseListener {
    /**
     * 音频列表
     */
    public JTable musicTable;

    /**
     * 音频列表
     */
    private DefaultTableModel musicModelList;

    /**
     * 播放列表长度
     */
    private int musicListSize = 0;

    /**
     * MusicPlayer
     */
    public static MusicPlayer musicPlayer;

    /**
     * 当前播放音频的序号
     */
    private int currentPlayIndex;

    /**
     * 前一个播放音频的序号
     */
    private int previousPlayIndex;

    /**
     * 定义长宽
     */
    private final int width, height;

    /**
     * 按钮
     */
    public static JButton btnLast, btnNext, btnPlayOrPause, btnMode;

    /**
     * 歌曲名称
     */
    public JLabel title;

    /**
     * 演唱者
     */
    public static JLabel author;

    /**
     * 进度条
     */
    private JSlider progressSlider;

    /**
     * 播放时间标签
     */
    private JLabel timeLabel;

    /**
     * 播放状态
     */
    private boolean gotoPlay = false;

    /**
     * 是否已经播放过
     */
    private boolean hasPlay = false;

    /**
     * 是否停止
     */
    private boolean isStopped = false;

    /**
     * 播放模式
     */
    private ModeEnum mode = ModeEnum.LIST;

    /**
     * 进度条启用状态
     */
    private boolean progressEnabled = false;

    /**
     * 是否正在拖动进度条
     */
    private boolean isSeeking = false;

    /**
     * 定时器
     */
    private final Timer timer;

    private final Project project;

    public void setMusicListSize(int musicListSize) {
        this.musicListSize = musicListSize;
    }

    public void setCurrentPlayIndex(int currentPlayIndex) {
        this.currentPlayIndex = currentPlayIndex;
    }

    public int getCurrentPlayIndex() {
        return currentPlayIndex;
    }

    public void setPreviousPlayIndex(int previousPlayIndex) {
        this.previousPlayIndex = previousPlayIndex;
    }

    public MusicPlayerPanel(Project project, int width, int height) {
        this.project = project;
        this.width = width;
        this.height = height;
        timer = new Timer(1000, e -> {
            if (progressEnabled && musicPlayer.isPlaying()) {
                progressSlider.setValue((int) musicPlayer.getCurrentPlayer().getCurrentSample());
            }
            if (musicPlayer.isPlaying() && !isSeeking) {
                updateTimeLabel();
            }
        });
        timer.start();

        musicPlayer = MusicPlayer.getInstancePlayer();
        //添加监听器
        musicPlayer.addListener(e -> {
            TrackData track = musicPlayer.getCurrentPlayer().getTrack();

            switch (e.getEventCode()) {
                case PLAYING_STARTED:
                    timer.start();
                    break;
                case PAUSED:
                    timer.stop();
                    break;
                case STOPPED:
                    timer.stop();
                    progressEnabled = false;
                    timeLabel.setText("");

                    if (hasPlay && !isStopped) {
                        //是播放完毕，则尝试自动播放下一首，如果播放失败，则尝试再下一首
                        do {
                            setCurrentPlayIndex(getNextPlayIndex());
                        } while (this.play(currentPlayIndex));
                    }

                    progressSlider.setValue(progressSlider.getMinimum());
                    break;
                case FILE_OPENED:
                    if (track != null) {
                        int max = (int) track.getTotalSamples();
                        if (max == -1) {
                            progressEnabled = false;
                        } else {
                            progressEnabled = true;
                            progressSlider.setMaximum(max);
                        }
                    }
                    progressSlider.setValue((int) musicPlayer.getCurrentPlayer().getCurrentSample());
                    updateTimeLabel();
                    break;
                case SEEK_FINISHED:
                    //跳播放进度结束
                    isSeeking = false;
                    break;
            }
        });
        setCurrentPlayIndex(-1);
        initUI();
    }

    private int getNextPlayIndex() {
        switch (mode) {
            case LIST:
                return (currentPlayIndex + 1) % musicListSize;
            case SINGLE:
                return currentPlayIndex;
            case RANDOM:
                return ThreadLocalRandom.current().nextInt(musicListSize);
        }
        return currentPlayIndex;
    }


    /**
     * 更新时间标记
     */
    private void updateTimeLabel() {
        Player player = musicPlayer.getCurrentPlayer();
        updateTimeLabel(player.getCurrentSample(), player);
    }

    /**
     * 根据当前时间更新时间标记
     *
     * @param currentSample 当前时间
     */
    private void updateTimeLabel(long currentSample) {
        Player player = musicPlayer.getCurrentPlayer();
        updateTimeLabel(currentSample, player);
    }

    /**
     * 根据当前时间更新时间标记
     *
     * @param currentSample 当前时间
     * @param player        音乐播放器实例
     */
    private void updateTimeLabel(long currentSample, Player player) {
        if (player.getTrack() != null) {
            TrackData trackData = player.getTrack();
            timeLabel.setText(TimeUtil.samplesToTime(currentSample, player.getTrack().getSampleRate()) + " / " + trackData.getLength());
            MusicStatusChangeListener publisher = ApplicationManager.getApplication().getMessageBus().syncPublisher(MusicStatusChangeListener.MUSIC_STATUS_CHANGE_LISTENER_TOPIC);
            publisher.updateStatus(title.getText(), timeLabel.getText());
        }
    }

    /**
     * 获得X轴滑动值
     *
     * @param slider 滑动控件
     * @param x      X轴值
     * @return X轴差值
     */
    private int getSliderValueForX(JSlider slider, int x) {
        return ((BasicSliderUI) slider.getUI()).valueForXPosition(x);
    }

    /**
     * 初始化UI
     */
    public void initUI() {

        this.setLayout(new BorderLayout());

        //进度条
        progressSlider = new MusicSlider(SwingConstants.HORIZONTAL);
        MusicSlider.MusicSliderUI musicSliderUI = new MusicSlider.MusicSliderUI(progressSlider);
        progressSlider.setUI(musicSliderUI);
        progressSlider.setPreferredSize(new Dimension(width, 5));
        progressSlider.setValue(0);
        progressSlider.setFocusable(false);
        //监听鼠标事件
        progressSlider.addMouseListener(new MouseAdapter() {
            //鼠标释放事件
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!progressEnabled) {
                    return;
                }
                musicPlayer.getCurrentPlayer().seek(progressSlider.getValue());
            }

            //鼠标按下事件
            @Override
            public void mousePressed(MouseEvent e) {
                if (!progressEnabled) {
                    return;
                }
                progressSlider.setValue(getSliderValueForX(progressSlider, e.getX()));
                //修改时间标签显示
                updateTimeLabel(progressSlider.getValue());
            }
        });
        //鼠标拖动事件
        progressSlider.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!progressEnabled) {
                    return;
                }
                //修改标记，表示正在拖动进度条
                isSeeking = true;
                //修改进度条值
                progressSlider.setValue(getSliderValueForX(progressSlider, e.getX()));
                //修改时间标签显示
                updateTimeLabel(progressSlider.getValue());

            }
        });

        // 播放器
        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        //上一曲
        btnLast = new JButton(MusicIcons.PREVIOUS);
        btnLast.setPreferredSize(new Dimension(40, 40));
        btnLast.setOpaque(false);
        btnLast.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        btnLast.setToolTipText("上一曲");
        btnLast.setContentAreaFilled(false);
        btnLast.setFocusPainted(false);
        btnLast.addMouseListener(this);

        //下一曲
        btnNext = new JButton(MusicIcons.NEXT);
        btnNext.setPreferredSize(new Dimension(40, 40));
        btnNext.setOpaque(false);
        btnNext.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        btnNext.setToolTipText("下一曲");
        btnNext.setContentAreaFilled(false);
        btnNext.setFocusPainted(false);
        btnNext.addMouseListener(this);

        //暂停或播放
        btnPlayOrPause = new JButton(MusicIcons.PLAY);
        btnPlayOrPause.setPreferredSize(new Dimension(40, 40));
        btnPlayOrPause.setOpaque(false);
        btnPlayOrPause.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        btnPlayOrPause.setContentAreaFilled(false);
        btnPlayOrPause.setFocusPainted(false);
        btnPlayOrPause.addMouseListener(this);

        //播放模式
        btnMode = new JButton(MusicIcons.MODE_LIST);
        btnMode.setPreferredSize(new Dimension(40, 40));
        btnMode.setOpaque(false);
        btnMode.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        btnMode.setContentAreaFilled(false);
        btnMode.setFocusPainted(false);
        btnMode.addMouseListener(this);

        playerPanel.add(btnLast);
        playerPanel.add(btnPlayOrPause);
        playerPanel.add(btnNext);
        playerPanel.add(btnMode);
        playerPanel.setPreferredSize(new Dimension(160, 40));
        playerPanel.setMaximumSize(new Dimension(160, 40));

        // 左侧标题和时间
        JPanel songInfo = new JPanel();
        songInfo.setLayout(new BoxLayout(songInfo, BoxLayout.Y_AXIS));
        JPanel titleAndAuthor = new JPanel();
        titleAndAuthor.setLayout(new BoxLayout(titleAndAuthor, BoxLayout.LINE_AXIS));
        title = new JLabel("");
        title.setFont(new Font("字体", Font.PLAIN, 14));
        title.setSize(new Dimension(80, 20));
        title.setMaximumSize(new Dimension(80, 20));
        title.setPreferredSize(new Dimension(80, 20));
        title.setVisible(false);
        title.setHorizontalAlignment(SwingConstants.LEFT);
        titleAndAuthor.add(title);
        author = new JLabel("");
        author.setFont(new Font("字体", Font.PLAIN, 10));
        author.setSize(new Dimension(80, 20));
        author.setMaximumSize(new Dimension(80, 20));
        author.setPreferredSize(new Dimension(80, 20));
        author.setVisible(false);
        author.setHorizontalAlignment(SwingConstants.LEFT);
        author.setVerticalAlignment(SwingConstants.BOTTOM);
        titleAndAuthor.add(author);
        titleAndAuthor.setAlignmentX(LEFT_ALIGNMENT);
        songInfo.add(titleAndAuthor);

        // 音频时间
        timeLabel = new JLabel("");
        timeLabel.setFont(new Font("字体", Font.PLAIN, 10));
        timeLabel.setForeground(JBColor.GRAY);
        timeLabel.setVisible(false);
        timeLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        songInfo.add(timeLabel);

        songInfo.setSize(250, 40);
        songInfo.setPreferredSize(new Dimension(250, 40));
        songInfo.setMaximumSize(new Dimension(250, 40));
        songInfo.setBorder(BorderFactory.createEmptyBorder(8, 10, 0, 0));
        songInfo.setAlignmentX(LEFT_ALIGNMENT);

        // 播放进度条
        JPanel processBarPanel = new JPanel();
        processBarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        processBarPanel.add(progressSlider);

        this.add(processBarPanel, BorderLayout.PAGE_START);
        this.add(songInfo, BorderLayout.LINE_START);
        this.add(playerPanel, BorderLayout.CENTER);
        this.setSize(width, height);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (btnPlayOrPause.equals(e.getComponent())) {
            playOrPause();
        } else if (btnLast.equals(e.getComponent())) {
            playPrevious();
        } else if (btnNext.equals(e.getComponent())) {
            playNext();
        } else if (btnMode.equals(e.getComponent())) {
            ModeEnum nexMode = ModeEnum.getNexMode(mode);
            mode = nexMode;
            switch (nexMode) {
                case LIST:
                    btnMode.setIcon(MusicIcons.MODE_LIST);
                    break;
                case SINGLE:
                    btnMode.setIcon(MusicIcons.MODE_SINGLE);
                    break;
                case RANDOM:
                    btnMode.setIcon(MusicIcons.MODE_RANDOM);
                    break;
            }
        }
    }

    public void playNext() {
        int maxTry = 3;
        do {
            if (mode == ModeEnum.SINGLE) {
                setCurrentPlayIndex((currentPlayIndex + 1) % musicListSize);
            } else {
                setCurrentPlayIndex(getNextPlayIndex());
            }
            maxTry--;
        } while (maxTry > 0 && this.play(currentPlayIndex));
    }

    public void playPrevious() {
        if (currentPlayIndex - 1 >= 0) {
            int maxTry = 3;
            do {
                setCurrentPlayIndex(currentPlayIndex - 1);
                maxTry--;
            } while (maxTry > 0 && this.play(currentPlayIndex));
        }
    }

    public void playOrPause() {
        gotoPlay = !gotoPlay;
        if (gotoPlay) {
            btnPlayOrPause.setIcon(MusicIcons.PAUSE);
            btnPlayOrPause.setToolTipText("暂停");

            if (hasPlay) {
                // 恢复播放
                musicPlayer.playOrPause();
            } else {
                // 第一次播放
                int maxTry = 3;
                do {
                    setCurrentPlayIndex(Math.min(currentPlayIndex + 1, musicListSize - 1));
                    maxTry--;
                } while (maxTry > 0 && this.play(currentPlayIndex));
            }
        } else {
            // 暂停
            btnPlayOrPause.setIcon(MusicIcons.PLAY);
            btnPlayOrPause.setToolTipText("播放");
            musicPlayer.playOrPause();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (btnPlayOrPause.equals(e.getComponent())) {
            if (gotoPlay) {
                btnPlayOrPause.setIcon(MusicIcons.PAUSE_HOVER);
            } else {
                btnPlayOrPause.setIcon(MusicIcons.PLAY_HOVER);
            }
        } else if (btnLast.equals(e.getComponent())) {
            btnLast.setIcon(MusicIcons.PREVIOUS_HOVER);
        } else if (btnNext.equals(e.getComponent())) {
            btnNext.setIcon(MusicIcons.NEXT_HOVER);
        } else if (btnMode.equals(e.getComponent())) {
            switch (mode) {
                case LIST:
                    btnMode.setIcon(MusicIcons.MODE_LIST_HOVER);
                    btnMode.setToolTipText("列表循环");
                    break;
                case SINGLE:
                    btnMode.setIcon(MusicIcons.MODE_SINGLE_HOVER);
                    btnMode.setToolTipText("单曲循环");
                    break;
                case RANDOM:
                    btnMode.setIcon(MusicIcons.MODE_RANDOM_HOVER);
                    btnMode.setToolTipText("随机播放");
                    break;
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (btnPlayOrPause.equals(e.getComponent())) {
            if (gotoPlay) {
                btnPlayOrPause.setIcon(MusicIcons.PAUSE);
            } else {
                btnPlayOrPause.setIcon(MusicIcons.PLAY);
            }
        } else if (btnLast.equals(e.getComponent())) {
            btnLast.setIcon(MusicIcons.PREVIOUS);
        } else if (btnNext.equals(e.getComponent())) {
            btnNext.setIcon(MusicIcons.NEXT);
        } else if (btnMode.equals(e.getComponent())) {
            switch (mode) {
                case LIST:
                    btnMode.setIcon(MusicIcons.MODE_LIST);
                    break;
                case SINGLE:
                    btnMode.setIcon(MusicIcons.MODE_SINGLE);
                    break;
                case RANDOM:
                    btnMode.setIcon(MusicIcons.MODE_RANDOM);
                    break;
            }
        }
    }

    /**
     * @param index 歌曲索引
     * @return 是否播放失败，需要尝试播放下一首
     */
    public boolean play(int index) {
        int rowCount = musicModelList.getRowCount();
        if (rowCount > 0) {
            if (index < rowCount && index >= 0) {
                // 播放
                Track track = (Track) musicModelList.getValueAt(index, 0);
                if (Objects.equals(track.getCanPlay(), false) || Objects.equals(track.getTryPlay(), true)) {
                    // 没有版权，直接跳过
                    return true;
                }
                if (playOrDownload(track)) {
                    if (!hasPlay) {
                        hasPlay = true;
//                        MusicStatusChangeListener publisher = ApplicationManager.getApplication().getMessageBus().syncPublisher(MusicStatusChangeListener.MUSIC_STATUS_CHANGE_LISTENER_TOPIC);
//                        publisher.updateStatus(null, null);
                    }
                    return false;
                } else {
                    return true;
                }
            }
        }
        // 不满足播放条件导致无法播放，返回
        return false;
    }

    /**
     * @return 是否播放成功，如果是由于播放限制，导致播放失败，也会返回true，只有获取歌曲信息失败或者加载失败，导致无法播放，才会返回false
     */
    public boolean playOrDownload(Track trackModel) {
        MusicConfig config = MusicPersistentConfig.getInstance().getMusicConfig();

        //使用配置路径
        String dirPath = config.getFilePath();

        if (StringUtils.isBlank(dirPath)) {
            MessageUtils.getInstance(project).showWarnMsg("config.first");
            return true;
        }
        if (!dirPath.endsWith(File.separator)) {
            dirPath = dirPath + File.separator;
        }

        if (FileUtil.dirExists(new File(dirPath))) {
            String name = trackModel.getName() + ".mp3";
            String playFileName = dirPath + name;

            if (!FileUtil.contain(playFileName)) {
                // 先下载
                String songUrl = CloudMusicUtil.getSongUrl(trackModel.getId());
                if (StringUtils.isBlank(songUrl)) {
                    // 没有获取到地址
                    MessageUtils.getInstance(project).showWarnMsg("《" + trackModel.getName() + "》 歌曲信息获取失败");
                    return false;
                }
                String downloadUrl = DownloadUtil.download(dirPath, songUrl, name);
                if (StringUtils.isBlank(downloadUrl)) {
                    // 下载失败
                    MessageUtils.getInstance(project).showWarnMsg("《" + trackModel.getName() + "》 歌曲下载失败");
                    return false;
                }
            }
            gotoPlay = true;
            btnPlayOrPause.setIcon(MusicIcons.PAUSE);
            btnPlayOrPause.setToolTipText("暂停");
            //存在文件则直接播放
            MusicPlayer player = MusicPlayer.getInstancePlayer();
            if (!player.loadMusicSrc(playFileName)) {
                // 加载失败
                MessageUtils.getInstance(project).showWarnMsg("《" + trackModel.getName() + "》 歌曲加载失败");
                return false;
            }
            player.openMusic();
            title.setText(trackModel.getName());
            title.setForeground(new JBColor(Gray._80, Gray._220));
            title.setVisible(true);
            author.setText(" - " + trackModel.getAr().get(0).getName());
            author.setForeground(JBColor.GRAY);
            author.setVisible(true);
            timeLabel.setVisible(true);
            TableModel model = musicTable.getModel();
            if (previousPlayIndex >= 0) {
                Track previousTrack = (Track) model.getValueAt(previousPlayIndex, 0);
                previousTrack.setCurrentPlay(false);
            }
            Track currentTrack = (Track) model.getValueAt(currentPlayIndex, 0);
            currentTrack.setCurrentPlay(true);
            setPreviousPlayIndex(currentPlayIndex);
            musicTable.scrollRectToVisible(musicTable.getCellRect(currentPlayIndex, 0, true));
            musicTable.updateUI();
            return true;
        }
        return true;
    }

    public void stop() {
        musicPlayer.stop();
        isStopped = true;
    }

    public void setMusicModelList(DefaultTableModel musicModelList) {
        this.musicModelList = musicModelList;
    }

    public void setMusicTable(JTable musicTable) {
        this.musicTable = musicTable;
    }

    public boolean isPlaying() {
        return musicPlayer.isPlaying();
    }

    public boolean isHasPlay() {
        return hasPlay;
    }
}
