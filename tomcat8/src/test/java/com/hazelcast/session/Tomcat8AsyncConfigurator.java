package com.hazelcast.session;

import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

public class Tomcat8AsyncConfigurator extends WebContainerConfigurator<Tomcat> {

    private final String baseDir;

    private Tomcat tomcat;
    private HazelcastSessionManager manager;

    public Tomcat8AsyncConfigurator(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public Tomcat configure() throws Exception {
        Tomcat tomcat = new Tomcat();
        if (!clientOnly) {
            P2PLifecycleListener listener = new P2PLifecycleListener();
            listener.setConfigLocation(configLocation);
            tomcat.getServer().addLifecycleListener(listener);
        } else {
            tomcat.getServer().addLifecycleListener(new ClientServerLifecycleListener());
        }
        tomcat.getEngine().setJvmRoute("tomcat-" + port);
        tomcat.getEngine().setName("engine-" + port);

        final Connector connector = tomcat.getConnector();
        connector.setPort(port);
        connector.setProperty("bindOnInit", "false");

        tomcat.addUser("someuser", "somepass");
        tomcat.addRole("someuser", "role1");

        Context context;
        try {
            context = tomcat.addWebapp("", baseDir);

            Wrapper asyncServlet = context.createWrapper();
            asyncServlet.setName("asyncServlet");
            asyncServlet.setServletClass(AsyncServlet.class.getName());
            asyncServlet.setAsyncSupported(true);

            context.addChild(asyncServlet);
            context.addServletMapping("/*", "asyncServlet");

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
    }

    private void setSessionTimeout() {
        manager.setSessionTimeout(sessionTimeout);
    }
}