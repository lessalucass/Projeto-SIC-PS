import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ParserObjeto {

    //H: nome(6) inicio(6) tamanho(6)
    public static class Header {
        public String nome;
        public int inicio;
        public int tamanho;
    }

    //D: (nome(6) endereco(6)) repetido
    public static class Simbolo {
        public String nome;
        public int endereco;
    }

    //T: inicio(6) tamanho(2) bytes
    public static class Text {
        public int inicio;
        public int tamanho;
        public String bytes;
    }

    //M: endereco(6) tamanho(2) sinal(1) simbolo(6)
    public static class Modificacao {
        public int endereco;
        public int tamanho;
        public char sinal = '+';
        public String simbolo = null;   // null = relocação simples
    }

    //Converte hexadecimal (String) para int
    private static int hex(String s) {
        return Integer.parseInt(s.trim(), 16);
    }

    //Remove os ^ caso o arquivo use
    private static String limpar(String linha) {
        return linha.replace("^", "");
    }

    public static Header parseH(String linha) {
        linha = limpar(linha);
        Header h = new Header();
        h.nome = linha.substring(1, 7).trim();
        h.inicio = hex(linha.substring(7, 13));
        h.tamanho = hex(linha.substring(13, 19));
        return h;
    }

    public static List<Simbolo> parseD(String linha) {
        linha = limpar(linha);
        List<Simbolo> lista = new ArrayList<>();
        for (int i = 1; i + 12 <= linha.length(); i += 12) {
            Simbolo s = new Simbolo();
            s.nome = linha.substring(i, i + 6).trim();
            s.endereco = hex(linha.substring(i + 6, i + 12));
            lista.add(s);
        }
        return lista;
    }

    public static List<String> parseR(String linha) {
        linha = limpar(linha);
        List<String> nomes = new ArrayList<>();
        for (int i = 1; i < linha.length(); i += 6) {
            String nome = linha.substring(i, Math.min(i + 6, linha.length())).trim();
            if (!nome.isEmpty()) nomes.add(nome);
        }
        return nomes;
    }

    public static Text parseT(String linha) {
        linha = limpar(linha);
        Text t = new Text();
        t.inicio = hex(linha.substring(1, 7));
        t.tamanho = hex(linha.substring(7, 9));
        t.bytes = linha.substring(9, 9 + t.tamanho * 2);
        return t;
    }

    public static Modificacao parseM(String linha) {
        linha = limpar(linha);
        Modificacao m = new Modificacao();
        m.endereco = hex(linha.substring(1, 7));
        m.tamanho = hex(linha.substring(7, 9));
        if (linha.length() > 9) {
            m.sinal = linha.charAt(9);
            m.simbolo = linha.substring(10).trim();
        }
        return m;
    }

    //E: endereco(6), opcional (-1 se não tiver)
    public static int parseE(String linha) {
        linha = limpar(linha);
        if (linha.length() <= 1) return -1;
        return hex(linha.substring(1, 7));
    }

    public static void lerArquivo(String caminho) throws IOException {
        for (String linha : Files.readAllLines(Paths.get(caminho))) {
            if (linha.isBlank()) continue;

            switch (linha.charAt(0)) {
                case 'H':
                    Header h = parseH(linha);
                    System.out.println("H: " + h.nome + " " + h.inicio + " " + h.tamanho);
                    break;
                case 'D':
                    for (Simbolo s : parseD(linha))
                        System.out.println("D: " + s.nome + " " + s.endereco);
                    break;
                case 'R':
                    System.out.println("R: " + parseR(linha));
                    break;
                case 'T':
                    Text t = parseT(linha);
                    System.out.println("T: " + t.inicio + " " + t.tamanho + " " + t.bytes);
                    break;
                case 'M':
                    Modificacao m = parseM(linha);
                    System.out.println("M: " + m.endereco + " " + m.tamanho + " " + m.sinal + m.simbolo);
                    break;
                case 'E':
                    System.out.println("E: " + parseE(linha));
                    break;
                default:
                    System.out.println("Registro desconhecido: " + linha);
            }
        }
    }
}
