public class DataAccount{
    public String IDHash;
    public String sender;
    public String receiver;
    public int myWriteCounter;
    public int myReadCounter;
    public DataAccount(String sender, String receiver){
        this.IDHash = Database.generateIDHash(sender, receiver);
        this.sender = sender;
        this.receiver = receiver;
    }
}