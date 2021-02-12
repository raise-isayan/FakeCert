package server.ocsp;

import com.google.gson.annotations.Expose;

/**
 *
 * @author raise.isayan
 */
public final class OptionProperty implements IOptionProperty<OCSPProperty> {
    public final static String OCSP_PROPERTY = "OCSPPropery";

    @Override
    public void setProperty(IOptionProperty<OCSPProperty> property) {
        this.setOption(property.getOption());
    }

    @Expose
    private final OCSPProperty ocsp = new OCSPProperty();
    
    @Override
    public OCSPProperty getOption() {
        return this.ocsp;
    }

    @Override
    public void setOption(OCSPProperty property) {
        this.ocsp.setProperty(ocsp);
    }
    
}
