package com.hazelcast.session;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

public class Tomcat7Configurator extends WebContainerConfigurator<Tomcat> {

    private Tomcat tomcat;
    private SessionManager manager;

    private String appName;

    public Tomcat7Configurator(String appName) {
        this.appName = appName;
    }

    public Tomcat7Configurator() {
        this.appName = "defaultApp";
    }

    @Override
    public Tomcat configure() throws Exception {
        final URL root = new URL(TestServlet.class.getResource("/"), "../../../tomcat-core/target/test-classes");
        final String cleanedRoot = URLDecoder.decode(root.getFile(), "UTF-8");

        final String docBase = cleanedRoot + File.separator + appName;

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

        Context context;
        try {
            context = tomcat.addWebapp(tomcat.getHost(), "/", docBase);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        this.manager = new HazelcastSessionManager();
        context.setManager((HazelcastSessionManager) manager);
        updateManager((HazelcastSessionManager) manager);
        context.setCookies(true);
        context.setBackgroundProcessorDelay(1);
        context.setReloadable(true);
        context.addLifecycleListener(new LifecycleListener() {
            @Override
            public void lifecycleEvent(LifecycleEvent event) {
                if (Lifecycle.BEFORE_START_EVENT.equals(event.getType())) {
                    ((Context) event.getLifecycle()).setSessionTimeout(sessionTimeout);
                }
            }
        });
        context.addApplicationListener("com.hazelcast.session.TomcatHttpSessionListener");

        return tomcat;
    }

    @Override
    public void start() throws Exception {
        tomcat = configure();
        tomcat.start();
    }

    @Override
    public void stop() throws Exception {
        if (tomcat.getServer().getState().isAvailable()) {
            tomcat.stop();
        }
    }

    @Override
    public void reload() {
        getContext().reload();
    }

    @Override
    public Context getContext() {
        return (Context) tomcat.getHost().findChild("/");
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
        manager.setProcessExpiresFrequency(1);
    }
}
