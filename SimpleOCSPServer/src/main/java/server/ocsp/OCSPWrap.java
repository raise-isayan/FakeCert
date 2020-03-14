package server.ocsp;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.BasicOCSPRespBuilder;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.OCSPRespBuilder;
import org.bouncycastle.cert.ocsp.Req;
import org.bouncycastle.cert.ocsp.RespID;
import org.bouncycastle.cert.ocsp.jcajce.JcaBasicOCSPRespBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.encoders.Base64;

/**
 *
 * @author isayan
 */
public class OCSPWrap {
    
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public OCSPWrap() {
    }
    
    public static byte [] decodeOCSPUrl(String path) {
        String url = URLDecoder.decode(path, StandardCharsets.ISO_8859_1);
        byte [] b64 = Base64.decode(url);
        return b64;
    }
    
    public static byte[] genOCSPRespEncoded(byte[] ocspRequest, PrivateKey issuerPrivateKey, X509Certificate issuerCert) throws IOException {
        try {
            OCSPResp resp = genOCSPResp(ocspRequest, issuerPrivateKey, issuerCert);
            return resp.getEncoded();
        } catch (OCSPException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
    }
        
    protected  static OCSPResp genOCSPResp(byte [] ocspRequest, PrivateKey issuerPrivateKey, X509Certificate issuerCert) throws OCSPException, IOException {
        OCSPReq ocspReq = new OCSPReq(ocspRequest);
        Req[] reqIDList = ocspReq.getRequestList();
        OCSPResp resp = genOCSPResp(reqIDList, issuerPrivateKey, issuerCert);
        return resp;
    }
        
    // final static AlgorithmIdentifier HASH_SHA256 = AlgorithmIdentifier.getInstance("2.16.840.1.101.3.4.2.1"); // SHA-256
    protected static OCSPResp genOCSPResp(Req[] reqIDList, PrivateKey issuerPrivateKey, X509Certificate issuerCert) throws OCSPException {
        OCSPResp ocspResp = null;
        try {
            X509CertificateHolder issuerCertHolder = new JcaX509CertificateHolder(issuerCert);
            DigestCalculatorProvider digCalcProv = new JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build();

            X509CertificateHolder[] chain = new X509CertificateHolder[]{issuerCertHolder};
            // RespID.HASH_SHA1 only
            BasicOCSPRespBuilder respGen = new JcaBasicOCSPRespBuilder(issuerCert.getPublicKey(), digCalcProv.get(RespID.HASH_SHA1));

            for (Req reqID : reqIDList) {
                respGen.addResponse(reqID.getCertID(), CertificateStatus.GOOD);
            }

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider(BouncyCastleProvider.PROVIDER_NAME).build(issuerPrivateKey);
            BasicOCSPResp resp = respGen.build(signer, chain, new Date());
            OCSPRespBuilder rGen = new OCSPRespBuilder();
            ocspResp = rGen.build(OCSPRespBuilder.SUCCESSFUL, resp);

        } catch (OperatorCreationException ex) {
            throw new OCSPException(ex.getMessage(), ex);
        } catch (CertificateEncodingException ex) {
            throw new OCSPException(ex.getMessage(), ex);
        }
        return ocspResp;
    }
       
}
