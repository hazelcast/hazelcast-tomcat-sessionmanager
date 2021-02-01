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
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PhoneHomeService.class})
@PowerMockIgnore("javax.net.ssl.*")
public class PhoneHomeIntegrationTest {

    private WireMockServer wireMockServer = new WireMockServer();
    private PhoneHomeInfo info = new PhoneHomeInfo("9", true, false, false, false);

    @Before
    public void setup() {
        System.setProperty("hazelcast.phone.home.enabled", "true");
        wireMockServer.start();
    }

    @Test
    public void phoneHomeRequestTest() {
        PhoneHomeService service = new PhoneHomeService("http://127.0.0.1:" + wireMockServer.port() +
                "/tomcat-sessionmanager", info);
        service.start();

        // verify 5 retries
        WireMock.verify(5, getRequestedFor(urlEqualTo("/tomcat-sessionmanager" + info.getQueryString())));

        service.shutdown();
    }

    @Test
    public void phoneHomeServiceSingleStartTest() {
        //given
        PhoneHomeInfo phoneHomeInfo = new PhoneHomeInfo("9", true, false, false, false);
        PhoneHomeService phoneHomeService1 = new PhoneHomeService("http://127.0.0.1:" + wireMockServer.port() +
                "/tomcat-sessionmanager", phoneHomeInfo);
        PhoneHomeService phoneHomeService2 = new PhoneHomeService("http://127.0.0.1:" + wireMockServer.port() +
                "/tomcat-sessionmanager", phoneHomeInfo);

        //when
        phoneHomeService1.start();

        //then
        assertTrue(phoneHomeService2.isStarted());

        //cleanup
        phoneHomeService1.shutdown();
    }

    @After
    public void teardown() {
        System.setProperty("hazelcast.phone.home.enabled", "false");
        wireMockServer.stop();
    }

}