package com.hazelcast.session.nonsticky.txsupport;

import com.hazelcast.session.nonsticky.Tomcat85P2PNonStickySessionsTest;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class Tomcat85TwoPhaseCommitP2PNonStickySessionsTest extends Tomcat85P2PNonStickySessionsTest {

    @Override
    public String getWriteStrategy() {
        return "twoPhaseCommit";
    }
}