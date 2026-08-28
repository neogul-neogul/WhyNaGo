package com.neogul.whynago.question.infra.ai.compare;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.time.Duration;

/**
 * 비교 대상 엔드포인트가 실제로 떠 있는지 미리 확인한다.
 * 로컬 ollama는 아직 설치·실행되지 않았을 수 있는데, 그 경우 모델마다 타임아웃을 기다리며 실패하는 대신
 * 테스트를 통째로 건너뛰게 하려고 확인한다.
 */
public class AiEndpointProbe {

    private static final int HTTPS_PORT = 443;
    private static final int HTTP_PORT = 80;

    public static boolean isReachable(String baseUrl, Duration timeout) {
        try (Socket socket = new Socket()) {
            URI uri = URI.create(baseUrl);
            socket.connect(new InetSocketAddress(uri.getHost(), port(uri)), (int) timeout.toMillis());
            return true;
        } catch (IOException | IllegalArgumentException e) {
            return false;
        }
    }

    private static int port(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? HTTPS_PORT : HTTP_PORT;
    }
}
