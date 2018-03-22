import com.hazelcast.session.AbstractSessionExpireTest;
import com.hazelcast.session.Tomcat8Configurator;
import com.hazelcast.session.WebContainerConfigurator;

public class Tomcat8SessionExpireTest extends AbstractSessionExpireTest {
    @Override
    protected WebContainerConfigurator<?> getWebContainerConfigurator() {
        return new Tomcat8Configurator();
    }
}
