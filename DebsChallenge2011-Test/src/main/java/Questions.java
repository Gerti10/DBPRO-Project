import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is responsible to read the questions from the questions.txt file and parsing questionId, question, answers
 * and the correct answer
 */
public class Questions {
    private InputStream in = this.getClass().getResourceAsStream("questions.txt");
    private List<String> lines;
    private List<String[]> questions;
    private int lineCounter = 0;
    Questions(){
        lines = new LinkedList<>();
        questions = new LinkedList<>();
        createQuestionLinkedList();
    }

    /**
     * This method reads the file line by line and adds the quetions in the questions list
     * @throws IOException if an error happens reading the file
     */
    public void createQuestionLinkedList(){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String st;
            while ((st = br.readLine()) != null){
                lines.add(st);
            }

            for (String line: lines){
                questions.add(line.split(","));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @return the next question from the questions list
     */
    public String[] getNextQuestion(){
        if(lineCounter == questions.size()) lineCounter = 0;
        String[] getQuestion = questions.get(lineCounter);
        lineCounter++;
        return getQuestion;
    }

    /**
     *
     * @return questions list
     */
    public List<String[]> getQuestionList(){
        return this.questions;
    }

    /**
     *
     * @return size of the questions list
     */
    public int getQuestionListSize(){
        return questions.size();
    }

}
