package com.jacker.plugin.music.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.jacker.plugin.music.constant.MusicConstant;

public class ConfigAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(anActionEvent.getProject(), MusicConstant.PLUGIN_NAME);
    }
}
