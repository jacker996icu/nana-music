package com.jacker.plugin.music.window;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.jacker.plugin.music.constant.DataKeys;
import com.jacker.plugin.music.constant.MusicConstant;
import com.jacker.plugin.music.manager.ViewManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class MusicToolWindowPanel extends SimpleToolWindowPanel {

    private final JPanel groupPanel;

    public MusicToolWindowPanel() {
        super(Boolean.TRUE, Boolean.TRUE);
        // 未登陆状态下，显示提示文案
        JTextPane tipsPane = ViewManager.buildTipsPane();

        // 主视图，使用border布局
        groupPanel = new JPanel(new BorderLayout());
        groupPanel.add(tipsPane, BorderLayout.CENTER);

        // 使用注册的 Action 组 music.ActionsToolbar，创建ActionToolbar，绑定到主视图
        final ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar(MusicConstant.ACTION_PREFIX + "Toolbar",
                (DefaultActionGroup) actionManager.getAction(MusicConstant.MUSIC_NAVIGATOR_ACTIONS_TOOLBAR), true);
        actionToolbar.setTargetComponent(groupPanel);

        setToolbar(actionToolbar.getComponent());
        setContent(groupPanel);

    }

    @Override
    public Object getData(@NotNull String dataId) {
        if (DataKeys.GROUP_PANEL.is(dataId)) {
            return groupPanel;
        }
        return super.getData(dataId);
    }
}
