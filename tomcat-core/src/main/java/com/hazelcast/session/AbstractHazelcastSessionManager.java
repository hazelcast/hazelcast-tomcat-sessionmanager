package com.hazelcast.session;

import com.hazelcast.session.txsupport.*;
import org.apache.catalina.session.ManagerBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Abstract implementation of {@link SessionManager} containing common code for each implementation.
 */
public abstract class AbstractHazelcastSessionManager extends ManagerBase implements SessionManager {

    private final Log log = LogFactory.getLog(this.getClass());

    protected String readStrategy = "default";
    protected String writeStrategy = "default";
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
            setMapWriteStrategy(new TwoPhaseCommitMapWriteStrategy(getHazelcastInstance(), mapName));
        } else if ("default".equals(writeStrategy)) {
            setMapWriteStrategy(new DefaultMapWriteStrategy(getDistributedMap()));
        } else {
            log.info(String.format("'%1$s' writeStrategy is not supported - using 'default'", writeStrategy));
            setMapWriteStrategy(new DefaultMapWriteStrategy(getDistributedMap()));
        }
    }

    /**
     * Configures and sets the hazelcast session map read strategy based on 'readStrategy' setting
     *
     * @param mapName      the name of the hazelcast tomcat session map.
     * @param readStrategy the readStrategy setting value.
     */
    void configureReadStrategy(String mapName, String readStrategy) {
        log.info(String.format("Configuring session map for '%1$s' read strategy", readStrategy));
        if (!"default".equals(readStrategy)) {
            log.info(String.format("'%1$s' readStrategy is not supported - using 'default'", readStrategy));
        }
        setMapQueryStrategy(new DefaultMapQueryStrategy(getDistributedMap()));
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

    public void setMapQueryStrategy(MapQueryStrategy mapQueryStrategy) {
        this.mapQueryStrategy = mapQueryStrategy;
    }

    public void setMapWriteStrategy(MapWriteStrategy mapWriteStrategy) {
        this.mapWriteStrategy = mapWriteStrategy;
    }
}
