package fake.cert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;

public class FakeBurpCert {

    private static ClassPool classPool;
    private static boolean debug;

    public static void premain(final String agentArgs, Instrumentation instrumentation) throws Exception {
        classPool = ClassPool.getDefault();
        debug = "debug".equals(agentArgs);

        instrumentation.addTransformer(new ClassFileTransformer() {

            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                try {
                    if (className != null && className.equals("sun/security/x509/X509CertImpl")) {
                        if (debug) {
                            System.out.println("className:" + className);
                        }
                        CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                        // 変換テーブル作成メソッドを追加
                        ctClass.addMethod(CtMethod.make(buildResourceCommand(FAKE_CREATEMAP_COMMAND), ctClass));
                        // 変換テーブルのフィールドを追加
                        CtField f = CtField.make("static java.util.Map translateMaps;", ctClass);
                        ctClass.addField(f, "createMap()");
                        // 変換処理を行うメソッドを追加
                        CtMethod translateTableMethod = CtMethod.make(buildResourceCommand(FAKE_CERT_COMMAND), ctClass);
                        ctClass.addMethod(translateTableMethod);

                        CtClass ctX509CertInfo = classPool.makeClass("sun.security.x509.X509CertInfo");
                        CtConstructor ctConstructor = ctClass.getDeclaredConstructor(new CtClass[]{ctX509CertInfo});
                        StringBuilder command = new StringBuilder();
                        command.append("{ $1 = sun.security.x509.X509CertImpl.burpCertInjection($1); }");
                        ctConstructor.insertBefore(command.toString());
                        return ctClass.toBytecode();
                    } else if (className != null && className.equals("org/bouncycastle/asn1/x509/V3TBSCertificateGenerator")) {
                        if (debug) {
                            System.out.println("className:" + className);
                        }
                        CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                        // 変換テーブル作成メソッドを追加
                        ctClass.addMethod(CtMethod.make(buildResourceCommand(FAKE_CREATEMAP_COMMAND), ctClass));

                        // 変換テーブルのフィールドを追加
                        CtField f = CtField.make("static java.util.Map translateMaps;", ctClass);
                        ctClass.addField(f, "createMap()");
                        // 変換処理を行うメソッドを追加
                        CtMethod translateTableMethod = CtMethod.make(buildResourceCommand(FAKE_BUNCY_CERT_COMMAND), ctClass);
                        ctClass.addMethod(translateTableMethod);

                        CtMethod ctLoadMethod = ctClass.getDeclaredMethod("generateTBSCertificate");
                        StringBuilder command = new StringBuilder();
                        command.append("{ burpCertInjection(); }");
                        ctLoadMethod.insertBefore(command.toString());
                        return ctClass.toBytecode();

// burp v2020.6 以降において以下の処理があると、HTTPSにおいて接続時にエラーとなってしまうためコメント
//                    } else if (debug && className != null && className.equals("java/security/KeyStore")) {
//                        System.out.println("className:" + className);
//                        CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
//                        StringBuilder command = new StringBuilder();
//                        command.append("{ System.out.println(\"pwd\\\"\" + new String($2) + \"\\\"\"); }");
//                        CtMethod ctLoadMethod = ctClass.getDeclaredMethod("load");
//                        ctLoadMethod.insertBefore(command.toString());
//                        return ctClass.toBytecode();
                    } else if (debug && className != null) {
//                        if (className.startsWith("org/bouncycastle/")) {
//                            System.out.println("className:" + className);
//                        }
                        System.out.println("className:" + className);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    IllegalClassFormatException e = new IllegalClassFormatException(ex.getMessage());
                    e.initCause(ex);
                    throw e;
                }

                return null;
            }

        });

    }

    public final static String FAKE_CREATEMAP_COMMAND = "/fake/resources/createMap.jav";
    public final static String FAKE_CERT_COMMAND = "/fake/resources/fakeCert.jav";
    public final static String FAKE_BUNCY_CERT_COMMAND = "/fake/resources/fakeBouncyCert.jav";

    public static String buildResourceCommand(String resourcePath) {
        StringBuilder command = new StringBuilder();
        try(InputStream inStream = FakeBurpCert.class.getResourceAsStream(resourcePath)) {
            command.append(new String(readAllBytes(inStream), StandardCharsets.ISO_8859_1));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return command.toString();
    }

    /* InputStream.readAllBytes は JDK 9 からサポート */
    public static byte[] readAllBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream bostm = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int len = 0;
        while ((len = stream.read(buff)) >= 0) {
            bostm.write(buff, 0, len);
        }
        return bostm.toByteArray();
    }

}
