package dev.dovecot.launcher.core;

import dev.dovecot.launcher.core.auth.AbstractAccount;
import dev.dovecot.launcher.core.auth.AuthlibInjectorAccount;
import dev.dovecot.launcher.core.auth.OfflineAccount;
import dev.dovecot.launcher.core.game.GameDirectory;
import dev.dovecot.launcher.core.game.GameTask;
import dev.dovecot.launcher.core.game.GameVersion;
import dev.dovecot.launcher.core.i18n.I18nManager;
import dev.dovecot.launcher.core.utils.FileUtils;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main
{
    public static final int JAVA_VERSION = (int) (Double.parseDouble(System.getProperty("java.class.version")) - 44);
    public static final String MAIN_CLASS = "dev.dovecot.launcher.core.Main";

    public static AbstractAccount account = new OfflineAccount("Dev");

    public static void main(final String[] args) throws Exception
    {
//        final String userDir = System.getProperty("user.dir") + File.separator + System.getProperty("java.class.path");
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
        if (new File("account.json").exists())
        {
            System.out.println(new String(FileUtils.readFile(new File("account.json")), StandardCharsets.UTF_8));
            account = AuthlibInjectorAccount.fromJson(new JSONObject(new String(FileUtils.readFile(new File("account.json")), StandardCharsets.UTF_8)));
            main();
        }
        else
        {
            login(WindowConstants.EXIT_ON_CLOSE);
        }
    }

    private static void main() throws Exception
    {
        AtomicBoolean isRunning = new AtomicBoolean(false);
        DovecotCore.init();
        final JFrame mainFrame = new JFrame(I18nManager.getTranslation("FRAME_TITLE"));
        mainFrame.setSize(600, 400);
        mainFrame.setResizable(false);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final JMenuBar menuBar = new JMenuBar();
        mainFrame.setJMenuBar(menuBar);

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        final JTextArea log = new JTextArea();
        log.setEditable(false);
        panel.add(log, BorderLayout.CENTER);
        final JButton launchButton = new JButton(I18nManager.getTranslation("BUTTON_LAUNCH"));
        launchButton.addActionListener(e ->
        {
            try
            {
                if (!isRunning.get())
                {
                    isRunning.set(true);
                    if (new File(".minecraft").exists())
                    {
                        final GameVersion version = new GameDirectory("Primary",".minecraft").loadVersions().get(0);
                        final GameTask task = GameTask.generateNewTask(version, System.getProperty("java.library.path").split(";")[0].replace("/", "\\") + "\\java.exe", GameTask.generateJVMArguments(version), account);
                        final Process process = task.run();
                        final Thread thread = new Thread(() ->
                        {
                            try
                            {
                                final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                                final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                                String logStr = "";
                                while (process.isAlive())
                                {
                                    logStr = reader.readLine() + "\n" + logStr;
                                    log.setText(logStr);
                                }
                                isRunning.set(false);
                            }
                            catch (IOException exception)
                            {
                                exception.printStackTrace();
                            }
                        });
                        thread.start();
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, I18nManager.getTranslation("TEXT_NO_GAME_DIR"), "Error!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        });
        panel.add(launchButton, BorderLayout.SOUTH);
        mainFrame.add(panel);

        mainFrame.setVisible(true);
    }

    private static void login(final int frameCloseOperation) throws Exception
    {
        final JFrame loginFrame = new JFrame();
        loginFrame.setSize(400, 150);
        loginFrame.setResizable(false);
        loginFrame.setTitle(I18nManager.getTranslation("TITLE_LOGIN"));
        loginFrame.setDefaultCloseOperation(frameCloseOperation);
        final JPanel panel = new JPanel();
        panel.setSize(loginFrame.getSize());
        panel.add(new JLabel(I18nManager.getTranslation("TITLE_LOGIN")));
        final JTextField nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(350, 24));
        panel.add(nameField);
        final JTextField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(350, 24));
        panel.add(passwordField);
        final JButton registerButton = new JButton(I18nManager.getTranslation("BUTTON_SIGN_UP"));
        registerButton.setPreferredSize(new Dimension(172, 24));
        panel.add(registerButton);
        registerButton.addActionListener(e ->
        {
            try
            {
                if (Desktop.isDesktopSupported())
                {
                    final Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI(DovecotCore.SKIN_WEBSITE));
                }
            }
            catch (IOException | URISyntaxException exception)
            {
                exception.printStackTrace();
            }
        });
        final JButton loginButton = new JButton(I18nManager.getTranslation("BUTTON_LOGIN"));
        loginButton.setPreferredSize(new Dimension(172, 24));
        loginButton.addActionListener(e ->
        {
            try
            {
                account = AuthlibInjectorAccount.authenticate(DovecotCore.SKIN_WEBSITE_YGGDRASIL, nameField.getText(), passwordField.getText())[0];
                loginFrame.setVisible(false);
                FileUtils.writeFile(new File("account.json"), account.toJson().toString().getBytes(StandardCharsets.UTF_8));
                main();
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
                loginFrame.setVisible(false);
                JOptionPane.showMessageDialog(null, exception.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                try
                {
                    login(frameCloseOperation);
                }
                catch (Exception ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        });
        panel.add(loginButton);
        loginFrame.add(panel);
        loginFrame.setVisible(true);
    }
}
