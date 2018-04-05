package com.hazelcast.session.nonsticky.txsupport;

import com.hazelcast.session.nonsticky.Tomcat7P2PNonStickySessionsTest;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class Tomcat7TwoPhaseCommitP2PNonStickySessionsTest extends Tomcat7P2PNonStickySessionsTest {

    @Override
    public String getWriteStrategy() {
        return "twoPhaseCommit";
    }
}
