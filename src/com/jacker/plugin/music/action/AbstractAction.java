package com.jacker.plugin.music.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.jacker.plugin.music.constant.MusicConstant;
import com.jacker.plugin.music.setting.MusicConfig;
import com.jacker.plugin.music.setting.MusicPersistentConfig;
import com.jacker.plugin.music.util.MessageUtils;
import com.jacker.plugin.music.util.PropertiesUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AbstractAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        MusicConfig config = MusicPersistentConfig.getInstance().getMusicConfig();
        final Project project = anActionEvent.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        if (Objects.isNull(config)) {
            MessageUtils.getInstance(project).showWarnMsg(PropertiesUtils.getInfo("config.first"));
            ShowSettingsUtil.getInstance().showSettingsDialog(project, MusicConstant.PLUGIN_NAME);
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project,anActionEvent.getActionManager().getId(this),false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                actionPerformed(anActionEvent, project, config);
            }
        });
    }

    public abstract void actionPerformed(AnActionEvent anActionEvent, Project project, MusicConfig config);
}
