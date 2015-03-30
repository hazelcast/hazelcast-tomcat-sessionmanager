package com.hazelcast.session;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Embedded;

import java.io.File;
import java.net.URL;

/**
 * Created by mesutcelik on 6/12/14.
 */
public class Tomcat6Configurator extends WebContainerConfigurator<Embedded> {

    private Embedded tomcat;
    private SessionManager manager;

    private static String DEFAULT_HOST = "localhost";

    private String p2pConfigLocation;
    private String clientServerConfigLocation;

    public Tomcat6Configurator(String p2pConfigLocation, String clientServerConfigLocation) {
        super();
        this.p2pConfigLocation = p2pConfigLocation;
        this.clientServerConfigLocation = clientServerConfigLocation;
    }

    public Tomcat6Configurator() {
        super();
        this.clientServerConfigLocation = "hazelcast-client-with-valid-license.xml";
        this.p2pConfigLocation = "hazelcast-with-valid-license.xml";
    }

    @Override
    public Embedded configure() throws Exception {
        final Embedded catalina = new Embedded();
        if (!clientOnly) {
            P2PLifecycleListener p2PLifecycleListener = new P2PLifecycleListener();
            p2PLifecycleListener.setConfigLocation(p2pConfigLocation);
            catalina.addLifecycleListener(p2PLifecycleListener);
        } else {
            ClientServerLifecycleListener clientServerLifecycleListener = new ClientServerLifecycleListener();
            clientServerLifecycleListener.setConfigLocation(clientServerConfigLocation);
            catalina.addLifecycleListener(clientServerLifecycleListener);
        }

        final StandardServer server = new StandardServer();
        server.addService(catalina);

        final URL root = new URL(Tomcat6Configurator.class.getResource("/"), "../test-classes");
        // use file to get correct separator char, replace %20 introduced by URL for spaces
        final String cleanedRoot = new File(root.getFile().replaceAll("%20", " ")).toString();

        final String fileSeparator = File.separator.equals("\\") ? "\\\\" : File.separator;
        final String docBase = cleanedRoot + File.separator + Tomcat6Configurator.class.getPackage().getName().replaceAll("\\.", fileSeparator);

        final Engine engine = catalina.createEngine();
        engine.setName("engine-" + port);
        engine.setDefaultHost(DEFAULT_HOST);
        engine.setJvmRoute("tomcat-" + port);

        catalina.addEngine(engine);
        engine.setService(catalina);

        final Host host = catalina.createHost(DEFAULT_HOST, docBase);
        engine.addChild(host);

        final Context context = createContext(catalina, "/", "webapp");
        host.addChild(context);

        this.manager = new HazelcastSessionManager();
        context.setManager((HazelcastSessionManager)manager);
        updateManager((HazelcastSessionManager)manager);
        context.setBackgroundProcessorDelay(1);
        context.setCookies(true);
        // new File( "webapp" + File.separator + "webapp" ).mkdirs();

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
        tomcat.stop();
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


    protected Context createContext(final Embedded catalina, final String contextPath, final String docBase) {
        return catalina.createContext(contextPath, docBase);
    }

    private void updateManager(HazelcastSessionManager manager) {
        manager.setSticky(sticky);
        manager.setClientOnly(clientOnly);
        manager.setMapName(mapName);
        manager.setMaxInactiveInterval(sessionTimeout);
        manager.setDeferredWrite(deferredWrite);
    }


}
