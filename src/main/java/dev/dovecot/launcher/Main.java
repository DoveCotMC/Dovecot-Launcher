package dev.dovecot.launcher;

import dev.dovecot.launcher.core.DovecotCore;
import dev.dovecot.launcher.core.auth.AbstractAccount;
import dev.dovecot.launcher.core.auth.AuthlibInjectorAccount;
import dev.dovecot.launcher.core.game.GameDirectory;
import dev.dovecot.launcher.core.game.GameTask;
import dev.dovecot.launcher.core.game.GameVersion;
import dev.dovecot.launcher.core.utils.FileUtils;
import dev.dovecot.launcher.i18n.I18nManager;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Main
{
    public static final int JAVA_VERSION = (int) (Double.parseDouble(System.getProperty("java.class.version")) - 44);
    public static final String MAIN_CLASS = "dev.dovecot.launcher.Main";
    public static final String ENVIRONMENT_CLASS = "dev.dovecot.launcher.javafx.JavaFxEnvironmentChecker";

    public static AbstractAccount account;

    public static void main(final String[] args) throws Exception
    {
//        if (JAVA_VERSION < 11)
//        {
//            JOptionPane.showMessageDialog(null, "Please use Java 11 or later!", "Error!", JOptionPane.ERROR_MESSAGE);
//            throw new RuntimeException("Unsupported Java version");
//        }
//        else
        // This program also run on Java 8
        if (true)
        {
            if (Arrays.asList(args).contains("--runExp"))
            {
                final String userDir = System.getProperty("java.class.path");
                if (!userDir.contains(";"))
                {
                    try
                    {
                        Class.forName("javafx.application.Application");
                    }
                    catch (ClassNotFoundException e)
                    {
                        final Process process = Runtime.getRuntime().exec(new String[]{System.getProperty("java.library.path").split(File.pathSeparator)[0].replace("/", "\\") + "\\java.exe", "-cp", userDir, ENVIRONMENT_CLASS});
                        System.out.println(new String(process.getInputStream().readAllBytes()));
                        System.out.println(new String(process.getErrorStream().readAllBytes()));
                    }
                }
            }
            else
            {
                DovecotCore.init();
                if (new File("account.json").exists())
                {
                    account = AuthlibInjectorAccount.fromJson(new JSONObject(new String(FileUtils.readFile(new File("account.json")), StandardCharsets.UTF_8)));
                    main();
                }
                else
                {
                    login(WindowConstants.EXIT_ON_CLOSE);
                }
            }
        }
    }

    private static void main() throws Exception
    {
        AtomicBoolean isRunning = new AtomicBoolean(false);
        final JFrame mainFrame = new JFrame(I18nManager.getTranslation("FRAME_TITLE"));
        mainFrame.setSize(600, 400);
        mainFrame.setResizable(false);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final JMenuBar menuBar = new JMenuBar();

        final JMenu authMenu = new JMenu(I18nManager.getTranslation("MENU_ACCOUNT"));
        final JMenuItem manageAccountItem = new JMenuItem(I18nManager.getTranslation("MENU_ITEM_ACCOUNT_MANAGE"));
        manageAccountItem.addActionListener(e ->
        {
            try
            {
                if (Desktop.isDesktopSupported())
                {
                    final Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI(DovecotCore.SKIN_WEBSITE + "user"));
                }
            }
            catch (Exception exception)
            {
                throw new RuntimeException(exception);
            }
        });
        authMenu.add(manageAccountItem);
        final JMenuItem logoutMenuItem = new JMenuItem(I18nManager.getTranslation("MENU_ITEM_LOGOUT"));
        logoutMenuItem.addActionListener(e ->
        {
            try
            {
                ((AuthlibInjectorAccount) account).invalidate();
                new File("account.json").delete();
                mainFrame.dispose();
                login(JFrame.EXIT_ON_CLOSE);
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        });
        authMenu.add(logoutMenuItem);
        menuBar.add(authMenu);

        mainFrame.setJMenuBar(menuBar);

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        final JTextArea log = new JTextArea();
        log.setEditable(false);
        log.setEnabled(false);
        log.setDisabledTextColor(Color.BLACK);
//        log.setDragEnabled(false);
        panel.add(log, BorderLayout.CENTER);
        final JButton launchButton = new JButton(I18nManager.getTranslation("BUTTON_LAUNCH"));
        launchButton.addActionListener(e ->
        {
            try
            {
                if (!isRunning.get())
                {
                    if (new File(".minecraft").exists())
                    {
                        launchButton.setEnabled(false);
                        isRunning.set(true);
                        if (!((AuthlibInjectorAccount) account).isTokenAvailable())
                        {
                            account = ((AuthlibInjectorAccount) account).refresh();
                        }
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
                                    final String line = reader.readLine();
                                    if (!Objects.isNull(line))
                                    {
                                        logStr = reader.readLine() + "\n" + logStr;
                                        log.setText(logStr);
                                    }
                                }
                                isRunning.set(false);
                                launchButton.setEnabled(true);
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
