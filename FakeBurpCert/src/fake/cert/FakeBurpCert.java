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
        debug = "degug".equals(agentArgs);

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
                        addMappingTableMethod(ctClass);
                        // 変換テーブルのフィールドを追加
                        CtField f = CtField.make("static java.util.Map translateMaps;", ctClass);
                        ctClass.addField(f, "createMap()");
                        // 変換処理を行うメソッドを追加
                        addCertIncectionMethod(ctClass);
                        CtClass ctX509CertInfo = classPool.makeClass("sun.security.x509.X509CertInfo");
                        CtConstructor ctConstructor = ctClass.getDeclaredConstructor(new CtClass[]{ctX509CertInfo});
                        insertX509CertCommand(ctConstructor);
                        return ctClass.toBytecode();
                    } else if (className != null && className.equals("java/security/KeyStore")) {
                        if (debug) {
                            System.out.println("className:" + className);
                            CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                            StringBuilder command = new StringBuilder();
                            command.append("{ System.out.println(\"pwd\\\"\" + new String($2) + \"\\\"\"); }");
                            CtMethod ctLoadMethod = ctClass.getDeclaredMethod("load");
                            ctLoadMethod.insertBefore(command.toString());
                            return ctClass.toBytecode();
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    IllegalClassFormatException e = new IllegalClassFormatException(ex.getMessage());
                    e.initCause(ex);
                    throw e;
                }

                return null;
            }

            CtConstructor insertX509CertCommand(CtConstructor ctConstructor) throws Exception {
                StringBuilder command = new StringBuilder();
                command.append("{");
                command.append("  $1 = sun.security.x509.X509CertImpl.burpCertInjection($1);");
                command.append("}");
                ctConstructor.insertBefore(command.toString());
                return ctConstructor;
            }

            CtClass addMappingTableMethod(CtClass ctClass) throws Exception {
                // 変換用のテーブルを作成する
                String command = buildCreateMap();
                CtMethod translateTableMethod = CtMethod.make(command, ctClass);
                ctClass.addMethod(translateTableMethod);
                return ctClass;
            }

            CtClass addCertIncectionMethod(CtClass ctClass) throws Exception {
                // 変換を行うメソッドを追加する
                String command = buildFakeCertCommand();
                CtMethod translateTableMethod = CtMethod.make(command, ctClass);
                ctClass.addMethod(translateTableMethod);
                return ctClass;
            }

        });

    }

    
    public static String buildFakeCertCommand() {
        StringBuilder command = new StringBuilder();
        try(InputStream inStream = FakeBurpCert.class.getResourceAsStream("/fake/resource/fakeCert.jav")) {            
            command.append(new String(readAllBytes(inStream), StandardCharsets.ISO_8859_1));      
        } catch (IOException ex) {        
            ex.printStackTrace();
        }
        return command.toString();
    }

    public static String buildCreateMap() {
        StringBuilder command = new StringBuilder();
        try(InputStream inStream = FakeBurpCert.class.getResourceAsStream("/fake/resource/createMap.jav")) {            
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
