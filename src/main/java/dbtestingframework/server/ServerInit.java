
package server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

public class ServerInit {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(StatusServlet.class, "/status");
        handler.addServletWithMapping(DataGeneratorServlet.class, "/datagenerator");
        server.start();
        server.join();

    }

}