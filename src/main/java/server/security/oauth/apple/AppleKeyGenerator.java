package server.security.oauth.apple;

import server.common.CustomException;
import server.common.ErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppleKeyGenerator {

    @Value("${apple.client-id}")
    private String bundleId;

    @Value("${apple.team-id}")
    private String appleTeamId;

    @Value("${apple.key-id}")
    private String appleKeyId;

    @Value("${apple.private-key}")
    private String privateKey;

    public String createClientSecret() {
        Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .setHeaderParam("kid", appleKeyId)
                .setHeaderParam("alg", "ES256")
                .setIssuer(appleTeamId)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .setAudience("https://appleid.apple.com")
                .setSubject(bundleId)
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();
    }

    private PrivateKey getPrivateKey() {
        try {
            String privateKeyString = privateKey.replace("\\n", "\n");
            Reader pemReader = new StringReader(privateKeyString);
            PEMParser pemParser = new PEMParser(pemReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
            return converter.getPrivateKey(object);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.APPLE_INVALID_PRIVATE_KEY);
        }
    }
    }
