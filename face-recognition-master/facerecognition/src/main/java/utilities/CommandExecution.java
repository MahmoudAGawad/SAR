package utilities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
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
import org.opencv.javacv.facerecognition.AlarmService;
import org.opencv.javacv.facerecognition.FdActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

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

    private Result result;
    private Context context;

    private FileOutputStream fos;
    private FileInputStream fis;
    private BufferedReader bufferedReader;
    private HashMap<String, String> tasksToRemind = new HashMap<String, String>();

    private TextToSpeechHelper textToSpeechHelper;

    private TelephonyManager manager;
    private StatePhoneReceiver myPhoneStateListener;

    boolean callFromApp = false; // To control the call has been made from the application
    boolean callFromOffHook = false; // To control the change to idle state is from the app call

    public CommandExecution(TextToSpeechHelper textToSpeechHelper , Context context){

        this.textToSpeechHelper = textToSpeechHelper;
        this.context = context;

        try {
            fos = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + "SAR/tasks.txt");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fis = new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + "SAR/tasks.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            bufferedReader = new BufferedReader(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        myPhoneStateListener = new StatePhoneReceiver(context);
        manager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
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
                Log.e("reading my emailllllll", "here");
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
            case "notifications.add":
                doAddingReminder(result);
                break;
            case "notifications.search":
                doSearchingForReminder(result);
                break;
            case "call.call":
                makeCall();
                break;
            case "clock.alarm_set":
                setAlarm();
                break;
        }

    }

    public String getContact(String name){

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                "lower(" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ")=lower('" + name + "')", null, null);
        StringBuffer temp=new StringBuffer();
        String phoneNumber= "400";
        if (cursor.moveToFirst()) {

            Log.d("Contact1","Enter IF");
            String contactId =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            //
            //  Get all phone numbers.
            //
            Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);

            while (phones.moveToNext()) {
                String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                Log.d("Contact1",type+" : "+number);
                if(!number.isEmpty()) {
                    temp.append(number + " : ");
                    String num = number.replaceAll("\\s", "");
                    if(phoneNumber.equals("400")){
                        phoneNumber = num;
                    }
                    if (num.matches("012.*")) {
                        return num;
                    }
                }
            }
            phones.close();
        }
        cursor.close();
        Log.d("Contact1", temp.toString());

        android.os.Handler mUiHandler = new android.os.Handler(Looper.getMainLooper());
        final String temp1 = name + ":" + phoneNumber;
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,temp1 , Toast.LENGTH_LONG).show();
            }
        });
        return phoneNumber;
    }

    private void makeCall() {
        String para;
        try {
            para  = result.getParameters().get("name_first").getAsString();
        }catch (Exception e){
            para  = result.getParameters().get("q").getAsString();
        }
        String number = getContact(para);
        Log.d("parameter1",para+" : "+para.length());
        Log.d("parameter2",number);
        manager.listen(myPhoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE); // start listening to the phone changes
        callFromApp = true;
        Intent i = new Intent(android.content.Intent.ACTION_CALL,
                Uri.parse("tel:" + number)); // Make the call

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        context.startActivity(i);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent intent1 = new Intent("org.opencv.javacv.facerecognition.StartUpActivity");
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);
    }

    private void setAlarm() {
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }
        try {
            String time = parameterString.split("T")[1];
            String time1 = time.split(":")[0];
            Log.d("parameter123", time.split(":")[0]+":"+time.split(":")[1]);
            Intent serviceIntent = new Intent(context, AlarmService.class);
            serviceIntent.putExtra("time", time.split(":")[0]+":"+time.split(":")[1]);
            context.startService(serviceIntent);
        } catch (Exception e) {
        }
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
                    tasksToRemind.put(TaskToRemind, TaskToRemind);
                    fos.write(TaskToRemind.getBytes());
                    fos.write('\n');
                    textToSpeechHelper.speak(TaskToRemind + " is added");

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
                    textToSpeechHelper.speak("Yes,  i plan to remind you about " + taskToSearch);
                } else {

                    textToSpeechHelper.speak("No, you do not ask me to remind you about  " + taskToSearch);
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

                while ((line = bufferedReader.readLine()) != null) {

                    stringBuilder.append(line);
                    stringBuilder.append("   ");

                }

                textToSpeechHelper.speak("you want me to remind you about  " + stringBuilder.toString());


            } catch (IOException e) {
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

    private void doOpenning(Result result , Context context) {
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
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
        }
        catch (Exception e){
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
            }
        }
    }
    public class StatePhoneReceiver extends PhoneStateListener {
        Context context;

        public StatePhoneReceiver(Context context) {
            this.context = context;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {

                case TelephonyManager.CALL_STATE_OFFHOOK: //Call is established
                    if (callFromApp) {
                        callFromApp = false;
                        callFromOffHook = true;

                        try {
                            Thread.sleep(500); // Delay 0,5 seconds to handle better turning on loudspeaker
                        } catch (InterruptedException e) {
                        }

                        //Activate loudspeaker
                        AudioManager audioManager = (AudioManager)
                                context.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                        audioManager.setSpeakerphoneOn(true);
                    }
                    break;

                case TelephonyManager.CALL_STATE_IDLE: //Call is finished
                    if (callFromOffHook) {
                        callFromOffHook = false;
                        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_NORMAL); //Deactivate loudspeaker
                        manager.listen(myPhoneStateListener, // Remove listener
                                PhoneStateListener.LISTEN_NONE);
                    }
                    break;
            }
        }
    }


}
