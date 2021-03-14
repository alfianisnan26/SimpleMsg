import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Database {
    private static final String databaseURL = "https://simplemessengger-default-rtdb.firebaseio.com/";

    private static String getData(String target) {
        final String getEndpoint = databaseURL + target + ".json";
        var request = HttpRequest.newBuilder().uri(URI.create(getEndpoint)).GET().build();

        var client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("GET Status Code: " + response.statusCode());
            return response.body();
        } catch (Exception ex) {
        }
        return "";
    }

    private static int sendData(final String target, final String data) {
        final String putEndpoint = databaseURL + target + ".json";
        var request = HttpRequest.newBuilder().uri(URI.create(putEndpoint)).header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(data)).build();

        var client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("PUT Status Code: " + response.statusCode());
            return response.statusCode();
        } catch (Exception ex) {
        }
        return 0;
    }

    public static String getContact(String username){
        username = getData("users/" + username + "/contact");
        return username.substring(1,username.length()-1);
    }
    public static boolean writeContact(String username, String contact){
        return sendData("users/" + username + "/contact", "\"" + contact + "\"") == 200;
    }

    public static boolean checkUser(String uname) {
        if (getData("users/" + uname + "/pass").equals("null")) {
            return true;
        }
        return false;
    }

    public static boolean createUser(String uname, char[] pass) {
        if (sendData("users/" + uname + "/pass", "\"" + generateIDHash(uname, String.valueOf(pass)) + "\"") == 200) {
            return true;
        }
        return false;
    }

    public static boolean loginUser(String uname, char[] pass) {
        String myHash = generateIDHash(uname, String.valueOf(pass));
        String in = getData("users/" + uname + "/pass");
        if (in.equals("null"))
            return false;
        in = in.substring(1, 33);
        System.out.print("Hash : ");
        System.out.print(myHash);
        System.out.print(" | " + in);
        if (in.equals(myHash))
            return true;
        return false;
    }

    public static String generateIDHash(String receiver, String uname) {
        if (uname.compareTo(receiver) < (receiver.compareTo(uname)))
            return Hasher.md5(uname + String.valueOf(receiver));
        else
            return Hasher.md5(receiver + String.valueOf(uname));
    }

    public static boolean sendChat(DataAccount data, String text) {
        if (sendData("/data/" + data.IDHash + "/" + data.sender + "/chat/" + data.myWriteCounter, "\"" + text + "\"") == 200) {
            data.myWriteCounter++;
            if (setMyWriteCounter(data)) {
                return true;
            }else{
                data.myWriteCounter--;
            }
        }
        return false;
    }

    public static String getChat(DataAccount data) {
        System.out.println("Wait on: " + data.myReadCounter);
        String out = getData("/data/" + data.IDHash + "/" + data.receiver + "/chat/" + data.myReadCounter);
        if (out.equals("null")) {
            return "null";
        } else {
            data.myReadCounter ++;
            if(!setMyReadCounter(data)){
                data.myReadCounter--;
                return "null";
            }
            return out;
        }
    }
    public static boolean initChat(DataAccount data){
        if(getMyWriteCounter(data)){
            if(getMyReadCounter(data)){
                System.out.println("Writer Counter : " + data.myWriteCounter + "\nReader Counter : " + data.myReadCounter);
                return true;
            }
        }
        return false;
    }


    private static boolean setMyWriteCounter(DataAccount data) {
        if (sendData("/data/" + data.IDHash + "/" + data.sender + "/counter/write", String.valueOf(data.myWriteCounter)) == 200)
            return true;
        return false;
    }

    private static boolean getMyWriteCounter(DataAccount data) {
        String out = getData("/data/" + data.IDHash + "/" + data.sender + "/counter/write");
        if (!out.equals("null")) {
            data.myWriteCounter = Integer.parseInt(out);
            return true;
        }
        return false;
    }

    private static boolean setMyReadCounter(DataAccount data) {
        if (sendData("/data/" + data.IDHash + "/" + data.sender + "/counter/read", String.valueOf(data.myReadCounter)) == 200)
            return true;
        return false;
    }

    private static boolean getMyReadCounter(DataAccount data) {
        String out = getData("/data/" + data.IDHash + "/" + data.sender + "/counter/read");
        if (!out.equals("null")) {
            data.myReadCounter = Integer.parseInt(out);
            return true;
        }
        return false;
    }
}