package com.jacker.plugin.music.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.jacker.plugin.music.manager.ViewManager;
import com.jacker.plugin.music.setting.MusicConfig;
import com.jacker.plugin.music.setting.MusicPersistentConfig;
import com.jacker.plugin.music.window.MusicPlayerPanel;
import icons.MusicIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ControlPlayAction extends AbstractAction {

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        if (anActionEvent.getProject() == null) {
            return;
        }
        MusicConfig config = MusicPersistentConfig.getInstance().getMusicConfig();
        if (config != null && !config.getShowNavigateBar()) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }
        JScrollPane musicScrollPane = ViewManager.getMusicScrollPane();
        JTable musicTable = ViewManager.getMusicTable();
        MusicPlayerPanel musicPlayerPanel = ViewManager.getMusicPlayerPanel();
        if (musicScrollPane == null || musicTable == null || musicPlayerPanel == null) {
            anActionEvent.getPresentation().setEnabled(false);
            return;
        }
        anActionEvent.getPresentation().setEnabled(true);
        if (musicPlayerPanel.isPlaying()) {
            anActionEvent.getPresentation().setIcon(MusicIcons.PAUSE_CONTROL);
            anActionEvent.getPresentation().setText("Pause");
            anActionEvent.getPresentation().setDescription("Pause");
        } else {
            anActionEvent.getPresentation().setIcon(MusicIcons.PLAY_CONTROL);
            anActionEvent.getPresentation().setText("Play");
            anActionEvent.getPresentation().setDescription("Play");
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Project project, MusicConfig config) {
        ViewManager.getMusicPlayerPanel().playOrPause();
    }
}
