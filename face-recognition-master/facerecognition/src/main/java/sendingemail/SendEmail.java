package sendingemail;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by wido on 2/8/2016.
 */


public class SendEmail extends AsyncTask<String, Void, Integer>
{

    @Override
    protected void onPreExecute()
    {
        // Dialog.setMessage("Doing something...");
        //       Dialog.show();
        Log.e("111", "beforeee");
    }

    @Override
    protected Integer doInBackground(String... params)
    {
        Log.e("2","beforeee");

        //Task for sending mail
        try {
            GmailSender sender = new GmailSender("sar.ai.assistant@gmail.com", "sarrobot123");

            Log.e("emaillllll",params[0]);
            Log.e("bodyyyy",params[1]);

            sender.sendMail("This is Subject",
                    params[1],
                    "sar.ai.assistant@gmail.com",
                    params[0]);

            Log.e("3333", "beforeee");


        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer result)
    {

        if(result==0)
        {
            Log.e("555", "after");

        }

    }


}
