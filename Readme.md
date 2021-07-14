Burp suite Certificate modification tool
=============
Language/[Japanese](Readme-ja.md)

This tool is used to modify or add information that is not included in the dynamically generated certificates in PortSwiggers Burp Suite.

This tool contains two projects.

FakeBurpCert
_____________

Change the certificates that the Burp suite creates.
You can currently do one of the following

1. modification of CN
2. set the serial number
3. set the date of the certificate
4. modification or add a SAN (Subject Alternative Name).
5. add an OCSP URI.

SimpleOCSPServer
_____________

A simple OCSP responder server.

# Usage

## FakeBurpCert

Put the following file in the folder with the Burp jar file.

* FakeBurpCert.jar
* cert.txt
* javassist.jar

In the folder containing the Burp suite jar files, start it with the -javaagent command line option (before the -jar option).

```
java -javaagent:FakeBurpCert.jar -Xmx1024m -jar burpsuite_free_v1.7.06.jar
```

## cert.txt file modification

The cert.txt file contains the rules for modifying the certificate.

This file should be written in the following format.
Note that the character encoding must be UTF-8.

```
# A line beginning with # is treated as a comment.
CN=www\.example\.com(,$)	x509.info.subject	CN=www.example.jp, OU=piyo CA, O=fuga, C=hoge
CN=www\.example\.com(,$)	x509.info.serialNumber	11223344AABB
CN=www\.example\.com(,$)	x509.info.validity	yyyy/MM/dd	2017/01/01	2027/12/31
CN=www\.example\.com(,$)	x509.info.extensions.SubjectAlternativeName	www.example.com
CN=www\.example\.com(,$)	x509.info.extensions.AuthorityInfoAccess.ocsp	http://www.example.com:8888/
```
rules can be stated on multiple lines.

each item is tab-delimited, and you can set the following values for each.

Column 1: Enter a regular expression that matches the subject of the certificate you want to change, and if it matches, it will be processed.

Column 2: Describe the type of field to be added or updated

Column 3: Value to be added or updated

The types of fields that can be specified in the 2 column are as follows

* x509.info.subject
    *  Certificate subject ... The subject of the modified certificate, which can be changed for CN.

* x509.info.serialNumber
    * Certificate serialNumber ... Hexadecimal notation..

* x509.info.validity
    * Certificate Expiration Date ... [dateFormatPattern] [fromDate] [toDate] in that order.
      Each item is tab-delimited.
         * dateFormatPattern ... Allows you to specify the date formats available in SimpleDateFormat
         * fromDate ...  Start date (in date format)
         * toDate ... End date (in date format)

* x509.info.extensions.SubjectAlternativeName
    * Certificate SAN ... Describe the SAN to be added. It does not change or delete the existing SAN, but adds the specified SAN.

* x509.info.extensions.SubjectAlternativeName.clear
    * Clears the existing SAN. If no value is specified, the certificate has no SAN specified and
      If a value is specified, the specified value is added after the existing SAN is cleared.

* x509.info.extensions.AuthorityInfoAccess.ocsp
    * OCSP URI of the certificate ... Include the URI.

By the way, the subject of a certificate generated dynamically by the Burp suite is in the following format

```
CN=www.example.jp, OU=PortSwigger CA, O=PortSwigger, C=PortSwigger
```

Starting with version 2020.2 of the Burp suite, Bouncy Castle is used in the library of certificates, in the following format


```
C=PortSwigger,O=PortSwigger,OU=PortSwigger CA,CN=www.example.com
```

If the OCSP URI of a certificate is included, a request for a validation process may be generated from a client application.
A simple OCSP responder server (SimpleOCSPServer) is provided to handle this OCSP verification process.

## SimpleOCSPServer

Start the Simple OCSP responder server.

You can start the responder server by the following command 


```
java -jar SimpleOCSPServer.jar -cafile=burp_ca.p12 -password=testca -port=8888
```

If multiple certificates are stored, you can specify them in alias.

```
java -jar SimpleOCSPServer.jar -cafile=burp_ca.p12 -alias=cacert -password=testca -port=8888
```

* -cafile
    * CA certificate with a private key (PKCS12 format)
* -password
    * CA certificate password.
* -alias 
    * Specifies an alias for the certificate. This is optional. If omitted. the first certificate found.
* -port
    * This is a standby port. Can be omitted. The default is 8888.

The CA of Burp suite can be exported by the following procedure.

1. Click [CA Certificate] on the [Options] tab.
2. Select the export [Certificate and Private Key in PKCS#12 Keystore] and go to the next
3. Export by specifying the file name and password to the next

SimpleOCSPServer also supports BurpExtender.

![OCSP Server](BurpExtender-ocsp.png) 

* [Start] button
    * Start the OCSP Server. Click again to stop it.
* Automatic start at loading 
    * When checked, OCSP Server is automatically started when the Burp suite is started.
* Listen port
    * The port to be used to listen for OCSP Server.
* CA Certificate
    * Burp suite default CA 
        * Use the CA that the Burp suite uses by default.
    * Use custom CA file
        * Specifies a CA certificate (PKCS12 format) that contains a private key.
        
Extenders in the Burp suite can be loaded as follows

1. Click [add] on the [Extender] tab.
2. Select file ... and select SimpleOCSPServer.jar.
3. Click [Next], make sure there are no errors, and then click [Close] to close the dialog.

# operating environment

## Burp suite
* v1.7 or higher (http://www.portswigger.net/burp/)
* v2020.2 (last version)

## development environment
* NetBeans 12.4 (https://netbeans.apache.org/)

## build
Build with NetBeans or build with gradle.

```
gradlew release
```

## Required library
Building requires a [BurpExtensionCommons](https://github.com/raise-isayan/BurpExtensionCommons) library.
* BurpExtensionCommons v0.4.x

## Library
* Jassist 3.26.0 (https://www.javassist.org/)
  * Mozilla Public License Version 1.1, GNU Lesser General Public License Version 2.1, Apache License Version 2.0
  * https://github.com/jboss-javassist/javassist

* Google gson 2.8.5 (https://github.com/google/gson)
  * Apache License 2.0
  * https://github.com/google/gson/

* Universal Chardet for java (https://code.google.com/archive/p/juniversalchardet/)
  * MPL 1.1
  * https://code.google.com/archive/p/juniversalchardet/

* BouncyCastle 1.6.4 (http://bouncycastle.org/)
  * MIT license
  * https://www.bouncycastle.org/license.html

* Jetty 9 (https://www.eclipse.org/jetty/)
  * Apache License 2.0, Eclipse Public License 1.0
  * https://www.eclipse.org/jetty/licenses.html

* Use Icon (http://www.famfamfam.com/lab/icons/silk/)
  * Creative Commons Attribution 2.5 License
  * http://www.famfamfam.com/lab/icons/silk/

## Note
This tool was developed by me personally and PortSwigger is not affiliated with it in any way. Please do not ask PortSwigger about any problems caused by using this tool.

This tool internally modifies the bytecode of the Java Runtime Environment, when used with Oracle's Java Runtime Environment [Binary Code License] (http://www.oracle.com/technetwork/java/javase/terms/license/index.html). We recommend that you check the license and run the program on a Java runtime 

## thanks
The idea for this tool was inspired by [Belle (Unofficial Burp Suite)](https://github.com/ankokuty/Belle).
I would like to thank the author of Belle for her advice on creating this tool. I would like to take this opportunity to thank the author of this tool for his advice.

