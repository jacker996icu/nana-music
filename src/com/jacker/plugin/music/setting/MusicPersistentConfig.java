package com.jacker.plugin.music.setting;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.jacker.plugin.music.constant.MusicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * 可持久化的用户配置
 */
@State(name = "MusicPersistentConfig", storages = {@Storage(value = "music-config.xml", roamingType = RoamingType.DISABLED)})
public class MusicPersistentConfig implements PersistentStateComponent<MusicPersistentConfig> {

    private MusicConfig musicConfig;

    public static MusicPersistentConfig getInstance() {
        return ApplicationManager.getApplication().getService(MusicPersistentConfig.class);
    }

    /**
     * IDEA关闭时，将内存中配置持久化到xml中
     */
    @Override
    public @Nullable MusicPersistentConfig getState() {
        return this;
    }

    /**
     * IDEA打开时，从xml中读取配置
     */
    @Override
    public void loadState(@NotNull MusicPersistentConfig musicPersistentConfig) {
        XmlSerializerUtil.copyBean(musicPersistentConfig, this);
    }

    public MusicConfig getMusicConfig() {
        return musicConfig;
    }

    public void setMusicConfig(MusicConfig musicConfig) {
        this.musicConfig = musicConfig;
    }

    public String getFilePath() {
        return getMusicConfig().getFilePath() + File.separator;
    }

    /**
     * 加密保存密码
     * https://plugins.jetbrains.com/docs/intellij/persisting-sensitive-data.html#retrieve-stored-credentials
     */
    public void savePassword(String password, String username) {
        if(username == null || password == null){
            return;
        }
        PasswordSafe.getInstance().set(new CredentialAttributes(MusicConstant.PLUGIN_ID, username, this.getClass()), new Credentials(username, password));
    }

    /**
     * 获取密码
     */
    public String getPassword(String username) {
        if (username != null) {
            return PasswordSafe.getInstance().getPassword(new CredentialAttributes(MusicConstant.PLUGIN_ID, username, this.getClass()));
        }
        return null;

    }
}
