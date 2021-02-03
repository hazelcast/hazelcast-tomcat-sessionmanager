package com.hazelcast.session;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PhoneHomeService.class})
@PowerMockIgnore("javax.net.ssl.*")
public class PhoneHomeIntegrationTest {

    private WireMockServer wireMockServer = new WireMockServer();

    @Before
    public void setup() {
        System.setProperty("hazelcast.phone.home.enabled", "true");
        wireMockServer.start();
    }

    @Test
    public void phoneHomeRequestTest() {
        PhoneHomeService service = new PhoneHomeService("http://127.0.0.1:" + wireMockServer.port() +
                "/tomcat-sessionmanager");
        PhoneHomeInfo phoneHomeInfo = new PhoneHomeInfo("9", true, false, false, false);
        service.start(phoneHomeInfo);

        // verify 5 retries
        WireMock.verify(5, getRequestedFor(urlEqualTo("/tomcat-sessionmanager" + phoneHomeInfo.getQueryString())));

        service.shutdown();
    }

    @After
    public void teardown() {
        System.setProperty("hazelcast.phone.home.enabled", "false");
        wireMockServer.stop();
    }

}