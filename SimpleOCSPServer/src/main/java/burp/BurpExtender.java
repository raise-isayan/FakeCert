package burp;

import extension.burp.BurpExtenderImpl;
import extension.helpers.ConvertUtil;
import extension.helpers.json.JsonUtil;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.ocsp.OCSPServerTab;
import server.ocsp.OptionProperty;

/**
 *
 * @author isayan
 */
public class BurpExtender extends BurpExtenderImpl {
   private final static Logger logger = Logger.getLogger(BurpExtender.class.getName());

    public BurpExtender() {
    }

    private final java.util.ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("burp/resources/release");

    @SuppressWarnings("unchecked")
    public static BurpExtender getInstance() {
        return BurpExtenderImpl.<BurpExtender>getInstance();
    }

    private OCSPServerTab ocspTab;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        super.registerExtenderCallbacks(callbacks);
        callbacks.setExtensionName(String.format("%s", BUNDLE.getString("projname")));

        // 設定ファイル読み込み
        Map<String, String> settings = this.option.loadConfigSetting();
        String configJSON = getCallbacks().loadExtensionSetting("configJSON");
        if (configJSON != null) {
            settings = jsonStringToMap(ConvertUtil.decompressZlibBase64(configJSON));
        }
        this.ocspTab = new OCSPServerTab();
        String settingValue = settings.getOrDefault(this.ocspTab.getSettingName(), this.ocspTab.defaultSetting());
        this.ocspTab.saveSetting(settingValue);
        this.ocspTab.addPropertyChangeListener(newPropertyChangeListener());
        callbacks.addSuiteTab(this.ocspTab);
        callbacks.registerExtensionStateListener(this.ocspTab);
    }

    public PropertyChangeListener newPropertyChangeListener() {
        final Map<String, String> settings = this.option.loadConfigSetting();
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (OptionProperty.OCSP_PROPERTY.equals(evt.getPropertyName())) {
                    String settingValue = ocspTab.loadSetting();
                    settings.put(ocspTab.getSettingName(), settingValue);
                    applyOptionProperty();
                }
            }
        };
    }

    private final OptionProperty option = new OptionProperty();

    public OptionProperty getProperty() {
        return this.option;
    }


    protected void applyOptionProperty() {
        try {
            final Map<String, String> settings = this.option.loadConfigSetting();
            String configJSON = mapToJsonString(settings);
            getCallbacks().saveExtensionSetting("configJSON", ConvertUtil.compressZlibBase64(configJSON));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public String mapToJsonString(Map<String, String> settings) {
        return JsonUtil.jsonToString(settings, true);
    }

    public Map<String, String> jsonStringToMap(String json) {
        final Map<String, String> settings = JsonUtil.jsonFromString(json, Map.class, true);
        return settings;
    }
}
