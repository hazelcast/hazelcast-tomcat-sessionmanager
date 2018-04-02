package com.hazelcast.session.txsupport;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.session.HazelcastSession;
import com.hazelcast.transaction.TransactionException;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionalTask;
import com.hazelcast.transaction.TransactionalTaskContext;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Implementation of {@link MapWriteStrategy} encapsulating write access to the Hazelcast Tomcat Session Map which
 * This implementation accesses the session map ({@link IMap}) directly, with all writes to the map using default
 * {@link TransactionOptions} providing Two Phase Commit Support across all nodes.
 *
 * @see HazelcastInstance#executeTransaction(TransactionOptions, TransactionalTask)
 * @see TransactionOptions.TransactionType#TWO_PHASE
 */
public class TwoPhaseCommitMapWriteStrategy implements MapWriteStrategy {

    private final Log log = LogFactory.getLog(TwoPhaseCommitMapWriteStrategy.class);
    private final HazelcastInstance hazelcastInstance;
    private final String sessionMapName;
    private final TransactionOptions transactionOptions;

    public TwoPhaseCommitMapWriteStrategy(HazelcastInstance hazelcastInstance, String sessionMapName) {
        this.hazelcastInstance = hazelcastInstance;
        this.sessionMapName = sessionMapName;
        transactionOptions = TransactionOptions.getDefault();
    }


    @Override
    public void setSession(final String sessionId, final HazelcastSession session) {
        hazelcastInstance.executeTransaction(transactionOptions, new TransactionalTask<Void>() {
            @Override
            public Void execute(TransactionalTaskContext context) throws TransactionException {
                log.info(String.format("Setting session '%1$s' in transactional context '%2$s'", sessionId, context.toString()));
                TransactionalMap<String, HazelcastSession> clusteredSessionMap = context.getMap(sessionMapName);
                clusteredSessionMap.set(sessionId, session);
                return null;
            }
        });
    }

    @Override
    public void removeSession(final String sessionId) {
        hazelcastInstance.executeTransaction(transactionOptions, new TransactionalTask<Void>() {
            @Override
            public Void execute(TransactionalTaskContext context) throws TransactionException {
                log.info(String.format("Removing session '%1$s' in transactional context '%2$s'", sessionId, context.toString()));
                TransactionalMap<String, HazelcastSession> clusteredSessionMap = context.getMap(sessionMapName);
                clusteredSessionMap.remove(sessionId);
                return null;
            }
        });
    }

    @Override
    public void removeAndSetSession(final String existingSessionId, final String sessionId, final HazelcastSession session) {
        hazelcastInstance.executeTransaction(transactionOptions, new TransactionalTask<Void>() {
            @Override
            public Void execute(TransactionalTaskContext context) throws TransactionException {

                TransactionalMap<String, HazelcastSession> clusteredSessionMap = context.getMap(sessionMapName);
                if (existingSessionId != null) {
                    log.info(String.format("Removing session '%1$s' and setting session '%2$s' in transactional context '%3$s'", sessionId, context.toString()));
                    clusteredSessionMap.remove(existingSessionId);
                } else {
                    log.info(String.format("Removing and setting session '%1$s' in transactional context '%2$s'", sessionId, context.toString()));
                    clusteredSessionMap.remove(sessionId);
                }
                clusteredSessionMap.set(sessionId, session);
                return null;
            }
        });
    }

    @Override
    public void removeAndSetSession(final String sessionId, final HazelcastSession session) {
        this.removeAndSetSession(null, sessionId, session);
    }
}
