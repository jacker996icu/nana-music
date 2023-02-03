package com.jacker.plugin.music.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.jacker.plugin.music.setting.MusicConfig;
import com.jacker.plugin.music.util.CloudMusicUtil;
import com.jacker.plugin.music.util.MessageUtils;
import com.jacker.plugin.music.util.PropertiesUtils;
import org.apache.commons.lang.StringUtils;

public class LoginAction extends AbstractAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Project project, MusicConfig config) {

        if (StringUtils.isNotBlank(CloudMusicUtil.cookie)) {
            // 如果内存中已经有cookie了，检查一下有没有过期
            if (CloudMusicUtil.checkLogin()) {
                MessageUtils.getInstance(project).showWarnMsg(PropertiesUtils.getInfo("login.exist"));
                return;
            }
        }

        if (StringUtils.isBlank(config.getCellphone())) {
            MessageUtils.getInstance(project).showWarnMsg(PropertiesUtils.getInfo("config.user"));
            return;
        }

        CloudMusicUtil.cellphoneLogin(config, project);

    }


}
