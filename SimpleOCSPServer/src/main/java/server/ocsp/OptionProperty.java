package server.ocsp;

import com.google.gson.annotations.Expose;
import extension.burp.IOptionProperty;
import extension.helpers.json.JsonUtil;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author raise.isayan
 */
public final class OptionProperty  implements IOptionProperty {
    public final static String OCSP_PROPERTY = "OCSPPropery";

    private final Map<String, String> config = new HashMap();


    @Override
    public void saveConfigSetting(Map<String, String> map) {
        this.config.putAll(map);
    }

    @Override
    public Map<String, String> loadConfigSetting() {
        return this.config;
    }

}
