package com.hazelcast.session;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.startup.Embedded;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

public class Tomcat6Configurator extends WebContainerConfigurator<Embedded> {

    private Embedded tomcat;
    private SessionManager manager;

    private static String DEFAULT_HOST = "localhost";

    private String appName;

    private final Log log = LogFactory.getLog(Tomcat6Configurator.class);

    public Tomcat6Configurator(String appName) {
        this.appName = appName;
    }

    public Tomcat6Configurator() {
        this.appName = "defaultApp";
    }

    @Override
    public Embedded configure() throws Exception {
        final URL root = new URL(Tomcat6Configurator.class.getResource("/"), "../../../tomcat-core/target/test-classes");
        final String cleanedRoot = URLDecoder.decode(root.getFile(), "UTF-8");

        final String docBase = cleanedRoot + File.separator + appName;

        MemoryRealm memoryRealm = new MemoryRealm();
        memoryRealm.setPathname(docBase + File.separator + "tomcat-users.xml");

        final Embedded catalina = new Embedded(memoryRealm);
        if (!clientOnly) {
            P2PLifecycleListener listener = new P2PLifecycleListener();
            listener.setConfigLocation(configLocation);
            catalina.addLifecycleListener(listener);
        } else {
            catalina.addLifecycleListener(new ClientServerLifecycleListener());
        }

        final StandardServer server = new StandardServer();
        server.addService(catalina);

        final Engine engine = catalina.createEngine();
        engine.setName("engine-" + port);
        engine.setDefaultHost(DEFAULT_HOST);
        engine.setJvmRoute("tomcat-" + port);

        catalina.addEngine(engine);
        engine.setService(catalina);

        final Host host = catalina.createHost(DEFAULT_HOST, docBase);
        engine.addChild(host);

        final Context context = createContext(catalina, "/", docBase);
        host.addChild(context);

        this.manager = new HazelcastSessionManager();
        context.setManager((HazelcastSessionManager) manager);
        updateManager((HazelcastSessionManager) manager);
        context.setBackgroundProcessorDelay(1);
        context.setCookies(true);

        final Connector connector = catalina.createConnector("localhost", port, false);
        connector.setProperty("bindOnInit", "false");
        catalina.addConnector(connector);

        return catalina;
    }

    @Override
    public void start() throws Exception {
        tomcat = configure();
        tomcat.start();
    }

    @Override
    public void stop() throws Exception {
        try {
            tomcat.stop();
        } catch (LifecycleException e) {
            log.warn("Failed to stop Tomcat. May be already stopped.");
        }
    }

    @Override
    public void reload() {
        Context ctx = (Context) tomcat.getContainer().findChild(DEFAULT_HOST).findChild("/");
        ctx.reload();
    }

    @Override
    public SessionManager getManager() {
        return manager;
    }

    private Context createContext(final Embedded catalina, final String contextPath, final String docBase) {
        return catalina.createContext(contextPath, docBase);
    }

    private void updateManager(HazelcastSessionManager manager) {
        manager.setSticky(sticky);
        manager.setClientOnly(clientOnly);
        manager.setMapName(mapName);
        manager.setMaxInactiveInterval(sessionTimeout);
        manager.setDeferredWrite(deferredWrite);
        manager.setProcessExpiresFrequency(1);
        manager.setReadStrategy(readStrategy);
        manager.setWriteStrategy(writeStrategy);
    }
}
