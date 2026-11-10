package processadormacros;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import montador.Analisador;

public class Expansor {
    private static final Pattern POSICAO = Pattern.compile("\\?(\\d+)");

    private Expansor() {}

    /** Diz se a linha chama uma macro ja definida. */
    public static boolean ehChamada(String linha) {
        String operacao = Analisador.getOperacao(linha);
        return operacao != null && TabelasMacro.TAB_NOME.containsKey(operacao.toUpperCase());
    }

    /** Recebe a linha de chamada e devolve as linhas da macro ja expandidas. */
    public static List<String> expandir(String linhaChamada) {
        List<String> saida = new ArrayList<>();

        String nome = Analisador.getOperacao(linhaChamada).toUpperCase();
        int[] dados = TabelasMacro.TAB_NOME.get(nome);
        List<String> argumentos = lerArgumentos(Analisador.getOperando(linhaChamada));

        if (argumentos.size() != dados[2]) {
            throw new IllegalArgumentException(
                    "Macro " + nome + " espera " + dados[2] + " argumento(s), recebeu " + argumentos.size() + ".");
        }

        int[] pos = { dados[0] };
        while (pos[0] < dados[1]) {
            String linha = proximaLinha(pos, dados[1], argumentos);
            if (Analisador.getOperacao(linha).equals("MACRO")) {
                // definicao aninhada: registra a macro interna
                TabelasMacro.TAB_NOME.remove(Analisador.getRotulo(linha).toUpperCase());
                Definidor.definir(linha, () -> proximaLinha(pos, dados[1], argumentos), 0);
            } else if (ehChamada(linha)) {
                saida.addAll(expandir(linha)); // chamada aninhada
            } else {
                saida.add(linha);
            }
        }

        // rotulo da chamada vai para a primeira linha expandida
        String rotulo = Analisador.getRotulo(linhaChamada);
        if (rotulo != null && !saida.isEmpty()) {
            saida.set(0, rotulo + saida.get(0));
        }
        return saida;
    }

    /** Devolve a proxima linha do corpo ja com os argumentos trocados ou null no fim. */
    private static String proximaLinha(int[] pos, int fim, List<String> argumentos) {
        if (pos[0] >= fim) {
            return null;
        }
        String linha = substituirArgumentos(TabelasMacro.TAB_DEFINICAO.get(pos[0]), argumentos);
        pos[0]++;
        return linha;
    }

    /** "A, B" -> ["A", "B"]. Vazio -> lista vazia. */
    private static List<String> lerArgumentos(String operando) {
        List<String> lista = new ArrayList<>();
        if (operando == null || operando.isEmpty()) {
            return lista;
        }
        for (String a : operando.split(",", -1)) {
            lista.add(a.trim());
        }
        return lista;
    }

    /** Troca cada ?n pelo n-esimo argumento. */
    private static String substituirArgumentos(String linha, List<String> argumentos) {
        Matcher m = POSICAO.matcher(linha);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            int pos = Integer.parseInt(m.group(1)) - 1;
            String troca = pos >= 0 && pos < argumentos.size() ? argumentos.get(pos) : m.group();
            m.appendReplacement(sb, Matcher.quoteReplacement(troca));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}