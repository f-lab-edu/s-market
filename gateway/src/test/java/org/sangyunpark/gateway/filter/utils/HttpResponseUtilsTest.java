package org.sangyunpark.gateway.filter.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sangyunpark.gateway.constant.code.ErrorCode;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class HttpResponseUtilsTest {

    private final HttpResponseUtils httpResponseUtils = new HttpResponseUtils();

    @Test
    @DisplayName("401 Unauthorized 응답이 생성된다")
    void unauthorizedResponseTest() {
        // given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/protected-resource")
        );

        // when
        StepVerifier.create(httpResponseUtils.unauthorized(exchange, ErrorCode.INVALID_TOKEN))
                .verifyComplete();

        // then
        var response = exchange.getResponse();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/json");

        // Body 내용 검증
        StepVerifier.create(response.getBody()
                        .map(dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            return new String(bytes, StandardCharsets.UTF_8);
                        }))
                .expectNext("{\"code\":\"INVALID_TOKEN\"}") // ErrorCode.INVALID_TOKEN의 code가 "A001"인 경우
                .verifyComplete();
    }
}