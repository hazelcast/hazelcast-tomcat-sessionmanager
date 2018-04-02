package com.hazelcast.session;

@SuppressWarnings("unused")
public abstract class WebContainerConfigurator<T> {

    protected int port;
    protected boolean sticky = true;
    protected boolean clientOnly;
    protected String mapName = "default";
    protected int sessionTimeout;
    protected boolean deferredWrite;
    protected String configLocation = "hazelcast.xml";
    protected String readStrategy = "default";
    protected String writeStrategy = "default";

    public WebContainerConfigurator<T> port(int port) {
        this.port = port;
        return this;
    }

    public WebContainerConfigurator<T> sticky(boolean sticky) {
        this.sticky = sticky;
        return this;
    }

    public WebContainerConfigurator<T> mapName(String mapName) {
        this.mapName = mapName;
        return this;
    }

    public WebContainerConfigurator<T> clientOnly(boolean clientOnly) {
        this.clientOnly = clientOnly;
        return this;
    }

    public WebContainerConfigurator<T> sessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    public WebContainerConfigurator<T> deferredWrite(boolean deferredWrite) {
        this.deferredWrite = deferredWrite;
        return this;
    }

    public WebContainerConfigurator<T> configLocation(String configLocation) {
        this.configLocation = configLocation;
        return this;
    }

    public WebContainerConfigurator readStrategy(String readStrategy) {
        this.readStrategy = readStrategy;
        return this;
    }

    public WebContainerConfigurator writeStrategy(String writeStrategy) {
        this.writeStrategy = writeStrategy;
        return this;
    }

    public int getPort() {
        return port;
    }

    public abstract T configure() throws Exception;

    public abstract void start() throws Exception;

    public abstract void stop() throws Exception;

    public abstract void reload();

    public abstract SessionManager getManager();
}
