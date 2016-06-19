package testingairesponse;

/**
 * Created by wido on 2/7/2016.
 */


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

public abstract class ListeningActivity extends Activity implements IVoiceControl {

    protected SpeechRecognizer sr;
    protected Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // starts the service
    protected void startListening() {
        try {
            initSpeech();
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            if (!intent.hasExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE))
            {
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                        "com.dummy");
            }
            sr.startListening(intent);
        } catch(Exception ex) {
            Log.d("SpeechRecognitio", "Bei ");
        }
    }

    // stops the service
    protected void stopListening() {
        if (sr != null) {
            sr.stopListening();
            sr.cancel();
            sr.destroy();
        }
        sr = null;
    }

    protected void initSpeech() {
        if (sr == null) {
            sr = SpeechRecognizer.createSpeechRecognizer(this);
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Toast.makeText(context, "Speech Recognition is not available",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            sr.setRecognitionListener(VoiceRecognitionListener.getInstance());
        }
    }

    @Override
    public void finish() {
        stopListening();
        super.finish();
    }

    @Override
    protected void onResume(){
        startListening();
        super.onResume();
    }

    @Override
    protected void onStop() {
        stopListening();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (sr != null) {
            sr.stopListening();
            sr.cancel();
            sr.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if(sr!=null){
            sr.stopListening();
            sr.cancel();
            sr.destroy();
        }
        sr = null;

        super.onPause();
    }

    //is abstract so the inheriting classes need to implement it. Here you put y    our code which should be executed once a command was found
    @Override
    public abstract void processVoiceCommands(String ... voiceCommands);

    @Override
    public void restartListeningService() {
        stopListening();
        startListening();
    }
}
