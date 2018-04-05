package com.hazelcast.session.sticky.txsupport;

import com.hazelcast.session.sticky.Tomcat6P2PStickySessionsTest;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class Tomcat6OnePhaseCommitP2PStickySessionsTest extends Tomcat6P2PStickySessionsTest {

    @Override
    public String getWriteStrategy() {
        return "onePhaseCommit";
    }
}
