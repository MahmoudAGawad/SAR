package utilities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private TextToSpeechHelper textToSpeechHelper;
    public CommandExecution(TextToSpeechHelper textToSpeechHelper , Context context){

        this.textToSpeechHelper = textToSpeechHelper;
        this.context = context;

// Test
    }

    public void setResult(Result result , Context context){

        this.result=result;
        this.context = context;
    }

    public void executeCommand(){

        //translation commands also needs to be handled
        if (result.getAction().startsWith("small")
                || result.getAction().startsWith("wisdom")){
            doTalk(result);
            return;
        }

        switch (result.getAction()) {
            case "email.write":
                doSending(result);
               break;
            case "email.edit":
                doEditing(result);
                break;
            case "apps.open":
                doOpenning(result , context);
                break;


        }
    }

    private void doTalk(Result result) {
        Fulfillment elem =result.getFulfillment();
        String speach = elem.getSpeech();

        textToSpeechHelper.speak(speach);
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
                            Log.d("debuggggg" , "inside loop");

                            if (i != null ) {
                                context.startActivity(i);
                                Log.d("debuggggg" , "inside if");

                                break;
                            }
//                        else
//                            textView.setText("Not found2");

                        }


                    }
                    else{
                        // Application not found
//                        textView.setText("Not found");
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


}
