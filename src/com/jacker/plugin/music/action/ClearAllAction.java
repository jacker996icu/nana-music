package com.jacker.plugin.music.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.jacker.plugin.music.dto.Track;
import com.jacker.plugin.music.manager.ViewManager;
import com.jacker.plugin.music.setting.MusicConfig;
import com.jacker.plugin.music.util.MessageUtils;
import com.jacker.plugin.music.util.PropertiesUtils;
import com.jacker.plugin.music.window.MusicPlayerPanel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Objects;

public class ClearAllAction extends AbstractAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Project project, MusicConfig config) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            ClearAllWarningPanel dialog = new ClearAllWarningPanel(project);
            dialog.setTitle("Clear All");

            if (dialog.showAndGet()) {
                String filePath = config.getFilePath();

                File file = new File(filePath);
                if (!file.exists() || !file.isDirectory()) {
                    MessageUtils.getInstance(project).showInfoMsg(PropertiesUtils.getInfo("clear.success"));
                    return;
                }

                try {
                    int delCount = delFile(file);
                    MessageUtils.getInstance(project).showInfoMsg(PropertiesUtils.getInfo("clear.success") + ": "  + delCount);
                } catch (Exception ee) {
                    MessageUtils.getInstance(project).showErrorMsg(PropertiesUtils.getInfo("clear.failed"));
                }
            }
        });
    }

    public int delFile(File filePath) {
        if (!filePath.exists() || !filePath.isDirectory()) {
            return 0;
        }
        String currentFileName = "";
        JTable musicTable = ViewManager.getMusicTable();
        MusicPlayerPanel musicPlayerPanel = ViewManager.getMusicPlayerPanel();
        if (Objects.nonNull(musicTable) && Objects.nonNull(musicPlayerPanel) && musicPlayerPanel.isPlaying()) {
            Track track = (Track) musicTable.getModel().getValueAt(musicPlayerPanel.getCurrentPlayIndex(), 0);
            if (Objects.nonNull(track)) {
                currentFileName = track.getName() + ".mp3";
            }
        }
        int count = 0;
        for (File file : Objects.requireNonNull(filePath.listFiles())) {
            if (file.isFile() && file.getName().endsWith("mp3")) {
                try {
                    if (StringUtils.isNotBlank(currentFileName) && file.getName().equals(currentFileName)) {
                        continue;
                    }
                    if (file.delete()) {
                        count++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return count;
    }

    private static class ClearAllWarningPanel extends DialogWrapper {

        private final JPanel jpanel;

        public ClearAllWarningPanel(@Nullable Project project) {
            super(project, true);
            jpanel = new JPanel();
            jpanel.add(new JLabel("Clear all cached music?"));
            jpanel.setMinimumSize(new Dimension(200, 50));
            setModal(true);
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            return jpanel;
        }
    }
}
