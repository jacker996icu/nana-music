package com.jacker.plugin.music.statusbar;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.util.messages.MessageBusConnection;
import com.jacker.plugin.music.manager.ViewManager;
import com.jacker.plugin.music.setting.MusicConfig;
import com.jacker.plugin.music.setting.MusicPersistentConfig;
import com.jacker.plugin.music.window.MusicPlayerPanel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class MusicStatusBarWidget extends JPanel implements CustomStatusBarWidget {

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public @NonNls
    @NotNull
    String ID() {
        return "MusicStatusBarWidget";
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        JLabel titleLabel = new JLabel();
        int height = 18;
        titleLabel.setSize(new Dimension(80, height));
        titleLabel.setPreferredSize(new Dimension(80, height));
        titleLabel.setMaximumSize(new Dimension(80, height));
        titleLabel.setFont(new Font("字体", Font.PLAIN, 11));
        titleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        JLabel timeLabel = new JLabel();
        timeLabel.setSize(new Dimension(70, height));
        timeLabel.setPreferredSize(new Dimension(70, height));
        timeLabel.setMaximumSize(new Dimension(70, height));
        timeLabel.setFont(new Font("字体", Font.PLAIN, 11));
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timeLabel.setVerticalAlignment(SwingConstants.CENTER);
        this.add(titleLabel);
        this.add(timeLabel);
        this.setSize(new Dimension(170, height));
        this.setPreferredSize(new Dimension(170, height));
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.setEnabled(false);
        this.setVisible(false);
        MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect(this);
        connection.subscribe(MusicStatusChangeListener.MUSIC_STATUS_CHANGE_LISTENER_TOPIC, (title, time) -> {
            MusicConfig config = MusicPersistentConfig.getInstance().getMusicConfig();
            MusicPlayerPanel musicPlayerPanel = ViewManager.getMusicPlayerPanel();
            this.setVisible(config.getShowStatusWidget() && Objects.nonNull(musicPlayerPanel) && musicPlayerPanel.isHasPlay());
            if (StringUtils.isNotBlank(title) && StringUtils.isNotBlank(time)) {
                titleLabel.setText(title);
                timeLabel.setText(time);
            }
        });
    }

    @Override
    public void dispose() {

    }
}
