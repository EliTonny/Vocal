package com.example.eli.vocal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import java.util.ArrayList;

/**
 * Author: Eli T. de Souza
 * Date: 17/02/2016.
 * Resume: Classe para a chamado do servico de reconhecimento de voz
 */
public class ReconhecedorDeVoz extends Activity {
    private static final int REQUEST_CODE = 1234;
    private EventoAcessibilidade eventoAcessibilidade;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        eventoAcessibilidade = EventoAcessibilidade.getInstance();
        reconhecer();
    }

    public void reconhecer(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Reconhecedor de Voz");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            ArrayList<String> resultados = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            //eventoAcessibilidade.retornoReconhecimento(resultados.get(0));
            System.out.println(resultados.toString());
            eventoAcessibilidade.setFraseRetorno(resultados.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
        this.finish();

    }
}
