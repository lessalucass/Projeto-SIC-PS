package montador;

public class Analisador {

    private Analisador() {}

    public static String getRotulo(String linha) {
        return separar(linha)[0];
    }

    public static String getOperacao(String linha) {
        return separar(linha)[1];
    }

    public static String getOperando(String linha) {
        return separar(linha)[2];
    }

    /** Linha vazia ou só com comentário. */
    public static boolean linhaVazia(String linha) {
        return removerComentario(linha).trim().isEmpty();
    }

    /** Devolve {rotulo, operacao, operando}. */
    private static String[] separar(String linha) {
        String s = removerComentario(linha);
        // remove espaços do fim, mas preserva os do início (indicam "sem rótulo")
        int fim = s.length();
        while (fim > 0 && Character.isWhitespace(s.charAt(fim - 1))) {
            fim--;
        }
        s = s.substring(0, fim);

        if (s.isEmpty()) {
            return new String[] { null, "", "" };
        }

        int i = 0;
        int n = s.length();

        // Rótulo: começa na primeira coluna
        String rotulo = null;
        if (!Character.isWhitespace(s.charAt(0))) {
            while (i < n && !Character.isWhitespace(s.charAt(i))) {
                i++;
            }
            rotulo = s.substring(0, i);
            if (rotulo.endsWith(":")) {
                rotulo = rotulo.substring(0, rotulo.length() - 1);
            }
        }

        // Operação
        while (i < n && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        int inicioOp = i;
        while (i < n && !Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        String operacao = s.substring(inicioOp, i).toUpperCase();

        // Operando: o resto da linha
        String operando = normalizarOperando(s.substring(i));

        return new String[] { rotulo, operacao, operando };
    }

    /** Remove o comentário, respeitando aspas simples. */
    private static String removerComentario(String linha) {
        if (linha == null) {
            return "";
        }
        boolean emAspas = false;
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '\'') {
                emAspas = !emAspas;
            } else if (c == ';' && !emAspas) {
                return linha.substring(0, i);
            }
        }
        return linha;
    }

    /** Tira espaços fora de aspas simples. */
    private static String normalizarOperando(String texto) {
        StringBuilder sb = new StringBuilder();
        boolean emAspas = false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '\'') {
                emAspas = !emAspas;
            }
            if (!emAspas && Character.isWhitespace(c)) {
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}