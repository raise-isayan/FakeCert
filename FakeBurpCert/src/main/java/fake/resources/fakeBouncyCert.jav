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
