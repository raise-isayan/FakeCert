package server.ocsp;

/**
 *
 * @author isayan
 */
public interface OptionProperty {
    public final static String OCSP_PROPERTY = "OCSPPropery";
    
    public OCSPProperty getOCSPProperty();
    
    public void setOCSPProperty(OCSPProperty ocsp);

}
