package processadormacros;

import montador.Analisador;

public class GeradorRotulos {
    
    private static int contador = 0;

    /** Reinicia a contagem (útil ao iniciar o processamento). */
    public static void reiniciar() {
        contador = 0;
    }

    /**
     * Gera um prefixo único para cada expansão de macro (ex: $A0_, $A1_, ...).
     */
    public static String gerarPrefixo() {
        String prefixo = String.format("$A%X_", contador);
        contador++;
        return prefixo;
    }

    /**
     * Aplica o prefixo único aos rótulos locais encontrados nas linhas da macro durante a expansão.
     * Ignora linhas vazias ou linhas cujos rótulos sejam parâmetros (começam com &).
     */
    public static String aplicarPrefixo(String linha, String prefixoUnico) {
        if (linha == null || linha.trim().isEmpty()) {
            return linha;
        }

        String rotulo = Analisador.getRotulo(linha);
        
        // Se a linha tem um rótulo e ele não é um parâmetro formal (ex: &X)
        if (rotulo != null && !rotulo.isEmpty() && !rotulo.startsWith("&")) {
            // Substitui o rótulo original pelo rótulo com o prefixo único
            return linha.replaceFirst(rotulo, prefixoUnico + rotulo);
        }

        return linha;
    }
}