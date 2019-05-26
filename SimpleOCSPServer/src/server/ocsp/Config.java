package server.ocsp;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXB;

/**
 *
 * @author isayan
 */
public final class Config {

    private Config() {
    }

    public static String getUserHome() {
        return System.getProperties().getProperty("user.home");
    }

    public static String getExtenderDir() {
        return ".fakecert";
    }
    
    public static String getUserDir() {
        return System.getProperties().getProperty("user.dir");
    }
    
    public static File getExtensionHomeDir() {
        return new File(Config.getUserHome(), Config.getExtenderDir());
    }

    public static void saveToXML(File fi, OptionProperty option) throws IOException {
        JAXB.marshal(option, fi);
    }

    public static void loadFromXML(File fi, OptionProperty option) throws IOException {
        OptionProperty property = JAXB.unmarshal(fi, OptionProperty.class);
        option.setProperty(property);
    }

    /**
     * Propertyファイルの読み込み
     *
     * @param content コンテンツ内容
     * @param option 設定オプション
     * @throws java.io.IOException
     */
    public static void loadFromXML(String content, OptionProperty option) throws IOException {
        OptionProperty property = JAXB.unmarshal(new StringReader(content), OptionProperty.class);
        option.setProperty(property);
    }
    
    public static String saveToXML(OptionProperty option) throws IOException {
        StringWriter writer = new StringWriter();
        JAXB.marshal(option, writer);
        return writer.toString();
    }

}
