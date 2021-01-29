package com.hazelcast.session;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PhoneHomeTest {

    @Test
    public void phoneHomeServiceUrlTest() {
        //given
        PhoneHomeInfo phoneHomeInfo = new PhoneHomeInfo("9", false, false, false, false);
        String expectedUrl = "http://phonehome.hazelcast.com/pingIntegrations/hazelcast-tomcat-sessionmanager";

        //when
        PhoneHomeService phoneHomeService = new PhoneHomeService(phoneHomeInfo);

        //then
        assertEquals(expectedUrl, phoneHomeService.getBaseUrl());
    }

    @Test
    public void queryStringTest() {
        //given
        int parametersSize = 6;
        String tomcatVersion = "7";
        boolean clientOnly = true;
        boolean sticky = false;
        boolean deferredWrite = false;
        boolean instanceNameDefault = false;
        PhoneHomeInfo phoneHomeInfo = new PhoneHomeInfo(tomcatVersion, clientOnly, sticky, deferredWrite, instanceNameDefault);

        //when
        String queryString = phoneHomeInfo.getQueryString();
        Map<String, String> parameters = toParametersMap(queryString);
        String resolvedPluginVersion = PhoneHomeInfo.resolveVersion();

        //then
        assertEquals(parametersSize, parameters.size());
        assertEquals(resolvedPluginVersion, parameters.get("version"));
        assertEquals(tomcatVersion, parameters.get("tomcat-version"));
        assertEquals(String.valueOf(clientOnly), parameters.get("client-only"));
        assertEquals(String.valueOf(sticky), parameters.get("sticky"));
        assertEquals(String.valueOf(deferredWrite), parameters.get("deferred-write"));
        assertEquals(String.valueOf(instanceNameDefault), parameters.get("instance-name-default"));
    }

    private Map<String, String> toParametersMap(String queryString) {
        Map<String, String> parametersMap = new HashMap<String, String>();
        String[] split = queryString.substring(1).split("&");
        for (String parameter : split) {
            String[] parameterSplit = parameter.split("=");
            parametersMap.put(parameterSplit[0], parameterSplit[1]);
        }
        return parametersMap;
    }


}
