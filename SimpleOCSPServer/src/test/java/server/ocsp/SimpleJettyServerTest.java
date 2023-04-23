                            // https://bugs.openjdk.java.net/browse/JDK-8214516
package server.ocsp;

import extension.helpers.CertUtil;
import extension.helpers.FileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
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
public class SimpleJettyServerTest {
    private final static Logger logger = Logger.getLogger(SimpleJettyServerTest.class.getName());

    public SimpleJettyServerTest() {
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
     * Test of genOCSPResp method, of class OCSPUtil.
     */
    @Test
    public void testServerOCSPReq() throws Exception {
        System.out.println("serverOCSPReq");
        String caFileName = OCSPUtilTest.class.getResource("/resources/burpca.p12").getPath();
        String password = "testca";
        int defaultPort = 8765;

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(caFileName), password.toCharArray());

        String alias = CertUtil.getFirstAlias(ks);

        PrivateKey issuerPrivateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
        X509Certificate issuerCert = (X509Certificate) ks.getCertificate(alias);

        SimpleJettyServer server = new SimpleJettyServer();
        server.startServer(issuerPrivateKey, issuerCert, defaultPort);

        for (int i = 0; i < 10; i++) {
            if (server.isRunning()) {
                break;
            }
            Thread.sleep(1000);
        }
        if (!server.isRunning()) {
            fail();
        }
        else {

            HttpClient httpClient = new HttpClient();
            httpClient.setFollowRedirects(false);
            httpClient.start();

            String reqFileName = OCSPUtilTest.class.getResource("/resources/req.der").getPath();

            {
                // 疎通確認用
                // URLのTOPは例外で正常を返す
                URL url = new URL("http", "127.0.0.1", defaultPort, "/");
                Request request = httpClient.newRequest(url.toURI());
                ContentResponse response = request.send();
                int status = response.getStatus();
                assertEquals(HttpURLConnection.HTTP_OK, status);
            }
            {
                URL url = new URL("http", "127.0.0.1", defaultPort, "/MG8wbTBGMEQwQjAJBgUrDgMCGgUABBS8yTAf%2BzfS3dkE3w3iAWPnuQhToAQU%2FZW%2FipZVJr947vgLStV5ogOLEg4CCQDtMCIHicWhHaIjMCEwHwYJKwYBBQUHMAECBBIEEFHb1rA7VYnQLA6o0rruOps%3D");
                Request request = httpClient.newRequest(url.toURI());
                ContentResponse response  = request.send();
                int status = response.getStatus();
                assertEquals(status, HttpURLConnection.HTTP_OK);
                OCSPResp resp = new OCSPResp(response.getContent());
                assertEquals(OCSPResp.SUCCESSFUL, resp.getStatus());

                BasicOCSPResp basicOCSPResp = (BasicOCSPResp)resp.getResponseObject();
                SingleResp[] singleResps = basicOCSPResp.getResponses();
                for (SingleResp singleResp : singleResps) {
                    assertEquals(new BigInteger("ED30220789C5A11D", 16),singleResp.getCertID().getSerialNumber());
                    assertEquals(CertificateStatus.GOOD, singleResp.getCertStatus());
                }
            }
            {
                URL url = new URL("http", "127.0.0.1", defaultPort, "/");
                Request request = httpClient.POST(url.toURI());
                request.content(new BytesContentProvider(new byte[] {}));
                ContentResponse response  = request.send();
                int status = response.getStatus();
                assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, status);
            }
            {
                URL url = new URL("http", "127.0.0.1", defaultPort, "/");
                Request request = httpClient.POST(url.toURI());
                request.content(new BytesContentProvider(FileUtil.bytesFromFile(new File(reqFileName))));
                ContentResponse response  = request.send();
                int status = response.getStatus();
                assertEquals(status, HttpURLConnection.HTTP_OK);
                OCSPResp resp = new OCSPResp(response.getContent());
                assertEquals(OCSPResp.SUCCESSFUL, resp.getStatus());
                BasicOCSPResp basicOCSPResp = (BasicOCSPResp)resp.getResponseObject();
                SingleResp[] singleResps = basicOCSPResp.getResponses();
                for (SingleResp singleResp : singleResps) {
                    assertEquals(new BigInteger("ED30220789C5A11D", 16),singleResp.getCertID().getSerialNumber());
                    assertEquals(CertificateStatus.GOOD, singleResp.getCertStatus());
                }
            }

            httpClient.stop();

        }
        server.stopServer();
    }
}
