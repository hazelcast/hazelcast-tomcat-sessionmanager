package com.hazelcast.session;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.test.HazelcastTestSupport;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;

/**
 * Created by mesutcelik on 6/12/14.
 */
public abstract class AbstractHazelcastSessionsTest extends HazelcastTestSupport{

    protected static int SERVER_PORT_1 = 8899;
    protected static int SERVER_PORT_2 = 8999;
    protected static String SESSION_REPLICATION_MAP_NAME = "session-replication-map";


    protected WebContainerConfigurator<?> instance1;
    protected WebContainerConfigurator<?> instance2;


    protected abstract WebContainerConfigurator<?> getWebContainerConfigurator();

    @After
    public void cleanup() throws Exception{
        instance1.stop();
        instance2.stop();
        Hazelcast.shutdownAll();
    }

    protected String executeRequest(String context, int serverPort, CookieStore cookieStore) throws Exception {
        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        HttpGet request = new HttpGet("http://localhost:" + serverPort + "/" + context);
        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }


}
