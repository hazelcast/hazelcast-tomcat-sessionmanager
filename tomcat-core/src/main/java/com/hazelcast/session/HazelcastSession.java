/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 */

package com.hazelcast.session;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.util.ExceptionUtil;
import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;

import java.io.IOException;
import java.lang.reflect.Field;
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

    private void updateSession() {
        if (sessionManager.isDeferredEnabled()) {
            dirty = true;
        } else {
            sessionManager.getDistributedMap().set(id, this);
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
        objectDataOutput.writeObject(getAttributes());
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
        setAttributes(objectDataInput.readObject());
        this.notes = objectDataInput.readObject();

        if (this.listeners == null) {
            this.listeners = new ArrayList();
        }
    }

    public Map getAttributes() {
        try {
            return (Map) getAttributesField().get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setAttributes(Object attributes) {
        try {
            getAttributesField().set(this, attributes);
        } catch (IllegalAccessException e) {
            ExceptionUtil.rethrow(e);
        }
    }

    /**
     * "attributes" field type is changed to ConcurrentMap with Tomcat 8.0.35+ and this causes NoSuchFieldException
     * if accessed directly. "attributes" is accessed through reflection to support Tomcat 8.0.35+
     */
    private Field getAttributesField() {
        try {
            Field attributesField = StandardSession.class.getDeclaredField("attributes");
            attributesField.setAccessible(true);
            return attributesField;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
