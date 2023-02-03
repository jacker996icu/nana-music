package com.jacker.plugin.music.window;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.jacker.plugin.music.constant.MusicConstant;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 工具栏构建工厂
 */
public class MusicWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(new MusicToolWindowPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    /**
     * 获取工具栏实例中的组件
     */
    public static DataContext getDataContext(@NotNull Project project) {
        AtomicReference<DataContext> dataContext = new AtomicReference<>();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(MusicConstant.TOOL_WINDOW_ID);
            if (Objects.nonNull(toolWindow)) {
                Content content = toolWindow.getContentManager().getContent(0);
                if (Objects.nonNull(content)) {
                    dataContext.set(DataManager.getInstance().getDataContext(content.getComponent()));
                }
            }
        });
        return dataContext.get();
    }

}
