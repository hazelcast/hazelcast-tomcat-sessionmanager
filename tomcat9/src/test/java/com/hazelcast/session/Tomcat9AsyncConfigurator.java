package com.hazelcast.session;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

public class Tomcat9AsyncConfigurator
        extends WebContainerConfigurator<Tomcat> {

    private final String baseDir;

    private Tomcat tomcat;
    private HazelcastSessionManager manager;

    public Tomcat9AsyncConfigurator(String baseDir) {
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
            context.addServletMappingDecoded("/*", "asyncServlet");

        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }

        this.manager = new HazelcastSessionManager();
        context.setManager(manager);
        updateManager(manager);
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
        Context context = (Context) tomcat.getHost().findChild("/");
        if (context == null) {
            context = (Context) tomcat.getHost().findChild("");
        }
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
        manager.setDeferredWrite(deferredWrite);
        manager.setProcessExpiresFrequency(1);
    }
}