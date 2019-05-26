package server.ocsp;

/**
 *
 * @author isayan
 */
public interface IOptionProperty {
    public final static String OCSP_PROPERTY = "OCSPPropery";
    
    public OCSPProperty getOCSPProperty();
    
    public void setOCSPProperty(OCSPProperty ocsp);

    public void setProperty(IOptionProperty property);
    
}
