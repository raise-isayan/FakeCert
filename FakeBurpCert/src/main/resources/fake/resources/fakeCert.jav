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
