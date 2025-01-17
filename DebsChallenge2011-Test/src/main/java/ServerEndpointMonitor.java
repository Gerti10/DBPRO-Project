import java.util.logging.Logger;
import java.util.logging.Level;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

//instantiate the ServerEndpoint only one time and provide this instance to every client that connects
public class ServerEndpointMonitor extends Configurator{
    private final static Logger LOGGER = Logger.getLogger(ServerEndpoint.class.getName());

    private final static ServerEndPoint serverEndpoint = new ServerEndPoint(); //create an instance of the server endpoint

    public ServerEndpointMonitor() {
        LOGGER.setLevel(Level.INFO);
    }

    /**
     *
     * @param endpointClass
     * @param <T>
     * @return the single instance of ServerEndPoint to be used by every client
     * @throws InstantiationException
     */

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass)
            throws InstantiationException {
        LOGGER.info("ServcerEndpointConfigurator: getEndpointInstance() invoked, endpointClass = " + endpointClass.getName());

        return (T) serverEndpoint;
    }


}
