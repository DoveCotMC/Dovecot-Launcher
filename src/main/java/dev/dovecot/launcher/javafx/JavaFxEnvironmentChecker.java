package dev.dovecot.launcher.javafx;

public class JavaFxEnvironmentChecker
{
    public static void main(final String[] args)
    {
        try
        {
            Class.forName("javafx.application.Application");
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
