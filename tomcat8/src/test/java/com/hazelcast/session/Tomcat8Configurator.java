package com.hazelcast.session;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.net.URL;

public class Tomcat8Configurator extends WebContainerConfigurator<Tomcat> {

    private Tomcat tomcat;


    private SessionManager manager;


    @Override
    public Tomcat configure() throws Exception {
        final URL root = new URL(TestServlet.class.getResource("/"), "../../../tomcat-core/target/test-classes");
        // use file to get correct separator char, replace %20 introduced by URL for spaces
        final String cleanedRoot = new File(root.getFile().replaceAll("%20", " ")).toString();

        final String fileSeparator = File.separator.equals("\\") ? "\\\\" : File.separator;
        final String docBase = cleanedRoot + File.separator + TestServlet.class.getPackage().getName().replaceAll("\\.", fileSeparator);

        Tomcat tomcat = new Tomcat();
        if (!clientOnly) {
            String configLocation = "hazelcast.xml";
            P2PLifecycleListener p2PLifecycleListener = new P2PLifecycleListener();
            p2PLifecycleListener.setConfigLocation(configLocation);
            tomcat.getServer().addLifecycleListener(p2PLifecycleListener);
        }
        tomcat.getEngine().setJvmRoute("tomcat-" + port);
        tomcat.setBaseDir(docBase);

        tomcat.getEngine().setName("engine-" + port);

        final Connector connector = tomcat.getConnector();
        connector.setPort(port);
        connector.setProperty("bindOnInit", "false");

        Context context;
        try {
            context = tomcat.addWebapp(tomcat.getHost(), "/", docBase + fileSeparator + "webapp");
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        this.manager = new HazelcastSessionManager();
        context.setManager((HazelcastSessionManager)manager);
        updateManager((HazelcastSessionManager)manager);
        context.setCookies(true);
        context.setBackgroundProcessorDelay(1);
        context.setReloadable(true);

        return tomcat;
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
        Context context = (Context) tomcat.getHost().findChild("/");
        context.reload();
    }

    @Override
    public SessionManager getManager() {
        return manager;
    }

    private void updateManager(HazelcastSessionManager manager) {
        manager.setSticky(sticky);
        manager.setClientOnly(clientOnly);
        manager.setMapName(mapName);
        manager.setMaxInactiveInterval(sessionTimeout);
        manager.setDeferredWrite(deferredWrite);
    }



}
