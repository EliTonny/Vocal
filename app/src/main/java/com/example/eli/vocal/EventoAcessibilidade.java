package com.example.eli.vocal;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by Eli on 2/13/2016.
 */
public class EventoAcessibilidade extends AccessibilityService {
    private TextToSpeech speaker;
    private ReconhecedorDeVoz reconhecedor;

    @Override
    public void onCreate(){
        reconhecedor = new ReconhecedorDeVoz();
        speaker = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                speaker.setLanguage(Locale.getDefault());
            }
        });
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        speaker.speak("Estou ouvindo",TextToSpeech.QUEUE_FLUSH,null);

        Intent intent = new Intent();
        intent.setClass(this,ReconhecedorDeVoz.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);
    }
}
