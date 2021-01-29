package com.hazelcast.session;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public abstract class AbstractPhoneHomeServiceTest
        extends AbstractHazelcastSessionsTest {

    PhoneHomeService phoneHomeService;

    @Before
    public void setUp() {
        phoneHomeService = mock(PhoneHomeService.class);
    }

    @Test
    public void phoneHomeServiceStartTest()
            throws Exception {
        //given
        instance1 = getWebContainerConfigurator().phoneHomeService(phoneHomeService);

        //when
        instance1.start();

        //then
        verify(phoneHomeService, times(1)).start();

        //cleanup
        instance1.stop();
    }

    @Test
    public void phoneHomeServiceStopTest()
            throws Exception {
        //given
        instance1 = getWebContainerConfigurator().phoneHomeService(phoneHomeService);
        instance1.start();

        //when
        instance1.stop();

        //then
        verify(phoneHomeService, times(1)).shutdown();
    }

    @After
    public void cleanup() {
        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }
}
