/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 */

package com.hazelcast.session;

import com.hazelcast.session.txsupport.DefaultMapQueryStrategy;
import com.hazelcast.session.txsupport.DefaultMapWriteStrategy;
import com.hazelcast.session.txsupport.MapQueryStrategy;
import com.hazelcast.session.txsupport.MapWriteStrategy;
import com.hazelcast.session.txsupport.OnePhaseCommitMapWriteStrategy;
import com.hazelcast.session.txsupport.TwoPhaseCommitMapWriteStrategy;
import org.apache.catalina.session.ManagerBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Abstract implementation of {@link SessionManager} containing common code for each implementation.
 */
public abstract class AbstractHazelcastSessionManager extends ManagerBase implements SessionManager {

    private final Log log = LogFactory.getLog(this.getClass());

    private String readStrategy = "default";
    private String writeStrategy = "default";
    private MapQueryStrategy mapQueryStrategy;
    private MapWriteStrategy mapWriteStrategy;


    /**
     * Configures and sets the hazelcast session map write strategy based on 'writeStrategy' setting
     *
     * @param mapName       the name of the hazelcast tomcat session map.
     * @param writeStrategy the writeStrategy setting value.
     */
    void configureWriteStrategy(String mapName, String writeStrategy) {
        log.info(String.format("Configuring session map for '%1$s' write strategy", writeStrategy));
        if ("twoPhaseCommit".equals(writeStrategy)) {
            this.mapWriteStrategy = new TwoPhaseCommitMapWriteStrategy(getHazelcastInstance(), mapName);
        } else if ("onePhaseCommit".equals(writeStrategy)) {
            this.mapWriteStrategy = new OnePhaseCommitMapWriteStrategy(getHazelcastInstance(), mapName);
        } else if ("default".equals(writeStrategy)) {
            this.mapWriteStrategy = new DefaultMapWriteStrategy(getDistributedMap());
        } else {
            log.info(String.format("'%1$s' writeStrategy is not supported - using 'default'", writeStrategy));
            this.mapWriteStrategy = new DefaultMapWriteStrategy(getDistributedMap());
        }
    }

    /**
     * Configures and sets the hazelcast session map read strategy based on 'readStrategy' setting
     *
     * @param readStrategy the readStrategy setting value.
     */
    void configureReadStrategy(String readStrategy) {
        log.info(String.format("Configuring session map for '%1$s' read strategy", readStrategy));
        if (!"default".equals(readStrategy)) {
            log.info(String.format("'%1$s' readStrategy is not supported - using 'default'", readStrategy));
        }
        this.mapQueryStrategy = new DefaultMapQueryStrategy(getDistributedMap());
    }

    public String getReadStrategy() {
        return readStrategy;
    }

    public void setReadStrategy(String readStrategy) {
        this.readStrategy = readStrategy;
    }

    public String getWriteStrategy() {
        return writeStrategy;
    }

    public void setWriteStrategy(String writeStrategy) {
        this.writeStrategy = writeStrategy;
    }

    @Override
    public MapQueryStrategy getMapQueryStrategy() {
        return mapQueryStrategy;
    }

    @Override
    public MapWriteStrategy getMapWriteStrategy() {
        return mapWriteStrategy;
    }

}
