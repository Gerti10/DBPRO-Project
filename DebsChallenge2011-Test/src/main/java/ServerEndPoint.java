import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.json.minimaljson.JsonObject;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.util.*;

/**
 * This class provides an Endpoint for the user Interface. Every user will be connected with the sockets provided
 * by this class. It creates the esper runtime, sends events to esper based on the player answers, iterates throgh
 * the score results provided by esper and attaches a listener to get the most frequent answer when asked from the
 * player.
 * It will be instantiated only once, so that every player connects with the same esper engine and get the same
 * question.
 */
@ServerEndpoint(value = "/socket", configurator = ServerEndpointMonitor.class)
public class ServerEndPoint {

    Questions questions = new Questions();
    List<String[]> questionList  = questions.getQuestionList(); //List with the Questions
    String[] q; //holds the questionId, question, the possible answers and the right answer as string array
    JsonObject jsonObject = new JsonObject();;  //hold the question in json format to send it to the player
    TriviaRuntime example = new TriviaRuntime(); //configurates esper runtime
    EPRuntime runtime = example.setup(); //
    public Set<Session> allSessions = new HashSet<>(); //saves all open sessions
    int counter = 0; //counter to stop the program after every question was asked
    public Timer timer; //timer to ensure that a question is made once in 30 sec

    /**
     * runDemo is the main function of the program. It is called every 30 seconds to create a question event, send it
     * to Esper runtime as well as to user interface. It also sends the updated scores to the client when a question
     * timer is finished.
     */
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
        },0,5000);
    }

    /**
     * Server Endpoint Constructor which is called at program startup to create a single instance of this class
     */
    public ServerEndPoint() {
        runDemo();
    }

    /**
     *
     * @param session is stored in the Sessions HashSet and is used to send the actual question to the players interface
     * @throws IOException if an error happens during transmission of the message to the user interface.
     */
    @OnOpen
    public void onOpen(final Session session) {
        System.out.printf("Session opened, id: %s%n", session.getId());
        try {
            allSessions.add(session); 
            session.getBasicRemote().sendText(jsonObject.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
         }
    }

    /**
     * This method listens for incoming messages from the user interface. It takes the proper actions based on the
     * message type (most frequent answer request, answer annulment, player answer)
     * @param message, received from the client in text form.
     * @param session, session from which the message comes to the server
     * @throws IOException if an error happens during transmission of message to the user interface.
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.printf("Message received. Session id: %s Message: %s%n", session.getId(), message);

        switch (message) {
            case "mfa": //creates a mfa event and sends it to the esper runtime. Result is printed in the console
                Map<String, Object> mfa = EventFactory.makePlayerFARequest(session.getId(), q[0]);
                runtime.getEventService().sendEventMap(mfa, "PlayerFARequest");
                getMFA(runtime);
                break;
            case "answerAnnulment": //creates ansAnnul event and send it to the esper runtime. Messages the user interface to enable the answer button of the player
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
            default: //creates an Answer Event and sends it to esper runtime.
                String[] msg = message.split(",");
                Map<String, Object> pAnswer = EventFactory.makePlayerAnswer(session.getId(), q[0], msg[0], Long.parseLong(msg[1]));
                runtime.getEventService().sendEventMap(pAnswer, "PlayerAnswer");
                break;
        }
    }

    /**
     *
     * @param e, Throws an Exception if an error happens on the sockets
     */
    @OnError
    public void onError(Throwable e) {
        System.out.println("Error happened on socket...");
        e.printStackTrace();
    }

    /**
     *
     * @param session, session is closed when client finishes playing the game or leaves the game
     */
    @OnClose
    public void onClose(Session session) {
        allSessions.remove(session); //remove session from the saved sessions when it is closed
        System.out.printf("Session closed with id: %s%n", session.getId());
    }

    /**
     *
     * @param question String array of data to be transformed in the json format
     * @return JsonObject containing the questionId, Question, Answers, correct Answer in json format
     */
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

    /**
     *
     * @param runtime, esper runtime to iterate through the score results
     * @return Map<String, Integer> containing every playerID and scores he has achieved
     */
    public static Map<String, Integer> getScores(EPRuntime runtime) {
        EPStatement stmt = runtime.getDeploymentService().getStatement("trivia", "Score window");
        Map<String, Integer> scores = new LinkedHashMap<String, Integer>();
        for (Iterator<EventBean> it = stmt.iterator(); it.hasNext(); ) {
            EventBean next = it.next();
            String playerId = (String) next.get("playerId");
            Integer score = (Integer) next.get("score");
            scores.put(playerId, score);
        }
        return scores;
    }

    /**
     *
     * @param questionId, for which to display the scores after the 30 sec of question expires
     * @param scores, scores to be transformed in json format
     * @return JsonObject, containing the scores to be send to the user interface
     */
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

    /**
     * This method overrides update Method of UpdateListener Interface provided by Esper to receive results in case
     * a query has something to return
     * @param runtime, esper runtime to add a listener to the PlayerFARResponse statement
     * @return String containing the most frequent answer
     */
    public static String getMFA(EPRuntime runtime) {
        EPStatement stmt = runtime.getDeploymentService().getStatement("trivia", "Outgoing-PlayerFAResponse");
        final String answerFA = "no answer";
        final List<String> strings = Collections.synchronizedList(new ArrayList<String>());
        stmt.addListener(new UpdateListener() {

            @Override
            public void update(EventBean[] newEvent, EventBean[] oldEvent, EPStatement epStatement, EPRuntime epRuntime) {
                String playerID = (String) newEvent[0].get("playerId");
                String questionId = (String) newEvent[0].get("questionId");
                strings.add((String) newEvent[0].get("answerFA"));
                System.out.println("PlayerID:  " + playerID + " questionID:  " + questionId + " AnswerFA: " + strings.get(0));
            }
        });
        if (strings.size() != 0){
            return strings.get(0);
        }
         return answerFA;
    }

    /**
     * This method sends the question to the esper runtime,
     * @return returns the question in json format
     */
    private JsonObject sendQuestionToEsperRuntime() {
        q = questions.getNextQuestion(); //gets next question from the quesion List
        jsonObject = getQuestionJson(q); //question in json format
        String[] answers = new String[4]; //array to hold the possible answers
        answers[0] = q[2];
        answers[1] = q[3];
        answers[2] = q[4];
        answers[3] = q[5];
        //takes qId, question, answer, time, choises as parameter
        Map<String, Object> questionEvent = EventFactory.makeTriviaQuestion(q[0], q[1], q[6], System.currentTimeMillis(), answers);
        System.out.println("Sending question event to runtime...");
        runtime.getEventService().sendEventMap(questionEvent, "TriviaQuestion");
        return jsonObject;
    }
}
