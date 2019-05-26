package burp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import extend.util.ConvertUtil;
import server.ocsp.Config;
import server.ocsp.OCSPServerTab;
import server.ocsp.IOptionProperty;
import server.ocsp.OptionProperty;

/**
 *
 * @author isayan
 */
public class BurpExtender extends BurpExtenderImpl {

    public static BurpExtender getInstance() {
        return BurpExtenderImpl.<BurpExtender>getInstance();
    }

    private OCSPServerTab ocspTab;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks cb) {
        super.registerExtenderCallbacks(cb);

        // 設定ファイル読み込み
        String configXML = getCallbacks().loadExtensionSetting("configXML");
        if (configXML != null) {
            try {
                Config.loadFromXML(ConvertUtil.decompressZlibBase64(configXML), this.getProperty());
            } catch (IOException ex) {
                Logger.getLogger(BurpExtender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.ocspTab = new OCSPServerTab();
        this.ocspTab.setOCSPProperty(this.option.getOCSPProperty());
        this.ocspTab.addPropertyChangeListener(newPropertyChangeListener());
        cb.addSuiteTab(this.ocspTab);
        cb.registerExtensionStateListener(this.ocspTab);
    }

    public PropertyChangeListener newPropertyChangeListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (IOptionProperty.OCSP_PROPERTY.equals(evt.getPropertyName())) {
                    getProperty().setOCSPProperty(ocspTab.getOCSPProperty());
                    applyOptionProperty();
                }
            }
        };
    }

    private final OptionProperty option = new OptionProperty();

    public OptionProperty getProperty() {
        return this.option;
    }
    
    public void setProperty(IOptionProperty property) {
        this.option.setProperty(property);
    }

    protected void applyOptionProperty() {
        try {
            String configXML = Config.saveToXML(this.getProperty());
            getCallbacks().saveExtensionSetting("configXML", ConvertUtil.compressZlibBase64(configXML));

        } catch (IOException ex) {
            Logger.getLogger(BurpExtender.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(BurpExtender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
