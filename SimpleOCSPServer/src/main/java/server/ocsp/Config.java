package server.ocsp;

import extend.util.external.JsonUtil;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author isayan
 */
public final class Config {

    private Config() {
    }

    public static String getUserHome() {
        return System.getProperties().getProperty("user.home");
    }

    public static String getExtensionDir() {
        return ".fakecert";
    }
    
    public static String getUserDir() {
        return System.getProperties().getProperty("user.dir");
    }
    
    public static File getExtensionHomeDir() {
        return new File(Config.getUserHome(), Config.getExtensionDir());
    }

    public static void saveToJson(File fo, OptionProperty option) throws IOException {
        JsonUtil.saveToJson(fo, option, true);
    }

    public static void loadFromJson(File fi, OptionProperty option) throws IOException {
        OptionProperty load = JsonUtil.loadFromJson(fi, OptionProperty.class, true);
        option.setProperty(load);
    }

    public static String stringToJson(OptionProperty option) {
        return JsonUtil.jsonToString(option, true);
    }

    public static void stringFromJson(String json, OptionProperty option) {
        OptionProperty load = JsonUtil.jsonFromString(json, OptionProperty.class, true);
        option.setProperty(load);
    }

    
}
