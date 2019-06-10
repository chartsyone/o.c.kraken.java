package one.chartsy.data.providers.kraken;

import java.io.IOException;
import java.net.ConnectException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import one.chartsy.Symbol;
import one.chartsy.SymbolNotFoundException;
import one.chartsy.TimeFrame;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


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
        final ZonedDateTime ACTUAL_TIME = ZonedDateTime.of( // from Time.json
                LocalDateTime.of(2019, 6, 10, 20, 31, 5), ZoneOffset.UTC);
        
        givenThat(get(urlEqualTo("/public/Time")).willReturn(aJsonResponse()
                .withBodyFile("public/Time.json")
        ));
        
        ZonedDateTime serverTime = service.getServerTime();
        assertEquals(ACTUAL_TIME, serverTime,
                "<SERVER TIME> differs from expected <ACTUAL TIME>");
    }
    
    @Test
    void getBaseTimeFrame_gives_minute_resolution_for_any_symbol() {
        final TimeFrame MINUTE_RESOLUTION = TimeFrame.Period.M1;
        final Symbol ANY_SYMBOL = new Symbol("<any symbol>");
        
        TimeFrame baseTimeFrame = service.getBaseTimeFrame(ANY_SYMBOL);
        assertEquals(MINUTE_RESOLUTION, baseTimeFrame);
    }

    private static void givenAvailableAssetPairs() {
        givenThat(get(urlEqualTo("/public/AssetPairs")).willReturn(aJsonResponse()
                .withBodyFile("public/AssetPairs.json")
        ));
    }
    
    @Test
    void getSymbol_gives_available_symbol() throws IOException, InterruptedException {
        givenAvailableAssetPairs();
        
        Symbol symbol = service.getSymbol("XXBTZUSD");
        assertEquals("XXBTZUSD", symbol.getRefIdAsString(), "symbol.refId");
        assertEquals("XBTUSD", symbol.getName(), "symbol.name");
    }
    
    @Test
    void getSymbol_throws_SymbolNotFoundException_for_unavailable_symbol() {
        givenAvailableAssetPairs();
        
        assertThrows(SymbolNotFoundException.class, () -> service.getSymbol("<Unavailable Symbol>"));
    }

    private void givenUnavailableApiEndpoint() {
        mockServer.stop();
    }
    
    @Test
    void throws_ConnectException_when_api_not_available() throws IOException {
        givenUnavailableApiEndpoint();
        
        String any = "<any>";
        assertThrows(ConnectException.class, () -> service.getServerTime());
        assertThrows(ConnectException.class, () -> service.getSymbol(any));
        assertThrows(ConnectException.class, () -> service.getSymbolInformation(any));
    }
}
