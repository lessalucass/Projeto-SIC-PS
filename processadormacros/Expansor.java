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
        List<String> argumentos = ParserArgumentos.extrairArgumentos(Analisador.getOperando(linhaChamada));

        if (argumentos.size() != dados[2]) {
            throw new IllegalArgumentException(
                    "Macro " + nome + " espera " + dados[2] + " argumento(s), recebeu " + argumentos.size() + ".");
        }

        String prefixoUnico = GeradorRotulos.gerarPrefixo();
        List<String> rotulosLocais = lerRotulosLocais(dados[0], dados[1]);

        int[] pos = { dados[0] };
        while (pos[0] < dados[1]) {
            String linha = proximaLinha(pos, dados[1], argumentos);
            if (linha == null) {
                break;
            }
            if (Analisador.getOperacao(linha).equals("MACRO")) {
                // definicao aninhada: registra a macro interna
                TabelasMacro.TAB_NOME.remove(Analisador.getRotulo(linha).toUpperCase());
                Definidor.definir(linha, () -> proximaLinha(pos, dados[1], argumentos), 0);
            } else if (ehChamada(linha)) {
                // chamada aninhada: os argumentos tambem recebem o prefixo
                saida.addAll(expandir(trocarReferencias(linha, rotulosLocais, prefixoUnico)));
            } else {
                String linhaComRotuloTratado = linha;
                if (rotulosLocais.contains(Analisador.getRotulo(linha))) {
                    linhaComRotuloTratado = prefixoUnico + linha; // rotulo fica na coluna 0
                }
                saida.add(trocarReferencias(linhaComRotuloTratado, rotulosLocais, prefixoUnico));
            }
        }

        // rotulo da chamada vai para a primeira linha expandida
        String rotulo = Analisador.getRotulo(linhaChamada);
        if (rotulo != null && !saida.isEmpty()) {
            if (Character.isWhitespace(saida.get(0).charAt(0))) {
                saida.set(0, rotulo + saida.get(0));
            } else {
                saida.add(0, rotulo); // primeira linha ja tem rotulo proprio
            }
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

    /** Rotulos definidos no corpo da macro (ignora macros internas, chamadas e parametros). */
    private static List<String> lerRotulosLocais(int inicio, int fim) {
        List<String> lista = new ArrayList<>();
        int nivel = 0;
        for (int i = inicio; i < fim; i++) {
            String linha = TabelasMacro.TAB_DEFINICAO.get(i);
            String operacao = Analisador.getOperacao(linha);
            if (operacao.equals("MACRO")) {
                nivel++;
            } else if (operacao.equals("MEND")) {
                nivel--;
            } else if (nivel == 0 && !TabelasMacro.TAB_NOME.containsKey(operacao)) {
                String rotulo = Analisador.getRotulo(linha);
                if (rotulo != null && !rotulo.isEmpty() && !rotulo.startsWith("?")) {
                    lista.add(rotulo);
                }
            }
        }
        return lista;
    }

    /** Coloca o prefixo nas referencias aos rotulos locais (depois do rotulo da linha). */
    private static String trocarReferencias(String linha, List<String> rotulosLocais, String prefixo) {
        int inicio = 0;
        while (inicio < linha.length() && !Character.isWhitespace(linha.charAt(inicio))) {
            inicio++; // pula o rotulo da linha
        }
        String resto = linha.substring(inicio);
        for (String r : rotulosLocais) {
            resto = resto.replaceAll("\\b" + Pattern.quote(r) + "\\b", Matcher.quoteReplacement(prefixo + r));
        }
        return linha.substring(0, inicio) + resto;
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