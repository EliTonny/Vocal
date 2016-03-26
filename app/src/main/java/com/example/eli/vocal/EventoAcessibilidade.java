package com.example.eli.vocal;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Eli on 2/13/2016.
 */
public class EventoAcessibilidade extends AccessibilityService {
    //Constantes de acao
    private final int ACAO_LEITURA = 1;
    private final int ACAO_FALA    = 2;
    private final int ACAO_DESLIGA = 3;
    private final int ACAO_TOQUE   = 4;
    private final int ACAO_HOME    = 5;
    private final int ACAO_VOLTAR  = 6;
    private final int ACAO_BOTAO   = 7;

    //Constantes de tipo de componentes de tela
    private final String BOTAO         = "android.widget.Button";
    private final String EDIT_TEXT     = "android.widget.EditText";
    private final String SWITCH        = "android.widget.Switch";

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
        fraseRetorno = null;
    }

    private void inicializaAcoes(){
        acoes = new HashMap<>();
        //Acoes referentes a leitura
        acoes.put("Leia",ACAO_LEITURA);
        acoes.put("leia",ACAO_LEITURA);
        acoes.put("Ler",ACAO_LEITURA);
        acoes.put("ler",ACAO_LEITURA);
        acoes.put("Le",ACAO_LEITURA);
        acoes.put("le",ACAO_LEITURA);

        //Acoes referentes a fala
        acoes.put("Fala",ACAO_FALA);
        acoes.put("fala",ACAO_FALA);
        acoes.put("Fale",ACAO_FALA);
        acoes.put("fale",ACAO_FALA);
        acoes.put("Falar",ACAO_FALA);
        acoes.put("falar",ACAO_FALA);
        acoes.put("Diga",ACAO_FALA);
        acoes.put("diga",ACAO_FALA);

        //Acoes referentes a desligar
        acoes.put("Desliga",ACAO_DESLIGA);
        acoes.put("desliga",ACAO_DESLIGA);
        acoes.put("Desligar",ACAO_DESLIGA);
        acoes.put("desligar",ACAO_DESLIGA);
        acoes.put("Parar",ACAO_DESLIGA);
        acoes.put("parar",ACAO_DESLIGA);

        //Acoes referentes a tocar em um componente
        acoes.put("Toque",ACAO_TOQUE);
        acoes.put("toque",ACAO_TOQUE);
        acoes.put("Clique",ACAO_TOQUE);
        acoes.put("clique",ACAO_TOQUE);
        acoes.put("Abra",ACAO_TOQUE);
        acoes.put("abra",ACAO_TOQUE);
        acoes.put("Abre",ACAO_TOQUE);
        acoes.put("abre",ACAO_TOQUE);

        //Acoes para pressionar o botao HOME
        acoes.put("Início",ACAO_HOME);
        acoes.put("início",ACAO_HOME);
        acoes.put("Home",ACAO_HOME);
        acoes.put("home",ACAO_HOME);
        acoes.put("Casa",ACAO_HOME);
        acoes.put("casa",ACAO_HOME);

        //Acoes para pressionar o botao Voltar
        acoes.put("Voltar",ACAO_VOLTAR);
        acoes.put("voltar",ACAO_VOLTAR);

        //Acoes para pressionar botoes
        acoes.put("Botão",ACAO_BOTAO);
        acoes.put("botão",ACAO_BOTAO);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int tipoEvento = event.getEventType();
        //AccessibilityNodeInfo nodeInfo;
        //System.out.println(event.eventTypeToString(event.getEventType()) + " Source:" + (event.getSource() == null));

        switch (tipoEvento){
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                speaker.speak("Estou ouvindo", TextToSpeech.QUEUE_FLUSH, null);
                //System.out.println("toString: " + event.toString());

                //Iniciar intent para ouvir o usuario
                Intent intent = new Intent();
                intent.setClass(this, ReconhecedorDeVoz.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                //nodeInfo = event.getSource();
                //System.out.println(event.getSource().toString());
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                /*if (nodeInfo != null){
                    System.out.println(event.getSource().getContentDescription());
                    System.out.println(event.getSource().getChildCount());

                    for (int i = 0; i < nodeInfo.getChildCount();i++){
                        System.out.println(nodeInfo.getChild(i).toString());
                    }
                }*/
                if (this.fraseRetorno != null && event.getSource() != null){
                    //nodeInfo = event.getSource();
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
        //info.flags = AccessibilityServiceInfo.DEFAULT;
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS|AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        //info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED;
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
        String sujeito = frase[frase.length -1];
        int acao = 0;
        try{
            //Encontrar o agrupador da palavra de acao
            acao = acoes.get(frase[0]);
        } catch (Exception e){
            acao = 999;
        }

        //Determinar o que fazer na acao solicitada
        switch (acao){
            case ACAO_LEITURA:
                if (nodeInfo != null){
                    String textoTela = "";
                    textoTela = getTextoTela(nodeInfo);
                    System.out.println(textoTela);
                    speaker.speak(textoTela, TextToSpeech.QUEUE_FLUSH, null);
                }
                break;
            case ACAO_FALA:
                //Montar frase a ser dita pelo Vocal
                String fraseSolicitada = "";
                for (int i = 1;i < frase.length;i++){
                    fraseSolicitada = fraseSolicitada.concat(frase[i]).concat(" ");
                }
                speaker.speak(fraseSolicitada, TextToSpeech.QUEUE_FLUSH, null);
                break;
            case ACAO_DESLIGA:
                if ("Vocal".equalsIgnoreCase(sujeito)){
                    AccessibilityServiceInfo info = new AccessibilityServiceInfo();
                    info.flags = AccessibilityServiceInfo.DEFAULT;
                    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
                    info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED;
                    info.notificationTimeout = 100;

                    this.setServiceInfo(info);
                    speaker.speak("Estou desligando, até mais", TextToSpeech.QUEUE_FLUSH, null);
                }
                break;
            case ACAO_TOQUE:
                if (nodeInfo != null){
                    executaAcao(nodeInfo, sujeito, null);
                }
                break;
            case ACAO_HOME:
                System.out.println("Passou home");
                /*View view = new View(this);
                view.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_HOME));*/
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    System.out.println("Passou home33");
                    super.onKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HOME));
                    super.onKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_HOME));
                }*/

                break;
            case ACAO_VOLTAR:
                /*KeyEvent evento = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK);
                evento.dispatch(evento);
                System.out.println("Passou voltar222");*/
                /*View view2 = new View(this);
                Keyboard
                View kbview = new KeyboardView(this,null);*/
                /*System.out.println(view2.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK)));
                System.out.println(view2.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_BACK)));
                view2.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_BACK));
                view2.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK));*/
                System.out.println(nodeInfo.toString());
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,new Bundle());
                break;
            case ACAO_BOTAO:
                executaAcao(nodeInfo, sujeito, BOTAO);
                break;
            default:
                speaker.speak("Desculpe, não entendi", TextToSpeech.QUEUE_FLUSH, null);
                break;
        }
        this.fraseRetorno = null;
        nodeInfo.recycle();
    }

    public void setFraseRetorno(String frase){
        this.fraseRetorno = frase;
        System.out.println("Setou frase retorno: " + fraseRetorno);
    }

    private String getTextoTela(AccessibilityNodeInfo nodeInfo){
        String textoRetorno = "";
        if (nodeInfo == null){
            return textoRetorno;
        }

        if (nodeInfo.getChildCount() > 0){
            for (int i = 0; i < nodeInfo.getChildCount();i++){

                if (nodeInfo.getChild(i) != null){
                    System.out.println(nodeInfo.getChild(i).toString());

                    if (nodeInfo.getChild(i).getContentDescription() != null){
                        textoRetorno += getTextoClasse(nodeInfo.getChild(i));
                        textoRetorno += nodeInfo.getChild(i).getContentDescription() + " ";
                    } else if (nodeInfo.getChild(i).getText() != null){
                        textoRetorno += getTextoClasse(nodeInfo.getChild(i));
                        textoRetorno += nodeInfo.getChild(i).getText() + " ";
                    }
                }
            }

            for (int i = 0; i < nodeInfo.getChildCount();i++){
                textoRetorno += getTextoTela(nodeInfo.getChild(i));
            }
        }

        return textoRetorno;
    }

    private String converteNumeral(String numero){
        if ("um".equalsIgnoreCase(numero)){
            return "1";
        } else if ("dois".equalsIgnoreCase(numero)){
            return "2";
        } else if ("três".equalsIgnoreCase(numero)){
            return "3";
        } else if ("quatro".equalsIgnoreCase(numero)){
            return "4";
        } else if ("cinco".equalsIgnoreCase(numero)){
            return "5";
        } else if ("seis".equalsIgnoreCase(numero)){
            return "6";
        } else if ("sete".equalsIgnoreCase(numero)){
            return "7";
        } else if ("oito".equalsIgnoreCase(numero)){
            return "8";
        } else if ("nove".equalsIgnoreCase(numero)){
            return "9";
        } else if ("zero".equalsIgnoreCase(numero)){
            return "0";
        }

        return numero;
    }

    private String getTextoClasse(AccessibilityNodeInfo nodeInfo){
        String className = nodeInfo.getClassName().toString();
        if (className == null){
            return "";
        }
        if (className.equalsIgnoreCase(BOTAO)){
            return "botão ";
        } else if (className.equalsIgnoreCase(EDIT_TEXT)){
            return "campo de texto ";
        } else if (className.equalsIgnoreCase(SWITCH)){
            return "chave ";
        }
        return "";
    }

    private void executaAcao(AccessibilityNodeInfo nodeInfo, String sujeito, String tipoComponente){
        sujeito = converteNumeral(sujeito);
        List<AccessibilityNodeInfo> nos = nodeInfo.findAccessibilityNodeInfosByText(sujeito);
        AccessibilityNodeInfo no = null;
        System.out.println(sujeito);
        if (nos != null){
            for (int i = 0; i < nos.size();i++) {
                System.out.println(nos.get(i).toString());
            }
            try{
                if (tipoComponente != null){
                    //Remover os nos que nao sao da classe desejada pela acao
                    for (int i = 0; i < nos.size();i++) {
                        if (!tipoComponente.equalsIgnoreCase(nos.get(i).getClassName().toString())){
                            nos.remove(i);
                        }
                    }
                }

                //Se a busca na tela encontrar apenas um no, ele sofrera a acao
                if (nos.size() == 1){
                    no = nos.get(0);
                } else {
                    //Se a busca na tela encontrar mais de um no, eh preciso selecionar o no que tem o
                    // texto exatamente igual ao sujeito
                    for (int i = 0; i < nos.size();i++) {
                        if (sujeito.equalsIgnoreCase(nos.get(i).getText().toString())){
                            no = nos.get(i);
                            i = nos.size();
                        }
                    }
                }

                if (no.isClickable()){
                    no.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }catch (IndexOutOfBoundsException indexEx) {
                speaker.speak("Não encontrei na tela " + sujeito, TextToSpeech.QUEUE_FLUSH, null);
            } catch (Exception ex){
                speaker.speak("Erro de tela desconhecido", TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }
}