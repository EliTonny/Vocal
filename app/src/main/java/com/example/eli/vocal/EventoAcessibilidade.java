package com.example.eli.vocal;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Author: Eli T. de Souza
 * Date: 13/02/2016.
 * Resume: Classe principal
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
    private final int ACAO_ESCREVA = 8;
    private final int ACAO_BUSCAR  = 9;
    private final int ACAO_DISCAR  = 10;
    private final int ACAO_ROLAR   = 11;
    private final int ACAO_LETRA   = 12;
    private final int ACAO_COLAR   = 13;
    private final int ACAO_LIMPAR  = 14;
    private final int ACAO_TESTE   = 99;

    //Constantes de tipo de componentes de tela
    private final String BOTAO         = "Button";
    private final String EDIT_TEXT     = "EditText";
    private final String TEXT          = "Text";
    private final String SWITCH        = "Switch";
    private final String IMAGE_VIEW    = "ImageView";
    private final String FRAME_LAYOUT  = "FrameLayout";

    private static EventoAcessibilidade eventoAcessibilidade;

    private String fraseRetorno;
    private String textoBuffer;
    private TextToSpeech speaker;
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
        fraseRetorno = null;
        textoBuffer = "";
    }

    private void inicializaAcoes(){
        acoes = new HashMap<>();
        //Acoes referentes a leitura
        acoes.put("leia",ACAO_LEITURA);
        acoes.put("ler",ACAO_LEITURA);
        acoes.put("le",ACAO_LEITURA);

        //Acoes referentes a fala
        acoes.put("fala",ACAO_FALA);
        acoes.put("fale",ACAO_FALA);
        acoes.put("falar",ACAO_FALA);
        acoes.put("diga",ACAO_FALA);

        //Acoes referentes a desligar
        acoes.put("desliga",ACAO_DESLIGA);
        acoes.put("desligar",ACAO_DESLIGA);
        acoes.put("parar",ACAO_DESLIGA);

        //Acoes referentes a tocar em um componente
        acoes.put("toque",ACAO_TOQUE);
        acoes.put("clique",ACAO_TOQUE);
        acoes.put("abra",ACAO_TOQUE);
        acoes.put("abre",ACAO_TOQUE);

        //Acoes para pressionar o botao HOME
        acoes.put("início",ACAO_HOME);
        acoes.put("home",ACAO_HOME);
        acoes.put("casa",ACAO_HOME);

        //Acoes para pressionar o botao Voltar
        acoes.put("voltar",ACAO_VOLTAR);

        //Acoes para pressionar botoes
        acoes.put("botão",ACAO_BOTAO);

        //Acoes para escrever em campos de texto
        acoes.put("escreva",ACAO_ESCREVA);
        acoes.put("escrever",ACAO_ESCREVA);
        acoes.put("escreve",ACAO_ESCREVA);

        //Acoes para buscar conversar no Whatsapp
        acoes.put("buscar", ACAO_BUSCAR);

        //Acoes para digitar telefones no teclado
        acoes.put("discar", ACAO_DISCAR);

        //Acoes para rolar a tela
        acoes.put("rolar", ACAO_ROLAR);
        acoes.put("ir", ACAO_ROLAR);

        acoes.put("letra", ACAO_LETRA);
        acoes.put("colar", ACAO_COLAR);
        acoes.put("limpar", ACAO_LIMPAR);

        acoes.put("teste", ACAO_TESTE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int tipoEvento = event.getEventType();
        //System.out.println(event.eventTypeToString(event.getEventType()) + " Source:" + (event.getSource() == null));
        switch (tipoEvento){
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                if (speaker.isSpeaking()){
                    speaker.stop();
                }

                //Iniciar intent para ouvir o usuario
                Intent intent = new Intent();
                intent.setClass(this, ReconhecedorDeVoz.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (event.getSource() != null){
                    if (this.fraseRetorno != null && (!event.getPackageName().equals("com.example.eli.vocal"))){
                        this.retornoReconhecimento(fraseRetorno, event.getSource());
                    }
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
        //Parametros de inicializacao
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS|AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.notificationTimeout = 100;

        this.setServiceInfo(info);
    }

    public void retornoReconhecimento(String retorno, AccessibilityNodeInfo nodeInfo){
        if(retorno == null){
            return;
        }

        //Dividir a frase dita pelo usuario para encontrar a acao solicitada
        String[] frase = retorno.split(" ");
        String sujeito = frase[frase.length -1];
        int acao;
        try{
            //Encontrar o agrupador da palavra de acao
            acao = acoes.get(frase[0].toLowerCase());
        } catch (Exception e){
            acao = 999;
        }

        //Determinar o que fazer na acao solicitada
        switch (acao){
            case ACAO_LEITURA:
                //Ler todos os textos da tela
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
                //Tornar a aplicacao inativa
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
                    executaAcao(nodeInfo, sujeito, null, AccessibilityNodeInfo.ACTION_CLICK, null, false);
                }
                break;
            case ACAO_HOME:
                //Ir para o home
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
                break;
            case ACAO_VOLTAR:
                break;
            case ACAO_BOTAO:
                executaAcao(nodeInfo, sujeito, BOTAO, AccessibilityNodeInfo.ACTION_CLICK, null, false);
                break;
            case ACAO_ESCREVA:
                //Montar frase a ser escrita pelo Vocal
                String fraseEditText = "";
                for (int i = 1;i < frase.length - 1;i++){
                    fraseEditText = fraseEditText.concat(frase[i]).concat(" ");
                }

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", fraseEditText);
                clipboard.setPrimaryClip(clip);
                executaAcao(nodeInfo, sujeito, TEXT, AccessibilityNodeInfo.ACTION_FOCUS, null, true);
                executaAcao(nodeInfo, sujeito, TEXT, AccessibilityNodeInfo.ACTION_PASTE, null, true);
                break;
            case ACAO_BUSCAR:
                executaAcao(nodeInfo, sujeito, IMAGE_VIEW, AccessibilityNodeInfo.ACTION_CLICK, null, false);
                break;
            case ACAO_DISCAR:
                String numeroDiscar = "";
                for (int i = 1; i < frase.length; i++){
                    numeroDiscar += converteNumeral(frase[i]);
                }
                char[] numeros = numeroDiscar.toCharArray();
                executaAcao(nodeInfo, "espaço", BOTAO, AccessibilityNodeInfo.ACTION_LONG_CLICK, null, false);
                for (int i = 0; i < numeros.length; i++){
                    if (numeros[i] != '-' && numeros[i] != ' '){
                        executaAcao(nodeInfo, String.valueOf(numeros[i]), FRAME_LAYOUT, AccessibilityNodeInfo.ACTION_CLICK, null, false);
                    }
                }
                executaAcao(nodeInfo, "discar", BOTAO, AccessibilityNodeInfo.ACTION_CLICK, null, false);
                break;
            case ACAO_ROLAR:
                List<AccessibilityNodeInfo> nos = encontraNoScrollable(nodeInfo, null);

                if (nos.size() > 0){
                    for (int i = 0; i < nos.size(); i++){
                        if("frente".equalsIgnoreCase(sujeito)){
                            nos.get(i).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        } else if ("trás".equalsIgnoreCase(sujeito)){
                            nos.get(i).performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
                        }
                    }
                }
                break;
            case ACAO_LETRA:
                //Concatenar a primeira letra da primeira palavra na variavel de memoria
                if (frase.length > 1){
                    speaker.speak(frase[1], TextToSpeech.QUEUE_FLUSH, null);
                    frase[1] = converteNumeral(frase[1]);
                    frase[1] = convertePontuacao(frase[1]);
                    char[] palavra = frase[1].toCharArray();
                    textoBuffer = textoBuffer.concat(String.valueOf(palavra[0]).toLowerCase());
                }
                break;
            case ACAO_LIMPAR:
                //Limpar a variavel de memoria
                if ("tudo".equalsIgnoreCase(sujeito)){
                    textoBuffer = "";
                } else if ("letra".equalsIgnoreCase(sujeito)){
                    if (textoBuffer.length() > 0){
                        textoBuffer = textoBuffer.substring(0,textoBuffer.length() - 1);
                    }
                }
                break;
            case ACAO_COLAR:
                //Colar o texto em memoria em um campo de texto da tela
                ClipboardManager clipboardColar = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipColar = ClipData.newPlainText("label", textoBuffer);
                clipboardColar.setPrimaryClip(clipColar);
                executaAcao(nodeInfo, sujeito, TEXT, AccessibilityNodeInfo.ACTION_FOCUS, null, true);
                executaAcao(nodeInfo, sujeito, TEXT, AccessibilityNodeInfo.ACTION_PASTE, null, true);
                break;
            case ACAO_TESTE:
                /*Bundle arguments = new Bundle();
                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                                 AccessibilityNodeInfo.MOVEMENT_GRANULARITY_CHARACTER);
                arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                        false);
                executaAcao(nodeInfo, sujeito, null, AccessibilityNodeInfo.ACTION_NEXT_AT_MOVEMENT_GRANULARITY,arguments);*/
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
        } else if ("asterisco".equalsIgnoreCase(numero)){
            return "*";
        }

        return numero;
    }

    private String convertePontuacao(String sinal){
        if ("ponto".equalsIgnoreCase(sinal)){
            return ".";
        } else if ("exclamação".equalsIgnoreCase(sinal)){
            return "!";
        } else if ("interrogação".equalsIgnoreCase(sinal)){
            return "?";
        } else if ("arroba".equalsIgnoreCase(sinal)){
            return "@";
        } else if ("sustenido".equalsIgnoreCase(sinal)){
            return "#";
        } else if ("percentual".equalsIgnoreCase(sinal)){
            return "%";
        } else if ("traço".equalsIgnoreCase(sinal)){
            return "-";
        } else if ("vírgula".equalsIgnoreCase(sinal)){
            return ",";
        }
        return sinal;
    }

    private String getTextoClasse(AccessibilityNodeInfo nodeInfo){
        if (nodeInfo.getClassName() == null){
            return "";
        }

        String className = nodeInfo.getClassName().toString();

        if (className.contains(BOTAO)){
            return "botão ";
        } else if (className.contains(EDIT_TEXT)){
            return "campo de texto ";
        } else if (className.contains(SWITCH)){
            return "chave ";
        }
        return "";
    }

    private void executaAcao(AccessibilityNodeInfo nodeInfo, String sujeito, String tipoComponente, int action, Bundle arguments, boolean focus){
        sujeito = converteNumeral(sujeito);
        System.out.println(sujeito);
        //List<AccessibilityNodeInfo> nos = nodeInfo.findAccessibilityNodeInfosByText(sujeito);
        List<AccessibilityNodeInfo> nos = encontraNoTela(nodeInfo, sujeito, tipoComponente);
        System.out.println(sujeito);
        AccessibilityNodeInfo no = null;

        if (nos != null){
            for (int i = 0; i < nos.size();i++) {
                System.out.println(nos.get(i).toString());
            }
            try{
                if (focus){
                    //Executar a acao no primeiro no focusable
                    for (int i = 0; i < nos.size();i++) {
                        if (nos.get(i).isFocusable()){
                            no = nos.get(i);
                            i = nos.size();
                        }
                    }
                } else {
                    //Executar a acao no primeiro no encontrado
                    no = nos.get(0);
                }

                if (no != null){
                    if (arguments == null){
                        no.performAction(action);
                    } else {
                        System.out.println(no.performAction(action, arguments));
                    }
                }
            }catch (IndexOutOfBoundsException indexEx) {
                speaker.speak("Não encontrei na tela " + sujeito, TextToSpeech.QUEUE_FLUSH, null);
            } catch (Exception ex){
                speaker.speak("Erro de tela desconhecido", TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    private List<AccessibilityNodeInfo> encontraNoTela(AccessibilityNodeInfo nodeInfo, String texto, String tipoComponente){
        List<AccessibilityNodeInfo> nos = new ArrayList<>();
        texto = texto.toLowerCase();
        String textoNo = "";
        if (nodeInfo == null){
            return nos;
        }

        if (nodeInfo.getChildCount() > 0) {
            //Adicionar os nos com o texto procurado no ArrayList de nos
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                textoNo = "";
                if (nodeInfo.getChild(i) != null){
                    if (nodeInfo.getChild(i).getText() != null){
                        textoNo += nodeInfo.getChild(i).getText().toString();
                    }
                    if (nodeInfo.getChild(i).getContentDescription() != null){
                        textoNo += nodeInfo.getChild(i).getContentDescription().toString();
                    }

                    textoNo = textoNo.toLowerCase();

                    if (textoNo.contains(texto)){
                        if (tipoComponente == null){
                            System.out.println("Add no: " + nodeInfo.getChild(i).toString());
                            nos.add(nodeInfo.getChild(i));
                        } else if (nodeInfo.getChild(i).getClassName().toString().contains(tipoComponente)){
                            System.out.println("Add no: " + nodeInfo.getChild(i).toString());
                            nos.add(nodeInfo.getChild(i));
                        }
                    }
                    //Buscar os demais nos relacionados com o texto solicitado recursivamente
                    nos.addAll(encontraNoTela(nodeInfo.getChild(i),texto, tipoComponente));
                }
            }
        }
        return nos;
    }

    private List<AccessibilityNodeInfo> encontraNoScrollable(AccessibilityNodeInfo nodeInfo, String tipoComponente){
        List<AccessibilityNodeInfo> nos = new ArrayList<>();
        if (nodeInfo == null){
            return nos;
        }

        if (nodeInfo.getChildCount() > 0) {
            //Adicionar todos os nos scrollable no ArrayList de nos
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                if (nodeInfo.getChild(i) != null){
                    if (nodeInfo.getChild(i).isScrollable()){
                        if (tipoComponente == null){
                            System.out.println("Add no: " + nodeInfo.getChild(i).toString());
                            nos.add(nodeInfo.getChild(i));
                        } else if (nodeInfo.getChild(i).getClassName().toString().contains(tipoComponente)){
                            System.out.println("Add no: " + nodeInfo.getChild(i).toString());
                            nos.add(nodeInfo.getChild(i));
                        }
                    }
                    //Buscar recursivamente os demais nos scrollable relacionados
                    nos.addAll(encontraNoScrollable(nodeInfo.getChild(i), tipoComponente));
                }
            }
        }
        return nos;
    }
}