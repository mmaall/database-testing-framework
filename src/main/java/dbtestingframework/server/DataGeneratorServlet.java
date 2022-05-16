
package server;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DataGeneratorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ServerState serverState = ServerState.getInstance();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        System.out.println("Content type: " + request.getContentType());
        System.out.println("Parameter Map: " + request.getParameterMap().toString());

        response.setStatus(HttpServletResponse.SC_OK);
    }

}