package server.ocsp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.util.Locale;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.Req;

/**
 *
 * @author isayan
 */
public class SimpleOCSPServer {
    protected final static java.util.ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("server/ocsp/resources/Resource");

    private HttpServer server = null;

    public SimpleOCSPServer() {
    }

    public void startServer(PrivateKey issuerPrivateKey, X509Certificate issuerCert, int listenPort) throws IOException {
        System.out.println("start listen port:" + listenPort);
        this.server = HttpServer.create(new InetSocketAddress(listenPort), 0);
        server.createContext("/", new MyHandler(issuerPrivateKey, issuerCert));
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    public boolean isRunning() {
        return (this.server != null);
    }

    public void stopServer() {
        if (this.server != null) {
            this.server.stop(1);
        }
        this.server = null;
    }

    private static void usage() {
        System.out.println(String.format("Usage: java -jar %s.jar <-cafile=<pkcs12>> <-password=<pkcs12 password>> [-port=<listen port>]", SimpleOCSPServer.class.getSimpleName()));
        System.out.println(BUNDLE.getString("server.ocsp.main.arg.cafile"));
        System.out.println(BUNDLE.getString("server.ocsp.main.arg.password"));
        System.out.println(BUNDLE.getString("server.ocsp.main.arg.alias"));
        System.out.println(BUNDLE.getString("server.ocsp.main.arg.port"));
        System.out.println("");
    }

    static class MyHandler implements HttpHandler {
        private PrivateKey issuerPrivateKey = null;
        private X509Certificate issuerCert = null;

        public MyHandler(PrivateKey issuerPrivateKey, X509Certificate issuerCert) {
            this.issuerPrivateKey = issuerPrivateKey;
            this.issuerCert = issuerCert;
        }

        @Override
        public void handle(HttpExchange he) throws IOException {
            try {
                // RFC2560
                String method = he.getRequestMethod();            
                if ("GET".equals(method)) {
                    doGet(he);            
                }
                else if ("POST".equals(method)) {
                    doPost(he);
                }
            } catch (OCSPException ex) {
                doThrowable(he, ex);
            } catch (Exception ex) {
                doThrowable(he, ex);
            }
        }
        
        public void doGet(HttpExchange he) throws IOException, OCSPException {
            String uri = he.getRequestURI().toString();
            String [] paths = uri.split("/");
            String url = URLDecoder.decode(paths[paths.length - 1], "8859_1");
            byte [] b64 = DatatypeConverter.parseBase64Binary(url);
            doResponse(he, b64);
        }    
        
        public void doPost(HttpExchange he) throws IOException, OCSPException {    
            try(ByteArrayOutputStream bostm = new ByteArrayOutputStream()) {
                byte buff[] = new byte[2048];
                try (InputStream is = he.getRequestBody()) {
                    int len = 0;
                    while ((len = is.read(buff)) > -1) {
                        bostm.write(buff, 0, len);
                    }
                }
                doResponse(he, bostm.toByteArray());
            }    
        }

        public void doResponse(HttpExchange he, byte [] ocspRequest) throws IOException, OCSPException {    
            OCSPReq ocspReq = new OCSPReq(ocspRequest);
            Req[] reqIDList = ocspReq.getRequestList();
            OCSPResp resp = OCSPUtil.genOCSPResp(reqIDList, issuerPrivateKey, issuerCert);
            he.getResponseHeaders().add("Content-Type", "application/ocsp-response");
            he.sendResponseHeaders(HttpURLConnection.HTTP_OK, resp.getEncoded().length);
            try (OutputStream os = he.getResponseBody()) {
                os.write(resp.getEncoded());
            }
        }

        public void doThrowable(HttpExchange he, Exception ex) throws IOException { 
                String errmsg = OCSPUtil.getStackTrace(ex);
                he.getResponseHeaders().add("Content-Type", "text/plain");
                he.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, errmsg.length());
                try (OutputStream os = he.getResponseBody()) {
                    os.write(errmsg.getBytes("UTF-8"));
                }
                Logger.getLogger(SimpleOCSPServer.class.getName()).log(Level.SEVERE, null, ex);                            
        }   

    }
    
