/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 */

package com.hazelcast.session;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Map;

public class HazelcastSession extends StandardSession implements DataSerializable {

    protected boolean dirty;

    private transient SessionManager sessionManager;

    public HazelcastSession(SessionManager sessionManager) {
        super((Manager) sessionManager);
        this.sessionManager = sessionManager;
    }

    public HazelcastSession() {
        super(null);
    }

    @Override
    public void setAttribute(String key, Object value) {
        super.setAttribute(key, value);
        updateSession();
    }

    @Override
    public void removeAttribute(String name) {
        super.removeAttribute(name);
        updateSession();
    }

    @Override
    public void setPrincipal(Principal principal) {
        super.setPrincipal(principal);
        updateSession();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setSessionManager(SessionManager sessionManager) {
        super.setManager((Manager) sessionManager);
        this.sessionManager = sessionManager;
    }

    public Map getAttributes() {
        return attributes;
    }

    private void updateSession() {
        if (sessionManager.isDeferredEnabled()) {
            dirty = true;
        } else {
            sessionManager.getDistributedMap().put(id, this);
        }
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeLong(creationTime);
        objectDataOutput.writeLong(lastAccessedTime);
        objectDataOutput.writeInt(maxInactiveInterval);
        objectDataOutput.writeBoolean(isNew);
        objectDataOutput.writeBoolean(isValid);
        objectDataOutput.writeLong(thisAccessedTime);
        objectDataOutput.writeObject(id);
        objectDataOutput.writeObject(attributes);
        objectDataOutput.writeObject(notes);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        this.creationTime = objectDataInput.readLong();
        this.lastAccessedTime = objectDataInput.readLong();
        this.maxInactiveInterval = objectDataInput.readInt();
        this.isNew = objectDataInput.readBoolean();
        this.isValid = objectDataInput.readBoolean();
        this.thisAccessedTime = objectDataInput.readLong();
        this.id = objectDataInput.readObject();
        this.attributes = objectDataInput.readObject();
        this.notes = objectDataInput.readObject();

        if (this.listeners == null) {
            this.listeners = new ArrayList();
        }
    }
}
