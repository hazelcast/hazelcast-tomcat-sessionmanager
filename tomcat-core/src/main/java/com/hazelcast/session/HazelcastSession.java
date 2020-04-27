/*
 * Copyright 2020 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.hazelcast.session;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.internal.util.ExceptionUtil;
import com.hazelcast.internal.util.MapUtil;
import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HazelcastSession extends StandardSession implements DataSerializable {

    private static final Field ATTRIBUTES_FIELD;
    private static final Log LOG = LogFactory.getLog(HazelcastSession.class);

    protected boolean dirty;

    private transient SessionManager sessionManager;


    static {
        ATTRIBUTES_FIELD = getAttributesField();
    }

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

    @Override
    public long getLastAccessedTime() {
        refreshAccessTimestamps();
        return super.getLastAccessedTime();
    }

    @Override
    public boolean isValid() {
        refreshAccessTimestamps();
        return super.isValid();
    }

    private void refreshAccessTimestamps() {
        if (this.sessionManager != null && !this.sessionManager.isSticky()) {
            HazelcastSession distributedSession = this.sessionManager.getDistributedMap().get(this.getId());
            if (distributedSession != null) {
                this.lastAccessedTime = distributedSession.lastAccessedTime;
                this.maxInactiveInterval = distributedSession.maxInactiveInterval;
                this.thisAccessedTime = distributedSession.thisAccessedTime;
            }
        }
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        super.setMaxInactiveInterval(interval);
        updateSessionWithMaxIdleSeconds(interval);
        if (LOG.isDebugEnabled()) {
            LOG.info("Session 'maxInactiveInterval' updated on Hazelcast cluster.");
        }
    }

    private void updateSessionWithMaxIdleSeconds(int interval) {
        sessionManager.getDistributedMap()
                      .set(id, this, -1, TimeUnit.MILLISECONDS, interval, TimeUnit.SECONDS);
    }

    void setMaxInactiveIntervalLocal(int interval) {
        super.setMaxInactiveInterval(interval);
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

        serializeMap(getAttributes(), objectDataOutput);
        serializeMap(notes, objectDataOutput);
    }

    private void serializeMap(Map map, ObjectDataOutput objectDataOutput) throws IOException {
        HashMap<Object, Object> serializableEntries = new HashMap<Object, Object>();
        for (Object entryObject : map.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObject;
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key != null && value != null) {
                serializableEntries.put(key, value);
            }
        }

        objectDataOutput.writeInt(serializableEntries.size());
        for (Map.Entry<Object, Object> entryObject : serializableEntries.entrySet()) {
            try {
                objectDataOutput.writeObject(entryObject.getKey());
                objectDataOutput.writeObject(entryObject.getValue());
            } catch (Exception e) {
                LOG.warn("Unable to serialize object in session", e);
            }
        }
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

        setAttributes(deserializeMap(objectDataInput, true));

        this.notes = deserializeMap(objectDataInput, false);

        if (this.listeners == null) {
            this.listeners = new ArrayList();
        }
    }

    private Map deserializeMap(ObjectDataInput objectDataInput, boolean concurrent) throws IOException {
        int mapSize = objectDataInput.readInt();
        Map map = concurrent ? new ConcurrentHashMap() : MapUtil.createHashMap(mapSize);
        for (int i = 0; i < mapSize; i++) {
            //noinspection unchecked
            try {
                map.put(objectDataInput.readObject(), objectDataInput.readObject());
            } catch (Exception ex) {
                LOG.warn("Unable to deserialize object in session", ex);
            }
        }
        return map;
    }

    public Map getAttributes() {
        try {
            return (Map) ATTRIBUTES_FIELD.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setAttributes(Object attributes) {
        try {
            ATTRIBUTES_FIELD.set(this, attributes);
        } catch (IllegalAccessException e) {
            ExceptionUtil.rethrow(e);
        }
    }

    /**
     * "attributes" field type is changed to ConcurrentMap with Tomcat 8.0.35+ and this causes NoSuchFieldException
     * if accessed directly. "attributes" is accessed through reflection to support Tomcat 8.0.35+
     */
    private static Field getAttributesField() {
        try {
            Field attributesField = StandardSession.class.getDeclaredField("attributes");
            attributesField.setAccessible(true);
            return attributesField;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
