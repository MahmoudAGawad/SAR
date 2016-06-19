package com.android.internal.telephony;

/**
 * Created by OmarElfarouk on 2/21/2016.
 */
public interface ITelephony {

    boolean endCall();

    void answerRingingCall();

    void silenceRinger();

}