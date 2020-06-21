
//saves the player id, first name and last name
public class Player {
    public String playerId;
    public String fName;
    public String lName;


    Player(String playerId, String fName, String lName){
        this.playerId = playerId;
        this.fName = fName;
        this.lName = lName;
    }


    public String getPlayerId() {
        System.out.println(playerId);
        return playerId;
    }
}
