package server.ocsp;

import com.google.gson.annotations.Expose;

/**
 *
 * @author raise.isayan
 */
public final class OptionProperty implements IOptionProperty {
 
    @Expose
    private final OCSPProperty ocsp = new OCSPProperty();

    @Override
    public OCSPProperty getOCSPProperty() {
        return this.ocsp;
    }
    
    @Override
    public void setOCSPProperty(OCSPProperty ocsp) {
        this.ocsp.setProperty(ocsp);
    }

    @Override
    public void setProperty(IOptionProperty property) {
        this.setOCSPProperty(property.getOCSPProperty());
    }
    
}
