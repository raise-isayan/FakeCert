package server.ocsp;

import extension.burp.BurpConfig;
import extension.helpers.json.JsonUtil;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author isayan
 */
public final class Config extends BurpConfig {

    private Config() {
    }

    public static String getExtensionPath() {
        return ".fakecert";
    }
        
    public static File getExtensionHomeFile() {
        return new File(BurpConfig.getUserHomePath(), getExtensionPath());
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

    public static String stringToJson(IOptionProperty option) {
        return JsonUtil.jsonToString(option.getOption(), true);
    }
    
}
