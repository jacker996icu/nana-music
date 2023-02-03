package com.jacker.plugin.music.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.jacker.plugin.music.constant.MusicConstant;

import javax.swing.*;
import java.awt.*;

@Service
public final class MessageUtils {

    private final Project project;

    public MessageUtils(Project project) {
        this.project = project;
    }

    public static MessageUtils getInstance(Project project) {
        return project.getService(MessageUtils.class);
    }

    public static void showMsg(JComponent component, MessageType messageType, String title, String body) {
        JBPopupFactory factory = JBPopupFactory.getInstance();
        BalloonBuilder builder = factory.createHtmlTextBalloonBuilder(body, messageType, null);
        builder.setTitle(title);
        builder.setFillColor(JBColor.background());
        Balloon b = builder.createBalloon();
        Rectangle r = component.getBounds();
        RelativePoint p = new RelativePoint(component, new Point(r.x + r.width, r.y + 30));
        b.show(p, Balloon.Position.atRight);
    }

    public void showInfoMsg(String body) {
        Notifications.Bus.notify(new Notification(MusicConstant.NOTIFICATION_GROUP, "", body, NotificationType.INFORMATION), project);
    }

    public void showWarnMsg(String body) {
        Notifications.Bus.notify(new Notification(MusicConstant.NOTIFICATION_GROUP, "", body, NotificationType.WARNING), project);
    }

    public void showErrorMsg(String body) {
        Notifications.Bus.notify(new Notification(MusicConstant.NOTIFICATION_GROUP, "", body, NotificationType.ERROR), project);
    }

    public static void showAllWarnMsg(String title, String body) {
        Notifications.Bus.notify(new Notification(MusicConstant.NOTIFICATION_GROUP, title, body, NotificationType.WARNING));
    }

    public String getComponentName() {
        return this.getClass().getName();
    }


}
