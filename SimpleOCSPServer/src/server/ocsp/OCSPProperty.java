package server.ocsp;

import java.io.File;

/**
 *
 * @author isayan
 */
public class OCSPProperty {

    private int listenPort = 8888;

    /**
     * @return the listenPort
     */
    public int getListenPort() {
        return this.listenPort;
    }

    /**
     * @param listenPort the listenPort to set
     */
    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public static enum CACertificateType {
        BurpCA, CustomCA
    };

    private CACertificateType caCertificateType = CACertificateType.BurpCA;

    /**
     * @return the caCACertificateType
     */
    public CACertificateType getCACertificateType() {
        return this.caCertificateType;
    }

    /**
     * @param caCertificateType the caCertificateType to set
     */
    public void setCACertificateType(CACertificateType caCertificateType) {
        this.caCertificateType = caCertificateType;
    }

    private File caFile = null;

    /**
     * @return the caFile
     */
    public File getCaFile() {
        return this.caFile;
    }

    /**
     * @param caFile the caFile to set
     */
    public void setCaFile(File caFile) {
        this.caFile = caFile;
    }

    private String password = "";

    /**
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    private boolean autoStart = false;

    /**
     * @return the autoStart
     */
    public boolean isAutoStart() {
        return this.autoStart;
    }

    /**
     * @param autoStart the autoStart to set
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public void setProperty(OCSPProperty property) {
        this.listenPort = property.listenPort;
        this.caCertificateType = property.caCertificateType;
        this.caFile = property.caFile;
        this.password = property.password;
        this.autoStart = property.autoStart;
    }

}
