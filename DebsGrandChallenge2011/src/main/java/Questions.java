import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


//reads a textfile with questions and parses the questions and answers, used to take a random question from a question set
public class Questions {
    private File file = new File("/Users/gertibushati/Desktop/esper-8.4.0/examples/DebsGrandChallenge2011/etc/questions.txt");

    private List<String> lines;
    private List<String[]> questions;
    private Random random;

    Questions(){
        lines = new LinkedList<>();
        questions = new ArrayList<>();
        random= new Random();
        System.out.println("a new Question object was made");
    }

    public void createQuestionArrayList(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
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

    public String[] getRandomQuestion(){
        return questions.get(random.nextInt(5));
    }

}
