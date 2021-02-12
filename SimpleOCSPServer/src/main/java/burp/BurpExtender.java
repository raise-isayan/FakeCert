package burp;

import extension.burp.BurpExtenderImpl;
import extension.helpers.ConvertUtil;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.ocsp.Config;
import server.ocsp.OCSPServerTab;
import server.ocsp.IOptionProperty;
import server.ocsp.OCSPProperty;
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
        callbacks.setExtensionName(String.format("%s v%s", BUNDLE.getString("projname"), BUNDLE.getString("version")));

        // 設定ファイル読み込み
        String configJSON = getCallbacks().loadExtensionSetting("configJSON");
        if (configJSON != null) {
            try {
                Config.stringFromJson(ConvertUtil.decompressZlibBase64(configJSON), this.getProperty());
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        this.ocspTab = new OCSPServerTab();
        this.ocspTab.setOCSPProperty(this.option.getOption());
        this.ocspTab.addPropertyChangeListener(newPropertyChangeListener());
        callbacks.addSuiteTab(this.ocspTab);
        callbacks.registerExtensionStateListener(this.ocspTab);
    }

    public PropertyChangeListener newPropertyChangeListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (OptionProperty.OCSP_PROPERTY.equals(evt.getPropertyName())) {
                    getProperty().setOption(ocspTab.getOCSPProperty());
                    applyOptionProperty();
                }
            }
        };
    }

    private final OptionProperty option = new OptionProperty();

    public OptionProperty getProperty() {
        return this.option;
    }

    public void setProperty(OptionProperty property) {
        this.option.setProperty(property);
    }

    protected void applyOptionProperty() {
        try {
            String configJSON = Config.stringToJson(this.getProperty());
            getCallbacks().saveExtensionSetting("configJSON", ConvertUtil.compressZlibBase64(configJSON));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

}
