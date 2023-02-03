package com.jacker.plugin.music.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.jacker.plugin.music.manager.ViewManager;
import com.jacker.plugin.music.setting.MusicConfig;
import com.jacker.plugin.music.util.CloudMusicUtil;
import com.jacker.plugin.music.util.MessageUtils;
import com.jacker.plugin.music.util.PropertiesUtils;
import org.jetbrains.annotations.NotNull;

public class LogoutAction extends AbstractAction {

    @Override
    public void update(@NotNull AnActionEvent anActionEvent) {
        if (anActionEvent.getProject() == null) {
            return;
        }
        anActionEvent.getPresentation().setEnabled(CloudMusicUtil.isLogin());
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Project project, MusicConfig config) {
        ViewManager.stopAndRemove(project);
        MessageUtils.getInstance(project).showInfoMsg(PropertiesUtils.getInfo("login.out"));
    }
}
