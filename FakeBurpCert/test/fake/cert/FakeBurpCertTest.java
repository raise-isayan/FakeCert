/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fake.cert;

import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author isayan
 */
public class FakeBurpCertTest {

    public FakeBurpCertTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of buildFakeCertCommand method, of class FakeBurpCert.
     */
    @Test
    public void testBuildFakeCertCommand() {
        System.out.println("static java.util.Map translateMaps = createMap();");

        String command = FakeBurpCert.buildCreateMap();
        System.out.println(command);

        String command2 = FakeBurpCert.buildFakeCertCommand();
        System.out.println(command2);
    }

    public static String escapeLiteral(String command) {
        command = command.replaceAll("\\\\", "\\\\\\\\");
        command = command.replaceAll("\"", "\\\\\"");
        return "\"" + command + "\"";
    }

    @Test
    public void testCommand() {
        System.out.println(escapeLiteral("xx\"x\\xx"));
    }

    static java.util.Map translateMaps = createMap();

    public static java.util.Map createMap() {
        java.util.Map map = new java.util.LinkedHashMap();
        java.io.BufferedReader reader = null;
        try {
            reader = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream("cert.txt"), "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                String[] inputs = line.split("	", 3);
                java.util.List item = new java.util.ArrayList();
                item.add(new java.util.AbstractMap.SimpleEntry(inputs[1], inputs[2]));
                java.util.List attrs = (java.util.List) map.putIfAbsent(java.util.regex.Pattern.compile(inputs[0]), item);
                if (attrs != null) {
                    attrs.addAll(item);
                }
            }
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (java.io.IOException ex) {
                }
            }
        }
        return map;
    }

    public static sun.security.x509.X509CertInfo burpCertInjection(sun.security.x509.X509CertInfo certInfo) {
        java.util.Iterator iterator = translateMaps.keySet().iterator();
        try {
            String subject = String.valueOf(certInfo.get(sun.security.x509.X509CertInfo.SUBJECT));
            while (iterator.hasNext()) {
                java.util.regex.Pattern regexSubject = (java.util.regex.Pattern) iterator.next();
                java.util.regex.Matcher m = regexSubject.matcher(subject);
                if (m.lookingAt()) {
                    java.lang.System.out.println("subject:" + subject + "\t");
                    java.util.List attrs = (java.util.List) translateMaps.get(regexSubject);
                    for (int i = 0; i < attrs.size(); i++) {
                        java.util.Map.Entry entry = (java.util.Map.Entry) attrs.get(i);
                        String key = ((String) entry.getKey());
                        java.lang.System.out.print("attr[" + key + "]	");
                        if (key.equals("x509.info.subject")) {
                            String value = (String) entry.getValue();
//                            String[] list = value.split("\t");
//                            for (int k = 0;  k < list.length; k++) {
//                                java.util.regex.Matcher matchReplace = java.util.regex.Pattern.compile("([A-Z]{1,2})=(.*)").matcher(list[k]);
//                                if (matchReplace.find()) {
//                                    String attr_name = matchReplace.group(1);
//                                    String attr_value = matchReplace.group(2);
//                                    subject = subject.replaceFirst(attr_name + "=([^,]*)", attr_name + "=" + attr_value);
//                                }
//                            }
                            java.lang.System.out.println("\treplace_subject[" + value + "]	");
                            certInfo.set(sun.security.x509.X509CertInfo.SUBJECT, new sun.security.x509.CertificateSubjectName(new sun.security.x509.X500Name(value)));
                        } else if(key.equals("x509.info.serialNumber")) {
                            certInfo.set(sun.security.x509.X509CertInfo.SERIAL_NUMBER, new sun.security.x509.CertificateSerialNumber(new java.math.BigInteger((String) entry.getValue(), 16)));
                        } else if (key.equals("x509.info.validity")) {
                            String value = (String) entry.getValue();
                            String[] list = value.split("\t", 3);
                            if (list.length != 3) {
                                new java.text.ParseException("x509.info.validity is value error: [dateFormat]\t[fromDate]\t[toDate]", list.length);
                            }
                            java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(list[0]);
                            java.util.Date fromDate = format.parse(list[1]);
                            java.util.Date toDate = format.parse(list[2]);
                            sun.security.x509.CertificateValidity interval = new sun.security.x509.CertificateValidity(fromDate, toDate);
                            certInfo.set(sun.security.x509.X509CertInfo.VALIDITY, interval);
                        } else if (key.equals("x509.info.extensions.AuthorityInfoAccess.ocsp")) {
                            sun.security.x509.CertificateExtensions ext = (sun.security.x509.CertificateExtensions) certInfo.get(sun.security.x509.X509CertInfo.EXTENSIONS);
                            if (ext == null) {
                                ext = new sun.security.x509.CertificateExtensions();
                            }
                            String value = (String) entry.getValue();
                            java.lang.System.out.println("ocsp:" + value);
                            java.util.List adList = new java.util.ArrayList();
                            adList.add(new sun.security.x509.AccessDescription(sun.security.x509.AccessDescription.Ad_OCSP_Id, new sun.security.x509.GeneralName(new sun.security.x509.URIName(value))));
                            ext.set(sun.security.x509.AuthorityInfoAccessExtension.NAME, new sun.security.x509.AuthorityInfoAccessExtension(adList));
                            certInfo.set(sun.security.x509.X509CertInfo.EXTENSIONS, ext);
                        }
                    }
                }
            }
        } catch (java.security.cert.CertificateException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return certInfo;
    }

    /**
     * Test of testCase method, of class FakeBurpCert.
     */
    @Test
    public void testTestCase() {
        Map map = createMap();
        for (Object key : map.keySet()) {
            java.util.List attrs = (java.util.List) map.get(key);
            for (Object attr : attrs) {
                Map.Entry entry = (java.util.Map.Entry) attr;
                System.out.print(key);
                System.out.print("\tkey=" + entry.getKey());
                System.out.print("\tval=" + entry.getValue());
                System.out.println();
            }
        }
    }

}
