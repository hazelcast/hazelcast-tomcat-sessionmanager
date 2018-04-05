/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 */

package com.hazelcast.session.txsupport;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.session.HazelcastSession;
import com.hazelcast.transaction.TransactionException;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionalTask;
import com.hazelcast.transaction.TransactionalTaskContext;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * {@code AbstractTransactionalMapWriteStrategy} is an abstract implementation containing
 * common implementations of {@link MapWriteStrategy}
 *
 * @see OnePhaseCommitMapWriteStrategy
 * @see TwoPhaseCommitMapWriteStrategy
 */
public abstract class AbstractTransactionalMapWriteStrategy implements MapWriteStrategy {
    private final HazelcastInstance hazelcastInstance;
    private final String sessionMapName;
    private final TransactionOptions transactionOptions;
    private final Log log = LogFactory.getLog(this.getClass());

    public AbstractTransactionalMapWriteStrategy(
            HazelcastInstance hazelcastInstance, String sessionMapName, TransactionOptions transactionOptions) {
        this.hazelcastInstance = hazelcastInstance;
        this.sessionMapName = sessionMapName;
        this.transactionOptions = transactionOptions;
    }

    @Override
    public final void setSession(final String sessionId, final HazelcastSession session) {
        hazelcastInstance.executeTransaction(transactionOptions, new TransactionalTask<Void>() {
            @Override
            public Void execute(TransactionalTaskContext context) throws TransactionException {
                log.debug(String.format("Setting session '%1$s'", sessionId));
                TransactionalMap<String, HazelcastSession> clusteredSessionMap = context.getMap(sessionMapName);
                clusteredSessionMap.set(sessionId, session);
                return null;
            }
        });
    }

    @Override
    public final void removeSession(final String sessionId) {
        hazelcastInstance.executeTransaction(transactionOptions, new TransactionalTask<Void>() {
            @Override
            public Void execute(TransactionalTaskContext context) throws TransactionException {
                log.debug(String.format("Removing session '%1$s'", sessionId));
                TransactionalMap<String, HazelcastSession> clusteredSessionMap = context.getMap(sessionMapName);
                clusteredSessionMap.remove(sessionId);
                return null;
            }
        });
    }

    @Override
    public final void removeAndSetSession(final String existingSessionId, final String sessionId,
                                          final HazelcastSession session) {
        hazelcastInstance.executeTransaction(transactionOptions, new TransactionalTask<Void>() {
            @Override
            public Void execute(TransactionalTaskContext context) throws TransactionException {

                TransactionalMap<String, HazelcastSession> clusteredSessionMap = context.getMap(sessionMapName);
                if (existingSessionId != null) {
                    log.debug(String.format("Removing session '%1$s' and setting session '%2$s'", existingSessionId, sessionId));
                    clusteredSessionMap.remove(existingSessionId);
                } else {
                    log.debug(String.format("Removing and replacing session '%1$s'", sessionId));
                    clusteredSessionMap.remove(sessionId);
                }
                clusteredSessionMap.set(sessionId, session);
                return null;
            }
        });
    }

    @Override
    public final void removeAndSetSession(final String sessionId, final HazelcastSession session) {
        this.removeAndSetSession(null, sessionId, session);
    }
}
