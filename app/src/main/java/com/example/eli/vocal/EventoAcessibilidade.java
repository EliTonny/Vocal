package com.example.eli.vocal;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Eli on 2/13/2016.
 */
public class EventoAcessibilidade extends AccessibilityService {
    //Constantes de acao
    private final int LEITURA = 1;
    private final int FALA    = 2;

    private TextToSpeech speaker;
    private static EventoAcessibilidade eventoAcessibilidade;
    private HashMap<String,Integer> acoes;
    public static EventoAcessibilidade getInstance(){
        return eventoAcessibilidade;
    }

    @Override
    public void onCreate(){
        speaker = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                speaker.setLanguage(Locale.getDefault());
            }
        });
        eventoAcessibilidade = this;
        inicializaAcoes();
    }

    private void inicializaAcoes(){
        acoes = new HashMap<>();
        //Acoes referentes a leitura
        acoes.put("Leia",LEITURA);
        acoes.put("leia",LEITURA);
        acoes.put("Ler",LEITURA);
        acoes.put("ler",LEITURA);
        acoes.put("Le",LEITURA);
        acoes.put("le",LEITURA);

        //Acoes referentes a fala
        acoes.put("Fala",FALA);
        acoes.put("fala",FALA);
        acoes.put("Fale",FALA);
        acoes.put("fale",FALA);
        acoes.put("Falar",FALA);
        acoes.put("falar",FALA);
        acoes.put("Diga",FALA);
        acoes.put("diga",FALA);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int tipoEvento = event.getEventType();
        AccessibilityNodeInfo nodeInfo = event.getSource();
        if (nodeInfo == null){
            return;
        }

        switch (tipoEvento){
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                speaker.speak("Estou ouvindo", TextToSpeech.QUEUE_FLUSH, null);
                //Iniciar intent para ouvir o usuario
                Intent intent = new Intent();
                intent.setClass(this, ReconhecedorDeVoz.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            default:
                break;
        }

        nodeInfo.recycle();

        //AccessibilityNodeInfo pai = nodeInfo.getParent();
        /*
        switch (tipoEvento){
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                //speaker.speak("Estou ouvindo",TextToSpeech.QUEUE_FLUSH,null);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

                    AccessibilityNodeInfo filho;
                    Integer qtdFilhos = pai.getChildCount();
                    speaker.speak(qtdFilhos.toString(),TextToSpeech.QUEUE_FLUSH,null);

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    /*for (int i=0;i < pai.getChildCount();i++){
                        filho = pai.getChild(i);
                        //filho.get;
                        if (filho.toString() != null){
                            speaker.speak(filho.getClass().getCanonicalName(), TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            speaker.speak("Merda",TextToSpeech.QUEUE_FLUSH,null);
                        }

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        filho.recycle();
                    }*   /
                }
                break;
        }
        pai.recycle();
        nodeInfo.recycle();*/
    }

    @Override
    public void onInterrupt() {

    }

    public void retornoReconhecimento(String retorno){
        if(retorno == null){
            System.out.println("Retorno null");
            return;
        }
        //Dividir a frase dita pelo usuario para encontrar a acao solicitada
        String[] frase = retorno.split(" ");
        int acao = 0;
        try{
            //Encontrar o agrupador da palavra de acao
            acao = acoes.get(frase[0]);
        } catch (Exception e){
            acao = 999;
        }

        //Determinar o que fazer na acao solicitada
        switch (acao){
            case LEITURA:
                break;
            case FALA:
                //Montar frase a ser dita pelo Vocal
                String fraseSolicitada = "";
                for (int i = 1;i < frase.length;i++){
                    fraseSolicitada = fraseSolicitada.concat(frase[i]).concat(" ");
                }
                speaker.speak(fraseSolicitada, TextToSpeech.QUEUE_FLUSH, null);
                break;
            default:
                speaker.speak("Desculpe, não entendi", TextToSpeech.QUEUE_FLUSH, null);
                break;
        }
    }
}
