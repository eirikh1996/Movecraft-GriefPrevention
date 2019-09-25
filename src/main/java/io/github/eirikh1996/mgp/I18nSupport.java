package io.github.eirikh1996.mgp;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class I18nSupport {
    private static Properties languageFile;
    public static boolean initialize(){
        languageFile = new Properties();
        File file = new File(MGP.getInstance().getDataFolder().getAbsolutePath() + "/localisation/mgplang_" + Settings.locale + ".properties");
        try {
            languageFile.load(new FileInputStream(file));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            MGP.getInstance().getLogger().severe("");
            Bukkit.getServer().getPluginManager().disablePlugin(MGP.getInstance());
            return false;
        }
    }

    public static String getInternationalisedString(String key){
        return languageFile.getProperty(key) != null ? languageFile.getProperty(key) : key;
    }
}

