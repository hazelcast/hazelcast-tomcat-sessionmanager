/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 */

package com.hazelcast.session.txsupport;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.transaction.TransactionOptions;

/**
 * Implementation of {@link MapWriteStrategy} encapsulating write access to the Hazelcast Tomcat Session Map which
 * This implementation accesses the session map ({@link IMap}) directly, with all writes to the map using default
 * {@link TransactionOptions} providing One Phase Commit Support across all nodes.
 *
 * This implementation is configured with a timeout of 2 minutes, durability of 1 and a TransactionType.ONE_PHASE.
 *
 * @see HazelcastInstance#executeTransaction(TransactionOptions, com.hazelcast.transaction.TransactionalTask)
 * @see TransactionOptions.TransactionType#ONE_PHASE
 */
public final class OnePhaseCommitMapWriteStrategy extends AbstractTransactionalMapWriteStrategy {

    public OnePhaseCommitMapWriteStrategy(HazelcastInstance hazelcastInstance, String sessionMapName) {
        super(hazelcastInstance, sessionMapName, new TransactionOptions().setTransactionType(
                TransactionOptions.TransactionType.ONE_PHASE));
    }
}
