package fake.cert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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


/******************************************************************************/
    
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
            String[] inputs = line.split("\t", 3);
            java.util.List item = new java.util.ArrayList();
            item.add(new java.util.AbstractMap.SimpleEntry(inputs[1], (inputs.length > 2) ? inputs[2] : ""));
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
    
/******************************************************************************/

    org.bouncycastle.asn1.ASN1Integer              serialNumber;
    org.bouncycastle.asn1.x509.AlgorithmIdentifier     signature;
    org.bouncycastle.asn1.x500.X500Name                issuer;
    org.bouncycastle.asn1.x509.Time                    startDate, endDate;
    org.bouncycastle.asn1.x500.X500Name                subject;
    org.bouncycastle.asn1.x509.SubjectPublicKeyInfo    subjectPublicKeyInfo;
    org.bouncycastle.asn1.x509.Extensions              extensions;

static java.util.Map translateMaps = createMap();

public void burpCertInjection() {
    java.util.Iterator iterator = translateMaps.keySet().iterator();

    String subjectCN = subject.toString();
    while (iterator.hasNext()) {
        java.util.regex.Pattern regexSubject = (java.util.regex.Pattern) iterator.next();
        java.util.regex.Matcher m = regexSubject.matcher(subjectCN);
        if (m.find()) {
            java.lang.System.out.println("subject:" + subjectCN + "\t");
            java.util.List attrs = (java.util.List) translateMaps.get(regexSubject);
            for (int i = 0; i < attrs.size(); i++) {
                java.util.Map.Entry entry = (java.util.Map.Entry) attrs.get(i);
                String key = ((String) entry.getKey());
                java.lang.System.out.print("attr[" + key + "]	");
                if (key.equals("x509.info.subject")) {
                    String value = ((String)entry.getValue()).trim();
                    java.lang.System.out.println("\treplace_subject:[" + value + "]	");
                    subject = new org.bouncycastle.asn1.x500.X500Name(value);                            
                } else if (key.equals("x509.info.serialNumber")) {
                    String value = ((String)entry.getValue()).trim();
                    serialNumber = new org.bouncycastle.asn1.ASN1Integer(new java.math.BigInteger(value, 16));
                } else if (key.equals("x509.info.validity")) {
                    try {
                        String value = ((String)entry.getValue()).trim();
                        java.lang.System.out.println("\treplace_date:[" + value + "]	");
                        String[] list = value.split("\t", 3);
                        if (list.length != 3) {
                            new java.text.ParseException("x509.info.validity is value error: [dateFormat]\t[fromDate]\t[toDate]", list.length);
                        }
                        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(list[0]);
                        java.util.Date fromDate = format.parse(list[1]);
                        java.util.Date toDate = format.parse(list[2]);
                        startDate = new org.bouncycastle.asn1.x509.Time(fromDate);
                        endDate  = new org.bouncycastle.asn1.x509.Time(toDate);
                    } catch (java.text.ParseException ex) {
                        ex.printStackTrace();
                    }                        
                } else if (key.startsWith("x509.info.extensions.SubjectAlternativeName")) {
                    try {
                        String value = ((String)entry.getValue()).trim();
                        java.lang.System.out.println("\treplace_san:" + value);

                        org.bouncycastle.asn1.x509.ExtensionsGenerator extensionsGenerator = new org.bouncycastle.asn1.x509.ExtensionsGenerator();
                        org.bouncycastle.asn1.ASN1ObjectIdentifier [] oids = extensions.getCriticalExtensionOIDs();
                        // not SAN Exension
                        for (int k = 0; k < oids.length; k++) {
                            if (!org.bouncycastle.asn1.x509.Extension.subjectAlternativeName.equals(oids[i])) {
                                extensionsGenerator.addExtension(extensions.getExtension(oids[i]));
                            }
                        }
                        org.bouncycastle.asn1.x509.Extension sunExtenson = extensions.getExtension(org.bouncycastle.asn1.x509.Extension.subjectAlternativeName);
                        if (sunExtenson == null) {
                            if (!value.isEmpty()) {
                                org.bouncycastle.asn1.x509.GeneralNames dnsNames = new org.bouncycastle.asn1.x509.GeneralNames(new org.bouncycastle.asn1.x509.GeneralName(org.bouncycastle.asn1.x509.GeneralName.dNSName, value));
                                extensionsGenerator.addExtension(org.bouncycastle.asn1.x509.Extension.subjectAlternativeName, false, dnsNames);
                            }
                        }
                        else {
                            org.bouncycastle.asn1.x509.GeneralNames gns = org.bouncycastle.asn1.x509.GeneralNames.fromExtensions(extensions, org.bouncycastle.asn1.x509.Extension.subjectAlternativeName);
                            java.util.List dnsNameList = new java.util.ArrayList();
                            if (!key.endsWith(".clear")) {
                                dnsNameList.addAll(java.util.Arrays.asList(gns.getNames()));                            
                            }
                            if (!value.isEmpty()) {
                                dnsNameList.add(new org.bouncycastle.asn1.x509.GeneralName(org.bouncycastle.asn1.x509.GeneralName.dNSName, value));
                            }
                            org.bouncycastle.asn1.x509.GeneralNames dnsNames = new org.bouncycastle.asn1.x509.GeneralNames((org.bouncycastle.asn1.x509.GeneralName[])dnsNameList.toArray(new org.bouncycastle.asn1.x509.GeneralName[dnsNameList.size()]));
                            extensionsGenerator.addExtension(org.bouncycastle.asn1.x509.Extension.subjectAlternativeName, false, dnsNames);                            
                        }                            
                        extensions = extensionsGenerator.generate();
                    } catch (java.io.IOException ex) {
                        ex.printStackTrace();
                    } catch (java.lang.Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (key.equals("x509.info.extensions.AuthorityInfoAccess.ocsp")) {
                    try {
                        String value = ((String)entry.getValue()).trim();
                        java.lang.System.out.println("ocsp:" + value);

                        org.bouncycastle.asn1.x509.ExtensionsGenerator extensionsGenerator = new org.bouncycastle.asn1.x509.ExtensionsGenerator();
                        org.bouncycastle.asn1.ASN1ObjectIdentifier [] oids = extensions.getCriticalExtensionOIDs();
                        for (int k = 0; k < oids.length; k++) {
                            if (!org.bouncycastle.asn1.x509.Extension.authorityInfoAccess.equals(oids[i])) {
                                extensionsGenerator.addExtension(extensions.getExtension(oids[i]));
                            }
                        }
                        org.bouncycastle.asn1.x509.Extension sunExtenson = extensions.getExtension(org.bouncycastle.asn1.x509.Extension.authorityInfoAccess);
                        if (sunExtenson == null) {
                            org.bouncycastle.asn1.x509.AuthorityInformationAccess authInfo = new org.bouncycastle.asn1.x509.AuthorityInformationAccess(org.bouncycastle.asn1.x509.AccessDescription.id_ad_ocsp, new org.bouncycastle.asn1.x509.GeneralName(org.bouncycastle.asn1.x509.GeneralName.uniformResourceIdentifier, value));
                            extensionsGenerator.addExtension(org.bouncycastle.asn1.x509.Extension.authorityInfoAccess, false, authInfo);
                        }
                        else {
                           org.bouncycastle.asn1.x509.AuthorityInformationAccess authInfo = new org.bouncycastle.asn1.x509.AuthorityInformationAccess(org.bouncycastle.asn1.x509.AccessDescription.id_ad_ocsp, new org.bouncycastle.asn1.x509.GeneralName(org.bouncycastle.asn1.x509.GeneralName.uniformResourceIdentifier, value));
                           extensionsGenerator.addExtension(org.bouncycastle.asn1.x509.Extension.authorityInfoAccess, false, authInfo);
                        }                            
                        extensions = extensionsGenerator.generate();
                    } catch (java.io.IOException ex) {
                        ex.printStackTrace();
                    } catch (java.lang.Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }
        }
    }
}

/******************************************************************************/

public static sun.security.x509.X509CertInfo burpCertInjection(sun.security.x509.X509CertInfo certInfo) {
    java.util.Iterator iterator = translateMaps.keySet().iterator();
    try {
        String subject = String.valueOf(certInfo.get(sun.security.x509.X509CertInfo.SUBJECT));
        while (iterator.hasNext()) {
            java.util.regex.Pattern regexSubject = (java.util.regex.Pattern) iterator.next();
            java.util.regex.Matcher m = regexSubject.matcher(subject);
            if (m.find()) {
                java.lang.System.out.println("subject:" + subject + "\t");
                java.util.List attrs = (java.util.List) translateMaps.get(regexSubject);
                for (int i = 0; i < attrs.size(); i++) {
                    java.util.Map.Entry entry = (java.util.Map.Entry) attrs.get(i);
                    String key = ((String) entry.getKey());
                    java.lang.System.out.print("attr[" + key + "]	");
                    if (key.equals("x509.info.subject")) {
                        String value = ((String)entry.getValue()).trim();
                        java.lang.System.out.println("\treplace_subject:[" + value + "]	");
                        certInfo.set(sun.security.x509.X509CertInfo.SUBJECT, new sun.security.x509.CertificateSubjectName(new sun.security.x509.X500Name(value)));
                    } else if(key.equals("x509.info.serialNumber")) {
                        String value = ((String)entry.getValue()).trim();
                        certInfo.set(sun.security.x509.X509CertInfo.SERIAL_NUMBER, new sun.security.x509.CertificateSerialNumber(new java.math.BigInteger(value, 16)));
                    } else if (key.equals("x509.info.validity")) {
                        String value = ((String)entry.getValue()).trim();
                        java.lang.System.out.println("\treplace_date:[" + value + "]	");
                        String[] list = value.split("\t", 3);
                        if (list.length != 3) {
                            new java.text.ParseException("x509.info.validity is value error: [dateFormat]\t[fromDate]\t[toDate]", list.length);
                        }
                        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(list[0]);
                        java.util.Date fromDate = format.parse(list[1]);
                        java.util.Date toDate = format.parse(list[2]);
                        sun.security.x509.CertificateValidity interval = new sun.security.x509.CertificateValidity(fromDate, toDate);
                        certInfo.set(sun.security.x509.X509CertInfo.VALIDITY, interval);
                    } else if (key.startsWith("x509.info.extensions.SubjectAlternativeName")) {
                        String value = ((String)entry.getValue()).trim();
                        java.lang.System.out.println("\treplace_san:" + value);
                        sun.security.x509.CertificateExtensions ext = (sun.security.x509.CertificateExtensions) certInfo.get(sun.security.x509.X509CertInfo.EXTENSIONS);
                        if (ext == null) {
                            ext = new sun.security.x509.CertificateExtensions();
                        }
                        sun.security.x509.SubjectAlternativeNameExtension san = (sun.security.x509.SubjectAlternativeNameExtension) ext.get(sun.security.x509.SubjectAlternativeNameExtension.NAME);
                        if (san == null) {
                            if (!value.isEmpty()) {
                                sun.security.x509.GeneralNames alternativeNames = new sun.security.x509.GeneralNames();                            
                                sun.security.x509.DNSName dnsName = new sun.security.x509.DNSName(value);                                                                                 
                                alternativeNames.add(new sun.security.x509.GeneralName(dnsName));                            
                                san = new sun.security.x509.SubjectAlternativeNameExtension(alternativeNames);                            
                            }
                        }
                        else {
                            sun.security.x509.GeneralNames alternativeNames = san.get(sun.security.x509.SubjectAlternativeNameExtension.SUBJECT_NAME);
                            if (alternativeNames == null) {
                                alternativeNames = new sun.security.x509.GeneralNames();                            
                            }
                            if (key.endsWith(".clear")) {
                                alternativeNames = new sun.security.x509.GeneralNames();  
                                if (ext.get(sun.security.x509.SubjectAlternativeNameExtension.NAME) != null) ext.delete(sun.security.x509.SubjectAlternativeNameExtension.NAME);
                                if (san.get(sun.security.x509.SubjectAlternativeNameExtension.SUBJECT_NAME) != null) san.delete(sun.security.x509.SubjectAlternativeNameExtension.SUBJECT_NAME);
                            }
                            if (!value.isEmpty()) {
                                sun.security.x509.DNSName dnsName = new sun.security.x509.DNSName(value);                                                                                 
                                alternativeNames.add(new sun.security.x509.GeneralName(dnsName));
                            }
                            if (alternativeNames.size() > 0) {
                                san.set(sun.security.x509.SubjectAlternativeNameExtension.SUBJECT_NAME, alternativeNames);                                                
                            }
                        }
                        if (san != null && san.get(sun.security.x509.SubjectAlternativeNameExtension.SUBJECT_NAME) != null) {                    
                            ext.set(sun.security.x509.SubjectAlternativeNameExtension.NAME, san);
                        }
                        if (ext.getElements().hasMoreElements()) {
                            certInfo.set(sun.security.x509.X509CertInfo.EXTENSIONS, ext);                    
                        }
                        else {
                            if (certInfo.get(sun.security.x509.X509CertInfo.EXTENSIONS) != null) certInfo.delete(sun.security.x509.X509CertInfo.EXTENSIONS);
                        }

                    } else if (key.equals("x509.info.extensions.AuthorityInfoAccess.ocsp")) {
                        sun.security.x509.CertificateExtensions ext = (sun.security.x509.CertificateExtensions) certInfo.get(sun.security.x509.X509CertInfo.EXTENSIONS);
                        if (ext == null) {
                            ext = new sun.security.x509.CertificateExtensions();
                        }
                        String value = ((String)entry.getValue()).trim();
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

}
