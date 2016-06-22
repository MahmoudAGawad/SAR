package utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.javacv.facerecognition.FdActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import ai.api.model.Fulfillment;
import ai.api.model.Result;
import sendingemail.SendEmail;
import texttospeach.TextToSpeechHelper;

/**
 * Created by wido on 2/8/2016.
 */
public class CommandExecution {

    private static final String TAG = "CommandExecuter";
    private Result result;
    private Context context;

    private FileOutputStream fos;
    private FileInputStream fis;
    private BufferedReader bufferedReader;
    private HashMap<String, String> tasksToRemind;
    private BufferedWriter write;
    private CameraBridgeViewBase.CvCameraViewFrame currentFrame;

    private TextToSpeechHelper textToSpeechHelper;

    private HashSet<String> cameraCommands;

    public CommandExecution(TextToSpeechHelper textToSpeechHelper , Context context){

        currentFrame = null;
        tasksToRemind = new HashMap<>();
        cameraCommands = new HashSet<>();
        cameraCommands.addAll(Arrays.asList(new String[]{"take photo", "take a photo", "take a picture", "take picture",
                "sar take photo", "sar take a photo", "sar take picture", "sar take a picture"}));

        this.textToSpeechHelper = textToSpeechHelper;
        this.context = context;

        try {
//            fos =
             write = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory() + File.separator + "SAR/tasks.txt"));
        }
        catch (IOException e){
            e.printStackTrace();
        }

        try {
            fis = new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + "SAR/tasks.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            bufferedReader = new BufferedReader(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


// Test
    }

    public void setResult(Result result , Context context){

        this.result=result;
        this.context = context;
    }

    public void speak(String text){

        textToSpeechHelper.speak(text);
        while (textToSpeechHelper.isSpeaking());

    }


    public void executeCommand(){
        //translation commands also needs to be handled
        if (result.getAction().startsWith("small")
                || result.getAction().startsWith("wisdom")){
            doTalk(result);
            return;
        }

        switch (result.getAction()) {

            case "email.read":
                doReading(result);
                break;
            case "email.write":
                doSending(result);
                break;
            case "email.edit":
                doEditing(result);
                break;
            case "apps.open":
                doOpenning(result, context);
                break;
            case "facebook.update":
                doPostOnFacebook(result, context);
                break;
            case "news.search":
                checkNews(result);
                break;
            case "translate.text":
                translateSentence(result);
                break;
            case "notifications.add":
                doAddingReminder(result);
                break;
            case "notifications.search":
                doSearchingForReminder(result);
                break;
            case "input.unknown":
                handleUnsupportedFeature(result);
                break;
            default:speak("i can't help you with that");
                break;


        }

    }


    private void handleUnsupportedFeature(Result result){
        Fulfillment elem =result.getFulfillment();
        String speech = elem.getSpeech();
        if (speech.equals("")){
            speak("i didn't understand what you said");
            return;
        }
        speak(speech);
    }

    private void doReading(Result result) {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
//        speak("inside Method");
        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            String userEmail = FdActivity.getUserEmail();
            String password = FdActivity.getUserPassword();
            store.connect("imap.gmail.com", "hamo220022@gmail.com", "159753221993");
            // store.connect("imap.gmail.com", userEmail, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message msg = inbox.getMessage(1);
            Address[] in = msg.getFrom();
//            speak("before loop");
            for (Address address : in) {
                System.out.println("FROM:" + address.toString());
                StringTokenizer str = new StringTokenizer(address.toString(),"<");
                speak("from "+str.nextToken());
            }

            Multipart mp = (Multipart) msg.getContent();
            BodyPart bp = mp.getBodyPart(0);

            System.out.println("SENT DATE:" + msg.getSentDate());
            System.out.println("SUBJECT:" + msg.getSubject());
            System.out.println("CONTENT:" + bp.getContent());
            textToSpeechHelper.speak(msg.getSubject()+"");
            while (textToSpeechHelper.isSpeaking());


        } catch (Exception mex) {
//            speak(mex.toString());
            Log.e("MYAPP", "exception: " + mex.toString());

            mex.printStackTrace();

        }


    }



    private void translateSentence(Result result) {
        String text = ""+result.getFulfillment().getSpeech();
        textToSpeechHelper.speak(text);
        while (textToSpeechHelper.isSpeaking());



    }

    private void doAddingReminder(Result result) {

        HashMap<String, JsonElement> hm = result.getParameters();

        JsonElement summary = hm.get("summary");

        if (summary != null) {

            String TaskToRemind = summary.toString();

            String toCompare = TaskToRemind.toLowerCase();
            if (!tasksToRemind.containsValue(toCompare)) {

                try {

                    write = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory() + File.separator + "SAR/tasks.txt", true));

                    write.append(TaskToRemind+"\n");
                    tasksToRemind.put(TaskToRemind, TaskToRemind);
//                    fos.write(TaskToRemind.getBytes());
//                    fos.write('\n');
                    write.close();
                    speak(TaskToRemind + " is added");

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }


        }


    }

    private void doSearchingForReminder(Result result) {

        HashMap<String, JsonElement> hm = result.getParameters();


        JsonElement summary = hm.get("summary");


        if (summary != null) {
            String taskToSearch = summary.toString();

            String toCompare = taskToSearch.toLowerCase();



            boolean found = false;
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.trim().toLowerCase().equals(toCompare))
                        found = true;
                    break;

                }

                if (found) {
                    speak("Yes,  i plan to remind you about " + taskToSearch);
                } else {

                    speak("No, you do not ask me to remind you about  " + taskToSearch);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


            //

        }

        JsonElement allNotifiacations = hm.get("all");

        String allNotify = "";
        if (allNotifiacations != null)
            allNotify = allNotifiacations.toString();

        if (allNotify.contains("true")) {


            String line;
            StringBuilder stringBuilder = new StringBuilder();

            try {
                fis = new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + "SAR/tasks.txt");
                InputStreamReader isr = new InputStreamReader(fis);
                bufferedReader = new BufferedReader(isr);
                while ((line = bufferedReader.readLine()) != null) {

                    stringBuilder.append(line);
                    stringBuilder.append("   ");

                }

                speak("you want me to remind you about  " + stringBuilder.toString());


            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            catch (IOException e) {
                e.printStackTrace();
            }


        }


    }



    private void checkNews(Result result) {
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                String key = entry.getKey();
                String URL = "";
                if(key.equals("topic") || key.equals("keyword")){
                    String topic = entry.getValue().getAsString();
                    if(topic.equals("business")){
                        URL = "b";
                    }
                    else if(topic.startsWith("Sport")){
                        URL = "s";
                    }
                    else if(topic.equals("election")){
                        URL = "el";
                    }

                    else if(topic.equals("tech")){
                        URL = "tc";
                    }
                    else if(topic.equals("science")){
                        URL = "snc";
                    }
                    else if(topic.equals("entertainment")){
                        URL = "e";
                    }
                    else URL = "w";




                }
                String link = "https://news.google.com/news?ned=us&hl=en&topic="+URL+"&output=rss";

//                                        String world = "w",sport = "s" , election = "el" , business = "b" , technology = "tc" , entertain = "e"
//                            ,science = "snc" , health = "m";
                HandleXML handle = new HandleXML(link);
                handle.fetchXML();
                while (handle.parsingComplete);

                // now we have the news
                int count = 0;
                StringTokenizer str = new StringTokenizer(handle.getTitle() , "\n");

                while (str.hasMoreTokens() && count < 3){
                    String news = str.nextToken();
                    textToSpeechHelper.speak(news);
                    while (textToSpeechHelper.isSpeaking());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    count++;
                }

            }
        }
    }

    private void doTalk(Result result) {
        Fulfillment elem =result.getFulfillment();
        String speech = elem.getSpeech();
        if (speech.equals("")){
            speak("i didn't understand what you said");
            return;
        }

        StringBuilder builder = new StringBuilder();
        short cnt = 2;
        for (int i = 0; i < speech.length() && cnt > 0; i++){
            if(speech.charAt(i) == '.'){
                cnt--;
            }
            builder.append(speech.charAt(i));
        }

        textToSpeechHelper.speak(builder.toString());
        while (textToSpeechHelper.isSpeaking());


    }

    private boolean savePicture(){
        File pictureFile = getOutputMediaFile();
        Log.e(TAG, "heey " + pictureFile);
        if (pictureFile == null) {
            // error
            return false;
        }

        Mat matPic = currentFrame.rgba();
        Bitmap pic = Bitmap.createBitmap(matPic.cols(), matPic.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matPic, pic);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            pic.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        return true;
    }

    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() +File.separator+
                "SAR"+File.separator+"Images");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        Log.e(TAG, "1111111111111111111111111111111111111111111111111111");
        if (! mediaStorageDir.exists()){
            return null;
        }
        Log.e(TAG, "222222222222222222222222222222222222222222222222222");
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="SAR_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    private void doOpenning(Result result , Context context) {

        String query = result.getResolvedQuery();

        if(cameraCommands.contains(query)){
            if(currentFrame != null){

                Log.d(TAG, "savvvvvvvvvvvvvvving piccccccccccccccccccccccccccccccc " + savePicture());
            }
        }else if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                if(entry.getKey().equalsIgnoreCase("app_name")){
                    Log.e("Testing here :" , "wid");

                    Log.e("Testing here :" , entry.getValue().getAsString());
                    final PackageManager pm = context.getPackageManager();

                    List<PackageInfo> packs = pm.getInstalledPackages(0);
                    final List<ApplicationInfo> apps = pm.getInstalledApplications(0);

                    ArrayList<HashMap<String,Object>> items =new ArrayList<HashMap<String,Object>>();

                    for (ApplicationInfo pi : apps) {
//                        Log.e("Package : ", pi.loadLabel(pm) + "");
                        if( (pi.loadLabel(pm)+"").toLowerCase().contains(entry.getValue().getAsString().toLowerCase())){
                            HashMap<String, Object> map = new HashMap<String, Object>();
                            map.put("appName", pi.loadLabel(pm));
                            map.put("packageName", pi.packageName);
                            items.add(map);

                        }
                    }
                    if(items.size()>=1){
                        String toSpeak = "now opening "+entry.getValue().getAsString();
                        textToSpeechHelper.speak(toSpeak);
                        for(int j = 1 ; j <= items.size() ; j++){
                            String packageName = (String) items.get(j-1).get("packageName");
                            Intent i = pm.getLaunchIntentForPackage(packageName);
                            if (i != null ) {
                                context.startActivity(i);
                                break;
                            }

                        }
                    }
                }

            }
        }
    }


    private void doEditing(Result result) {

        try {

            String newMessage="";

            HashMap<String, JsonElement> hm = result.getParameters();
            JsonElement mes = hm.get("message");
            JsonElement value= hm.get("value");

            newMessage=value.toString();
            newMessage=newMessage.replace("\"", "");

            Log.e("value", value.toString());
            Log.e("messageeee", mes.toString());
            JsonElement rec = hm.get("recipient");
            JsonArray arr = rec.getAsJsonArray();
            JsonElement el = arr.get(0);
            JsonObject ob = el.getAsJsonObject();
            Log.e("rec name", ob.get("recipient_name").toString());
            String reciever=ob.get("recipient_name").toString();
               reciever=reciever.replace("\"","");
           newMessage=newMessage.substring(4);
            newMessage=newMessage.replace(reciever,"");

            String recEmail = "";

            if (ob.get("recipient_name").toString().equalsIgnoreCase("\"mostafa\"")) {
                recEmail = "mostafahamdy6@gmail.com";
                new SendEmail().execute(recEmail, newMessage);
            }

            if (ob.get("recipient_name").toString().equalsIgnoreCase("\"waleed\"")) {
                recEmail = "waleed.adel.mahmoud@gmail.com";
                new SendEmail().execute(recEmail, newMessage);
            }

            if (ob.get("recipient_name").toString().equalsIgnoreCase("\"computer\"")) {
                recEmail = "waleed.adel.mahmoud@gmail.com";
                new SendEmail().execute(recEmail, newMessage);
            }
        }
        catch (Exception e){
            Log.e("exception here ", e.toString());
        }
    }






    private void doSending(Result result) {
        try {
            HashMap<String, JsonElement> hm = result.getParameters();
            JsonElement mes = hm.get("message");
            Log.e("messageeee", mes.toString());
            JsonElement rec = hm.get("recipient");
            JsonArray arr = rec.getAsJsonArray();
            JsonElement el = arr.get(0);
            JsonObject ob = el.getAsJsonObject();
            Log.e("rec name", ob.get("recipient_name").toString());
            String reciever=ob.get("recipient_name").toString();

            String recEmail = "";

            if (reciever.equalsIgnoreCase("\"mostafa\"")) {
                recEmail = "mostafahamdy6@gmail.com";
                new SendEmail().execute(recEmail, mes.toString());
            }

            if (reciever.equalsIgnoreCase("\"waleed\"")) {
                recEmail = "waleed.adel.mahmoud@gmail.com";
                new SendEmail().execute(recEmail, mes.toString());
            }

            if (reciever.equalsIgnoreCase("\"computer\"")) {
                recEmail = "waleed.adel.mahmoud@gmail.com";
                new SendEmail().execute(recEmail, mes.toString());
            }

            speak("email has been sent");
        }
        catch (Exception e){
            speak("something went wrong, i couldn't send the email");
         Log.e("exception here ", e.toString());
        }
    }






     private void doPostOnFacebook(Result result, Context context){

        HashMap<String, JsonElement> parameters = result.getParameters();
        if(parameters == null){
        //   java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
        //            Toast.makeText(context, "Nothing to post on facebook. Cancelling...", Toast.LENGTH_SHORT).show();
        }else{
            JsonElement toBePosted = parameters.get("text");
            if(toBePosted == null){
//                Toast.makeText(context, "Nothing to post on facebook. Cancelling...", Toast.LENGTH_SHORT).show();
            }else {
                // https://www.youtube.com/watch?v=-fs_PL-fLOY
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if (!accessToken.getPermissions().contains("publish_actions")) {
                    // (Activity) context? error?
                    LoginManager.getInstance().logInWithPublishPermissions((Activity) context, Arrays.asList("publish_actions"));
                }

            /* Graph API request example:
                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                // Application code
                                try {
                                    Object name = object.get("name");
                                    System.out.println("nameeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee: "+ name.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle requestParameters = new Bundle();
                requestParameters.putString("fields", "id,name,link");
                request.setParameters(requestParameters);
                request.executeAsync();
                */
                GraphRequest request = GraphRequest.newPostRequest(accessToken, "me/feed", null,
                        new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                Log.d("facebook.update", "Successfully Posted on facebook!");
                            }
                        });

                Bundle postParams = request.getParameters();
                postParams.putString("message", toBePosted.getAsString());
                request.setParameters(postParams);
                request.executeAsync();
                speak("facebook status updated");

            }
        }
    }


    public void setCurrentFrame(CameraBridgeViewBase.CvCameraViewFrame currentFrame) {
        this.currentFrame = currentFrame;
    }
}
