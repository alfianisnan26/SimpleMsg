import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.Desktop;

public class Database {
    private static final String databaseURL = "https://simplemessengger-default-rtdb.firebaseio.com/";;

    private static String getData(String target) {
        final String getEndpoint = databaseURL + target + ".json";
        var request = HttpRequest.newBuilder().uri(URI.create(getEndpoint)).GET().build();

        var client = HttpClient.newHttpClient();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("GET Status Code: " + response.statusCode());
            return response.body();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static boolean downloadFile(String url, File file) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download file: " + response);
            }
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
            fos.write(response.body().bytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String uploadFile(File file) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("reqtype", "fileupload")
                .addFormDataPart("fileToUpload", file.getAbsolutePath(), RequestBody
                        .create(new File(file.getAbsolutePath()), MediaType.parse("application/octet-stream")))
                .build();
        Request request = new Request.Builder().url("https://catbox.moe/user/api.php").method("POST", body).build();
        Response resp = null;
        try {
            resp = client.newCall(request).execute();
        } catch (IOException e) {

            e.printStackTrace();
        }
        try {
            return resp.body().string();
        } catch (IOException e) {

            e.printStackTrace();
        }
        return "null";
    }

    public static void open(File document) {
        Desktop dt = Desktop.getDesktop();
        try {
            dt.open(document);
        } catch (IOException e) {

            e.printStackTrace();
        }
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

    public static String getContact(String username) {
        username = getData("users/" + username + "/contact");
        return username.substring(1, username.length() - 1);
    }

    public static boolean writeContact(String username, String contact) {
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
        if (sendData("/data/" + data.IDHash + "/" + data.sender + "/chat/" + data.myWriteCounter,
                "\"" + text + "\"") == 200) {
            data.myWriteCounter++;
            if (setMyWriteCounter(data)) {
                return true;
            } else {
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
            data.myReadCounter++;
            if (!setMyReadCounter(data)) {
                data.myReadCounter--;
                return "null";
            }
            return out;
        }
    }

    public static boolean initChat(DataAccount data) {
        if (getMyWriteCounter(data)) {
            if (getMyReadCounter(data)) {
                System.out.println(
                        "Writer Counter : " + data.myWriteCounter + "\nReader Counter : " + data.myReadCounter);
                return true;
            }
        }
        return false;
    }

    private static boolean setMyWriteCounter(DataAccount data) {
        if (sendData("/data/" + data.IDHash + "/" + data.sender + "/counter/write",
                String.valueOf(data.myWriteCounter)) == 200)
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
        if (sendData("/data/" + data.IDHash + "/" + data.sender + "/counter/read",
                String.valueOf(data.myReadCounter)) == 200)
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