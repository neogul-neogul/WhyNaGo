package com.neogul.whynago.auth.infra;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.neogul.whynago.auth.infra.dto.GoogleUserInfo;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GoogleIdTokenClient {

    private final GoogleIdTokenVerifier verifier;

    // setAudience를 빠뜨리면 다른 앱에서 발급된 id_token으로도 로그인이 통과한다
    public GoogleIdTokenClient(@Value("${google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(List.of(clientId))
                .build();
    }

    public Optional<GoogleUserInfo> verify(String idToken) {
        try {
            GoogleIdToken parsed = verifier.verify(idToken);
            if (parsed == null) {
                return Optional.empty();
            }
            GoogleIdToken.Payload payload = parsed.getPayload();
            return Optional.of(new GoogleUserInfo(
                    payload.getSubject(),
                    payload.getEmail(),
                    Boolean.TRUE.equals(payload.getEmailVerified())));
        } catch (IOException e) {
            // 구글 공개키 조회 실패. 토큰 자체는 멀쩡할 수 있으므로 원인을 남긴다
            log.warn("구글 id_token 검증 중 통신에 실패했습니다.", e);
            return Optional.empty();
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}