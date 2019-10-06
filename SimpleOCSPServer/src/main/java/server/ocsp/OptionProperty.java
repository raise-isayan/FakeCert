package server.ocsp;

/**
 *
 * @author raise.isayan
 */
public final class OptionProperty implements IOptionProperty {
 
    private final OCSPProperty ocsp = new OCSPProperty();

    @Override
    public OCSPProperty getOCSPProperty() {
        return ocsp;
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

