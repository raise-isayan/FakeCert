package server.ocsp;

import extension.helpers.CertUtil;
import extension.helpers.StringUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

/**
 *
 * @author isayan
 */
public class SimpleJettyServer {
   private final static Logger logger = Logger.getLogger(SimpleJettyServer.class.getName());

    protected final static java.util.ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("server/ocsp/resources/Resource");

    public SimpleJettyServer() {
    }

    private boolean debug = false;
    private Server server = null;

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }


    public void startServer(PrivateKey issuerPrivateKey, X509Certificate issuerCert, int listenPort) throws Exception {
        System.out.println("start listen port:" + listenPort);
        this.server = createServer(listenPort);

        ContextHandler contextRoot = new ContextHandler("/");
        contextRoot.setHandler(new MyHandler(issuerPrivateKey, issuerCert));

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { contextRoot });

        server.setHandler(contexts);
        server.setDumpAfterStart(this.debug);
        server.start();
    }

    public boolean isRunning() {
        return (this.server != null);
    }

    public void joinServer() {
        if (this.server != null) {
            try {
                this.server.join();
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }


    public void stopServer() {
        if (this.server != null) {
            try {
                this.server.stop();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        this.server = null;
    }

    private static void usage() {
        System.out.println("");
        System.out.println(String.format("Usage: java -jar %s.jar [option] <-cafile=<pkcs12>> <-password=<pkcs12 password>> [-port=<listen port>]", SimpleJettyServer.class.getSimpleName()));
        System.out.println("[option]");
        System.out.println("\t" + BUNDLE.getString("server.ocsp.main.arg.help"));
        System.out.println("\t" + BUNDLE.getString("server.ocsp.main.arg.version"));
        System.out.println("\t" + BUNDLE.getString("server.ocsp.main.arg.debug"));
        System.out.println("[command]");
        System.out.println("\t" + BUNDLE.getString("server.ocsp.main.arg.cafile"));
        System.out.println("\t" + BUNDLE.getString("server.ocsp.main.arg.password"));
        System.out.println("\t" + BUNDLE.getString("server.ocsp.main.arg.alias"));
        System.out.println("\t" + BUNDLE.getString("server.ocsp.main.arg.port"));
        System.out.println("");
    }

    class MyHandler extends AbstractHandler  {

        private PrivateKey issuerPrivateKey = null;
        private X509Certificate issuerCert = null;

        public MyHandler(PrivateKey issuerPrivateKey, X509Certificate issuerCert) {
            this.issuerPrivateKey = issuerPrivateKey;
            this.issuerCert = issuerCert;
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            try {
                // RFC2560
                String method = baseRequest.getMethod();
                if ("GET".equals(method)) {
                    doGet(baseRequest, request, response);
                } else if ("POST".equals(method)) {
                    doPost(baseRequest, request, response);
                }
            } catch (Exception ex) {
                doThrowable(response, ex);
            }
        }

        public void doGet(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            String uri = baseRequest.getRequestURI();
            if (uri.equals("/")) {
                doSuccessResponse(response);
            }
            else {
                String[] paths = uri.split("/");
                byte[] b64 = OCSPWrap.decodeOCSPUrl(paths[paths.length - 1]);
                doResponse(response, b64);
            }
        }

        public void doPost(Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            try (ByteArrayOutputStream bostm = new ByteArrayOutputStream()) {
                byte buff[] = new byte[2048];
                try (InputStream is = request.getInputStream()) {
                    int len = 0;
                    while ((len = is.read(buff)) > -1) {
                        bostm.write(buff, 0, len);
                    }
                }
                doResponse(response, bostm.toByteArray());
            }
        }

        public void doSuccessResponse(HttpServletResponse response) throws IOException {
            response.setContentType("text/html");
            response.setStatus(HttpURLConnection.HTTP_OK);
            try (PrintStream os = new PrintStream(response.getOutputStream())) {
                os.println("<html>" +
                            "<p>Success</p>" +
                            "<p>Support RFC2560</p>" +
                            "<p><strong>GET</strong>" +
                            "<pre><code>GET {url}/{url-encoding of base-64 encoding of the DER encoding of the OCSPRequest}</code></pre>" +
                            "<p><strong>POST</strong>" +
                            "<pre><code>POST {url}/ HTTP/1.1\n" +
                            "Host: 192.0.2.11\n" +
                            "Content-Length: ...\n" +
                            "\n" +
                            "{DER encoding of the OCSPRequest}</code></pre>" +
                            "" +
                            "<p><strong>GET/POST Response</strong>" +
                            "<pre><code>{CertStatus returns only Good}</code></pre>" +
                            "</html>");
            }
        }

        public void doResponse(HttpServletResponse response, byte[] ocspRequest) throws IOException {
            byte[] resp = OCSPWrap.genOCSPRespEncoded(ocspRequest, issuerPrivateKey, issuerCert);
            response.setContentType("application/ocsp-response");
            response.setStatus(HttpURLConnection.HTTP_OK);
            response.setContentLength(resp.length);
            try (OutputStream os = response.getOutputStream()) {
                os.write(resp);
            }
        }

        public void doThrowable(HttpServletResponse response, Exception ex) throws IOException {
            StringBuilder errmsg = new StringBuilder();
            errmsg.append(ex.getMessage());
            errmsg.append(StringUtil.getStackTrace(ex));
            response.setContentType("text/plain");
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            response.setContentLength(errmsg.length());
            try (PrintStream os = new PrintStream(response.getOutputStream())) {
                os.println(errmsg.toString());
            }
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    public static Server createServer(int port) {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);
        return server;
    }

    private final static java.util.ResourceBundle RELEASE = java.util.ResourceBundle.getBundle("burp/resources/release");

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
        boolean debug = false;

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
                String[] param = arg.split("=", 2);
                if (param.length < 2) {
                    // single parameter
                    if ("-d".equals(arg)) {
                        debug = true;
                    }
                }
                else {
                    // multi parameter
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
            }

            // 必須チェック
            if (pkcs_ca == null || password == null) {
                System.out.println("-cafile or -password argument err ");
                usage();
                return;
            }

            // 存在チェック
            if (!(pkcs_ca.exists() && pkcs_ca.isFile())) {
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
                alias = CertUtil.getFirstAlias(ks);
            }

            PrivateKey issuerPrivateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
            X509Certificate issuerCert = (X509Certificate) ks.getCertificate(alias);

            SimpleJettyServer server = new SimpleJettyServer();
            server.setDebug(debug);
            server.startServer(issuerPrivateKey, issuerCert, defaultPort);
            server.joinServer();

        } catch (Exception ex) {
            String errmsg = String.format("%s: %s", ex.getClass().getName(), ex.getMessage());
            System.out.println(errmsg);
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            usage();
        }
    }
 
}
