package com.hazelcast.session.txsupport;

import com.hazelcast.session.HazelcastSession;

/**
 * {@code MapWriteStrategy} defines a strategy for writing to the hazelcast tomcat session map.
 *
 */
public interface MapWriteStrategy {

    /**
     * Sets the {@Link HazelcastSession} into the session map with the specified sessionId as the map key.
     * @see com.hazelcast.core.IMap#set(Object, Object)
     * @param sessionId the key of the {@link HazelcastSession}.
     * @param session the session to set into the map
     */
    void setSession(String sessionId, HazelcastSession session);

    /**
     * Removes the object from the Hazelcast Tomcat Session Map at the specified key.
     * @see com.hazelcast.core.IMap#remove(Object)
     * @param sessionId the id of the {@link HazelcastSession} to remove from the session map.
     */
    void removeSession(String sessionId);

    /**
     * Remove and set the {@link HazelcastSession} with the specified sessionId.
     * If {@code existingSessionId} is not null, the {@link HazelcastSession} at key {@code existingSessionId}
     * will be removed. The intention of this is to trigger eviction listeners for each node.
     *
     * @param existingSessionId the id of the session to remove, or null.
     * @param sessionId the key of the {@link HazelcastSession}
     * @param session the session to set into the map.
     */
    void removeAndSetSession(String existingSessionId, String sessionId, HazelcastSession session);

    /**
     * Remove and set the {@link HazelcastSession} with the specified sessionId.
     * The {@link HazelcastSession} at key {@code sessionId}
     * will be removed and re-added. The intention of this is to trigger eviction listeners for each node.
     *
     * @param sessionId the key of the {@link HazelcastSession}
     * @param session the session to set into the map.
     */
    void removeAndSetSession(String sessionId, HazelcastSession session);
}
