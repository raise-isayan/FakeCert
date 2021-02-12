package server.ocsp;

/**
 *
 * @author isayan
 */
public interface IOptionProperty<X> {
    
    public X getOption();
    
    public void setOption(X property);

    public void setProperty(IOptionProperty<X> property);
    
}
