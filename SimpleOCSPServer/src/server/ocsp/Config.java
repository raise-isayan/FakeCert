package server.ocsp;

import extend.util.IniProp;
import extend.util.Util;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author isayan
 */
public class Config {

    /**
     * Propertyファイルの読み込み
     *
     * @param content コンテンツ内容
     * @param option 設定オプション
     * @throws java.io.IOException
     */
    public static void loadFromXml(String content, OptionProperty option) throws IOException {
        IniProp prop = new IniProp();
        prop.loadFromXML(content);
        loadFromXml(prop, option);
    }

    protected static void loadFromXml(IniProp prop, OptionProperty option) throws IOException {
        OCSPProperty ocspProperty = option.getOCSPProperty();
        ocspProperty.setAutoStart(prop.readEntryBool("server.ocsp", "autoStart", false));
        ocspProperty.setListenPort(prop.readEntryInt("server.ocsp", "listenPort", 8888));
        String caCert = prop.readEntry("server.ocsp", "CACertificate", String.valueOf(ocspProperty.getCACertificateType()));
        OCSPProperty.CACertificateType caCertType = (OCSPProperty.CACertificateType) Util.parseEnumDefault(OCSPProperty.CACertificateType.class, caCert, null);
        ocspProperty.setCACertificateType(caCertType);
        if (caCertType == OCSPProperty.CACertificateType.CustomCA) {
            ocspProperty.setCaFile(new File(prop.readEntry("server.ocsp", "caFile", null)));
            // Do not store password
            // option.setPassword(prop.readEntry("server.ocsp", "password", null));
        }
    }

    /**
     * Propertyファイルの書き込み
     *
     * @param fo ファイル名
     * @param option 設定オプション
     * @throws java.io.IOException
     */
    public static void saveToXML(File fo, OptionProperty option) throws IOException {
        IniProp prop = new IniProp();
        saveToXML(prop, option);
        prop.storeToXML(fo, "Temporary Properties", "UTF-8");
    }

    public static String saveToXML(OptionProperty option) throws IOException {
        IniProp prop = new IniProp();
        saveToXML(prop, option);
        return prop.storeToXML("Temporary Properties", "UTF-8");
    }

    protected static void saveToXML(IniProp prop, OptionProperty option) throws IOException {
        OCSPProperty ocspProperty = option.getOCSPProperty();
        prop.writeEntryBool("server.ocsp", "autoStart", ocspProperty.isAutoStart());
        prop.writeEntryInt("server.ocsp", "listenPort", ocspProperty.getListenPort());
        prop.writeEntry("server.ocsp", "CACertificate", String.valueOf(ocspProperty.getCACertificateType()));
        OCSPProperty.CACertificateType caCertType = ocspProperty.getCACertificateType();
        ocspProperty.setCACertificateType(caCertType);
        if (caCertType == OCSPProperty.CACertificateType.CustomCA) {
            prop.writeEntry("server.ocsp", "caFile", String.valueOf(ocspProperty.getCaFile()));
            // Do not store password
            // prop.writeEntry("server.ocsp", "password", option.getPassword());
        }
    }

}
