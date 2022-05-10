package dev.dovecot.launcher.core.i18n;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class I18nManager
{
    public static JSONObject json;

    public static void loadLang(final String name) throws IOException
    {
        json = new JSONObject(new String(Objects.requireNonNull(I18nManager.class.getClassLoader().getResourceAsStream("i18n/" + name.toLowerCase() + ".json")).readAllBytes(), StandardCharsets.UTF_8));
    }

    public static String getTranslation(final String key) throws IOException
    {
        if (Objects.isNull(json))
        {
            if (!Objects.isNull(I18nManager.class.getClassLoader().getResource("i18n/" + (Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry()).toLowerCase() + ".json")))
            {
                json = new JSONObject(new String(Objects.requireNonNull(I18nManager.class.getClassLoader().getResourceAsStream("i18n/" + (Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry()).toLowerCase() + ".json")).readAllBytes(), StandardCharsets.UTF_8));
            }
            else
            {
                json = new JSONObject(new String(Objects.requireNonNull(I18nManager.class.getClassLoader().getResourceAsStream("i18n/en_us.json")).readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        if (json.has(key))
        {
            return json.getString(key);
        }
        return key;
    }

    public static List<Map<String, String>> getLocals()
    {
        final List<Map<String, String>> values = new ArrayList<>();
        values.add(Map.of("en_us", "English (United States)"));
        values.add(Map.of("de_de", "Deutsch (Deutschland)"));
        values.add(Map.of("zh_cn", "简体中文 (中国大陆)"));
        values.add(Map.of("zh_tw", "繁體中文 (中國台灣)"));
        return values;
    }
}