    public static class ThreadWrap extends Thread {
        private final SimpleOCSPServer server = new SimpleOCSPServer();
        private final PrivateKey issuerPrivateKey;
        private final X509Certificate issuerCert;
        private final int listenPort;

        public ThreadWrap(PrivateKey issuerPrivateKey, X509Certificate issuerCert, int listenPort) {
            this.issuerPrivateKey = issuerPrivateKey;
            this.issuerCert = issuerCert;
            this.listenPort = listenPort;
        }

        public void startServer() {
            this.start();
        }

        @Override
        public void run() {
            try {
                this.server.startServer(this.issuerPrivateKey, this.issuerCert, this.listenPort);
            } catch (IOException ex) {
                Logger.getLogger(OCSPServerTab.class.getName()).log(Level.SEVERE, null, ex);
                UncaughtExceptionHandler handler = this.getUncaughtExceptionHandler();
                if (handler != null) {
                    handler.uncaughtException(this, ex);
                }
            }
        }

        public boolean isRunning() {
            return this.server.isRunning();
        }

        public void stopServer() {
            this.server.stopServer();
        }

    }

    private final static java.util.ResourceBundle RELEASE = java.util.ResourceBundle.getBundle("burp/release");

    private static String getVersion() {
        return RELEASE.getString("version");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int defaultPort = 8888;
        File pkcs_ca = null;
        String password = null;
        String alias = null;

        try {
            for (String arg : args) {
                // single parameter
                if ("-v".equals(arg)) {
                    System.out.println("Version: " + getVersion());
                    System.out.println("Language: " + Locale.getDefault().getLanguage());
                    System.exit(0);
                }
                if ("-h".equals(arg)) {
                    usage();
                    System.exit(0);
                }
                // multi parameter
                String[] param = arg.split("=", 2);
                if (param.length < 2) {
                    throw new IllegalArgumentException("argment err:" + String.join(" ", param));
                }
                if ("-cafile".equals(param[0])) {
                    pkcs_ca = new File(param[1]);
                } else if ("-password".equals(param[0])) {
                    password = param[1];
                } else if ("-alias".equals(param[0])) {
                    alias = param[1];
                } else if ("-port".equals(param[0])) {
                    defaultPort = Integer.parseInt(param[1]);
                }
            }

            // 必須チェック
            if (pkcs_ca == null || password == null) {
                System.out.println("-cafile or -password argument err ");
                usage();
                return;
            }

            // 存在チェック
            if (!(pkcs_ca != null && pkcs_ca.exists() && pkcs_ca.isFile())) {
                System.out.println("-cafile: File not found = " + pkcs_ca);
                return;
            }

            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(pkcs_ca), password.toCharArray());

            // ailias 存在確認
            if (alias != null && !ks.isKeyEntry(alias)) {
                System.out.println("-alias: alias of keystore entry not found = " + alias);
                System.out.println("alias included in certificate:");
                Enumeration<String> aliases = ks.aliases();
                while (aliases.hasMoreElements()) {
                    System.out.println("        " + aliases.nextElement());
                }                
                return;
            }
            // 最初にみつかったalias
            if (alias == null) {
                alias = OCSPUtil.getFirstAlias(ks);
            }

            PrivateKey issuerPrivateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
            X509Certificate issuerCert = (X509Certificate) ks.getCertificate(alias);

            SimpleOCSPServer server = new SimpleOCSPServer();
            server.startServer(issuerPrivateKey, issuerCert, defaultPort);

        } catch (Exception ex) {
            String errmsg = String.format("%s: %s", ex.getClass().getName(), ex.getMessage());
            System.out.println(errmsg);
            Logger.getLogger(SimpleOCSPServer.class.getName()).log(Level.SEVERE, null, ex);
            usage();
        }
    }

}
