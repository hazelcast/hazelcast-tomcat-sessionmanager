package com.hazelcast.session;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

public class Tomcat8Configurator extends WebContainerConfigurator<Tomcat> {

    private Tomcat tomcat;
    private HazelcastSessionManager manager;
    private String appName;

    public Tomcat8Configurator(String appName) {
        this.appName = appName;
    }

    public Tomcat8Configurator() {
        this.appName = "defaultApp";
    }

    @Override
    public Tomcat configure() throws Exception {
        final URL root = new URL(TestServlet.class.getResource("/"), "../../../tomcat-core/target/test-classes");
        final String cleanedRoot = URLDecoder.decode(root.getFile(), "UTF-8");

        final String fileSeparator = File.separator.equals("\\") ? "\\\\" : File.separator;
        final String docBase = cleanedRoot + File.separator + appName + fileSeparator;

        Tomcat tomcat = new Tomcat();
        if (!clientOnly) {
            P2PLifecycleListener listener = new P2PLifecycleListener();
            listener.setConfigLocation(configLocation);
            tomcat.getServer().addLifecycleListener(listener);
        } else {
            tomcat.getServer().addLifecycleListener(new ClientServerLifecycleListener());
        }
        tomcat.getEngine().setJvmRoute("tomcat-" + port);
        tomcat.setBaseDir(docBase);

        tomcat.getEngine().setName("engine-" + port);

        final Connector connector = tomcat.getConnector();
        connector.setPort(port);
        connector.setProperty("bindOnInit", "false");

        tomcat.addUser("someuser", "somepass");
        tomcat.addRole("someuser", "role1");

        Context context;
        try {
            context = tomcat.addWebapp(tomcat.getHost(), "/", docBase);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        this.manager = new HazelcastSessionManager();
        context.setManager(manager);
        updateManager(manager);
        context.setCookies(true);
        context.setBackgroundProcessorDelay(1);
        context.setReloadable(true);

        return tomcat;
    }

    @Override
    public void start() throws Exception {
        tomcat = configure();
        tomcat.start();
        setSessionTimeout();
    }

    @Override
    public void stop() throws Exception {
        if (tomcat.getServer().getState().isAvailable()) {
            tomcat.stop();
        }
    }

    @Override
    public void reload() {
        Context context = (Context) tomcat.getHost().findChild("/");
        if (context == null) {
            //Starting with Tomcat 8.0.35, child name is changed
            context = (Context) tomcat.getHost().findChild("");
        }
        context.reload();
        setSessionTimeout();
    }

    @Override
    public SessionManager getManager() {
        return manager;
    }

    private void updateManager(HazelcastSessionManager manager) {
        manager.setSticky(sticky);
        manager.setClientOnly(clientOnly);
        manager.setMapName(mapName);
        manager.setDeferredWrite(deferredWrite);
        manager.setProcessExpiresFrequency(1);
        manager.setReadStrategy(readStrategy);
        manager.setWriteStrategy(writeStrategy);
    }

    private void setSessionTimeout() {
        manager.setSessionTimeout(sessionTimeout);
    }
}
