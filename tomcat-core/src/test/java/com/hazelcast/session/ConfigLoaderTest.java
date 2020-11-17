package com.hazelcast.session;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.InvalidConfigurationException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ConfigLoaderTest {

    private P2PConfigLoader memberConfigLoader;
    private ClientServerConfigLoader clientConfigLoader;

    @Before
    public void setup()
            throws Exception {
        memberConfigLoader = new P2PConfigLoader();
        clientConfigLoader = new ClientServerConfigLoader();
    }

    @Test
    public void testLoad_withMemberXmlConfig() throws Exception {
        //given
        String path = "hazelcast.xml";
        //when
        Config config = memberConfigLoader.load(path);
        //then
        assertNotNull(config);
    }

    @Test(expected = InvalidConfigurationException.class)
    public void testLoad_invalidMemberXmlConfig() throws Exception {
        //given
        String path = "hazelcast-invalid.xml";
        //when
        memberConfigLoader.load(path);
        //then
        //expect com.hazelcast.config.InvalidConfigurationException
    }

    @Test
    public void testLoad_withMemberYamlConfig() throws Exception {
        //given
        String path = "test-hazelcast.yaml";
        //when
        Config config = memberConfigLoader.load(path);
        //then
        assertNotNull(config);
    }

    @Test(expected = InvalidConfigurationException.class)
    public void testLoad_invalidMemberYamlConfig() throws Exception {
        //given
        String path = "hazelcast-invalid.yaml";
        //when
        memberConfigLoader.load(path);
        //then
        //expect com.hazelcast.config.InvalidConfigurationException
    }

    @Test
    public void testLoad_withClientXmlConfig() throws Exception {
        //given
        String path = "hazelcast-client-default.xml";
        //when
        ClientConfig config = clientConfigLoader.load(path);
        //then
        assertNotNull(config);
    }

    @Test(expected = InvalidConfigurationException.class)
    public void testLoad_invalidClientXmlConfig() throws Exception {
        //given
        String path = "hazelcast-invalid.xml";
        //when
        clientConfigLoader.load(path);
        //then
        //expect com.hazelcast.config.InvalidConfigurationException
    }

    @Test
    public void testLoad_withClientYamlConfig() throws Exception {
        //given
        String path = "test-hazelcast-client.yaml";
        //when
        ClientConfig config = clientConfigLoader.load(path);
        //then
        assertNotNull(config);
    }

    @Test(expected = InvalidConfigurationException.class)
    public void testLoad_invalidClientYamlConfig() throws Exception {
        //given
        String path = "hazelcast-invalid.yaml";
        //when
        clientConfigLoader.load(path);
        //then
        //expect com.hazelcast.config.InvalidConfigurationException
    }
}
