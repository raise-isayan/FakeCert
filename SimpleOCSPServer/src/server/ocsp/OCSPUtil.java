package server.ocsp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.BasicOCSPRespBuilder;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cert.ocsp.OCSPException;
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

/**
 *
 * @author isayan
 */
public class OCSPUtil {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    // final static AlgorithmIdentifier HASH_SHA256 = AlgorithmIdentifier.getInstance("2.16.840.1.101.3.4.2.1"); // SHA-256
    public static OCSPResp genOCSPResp(Req[] reqIDList, PrivateKey issuerPrivateKey, X509Certificate issuerCert) throws OCSPException {
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

    public static String getFirstAlias(KeyStore ks) throws KeyStoreException {
        String alias = null;
        // 最初にみつかったalias
        if (alias == null) {
            Enumeration<String> e = ks.aliases();
            while (e.hasMoreElements()) {
                alias = e.nextElement();
                break;
            }
        }
        return alias;
    }

    public static String getStackTrace(Throwable ex) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        ex.printStackTrace(printWriter);
        return result.toString();
    }

}
