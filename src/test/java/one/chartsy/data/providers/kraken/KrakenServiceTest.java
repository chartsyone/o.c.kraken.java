package one.chartsy.data.providers.kraken;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class KrakenServiceTest {
    
    private WireMockServer mockServer = new WireMockServer(
            WireMockConfiguration.options().usingFilesUnderDirectory("src/test/resources/kraken.com"));
    private KrakenService service;
    
    @BeforeEach
    void startMockServer() throws Exception {
        mockServer.start();
    }
    
    @AfterEach
    void stopMockServer() {
        mockServer.stop();
    }

    @BeforeEach
    void launchService() throws Exception {
        service = new KrakenService(mockServer.baseUrl());
    }
    
    @AfterEach
    void destroyService() throws IOException {
        service.close();
    }

    private static ResponseDefinitionBuilder aJsonResponse() {
        return aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json;charset=UTF-8");
    }
    
    @Test
    void getServerTime_gives_actual_time_on_server() throws Exception {
        ZonedDateTime actualTime = ZonedDateTime.of( //from Time.json
                LocalDateTime.of(2019, 6, 10, 20, 31, 5), ZoneOffset.UTC);
        
        givenThat(get(urlEqualTo("/public/Time")).willReturn(aJsonResponse()
                .withBodyFile("public/Time.json")
        ));
        
        ZonedDateTime serverTime = service.getServerTime();
        assertEquals(actualTime, serverTime,
                "<SERVER TIME> differs from expected <ACTUAL TIME>");
    }
}
