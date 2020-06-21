//class to test some function
public class Worker {

    public static void main(String[] args) {


        Questions q = new Questions();
        q.createQuestionArrayList();
        String[] question = q.getRandomQuestion();
        String[] q2 = q.getRandomQuestion();  //q2[0] = id, q2[1] = question, q2[2] = ans1 ...




        System.out.println("Id: " + question[0]+ "   " + "Question: " + question[1] + "   Ans1:  "  +question[2]
                + "   Ans2:  "  +question[3] + "   Ans3:  "  + question[4] + "  Ans4:  "  +question[5] + "  correct ans:  "
                + question[6]);

        System.out.println("Id: " + q2[0]+ "   " + "Question: " + q2[1] + "   Ans1:  "  +q2[2]
                + "   Ans2:  "  +q2[3] + "   Ans3:  "  + q2[4] + "  Ans4:  "  +q2[5] + "  correct ans:  "
                + q2[6]);

   }



}
