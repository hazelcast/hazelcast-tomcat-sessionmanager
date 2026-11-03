package com.hazelcast.session.springboot;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.hazelcast.test.HazelcastTestSupport.assertTrueEventually;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractSpringBootConfigurationTest {
    protected ApplicationContext applicationContext;

    public abstract void setup();

    public abstract void clean();

    @Test
    public void testManagerCustomizerBean() {
        assertNotNull(applicationContext.getBean("hazelcastTomcatSessionManagerCustomizer"));
    }

    @Test
    public void testSessionMapCreated() {
        //given
        //the Spring Boot application is started
        //then
        HazelcastInstance hazelcastInstance = (HazelcastInstance) applicationContext.getBean("hazelcastInstance");
        LinkedList<DistributedObject> distributedObjects = (LinkedList<DistributedObject>) hazelcastInstance.getDistributedObjects();
        assertEquals("Session map should be created.", 1, distributedObjects.size());
    }

    @Test
    public void testSessionStoredInHazelcast() throws Exception {
        //given
        //the Spring Boot application is started
        HazelcastInstance hazelcastInstance = (HazelcastInstance) applicationContext.getBean("hazelcastInstance");

        //when
        CookieStore cookieStore = new BasicCookieStore();
        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        HttpGet request = new HttpGet("http://localhost:9999/set");
        client.execute(request);

        //then
        assertTrueEventually(() -> {
            List<DistributedObject> distributedObjects = new ArrayList<>(hazelcastInstance.getDistributedObjects());
            assertThat(distributedObjects).isNotEmpty();
            IMap<Object, Object> sessionMap = hazelcastInstance.getMap(distributedObjects.get(0).getName());
            assertEquals("Session should be created.",1, sessionMap.size());
        });
    }
}
