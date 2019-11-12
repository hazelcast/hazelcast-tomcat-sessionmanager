/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 */

package com.hazelcast.session;

public class CustomAttribute {
    private String value;

    //Needed for Kryo
    public CustomAttribute() {
    }

    public CustomAttribute(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "CustomAttribute{" + "value='" + value + '\'' + '}';
    }
}
