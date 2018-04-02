package com.hazelcast.session.txsupport;

import com.hazelcast.session.HazelcastSession;

/**
 * {@code MapQueryStrategy} defines a strategy for querying the hazelcast session map.
 */
public interface MapQueryStrategy {

    /**
     * Retrieve the {@link HazelcastSession} from the hazelcast session map by it's session id.
     *
     * @param sessionId the id of the session, aka session key within the hazelcast session map
     * @return the session with the specified id.
     */
    HazelcastSession getSession(String sessionId);
}
