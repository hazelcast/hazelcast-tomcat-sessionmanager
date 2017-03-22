# Table of Contents

* [Tomcat Based Web Session Replication](#tomcat-based-web-session-replication)
* [Features and Requirements](#features-and-requirements)
* [How Tomcat Session Replication Works](#how-tomcat-session-replication-works)
  * [Deploying P2P for Tomcat](#deploying-p2p-for-tomcat)
  * [Deploying Client-Server for Tomcat](#deploying-client-server-for-tomcat)
* [Configuring Manager Element for Tomcat](#configuring-manager-element-for-tomcat)
* [Controlling Session Caching with deferredWrite](#controlling-session-caching-with-deferredWrite)
* [Setting Session Expiration Checks](#setting-session-expiration-checks)
* [Enabling Session Replication in Multi-App Environments](#enabling-session-replication-in-multi-app-environments)
* [Sticky Sessions and Tomcat](#sticky-sessions-and-tomcat)
* [Tomcat Failover and the jvmRoute Parameter](#tomcatfailover-and-the-jvmroute-parameter)


# Tomcat Based Web Session Replication

***Sample Code:*** *Please see our <a href="https://github.com/hazelcast/hazelcast-code-samples/tree/master/hazelcast-integration/manager-based-session-replication" target="_blank">sample application</a> for Tomcat Based Web Session Replication.*


# Features and Requirements

<a href="https://github.com/hazelcast/hazelcast-tomcat-sessionmanager" target="_blank">Hazelcast Tomcat Session Manager</a> is a container specific module that enables session replication for JEE Web Applications without requiring changes to the application.


***Features***

- Seamless Tomcat 6, 7 & 8 integration.
- Support for sticky and non-sticky sessions.
- Tomcat failover.
- Deferred write for performance boost.
<br></br>

***Supported Containers***

Tomcat Web Session Replication Module has been tested against the following containers.

- Tomcat 6.0.x - It can be downloaded <a href="http://tomcat.apache.org/download-60.cgi" target="_blank">here</a>.
- Tomcat 7.0.x - It can be downloaded <a href="http://tomcat.apache.org/download-70.cgi" target="_blank">here</a>.
- Tomcat 8.0.x - It can be downloaded <a href="http://tomcat.apache.org/download-80.cgi" target="_blank">here</a>.

The latest tested versions are **6.0.39**, **7.0.40** and **8.0.36**.
<br></br>

***Requirements***

 - Tomcat instance must be running with Java 1.6 or higher.
 - Session objects that need to be clustered have to be Serializable.

# How Tomcat Session Replication Works

Hazelcast Tomcat Session Manager is a Hazelcast Module where each created `HttpSession` Object is kept in the Hazelcast Distributed Map. If configured with Sticky Sessions, each Tomcat Instance has its own local copy of the session for performance boost. 

Since the sessions are in Hazelcast Distributed Map, you can use all the available features offered by Hazelcast Distributed Map implementation, such as MapStore and WAN Replication.

Tomcat Web Sessions run in two different modes:

- **P2P**: all Tomcat instances launch its own Hazelcast Instance and join to the Hazelcast Cluster and,
- **Client/Server**: all Tomcat instances put/retrieve the session data to/from an existing Hazelcast Cluster.

## Deploying P2P for Tomcat

P2P deployment launches an embedded Hazelcast member in each server instance.

This type of deployment is simple: just configure your Tomcat and launch. There is no need for an external Hazelcast cluster.

The following steps configure a sample P2P for Hazelcast Session Replication.

1. Go to <a href="http://www.hazelcast.org/" target="_blank">hazelcast.org</a> and download the latest Hazelcast.
2. Unzip the Hazelcast zip file into the folder `$HAZELCAST_ROOT`.
3. Go to <a href="https://github.com/hazelcast/hazelcast-tomcat-sessionmanager/releases" target="_blank">hazelcast-tomcat-sessionmanager</a> repository and download the latest version.
4. Put `$HAZELCAST_ROOT/lib/hazelcast-all-`<*version*>`.jar`,   and `hazelcast-tomcat`<*tomcatversion*>`-sessionmanager-`<*version*>`.jar` and `hazelcast.xml` (if you want to change defaults) in the folder `$CATALINA_HOME/lib/`.

5. Put a `<Listener>` element into the file `$CATALINA_HOME$/conf/server.xml` as shown below.

 ```xml
        <Server>
        	...
            <Listener className="com.hazelcast.session.P2PLifecycleListener"/>
            ...
        </Server>
```

6. Put a `<Manager>` element into the file `$CATALINA_HOME$/conf/context.xml` as shown below.

 ```xml
        <Context>
        	...
            <Manager className="com.hazelcast.session.HazelcastSessionManager"/>
            ...
        </Context>
 ```

7. Start Tomcat instances with a configured load balancer and deploy the web application.

***Optional Attributes for Listener Element***

Optionally, you can add a `configLocation` attribute into the `<Listener>` element. If not provided, `hazelcast.xml` in the classpath is used by default. URL or full filesystem path as a `configLocation` value is supported.

## Deploying Client-Server for Tomcat

In this deployment type, Tomcat instances work as clients on an existing Hazelcast Cluster.

***Features***

-	The existing Hazelcast cluster is used as the Session Replication Cluster.
-	Offloading Session Cache from Tomcat to the Hazelcast Cluster.
-	The architecture is completely independent. Complete reboot of Tomcat instances.
<br></br>

The following steps configure a sample Client/Server for Hazelcast Session Replication.

1. Go to <a href="http://www.hazelcast.org/" target="_blank">hazelcast.org</a> and download the latest Hazelcast.
2. Unzip the Hazelcast zip file into the folder `$HAZELCAST_ROOT`.
3. Go to <a href="https://github.com/hazelcast/hazelcast-tomcat-sessionmanager/releases" target="_blank">hazelcast-tomcat-sessionmanager</a> repository and download the latest version.
4. Put `$HAZELCAST_ROOT/lib/hazelcast-all-`<*version*>`.jar`,   and `hazelcast-tomcat`<*tomcatversion*>`-sessionmanager-`<*version*>`.jar` and `hazelcast.xml` (if you want to change defaults) in the folder `$CATALINA_HOME/lib/`.

5. Put a `<Listener>` element into the `$CATALINA_HOME$/conf/server.xml` as shown below.

 ```xml
        <Server>
        	...
            <Listener className="com.hazelcast.session.ClientServerLifecycleListener"/>
            ...
        </Server>
 ```

6. Update the `<Manager>` element in the `$CATALINA_HOME$/conf/context.xml` as shown below.

 ```xml
        <Context>
             <Manager className="com.hazelcast.session.HazelcastSessionManager"
              clientOnly="true"/>
        </Context>
 ```

7. Launch a Hazelcast Instance using `$HAZELCAST_ROOT/bin/server.sh` or `$HAZELCAST_ROOT/bin/server.bat`.

7. Start Tomcat instances with a configured load balancer and deploy the web application.

***Optional Attributes for Listener Element***

Optionally, you can add `configLocation` attribute into the `<Listener>` element. If not provided, `hazelcast-client-default.xml` in `hazelcast-client-`<*version*>`.jar` file is used by default. Any client XML file in the classpath, URL or full filesystem path as a `configLocation` value is also supported.

An example client config that connects directly (i.e. doesn't use multicast) to a specified cluster is:

```
<?xml version="1.0" encoding="UTF-8"?>
<hazelcast-client xsi:schemaLocation="http://www.hazelcast.com/schema/client-config hazelcast-client-config-3.9.xsd"
                  xmlns="http://www.hazelcast.com/schema/client-config"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <network>
      <cluster-members>
        <address>HAZELCAST_MEMBER</address>
      </cluster-members>
    </network>
</hazelcast-client>
```

# Configuring Manager Element for Tomcat

`<Manager>` element is used both in P2P and Client/Server mode. You can use the following attributes to configure Tomcat Session Replication Module to better serve your needs.

- Add `mapName` attribute into `<Manager>` element. Its default value is *default Hazelcast Distributed Map*. Use this attribute if you have a specially configured map for special cases like WAN Replication, Eviction, MapStore, etc.
- Add `sticky` attribute into `<Manager>` element. Its default value is *true*.
- Add `processExpiresFrequency` attribute into `<Manager>` element. It specifies the frequency of session validity check, in seconds. Its default value is *6* and the minimum value that you can set is *1*.
- Add `deferredWrite` attribute into `<Manager>` element. Its default value is *true*.

<br></br>

# Controlling Session Caching with deferredWrite

Tomcat Web Session Replication Module has its own nature of caching. Attribute changes during the HTTP Request/HTTP Response cycle is cached by default. Distributing those changes to the Hazelcast Cluster is costly. Because of that, Session Replication is only done at the end of each request for updated and deleted attributes. The risk in this approach is losing data if a Tomcat crash happens in the middle of the HTTP Request operation.

You can change that behavior by setting `deferredWrite=false` in your `<Manager>` element. By disabling it, all updates that are done on session objects are directly distributed into Hazelcast Cluster.

# Setting Session Expiration Checks

Based on Tomcat configuration or `sessionTimeout` setting in `web.xml`, sessions are expired over time. This requires a cleanup on the Hazelcast Cluster since there is no need to keep expired sessions in the cluster. 

`processExpiresFrequency`, which is defined in [`<Manager>`](#configuring-manager-element-for-tomcat), is the only setting that controls the behavior of session expiry policy in the Tomcat Web Session Replication Module. By setting this, you can set the frequency of the session expiration checks in the Tomcat Instance.

# Enabling Session Replication in Multi-App Environments

Tomcat can be configured in two ways to enable Session Replication for deployed applications.

- Server Context.xml Configuration
- Application Context.xml Configuration

***Server Context.xml Configuration***

By configuring `$CATALINA_HOME$/conf/context.xml`, you can enable session replication for all applications deployed in the Tomcat Instance. 


***Application Context.xml Configuration***

By configuring `$CATALINA_HOME/conf/[enginename]/[hostname]/[applicationName].xml`, you can enable Session Replication per deployed application.

# Sticky Sessions and Tomcat

***Sticky Sessions (default)***

Sticky Sessions are used to improve the performance since the sessions do not move around the cluster.
 
Requests always go to the same instance where the session was firstly created. By using a sticky session, you mostly eliminate session replication problems, except for the failover cases. In case of failovers, Hazelcast helps you to not lose existing sessions.


***Non-Sticky Sessions***

Non-Sticky Sessions are not good for performance because you need to move session data all over the cluster every time a new request comes in.

However, load balancing might be super easy with Non-Sticky caches. In case of heavy load, you can distribute the request to the least used Tomcat instance. Hazelcast supports Non-Sticky Sessions as well. 

# Tomcat Failover and the jvmRoute Parameter

Each HTTP Request is redirected to the same Tomcat instance if sticky sessions are enabled. The parameter `jvmRoute` is added to the end of session ID as a suffix, to make Load Balancer aware of the target Tomcat instance. 

When Tomcat Failure happens and Load Balancer cannot redirect the request to the owning instance, it sends a request to one of the available Tomcat instances. Since the `jvmRoute` parameter of session ID is different than that of the target Tomcat instance, Hazelcast Session Replication Module updates the session ID of the session with the new `jvmRoute` parameter. That means that the Session is moved to another Tomcat instance and Load Balancer will redirect all subsequent HTTP Requests to the new Tomcat Instance.

![image](images/NoteSmall.jpg) ***NOTE:*** *If stickySession is enabled, `jvmRoute` parameter must be set in `$CATALINA_HOME$/conf/server.xml` and unique among Tomcat instances in the cluster.*

```xml
 <Engine name="Catalina" defaultHost="localhost" jvmRoute="tomcat-8080">
```

# License
Hazelcast Tomcat Session Manager is available under the Apache 2 License. 
