package com.hazelcast.session.sticky;

import com.hazelcast.session.TomcatConfigurator;
import com.hazelcast.session.WebContainerConfigurator;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class TomcatClientServerStickySessionsTest extends AbstractClientServerStickySessionsTest {

    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new TomcatConfigurator();
    }
}
