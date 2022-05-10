package dev.dovecot.launcher.core;

import dev.dovecot.launcher.core.i18n.I18nManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class Main
{
    public static final int JAVA_VERSION = (int) (Double.parseDouble(System.getProperty("java.class.version")) - 44);
    public static final String MAIN_CLASS = "dev.dovecot.launcher.core.Main";

    public static void main(final String[] args) throws Exception
    {
        final String userDir = System.getProperty("user.dir") + File.separator + System.getProperty("java.class.path");
//        if (!userDir.contains(";"))
//        {
//            try
//            {
//                Class.forName("javafx.application.Application");
//            }
//            catch (ClassNotFoundException e)
//            {
//                Process process = Runtime.getRuntime().exec(new String[]{System.getProperty("java.library.path").split(File.pathSeparator)[0].replace("/", "\\") + "\\java.exe", "-cp", userDir, });
//                System.out.println(new String(process.getInputStream().readAllBytes()));
//                System.out.println(new String(process.getErrorStream().readAllBytes()));
//            }
//        }
        final JFrame mainFrame = new JFrame(I18nManager.getTranslation("FRAME_TITLE"));
        mainFrame.setSize(600, 400);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final JMenuBar menuBar = new JMenuBar();

        final JMenu menuGeneral = new JMenu(I18nManager.getTranslation("MENU_GENERAL_SETTINGS"));

        final JMenuItem launcherSettingsItem = new JMenuItem(I18nManager.getTranslation("LAUNCHER_SETTINGS"));
        launcherSettingsItem.addActionListener(e ->
        {
            try
            {
                final JFrame frame = new JFrame(I18nManager.getTranslation("LAUNCHER_SETTINGS"));
                frame.setSize(400, 400);
                frame.setVisible(true);
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        });
        menuGeneral.add(launcherSettingsItem);

        final JMenuItem languageSettingsItem = new JMenuItem(I18nManager.getTranslation("LANGUAGE_SETTINGS"));
        menuGeneral.add(languageSettingsItem);

        menuBar.add(menuGeneral);
        mainFrame.setJMenuBar(menuBar);

        final JMenu menuGame = new JMenu(I18nManager.getTranslation("MENU_GAME_SETTINGS"));
        menuGame.add(new JMenuItem(I18nManager.getTranslation("JVM_SETTINGS")));
        menuGame.add(new JMenuItem(I18nManager.getTranslation("ACCOUNT_SETTINGS")));
        menuBar.add(menuGame);
        mainFrame.setJMenuBar(menuBar);

        final JMenu menuMisc = new JMenu(I18nManager.getTranslation("MENU_MISC_SETTINGS"));
        menuMisc.add(new JMenuItem(I18nManager.getTranslation("ABOUT_LAUNCHER")));
        menuMisc.add(new JMenuItem(I18nManager.getTranslation("OPEN_GITHUB")));
        menuBar.add(menuMisc);
        mainFrame.setJMenuBar(menuBar);

        mainFrame.setVisible(true);
    }
}
