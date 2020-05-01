package server.ocsp;

import extend.util.CertUtil;
import extend.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.Req;
import org.bouncycastle.util.encoders.Base64;
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
public class OCSPUtilTest {

    public OCSPUtilTest() {
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
    public void testGenOCSPResp() throws Exception {
        System.out.println("genOCSPResp");
        String caFileName = OCSPUtilTest.class.getResource("/resources/burpca.p12").getPath();
        String password = "testca";
        String reqFileName = OCSPUtilTest.class.getResource("/resources/req.der").getPath();

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(caFileName), password.toCharArray());

        String alias = CertUtil.getFirstAlias(ks);
        PrivateKey issuerPrivateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
        X509Certificate issuerCert = (X509Certificate) ks.getCertificate(alias);

        OCSPReq ocspReq = new OCSPReq(Util.bytesFromFile(new File(reqFileName)));
        Req[] reqIDList = ocspReq.getRequestList();

        assertEquals(1, reqIDList.length);
        assertEquals(new BigInteger("ED30220789C5A11D", 16), reqIDList[0].getCertID().getSerialNumber());

        OCSPResp resp = OCSPWrap.genOCSPResp(reqIDList, issuerPrivateKey, issuerCert);
        assertEquals(OCSPResp.SUCCESSFUL, resp.getStatus());

    }

    /**
     * Test of testDecodeOCSPUrl method, of class OCSPUtil.
     */
    @Test
    public void testDecodeOCSPUrl() throws Exception {
        System.out.println("testDecodeOCSPUrl");
        String reqFileName = OCSPUtilTest.class.getResource("../../resources/req.der").getPath();
        byte[] buff = Util.bytesFromFile(new File(reqFileName));
        String ocspURL = URLEncoder.encode(Base64.toBase64String(buff), "8859_1");
        System.out.println("ocspURL:" + ocspURL);
        assertEquals("MG8wbTBGMEQwQjAJBgUrDgMCGgUABBS8yTAf%2BzfS3dkE3w3iAWPnuQhToAQU%2FZW%2FipZVJr947vgLStV5ogOLEg4CCQDtMCIHicWhHaIjMCEwHwYJKwYBBQUHMAECBBIEEFHb1rA7VYnQLA6o0rruOps%3D", ocspURL);        
        byte[] decode = OCSPWrap.decodeOCSPUrl(ocspURL);
        assertArrayEquals(buff, decode);
    }

    /**
     * Test of getFirstAlias method, of class OCSPUtil.
     */
    @Test
    public void testGetURL() throws Exception {
        System.out.println("getURL");
        String uri = "/base64_encode";
        String[] paths = uri.split("/");
        assertEquals("base64_encode", paths[paths.length - 1]);    
    }

}
