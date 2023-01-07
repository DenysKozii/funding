package bot.binance;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

class ApiSignature {
    static final String op = "op";
    static final String opValue = "auth";
    private static final String signatureMethodValue = "HmacSHA256";
    public static final String signatureVersionValue = "2";

    ApiSignature() {
    }

    void createSignature(String accessKey, String secretKey, UrlParamsBuilder builder) {
        if (accessKey != null && !"".equals(accessKey) && secretKey != null && !"".equals(secretKey)) {
            builder.putToUrl("recvWindow", Long.toString(80000L)).putToUrl("timestamp", Long.toString(System.currentTimeMillis()));

            Mac hmacSha256;
            try {
                hmacSha256 = Mac.getInstance("HmacSHA256");
                SecretKeySpec secKey = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
                hmacSha256.init(secKey);
            } catch (NoSuchAlgorithmException var7) {
                throw new BinanceApiException("RuntimeError", "[Signature] No such algorithm: " + var7.getMessage());
            } catch (InvalidKeyException var8) {
                throw new BinanceApiException("RuntimeError", "[Signature] Invalid key: " + var8.getMessage());
            }

            String payload = builder.buildSignature();
            String actualSign = new String(Hex.encodeHex(hmacSha256.doFinal(payload.getBytes())));
            builder.putToUrl("signature", actualSign);
        } else {
            throw new BinanceApiException("KeyMissing", "API key and secret key are required");
        }
    }
}
