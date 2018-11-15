
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Kevin
 */
public class Player {
    private GameCore gameCore;
    private LinkedList<Item> currentInventory;
    private String name;
    private String lastWhisperName;
    private int currentRoom;
    private Direction currentDirection;
    private PrintWriter replyWriter = null;
    private DataOutputStream outputWriter = null;
    private Money money;
    private String inTradeWithName = null;
    private String inTradeWithItem = null;

    /* START 405_ignore variables*/
    private ArrayList<String> ignoreList;
    private ArrayList<String> ignoredByList;
    /* END 405_ignore variables*/

    public Player(GameCore gameCore, String name) {
        this.gameCore = gameCore;
        this.currentRoom = Map.SPAWN_ROOM_ID;
        this.currentDirection = Direction.NORTH;
        this.name = name;
        this.currentInventory = new LinkedList<>();
        this.money = new Money(20);
        /* START 405_ignore*/
        this.ignoreList = new ArrayList<String>();
        this.ignoredByList = new ArrayList<String>();
        /* END 405_ignore  */
    }

    public void turnLeft() {
        synchronized (this) {
            switch (this.currentDirection.toString()) {
                case "North":
                    this.currentDirection = Direction.WEST;
                    break;
                case "South":
                    this.currentDirection = Direction.EAST;
                    break;
                case "East":
                    this.currentDirection = Direction.NORTH;
                    break;
                case "West":
                    this.currentDirection = Direction.SOUTH;
                    break;
            }
        }
    }

