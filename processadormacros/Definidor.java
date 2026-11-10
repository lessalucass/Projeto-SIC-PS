package processadormacros;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import montador.Analisador;
import montador.LeitorFonte;

public class Definidor {
    private static final Pattern PARAMETRO = Pattern.compile("&[A-Za-z0-9_]+");

    private Definidor() {}

    /** Define a macro lendo as linhas do arquivo fonte */
    public static String definir(String linhaPrototipo, LeitorFonte leitor) {
        return definir(linhaPrototipo, () -> leitor.lerLinha(), leitor.getNumeroLinha());
    }

    /** Define a macro lendo as linhas de qualquer fonte arquivo ou corpo de outra macro */
    public static String definir(String linhaPrototipo, Supplier<String> proximaLinha, int linhaAbertura) {
        String nome = Analisador.getRotulo(linhaPrototipo);
        if (nome == null || nome.isEmpty()) {
            throw new IllegalArgumentException(
                    "Macro sem nome na linha " + linhaAbertura + ".");
        }
        nome = nome.toUpperCase();
        if (TabelasMacro.TAB_NOME.containsKey(nome)) {
            throw new IllegalArgumentException(
                    "Macro definida mais de uma vez: " + nome);
        }

        List<String> parametros = ParserArgumentos.extrairParametros(Analisador.getOperando(linhaPrototipo));

        int inicio = TabelasMacro.TAB_DEFINICAO.size();
        int nivel = 1;
        String linha;
        while (nivel > 0 && (linha = proximaLinha.get()) != null) {
            if (Analisador.linhaVazia(linha)) {
                continue; // nao gasta espaco da tabela com linhas em branco/comentario
            }
            String operacao = Analisador.getOperacao(linha);
            if (operacao.equals("MACRO")) {
                nivel++;
            } else if (operacao.equals("MEND")) {
                nivel--;
                if (nivel == 0) {
                    continue; // o MEND final nao entra no corpo
                }
            }
            TabelasMacro.TAB_DEFINICAO.add(substituirParametros(linha, parametros));
        }

        if (nivel > 0) {
            // desfaz o que foi guardado pela macro incompleta
            TabelasMacro.TAB_DEFINICAO.subList(inicio, TabelasMacro.TAB_DEFINICAO.size()).clear();
            throw new IllegalArgumentException(
                    "Macro " + nome + " (linha " + linhaAbertura + ") sem MEND correspondente.");
        }

        int fim = TabelasMacro.TAB_DEFINICAO.size();
        TabelasMacro.TAB_NOME.put(nome, new int[] { inicio, fim, parametros.size() });
        return nome;
    }

    /** "&A,&B" -> ["&A", "&B"] (em maiusculas). Vazio -> lista vazia. */
    private static List<String> lerParametros(String operando) {
        List<String> lista = new ArrayList<>();
        if (operando == null || operando.isEmpty()) {
            return lista;
        }
        for (String p : operando.split(",", -1)) {
            p = p.trim().toUpperCase();
            if (!PARAMETRO.matcher(p).matches()) {
                throw new IllegalArgumentException(
                        "Parametro de macro invalido (esperado &NOME): '" + p + "'");
            }
            if (lista.contains(p)) {
                throw new IllegalArgumentException("Parametro repetido na macro: " + p);
            }
            lista.add(p);
        }
        return lista;
    }

    /** Troca cada &PARAM do prototipo por ?posicao; outros &X ficam como estao. */
    private static String substituirParametros(String linha, List<String> parametros) {
        if (parametros.isEmpty()) {
            return linha;
        }
        Matcher m = PARAMETRO.matcher(linha);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            int pos = parametros.indexOf(m.group().toUpperCase());
            String troca = pos >= 0 ? "?" + (pos + 1) : m.group();
            m.appendReplacement(sb, Matcher.quoteReplacement(troca));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
