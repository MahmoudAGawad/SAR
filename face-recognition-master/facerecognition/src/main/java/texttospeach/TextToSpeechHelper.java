package texttospeach;



/**
 * Created by Abdallh Abasery and Mahmoud Abd-Elgwad on 2/8/2016.
 */

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TextToSpeechHelper {
    TextToSpeech textToSpeech;
    private boolean isReadyToSpeak;

    public  TextToSpeechHelper(Context context){

        isReadyToSpeak = false;

        textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    isReadyToSpeak = true;
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });
    }

    public boolean isReadyToSpeak(){
        return  isReadyToSpeak;
    }



    public void speak(String toSpeak){
        while (!isReadyToSpeak){}

        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }
    public boolean isSpeaking(){
        return textToSpeech.isSpeaking();
    }

    protected  void closeSpeaking(){
        if(textToSpeech !=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }


}