    public void turnRight() {
        synchronized (this) {
            switch (this.currentDirection.toString()) {
                case "North":
                    this.currentDirection = Direction.EAST;
                    break;
                case "South":
                    this.currentDirection = Direction.WEST;
                    break;
                case "East":
                    this.currentDirection = Direction.SOUTH;
                    break;
                case "West":
                    this.currentDirection = Direction.NORTH;
                    break;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        synchronized (this) {
            this.name = name;
        }
    }

    public void setLastWhisperName(String name) {
        synchronized (this) {
            this.lastWhisperName = name;
        }
    }

    public String getLastWhisperName() {
        return this.lastWhisperName;
    }

    public LinkedList<Item> getCurrentInventory() {
        return currentInventory;
    }

    public void setCurrentInventory(LinkedList<Item> currentInventory) {
        synchronized (this) {
            this.currentInventory = currentInventory;
        }
    }

    public void addObjectToInventory(Item object) {
        synchronized (this) {
            this.currentInventory.add(object);
        }
    }

    public Item removeObjectFomInventory(String object) {
        synchronized (this) {
            for (Item obj : this.currentInventory) {
                if (obj.getItemName().equalsIgnoreCase(object)) {
                    this.currentInventory.remove(obj);
                    return obj;
                }
            }
            return null;
        }
    }

    /**
     * Allows an an object to be taken away from player's inventory.
     * @return Message showing success.
     */
    public String removeRandomItem() {
        synchronized (this) {
            if (this.currentInventory.isEmpty()) {
                return "nothing";
            }
            Random randInt = new Random();
            int randItem = randInt.nextInt(this.currentInventory.size());
            String itemName = this.currentInventory.remove(randItem).getItemName();
            setCurrentInventory(this.currentInventory);
            return itemName;
        }
    }

    public void setReplyWriter(PrintWriter writer) {
        synchronized (this) {
            this.replyWriter = writer;
        }
    }

    public PrintWriter getReplyWriter() {
        return this.replyWriter;
    }

    public void setOutputWriter(DataOutputStream writer) {
        synchronized (this) {
            this.outputWriter = writer;
        }
    }

    public DataOutputStream getOutputWriter() {
        return this.outputWriter;
    }

    public int getCurrentRoom() {
        return this.currentRoom;
    }

    public Room getCurrentRoomObject() {
        return gameCore.getMap().findRoom(currentRoom);
    }

    public void setCurrentRoom(int room) {
        synchronized (this) {
            this.currentRoom = room;
        }
    }

    public String getCurrentDirection() {
        return this.currentDirection.name();
    }

    public Direction getDirection() {
        return this.currentDirection;
    }
    public Money getMoney() {
        return this.money;
    }

    public void addMoney(double value) {
        synchronized (this) {
            Money added = new Money();
            int fives = (int)(value / 5);
            int ones = (int)(value % 5);
            double coins = Math.round((value - ((int)value)) * 100);
            coins = (int) coins;
            int quarters = (int)(coins / 25);
            int dimes = (int)((coins - (quarters * 25)) / 10);
            int pennies = (int)(coins - (25 * quarters) - (10 * dimes));

            added.numFives = fives;
            added.numOnes = ones;
            added.numQuarters = quarters;
            added.numDimes = dimes;
            added.numPennies = pennies;

            addMoney(added);
        }
    }

    public void addMoney(Money moneyToAdd){
        synchronized (this) {
            this.money.numFives += moneyToAdd.numFives;
            this.money.numOnes += moneyToAdd.numOnes;
            this.money.numQuarters += moneyToAdd.numQuarters;
            this.money.numDimes += moneyToAdd.numDimes;
            this.money.numPennies += moneyToAdd.numPennies;
        }
    }

    public void removeMoney(Money moneyRemove){
        synchronized (this) {
            this.money.numFives -= moneyRemove.numFives;
            this.money.numOnes -= moneyRemove.numOnes;
            this.money.numQuarters -= moneyRemove.numQuarters;
            this.money.numDimes -= moneyRemove.numDimes;
            this.money.numPennies -= moneyRemove.numPennies;
        }
    }

    public void removeMoney(double value){
        synchronized (this) {
            if(this.money.sum() < value){
                return;
            }
            double valueCopy = value;
            int[] unitsRemoved = new int[5];
            int[] numUnits = new int[]{this.money.numFives, this.money.numOnes, this.money.numQuarters, this.money.numDimes, this.money.numPennies};
            double[] unitVals = new double[]{5, 1, 0.25, 0.10, 0.01};
            int index = 0;

            while(index < 5){
                for (int i = 1; i <= numUnits[index]; i++){
                    if(unitVals[index] * i > valueCopy){
                        break;
                    }
                    unitsRemoved[index]++;
                }

                if(unitsRemoved[index] > 0){
                    valueCopy -= unitVals[index] * unitsRemoved[index];
                }
                index++;
            }

            if(valueCopy == 0.0){
                this.money.numFives -= unitsRemoved[0];
                this.money.numOnes -= unitsRemoved[1];
                this.money.numQuarters -= unitsRemoved[2];
                this.money.numDimes -= unitsRemoved[3];
                this.money.numPennies -= unitsRemoved[4];
                return;
            }
            // player must overcompensate and then get change
            else if(valueCopy > 0){
                double currentSum = value - valueCopy;
                // round to the nearest dollar, if there are enough dollars
                double rounded = Math.ceil(value);
                int extraOnes = 1;
                double valRemoved = 0;
                while(unitsRemoved[1] + extraOnes <= numUnits[1]){
                    // add ones until the value has been met
                    if(currentSum + extraOnes >= value){
                        unitsRemoved[2] = 0;
                        unitsRemoved[3] = 0;
                        unitsRemoved[4] = 0;
                        unitsRemoved[1] += extraOnes;
                        // value has been met so change player money and return
                        this.money.numFives -= unitsRemoved[0];
                        this.money.numOnes -= unitsRemoved[1];
                        this.money.numQuarters -= unitsRemoved[2];
                        this.money.numDimes -= unitsRemoved[3];
                        this.money.numPennies -= unitsRemoved[4];
                        currentSum += extraOnes;
                        addMoney(rounded - value);
                        return;
                    }
                    extraOnes++;
                }
            }
        }
    }

    public String viewMoney() {
        return this.money.toString();
    }

    public Money giveMoney(Player giver,Player receiver,double value){
        synchronized (this) {
            Money moneyToGive = new Money();
            replyWriter.println("You are giving away "+ String.format("%1$,.2f", value));

            if(this.money.sum() <= 0){
                replyWriter.println("Must give a positive amount of money!");
                return moneyToGive;
            }
            // send money in units available to player
            // if correct units are unavaialable, then return and give a message
            double valueCopy = value;
            int[] unitsGiven = new int[5];
            int[] numUnits = new int[]{this.money.numFives, this.money.numOnes, this.money.numQuarters, this.money.numDimes, this.money.numPennies};
            double[] unitVals = new double[]{5, 1, 0.25, 0.10, 0.01};
            int index = 0;

            while(index < 5){
                for (int i = 1; i <= numUnits[index]; i++){
                    if(unitVals[index] * i > valueCopy){
                        break;
                    }
                    unitsGiven[index]++;
                }

                if(unitsGiven[index] > 0){
                    valueCopy -= unitVals[index] * unitsGiven[index];
                }
                index++;
            }

            if(valueCopy > 0){
                replyWriter.println("You don't have enough money with the units of money that you have!");
                return moneyToGive;
            }

            else{
                moneyToGive.numFives = unitsGiven[0];
                moneyToGive.numOnes = unitsGiven[1];
                moneyToGive.numQuarters = unitsGiven[2];
                moneyToGive.numDimes = unitsGiven[3];
                moneyToGive.numPennies = unitsGiven[4];
                receiver.addMoney(moneyToGive);
                removeMoney(moneyToGive);
                receiver.getReplyWriter().println("You received " + String.format("%1$,.2f", value) + " dollars!");
                return moneyToGive;
            }
        }
    }

    public void setDirection(Direction direction){
        synchronized (this) {
            this.currentDirection = direction;
        }
    }

    public String getInTradeWithName(){
        return this.inTradeWithName;
    }

    public String getInTradeWithItem(){
        return this.inTradeWithItem;
    }
    public void setInTradeWithName(String playerName){
        synchronized (this) {
            this.inTradeWithName = playerName;
        }
    }

    public void setInTradeWithItem(String itemName){
        synchronized (this) {
            this.inTradeWithItem = itemName;
        }
    }

    public String viewInventory() {
        synchronized (this) {
            String result = "";
            if (this.currentInventory.isEmpty() == true) {
                return " nothing.";
            } else {
                for (Item obj : this.currentInventory) {
                    result += " " + obj;
                }
                result += ".";
            }
            result += ".";
            return result;
        }
    }

    @Override
    public String toString() {
        synchronized (this) {
            return "Player " + this.name + ": " + currentDirection.toString();
        }
    }

    /* START 405_ignore */
    public void ignorePlayer(String name) {
        synchronized (this) {
            ignoreList.add(name);
        }
    }

    public void addIgnoredBy( String name) {
        synchronized (this) {
            ignoredByList.add(name);
        }
    }

    public boolean searchIgnoredBy(String name) {
        synchronized (this) {
            int listSize = ignoredByList.size();
            for( int x = 0; x < listSize; x++){
                if( name.equalsIgnoreCase(ignoredByList.get(x)))
                    return true;
            }
            return false;
        }
    }

    public boolean searchIgnoreList(String name) {
        synchronized (this) {
            int listSize = ignoreList.size();
            for( int x = 0; x < listSize; x++){
                if( name.equalsIgnoreCase(ignoreList.get(x)))
                    return true;
            }
            return false;
        }
    }
    /* END 405_ignore */
    //407
    public String showIgnoreList() {
        synchronized (this) {
            String res = "";
            for(int i = 0; i < ignoreList.size(); i++)
                res += ignoreList.get(i) + " ";
            return res;
        }
    }

    public void unIgnorePlayer(String name) {
        synchronized (this) {
            ignoreList.remove(name);
        }
    }

    public void removeIgnoredBy( String name) {
        synchronized (this) {
            ignoredByList.remove(name);
        }
    }

    /**
     * Output a message to this player
     * @param message to send
     */
    public void broadcast(String message) {
        synchronized (this) {
            if (replyWriter == null)
                System.err.println("Trying to broadcast to " + getName() + ", who doesn't have a ReplyWriter yet.");
            else
                replyWriter.println(message);
        }
    }

    /**
     * Output a message to all players in the same room as this player,
     * including outputting a message to this player.
     * @param message to send
     */
    public void broadcastToAllInRoom(String message) {
        getCurrentRoomObject().broadcast(message);
    }

    /**
     * Output a message to all other players in the same room as this player,
     * not including outputting a message to this player.
     * @param message to send
     */
    public void broadcastToOthersInRoom(String message) {
        for (Player player : gameCore.getPlayerList()) {
            if (player.currentRoom == this.currentRoom && player != this)
                player.broadcast(message);
        }
    }
}
