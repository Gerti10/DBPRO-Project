import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.io.IOException;


@WebServlet("/DebsServlet")
public class DebsServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //save form data to be given to esper engine to process
        request.setAttribute("playerId", request.getParameter("pid"));
        request.setAttribute("firstName", request.getParameter("fname"));
        request.setAttribute("lastName", request.getParameter("lname"));

        RequestDispatcher rd = request.getRequestDispatcher("/GameServlet");
        rd.forward(request,response); //forward player data to the second servlet

        //System.out.println(request.getParameter("pid"));

        String site = "http://localhost:8080/DebsGrandChallenge2011_war_exploded/GameServlet";

        response.setStatus(response.SC_MOVED_TEMPORARILY);
        response.setHeader("Location", site);

    }

}
