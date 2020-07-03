import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.json.minimaljson.Json;
import com.espertech.esper.common.client.json.minimaljson.JsonObject;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;

import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnError;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ServerEndpoint(value = "/socket", configurator = ServerEndpointMonitor.class)
public class ServerEndPoint {


    //create a question
    Questions questions;
    List<String[]> questionList;
    String[] q;
    JsonObject jsonObject;

    EPRuntime runtime;

    public void run(){
        //init TriviaRuntime
        TriviaRuntime example = new TriviaRuntime();
        runtime = example.setup();

        questions = new Questions();
        questionList = questions.getQuestionList();
        q = questions.getQuestion();
        jsonObject = getQuestionJson(q);

        //create Map to send to esper runtime
        String[] answers = new String[4];
        answers[0] = q[2];
        answers[1] = q[3];
        answers[2] = q[4];
        answers[3] = q[5];
        Map<String, Object> questionEvent = EventFactory.makeTriviaQuestion(q[0], q[1], q[6],System.currentTimeMillis(),answers);
        System.out.println("Sending question event to runtime...");
        runtime.getEventService().sendEventMap(questionEvent, "TriviaQuestion");
    }

    //constructor
    public ServerEndPoint() {
        System.out.println("class loaded " + this.getClass());
        run();
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.printf("Session opened, id: %s%n", session.getId());
        try {
            session.getBasicRemote().sendText(jsonObject.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {

        System.out.printf("Message received. Session id: %s Message: %s%n",
                session.getId(), message);

        //send player Answer to the runtime
        String[] msg =  message.split(",");
        System.out.println("This is answer "+msg[0] + "and time: "+ msg[1]);
        Map<String,Object> pAnswer = EventFactory.makePlayerAnswer("1", q[1], msg[0], Long.parseLong(msg[1]));
        runtime.getEventService().sendEventMap(pAnswer, "PlayerAnswer");

        //trigger score update
        runtime.getEventService().sendEventMap(EventFactory.makeUpdateScore(q[0]), "UpdateScore");

        //get player Scores
        printScore(q[0], getScores(runtime));


        try {
            //session.getBasicRemote().sendText(String.format("We received your message: %s%n", message));
            session.getBasicRemote().sendText("Do nothing first");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @OnError
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {
        System.out.printf("Session closed with id: %s%n", session.getId());
    }


    //method to get the question in json format
    public JsonObject getQuestionJson(String[] question){
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("qId", q[0]);
        jsonObject.add("question", q[1]);
        jsonObject.add("ans1", q[2]);
        jsonObject.add("ans2", q[3]);
        jsonObject.add("ans3",q[4]);
        jsonObject.add("ans4", q[5]);
        jsonObject.add("correctAns", q[6]);
        return jsonObject;
    }

    public static Map<String, Integer> getScores(EPRuntime runtime) {
        EPStatement stmt = runtime.getDeploymentService().getStatement("trivia", "Score window");
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (Iterator<EventBean> it = stmt.iterator(); it.hasNext(); ) {
            EventBean next = it.next();
            String playerId = (String) next.get("playerId");
            Integer score = (Integer) next.get("score");
            result.put(playerId, score);
        }
        return result;
    }

    private void printScore(String questionId, Map<String, Integer> scores) {
        System.out.println("Score after question " + questionId + ":");
        for (Map.Entry<String, Integer> score : scores.entrySet()) {
            System.out.println("  User " + score.getKey() + " : " + score.getValue());
        }
    }
}
