package com.hazelcast.session.nonsticky.txsupport;

import com.hazelcast.session.nonsticky.Tomcat6ClientServerNonStickySessionsTest;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class Tomcat6TwoPhaseCommitClientServerNonStickySessionsTest extends Tomcat6ClientServerNonStickySessionsTest {

    @Override
    public String getWriteStrategy() {
        return "twoPhaseCommit";
    }
}
