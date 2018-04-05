package com.hazelcast.session.sticky.txsupport;

import com.hazelcast.session.sticky.Tomcat85ClientServerStickySessionsTest;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class Tomcat85TwoPhaseCommitClientServerStickySessionsTest extends Tomcat85ClientServerStickySessionsTest {

    @Override
    public String getWriteStrategy() {
        return "twoPhaseCommit";
    }
}
