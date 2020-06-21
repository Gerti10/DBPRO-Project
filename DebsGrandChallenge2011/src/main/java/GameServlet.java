import com.espertech.esper.runtime.client.EPRuntime;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/GameServlet")
public class GameServlet extends HttpServlet {


    private Questions q = new Questions(); //create a questions Object, will be created only one time
    private TriviaRuntime example = new TriviaRuntime();
    private EPRuntime runtime = example.setup(); // set up the esper runtime

    //create a function that send a question to the esper runtime every 30 sec




    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //get redirected here from DebsServlet

        //q1[0] is QID, q1[1] is question, q1[6] is correct answer, System.currentTimeMillis is time as long value,
        //problems to be solved-- 1) every user will send a question event. Question event should be send only once and
        //and wait 30 sec for incoming answer events

        //idee: create question event only if this is the first thread of the game
            //System.out.println("this is the counter:  " +counter);
            q.createQuestionArrayList();
            String[]q1 =  q.getRandomQuestion(); //find a way to get the current question not a random question per Thread
            String[] answers = new String[4];
            answers[0] = q1[2];
            answers[1] = q1[3];
            answers[2] = q1[4];
            answers[3] = q1[5];
           // Map<String, Object> questionEvent = EventFactory.makeTriviaQuestion(q1[0],q1[1],q1[6], System.currentTimeMillis(), answers);
           // runtime.getEventService().sendEventMap(questionEvent, "TriviaQuestion"); //Send Question event to esper runtime



        //next step -- send player answer



        //next step -- update score

        //print scores to user




        PrintWriter out = response.getWriter();



        out.println("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>Welcome to Trivia Geeks Club</title>" +
                " <link rel=\"stylesheet\" href=\"gameStyle.css\"></head>"); //header
        out.println("<body bgcolor = \"#f0f0f0\"><fieldset><div class=\"block_1 hline-bottom\"> Answer the questions </div>" +
                "<br><br><h1>QuestionID:" + q1[0] +"</h1><br><br><h1>Question: "+ q1[1] +"</h1><br><br><br>"); //write the page till form

        out.println("<input type=\"radio\" id=\"1\" name=\"1\" value=\"1\"><label for=\"1\"> "+q1[2] + "</label>");
        out.println("<input type=\"radio\" id=\"2\" name=\"1\" value=\"2\">\n" +
                "    <label for=\"2\"> " + q1[3] + "</label>\n" +
                "    <input type=\"radio\" id=\"3\" name=\"1\" value=\"3\">\n" +
                "    <label for=\"3\"> "+ q1[4] +" </label>\n" +
                "    <input type=\"radio\" id=\"4\" name=\"1\" value=\"4\">\n" +
                "    <label for=\"4\"> " + q1[5] + " </label>\n" +
                "    <br><br>\n" +
                "    <input type=\"submit\" value=\"Answer\">\n" +
                "    <input type=\"submit\" value=\"Ask for the most frequent answer\">"+
                "    <p>Most frequent Answer is: ...</p>"+
                "    <br><br><br><br>\n" +
                "    <table class=\"a\">\n" +
                "        <tr>\n" +
                "            <td>PlayerId</td>\n" +
                "            <td>PlayerName</td>\n" +
                "            <td>Scores</td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td>"+request.getAttribute("playerId")+"</td>\n" +
                "            <td>"+request.getAttribute("firstName")+"</td>\n" +
                "            <td>"+request.getAttribute("lastName")+"</td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "</fieldset>\n" +
                "\n" +
                "</body>\n" +
                "</html>");

        response.setIntHeader("Refresh", 5);


    }
}
