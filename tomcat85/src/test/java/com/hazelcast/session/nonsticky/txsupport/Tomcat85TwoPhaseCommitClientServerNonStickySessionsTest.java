package com.hazelcast.session.nonsticky.txsupport;

import com.hazelcast.session.nonsticky.Tomcat85ClientServerNonStickySessionsTest;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class Tomcat85TwoPhaseCommitClientServerNonStickySessionsTest extends Tomcat85ClientServerNonStickySessionsTest {

    @Override
    public String getWriteStrategy() {
        return "twoPhaseCommit";
    }
}
