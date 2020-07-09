import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.json.minimaljson.JsonObject;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.util.*;

@ServerEndpoint(value = "/socket", configurator = ServerEndpointMonitor.class)
public class ServerEndPoint {

    Questions questions = new Questions();
    List<String[]> questionList  = questions.getQuestionList(); //List with the Questions
    String[] q;
    JsonObject jsonObject = new JsonObject();;  //hold the question in json format to send it to the player
    TriviaRuntime example = new TriviaRuntime();
    EPRuntime runtime = example.setup();
    public Set<Session> allSessions = new HashSet<>(); //saves all open sessions
    int counter = 0;

    public Timer timer;
    public final void runDemo(){
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                JsonObject b = new JsonObject();
                counter++;
                if (questions.getQuestionListSize()==counter-2){
                  System.exit(0);
                }
                if (counter > 1){
                    runtime.getEventService().sendEventMap(EventFactory.makeUpdateScore(q[0]), "UpdateScore");
                    b = printScore(q[0], getScores(runtime));
                }
                JsonObject a = sendQuestionToEsperRuntime();
                //send the question to all the opened sessions and score updates
                if(!allSessions.isEmpty()){
                    for (Session s : allSessions){
                        try {
                            s.getBasicRemote().sendText(a.toString());
                            if(!b.isEmpty()){
                                s.getBasicRemote().sendText(b.toString());
                            }
                        }catch (IOException io){
                            System.out.println("Exception happened sending question to all opened sockets");
                            io.printStackTrace();
                        }
                    }
                }
            }
        },0,15000);
    }

    //constructor
    public ServerEndPoint() {
        System.out.println("class loaded " + this.getClass());
        runDemo();
    }

    @OnOpen
    public void onOpen(final Session session) {
        System.out.printf("Session opened, id: %s%n", session.getId());
        try {
            allSessions.add(session); //save the open sesssion;
            session.getBasicRemote().sendText(jsonObject.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
         }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.printf("Message received. Session id: %s Message: %s%n", session.getId(), message);

        //message is most frequent answer
        switch (message) {
            case "mfa":
                Map<String, Object> mfa = EventFactory.makePlayerFARequest("User1", q[0]);
                runtime.getEventService().sendEventMap(mfa, "PlayerFARequest");
                //get the most frequent answer and send it to the client
                break;
            case "answerAnnulment":
                Map<String, Object> answerAnnulment = EventFactory.makePlayerAnnulment(session.getId(), q[0], System.currentTimeMillis());
                runtime.getEventService().sendEventMap(answerAnnulment, "PlayerAnnulment");
                JsonObject j = new JsonObject();
                j.add("annul", "ansAnnul");
                try {
                    session.getBasicRemote().sendText(j.toString());
                }catch (IOException ex){
                    ex.printStackTrace();
                }
                break;
            default: //message is player answer
                //send player Answer to the runtime
                String[] msg = message.split(",");
                Map<String, Object> pAnswer = EventFactory.makePlayerAnswer(session.getId(), q[0], msg[0], Long.parseLong(msg[1]));
                runtime.getEventService().sendEventMap(pAnswer, "PlayerAnswer");
                break;
        }
    }

    @OnError
    public void onError(Throwable e) {
        System.out.println("Error happened on socket...");
        e.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {
        allSessions.remove(session); //remove session from the saved sessions when it is closed
        System.out.printf("Session closed with id: %s%n", session.getId());
    }

    //get the question in json format
    public JsonObject getQuestionJson(String[] question) {
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

    public static String getMFA(EPRuntime runtime) {
        EPStatement stmt = runtime.getDeploymentService().getStatement("trivia", "Outgoing-PlayerFAResponse");
        Map<String, String> result = new LinkedHashMap<String, String>();
        return stmt.iterator().next().get("answerFA").toString();
    }

    private JsonObject printScore(String questionId, Map<String, Integer> scores) {
        System.out.println("Score after question " + questionId + ":");
        JsonObject obj = new JsonObject();
        obj.add("UserID", "Score");
        for (Map.Entry<String, Integer> score : scores.entrySet()) {
            obj.add(score.getKey(), score.getValue());
            System.out.println("  User " + score.getKey() + " : " + score.getValue());
        }
        return obj;
    }

    /*
    sends Question to esper runtime
    returns Question in Json format to send it to the player
     */
    private JsonObject sendQuestionToEsperRuntime() {
        //get random question and send it to the client
        //q = questionList.get(random.nextInt(5));
        q = questions.getNextQuestion();
        jsonObject = getQuestionJson(q);
        System.out.println(jsonObject.toString());
        String[] answers = new String[4];
        answers[0] = q[2];
        answers[1] = q[3];
        answers[2] = q[4];
        answers[3] = q[5];
        //qId, question, answer, time, choises
        Map<String, Object> questionEvent
                = EventFactory.makeTriviaQuestion(q[0], q[1], q[6], System.currentTimeMillis(), answers);
        System.out.println("Sending question event to runtime...");
        runtime.getEventService().sendEventMap(questionEvent, "TriviaQuestion");
        return jsonObject;
    }
}
