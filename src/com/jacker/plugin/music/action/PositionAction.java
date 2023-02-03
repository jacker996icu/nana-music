package com.jacker.plugin.music.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.jacker.plugin.music.manager.ViewManager;
import com.jacker.plugin.music.setting.MusicConfig;
import com.jacker.plugin.music.window.MusicPlayerPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PositionAction extends AbstractAction {

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        if (anActionEvent.getProject() == null) {
            return;
        }
        JScrollPane musicScrollPane = ViewManager.getMusicScrollPane();
        JTable musicTable = ViewManager.getMusicTable();
        MusicPlayerPanel musicPlayerPanel = ViewManager.getMusicPlayerPanel();
        if (musicScrollPane == null || musicTable == null || musicPlayerPanel == null
                || musicPlayerPanel.getCurrentPlayIndex() < 0) {
            anActionEvent.getPresentation().setEnabled(false);
            return;
        }
        anActionEvent.getPresentation().setEnabled(true);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Project project, MusicConfig config) {
        ViewManager.position();
    }
}
