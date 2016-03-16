package com.example.eli.vocal;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Eli on 2/13/2016.
 */
public class EventoAcessibilidade extends AccessibilityService {
    //Constantes de acao
    private final int LEITURA = 1;
    private final int FALA    = 2;
    private final int DESLIGA = 3;

    private static EventoAcessibilidade eventoAcessibilidade;

    private String fraseRetorno;
    private TextToSpeech speaker;
    private HashMap<String,Integer> acoes;
    //private AccessibilityNodeInfo nodeInfo;

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
        fraseRetorno = "";
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

        //Acoes referentes a desligar
        acoes.put("Desliga",DESLIGA);
        acoes.put("desliga",DESLIGA);
        acoes.put("Desligar",DESLIGA);
        acoes.put("desligar",DESLIGA);
        acoes.put("Parar",DESLIGA);
        acoes.put("parar",DESLIGA);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int tipoEvento = event.getEventType();
        AccessibilityNodeInfo nodeInfo;
        System.out.println(event.eventTypeToString(event.getEventType()) + " Source:" + (event.getSource() == null));

        switch (tipoEvento){
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                speaker.speak("Estou ouvindo", TextToSpeech.QUEUE_FLUSH, null);
                System.out.println("toString: " + event.toString());

                //Iniciar intent para ouvir o usuario
                Intent intent = new Intent();
                intent.setClass(this, ReconhecedorDeVoz.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                /*if (nodeInfo != null) {
                    nodeInfo.recycle();
                }*/
                break;
            /*case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                //nodeInfo = event.getSource();
                System.out.println(event.getSource().toString());
                break;*/
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                System.out.println(event.getSource().getContentDescription());
                System.out.println(event.getSource().getChildCount());
                nodeInfo = event.getSource();
                for (int i = 0; i < nodeInfo.getChildCount();i++){
                    System.out.println(nodeInfo.getChild(i).toString());
                }
                if (this.fraseRetorno != null){
                    this.retornoReconhecimento(fraseRetorno, event.getSource());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public  void onDestroy(){
        speaker.shutdown();
    }

    @Override
    public void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS|AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.notificationTimeout = 100;

        this.setServiceInfo(info);
    }

    public void retornoReconhecimento(String retorno, AccessibilityNodeInfo nodeInfo){
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
                String textoTela = "";
                textoTela = getTextoTela(nodeInfo);
                speaker.speak(textoTela, TextToSpeech.QUEUE_FLUSH, null);
                /*if(nodeInfo != null){
                    System.out.println(nodeInfo.getContentDescription());
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else{
                    System.out.println("Nodeinfo null");
                }*/
                break;
            case FALA:
                //Montar frase a ser dita pelo Vocal
                String fraseSolicitada = "";
                for (int i = 1;i < frase.length;i++){
                    fraseSolicitada = fraseSolicitada.concat(frase[i]).concat(" ");
                }
                speaker.speak(fraseSolicitada, TextToSpeech.QUEUE_FLUSH, null);
                break;
            case DESLIGA:
                if (frase.length > 1){
                    if ("Vocal".equalsIgnoreCase(frase[1])){
                        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
                        info.flags = AccessibilityServiceInfo.DEFAULT;
                        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
                        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED;
                        info.notificationTimeout = 100;

                        this.setServiceInfo(info);
                        speaker.speak("Estou desligando, até mais", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                break;
            default:
                speaker.speak("Desculpe, não entendi", TextToSpeech.QUEUE_FLUSH, null);
                break;
        }
    }

    public void setFraseRetorno(String frase){
        fraseRetorno = frase;
    }

    private String getTextoTela(AccessibilityNodeInfo nodeInfo){
        String textoRetorno = "";
        if (nodeInfo.getParent() != null){
            textoRetorno = getTextoTela(nodeInfo.getParent());
        }

        if (nodeInfo.getChildCount() > 0){
            for (int i = 0; i < nodeInfo.getChildCount();i++){
                if (nodeInfo.getChild(i).getText() != null){
                    textoRetorno += nodeInfo.getChild(i).getText() + " ";
                }

                if (nodeInfo.getChild(i).getContentDescription() != null){
                    textoRetorno += nodeInfo.getChild(i).getContentDescription() + " ";
                }
            }
        }
        return textoRetorno;
    }
}