package com.jacker.plugin.music.statusbar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.jacker.plugin.music.constant.MusicConstant;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class MusicStatusBarFactory implements StatusBarWidgetFactory {
    @Override
    public @NonNls
    @NotNull String getId() {
        return MusicConstant.STATUS_WIDGET_ID;
    }

    @Override
    public @Nls
    @NotNull String getDisplayName() {
        return MusicConstant.PLUGIN_NAME;
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return isAvailable();
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new MusicStatusBarWidget();
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget statusBarWidget) {

    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return isAvailable();
    }

    public static boolean isAvailable() {
        return true;
    }


}
