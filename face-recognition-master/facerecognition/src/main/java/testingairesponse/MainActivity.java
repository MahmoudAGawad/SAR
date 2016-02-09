package testingairesponse;


import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;

import org.opencv.javacv.facerecognition.R;

import java.util.Map;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import texttospeach.TextToSpeechHelper;
import utilities.CommandExecution;


public class MainActivity extends ListeningActivity{

    private LinearLayout content;
    private TextView text , textResult;
    CommandExecution commandExecuter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextToSpeechHelper textToSpeechHelper = new TextToSpeechHelper(getApplicationContext());
        commandExecuter = new CommandExecution(textToSpeechHelper , getApplicationContext());
        setContentView(R.layout.activity_main);
//        text = (TextView)findViewById(R.id.text);
//        textResult = (TextView) findViewById(R.id.result);
//        text.setText("widooooooooooo");
        content = (LinearLayout)findViewById(R.id.commands);

        // The following 3 lines are needed in every onCreate method of a ListeningActivity
        context = getApplicationContext(); // Needs to be set
        VoiceRecognitionListener.getInstance().setListener(this); // Here we set the current listener
        startListening(); // starts listening

    }
    @Override
    public void processVoiceCommands(String... voiceCommands) {
//        content.removeAllViews();
//        for (String command : voiceCommands) {
//            TextView txt = new TextView(getApplicationContext());
//            txt.setText(command);
//            txt.setTextSize(20);
//            txt.setTextColor(Color.BLACK);
//            txt.setGravity(Gravity.CENTER);
//            content.addView(txt);
//        }
//        restartListeningService();




        text = (TextView)findViewById(R.id.text);
        textResult = (TextView) findViewById(R.id.result);
        text.setText(voiceCommands[0]);


        final AIConfiguration config = new AIConfiguration("a7ee7ac49bac4559b295d1c38a18812f",
                "9a44c559-6daa-45b7-adc4-375c71de82d7", AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);


//        final AIConfiguration config = new AIConfiguration("a7ee7ac49bac4559b295d1c38a18812f",
//                "9a44c559-6daa-45b7-adc4-375c71de82d7",
//                AIConfiguration.SupportedLanguages.English,
//                AIConfiguration.RecognitionEngine.System);
        final AIDataService aiDataService = new AIDataService(context , config);

        final AIRequest aiRequest = new AIRequest();
//


//
        aiRequest.setQuery(voiceCommands[0]);
        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                final AIRequest request = requests[0];
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }
            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse != null) {
                    // process aiResponse here
                    final Result result = aiResponse.getResult();

// Get parameters
                    String parameterString = "";
                    if (result.getParameters() != null && !result.getParameters().isEmpty()) {
                        for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                            parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
                        }
                    }

                    Log.e("Testing here :" , "widooooooooooooooooo");




                    textResult.setText("helooooooooooo");
// Show results in TextView.
                    textResult.setText("Query:" + result.getResolvedQuery() +
                            "\nAction: " + result.getAction() +
                            "\nParameters: " + parameterString);



                    checkResult(result);


                }
                restartListeningService();

            }

        }.execute(aiRequest);

//                restartListeningService();


    }


    private void checkResult(Result result) {
        commandExecuter.setResult(result , this);
        commandExecuter.executeCommand();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    protected void onPause() {

//        restartListeningService();

        super.onPause();


    }


    @Override
    protected void onResume() {
        restartListeningService();

        super.onResume();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


























}
