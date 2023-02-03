package com.jacker.plugin.music.setting;

import com.intellij.openapi.options.SearchableConfigurable;
import com.jacker.plugin.music.constant.MusicConstant;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * setting下的配置页面
 */
public class MusicConfigurable implements SearchableConfigurable {

    /**
     * GUI配置窗口
     */
    private MusicConfigForm musicConfigForm;

    /**
     * 返回唯一id
     * 这个id要与xml中的id（如果指定）一致
     * @return
     */
    @Override
    public @NotNull
    @NonNls String getId() {
        return MusicConstant.PLUGIN_ID;
    }

    @Override
    @Nls
    public String getDisplayName() {
        return MusicConstant.PLUGIN_NAME;
    }

    @Override
    public @Nullable JComponent createComponent() {
        musicConfigForm = new MusicConfigForm();
        return musicConfigForm.getMainPanel();
    }

    @Override
    public boolean isModified() {
        return musicConfigForm.isModified();
    }

    @Override
    public void apply() {
        musicConfigForm.apply();
    }

    @Override
    public void reset() {
        musicConfigForm.reset();
    }

    @Override
    public void disposeUIResources() {
        musicConfigForm = null;
    }
}
