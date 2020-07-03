import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

//reads a textfile with questions and parses the questions and answers
public class Questions {
    private InputStream in = this.getClass().getResourceAsStream("questions.txt");

    private List<String> lines;
    private List<String[]> questions;
    private int lineCounter = 0;
    Questions(){
        lines = new LinkedList<>();
        questions = new ArrayList<>();
        createQuestionArrayList();
    }

    public void createQuestionArrayList(){
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

    public String[] getQuestion(){
        String[] getQuestion = questions.get(lineCounter);
        lineCounter++;
        return getQuestion; //return a single question
    }

    public List<String[]> getQuestionList(){
        return this.questions; //return the question list
    }



}
