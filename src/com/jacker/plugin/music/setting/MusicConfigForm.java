package com.jacker.plugin.music.setting;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.jacker.plugin.music.statusbar.MusicStatusChangeListener;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.io.File;
import java.util.Objects;

public class MusicConfigForm {
    private JPanel mainPanel;

    private JBTextField cellphoneField;

    private JBPasswordField passwordField;

    private TextFieldWithBrowseButton filePathField;

    private JCheckBox showNavCheckBox;

    private JCheckBox showStatusWidgetCheckBox;

    public MusicConfigForm() {
        // 文件选择器
        filePathField.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()) {});
        // 读取持久化中的配置
        loadSetting();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public boolean isModified() {
        MusicPersistentConfig musicPersistentConfig = MusicPersistentConfig.getInstance();
        if (musicPersistentConfig == null) {
            return true;
        }
        MusicConfig config = musicPersistentConfig.getMusicConfig();
        if (config == null) {
            return true;
        } else {
            MusicConfig currentState = new MusicConfig();
            process(currentState);
            if (currentState.equals(config)) {
                String password = String.valueOf(passwordField.getPassword());
                return !password.equals(MusicPersistentConfig.getInstance().getPassword(config.getCellphone()));
            } else {
                return true;
            }
        }
    }

    public void apply() {
        MusicConfig musicConfig;
        MusicPersistentConfig musicPersistentConfig = MusicPersistentConfig.getInstance();
        if (musicPersistentConfig == null) {
            musicConfig = new MusicConfig();
        } else {
            musicConfig = musicPersistentConfig.getMusicConfig();
        }
        if (musicConfig == null) {
            musicConfig = new MusicConfig();
        }
        process(musicConfig);
        File file = new File(musicConfig.getFilePath() + File.separator);
        if (!file.exists()) {
            file.mkdirs();
        }
        MusicPersistentConfig.getInstance().setMusicConfig(musicConfig);
        MusicPersistentConfig.getInstance().savePassword(String.valueOf(passwordField.getPassword()), musicConfig.getCellphone());
    }

    private void loadSetting() {
        MusicConfig config = null;
        MusicPersistentConfig musicPersistentConfig = MusicPersistentConfig.getInstance();
        if (musicPersistentConfig != null) {
            config = musicPersistentConfig.getMusicConfig();
        }
        if (config != null) {
            cellphoneField.setText(config.getCellphone());
            passwordField.setText(musicPersistentConfig.getPassword(config.getCellphone()));
            if (StringUtils.isNotBlank(config.getFilePath())) {
                filePathField.setText(config.getFilePath());
            }
            showNavCheckBox.setSelected(config.getShowNavigateBar());
            showStatusWidgetCheckBox.setSelected(config.getShowStatusWidget());
        }
    }

    public void process(MusicConfig config) {
        config.setCellphone(cellphoneField.getText());
        config.setFilePath(filePathField.getText());
        if (!Objects.equals(config.getShowStatusWidget(), showStatusWidgetCheckBox.isSelected())) {
            config.setShowStatusWidget(showStatusWidgetCheckBox.isSelected());
            MusicStatusChangeListener publisher = ApplicationManager.getApplication().getMessageBus().syncPublisher(MusicStatusChangeListener.MUSIC_STATUS_CHANGE_LISTENER_TOPIC);
            publisher.updateStatus(null, null);
        }
        config.setShowNavigateBar(showNavCheckBox.isSelected());
    }

    public void reset() {
        loadSetting();
    }

}
