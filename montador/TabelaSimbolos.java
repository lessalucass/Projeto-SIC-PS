package montador;

import java.util.HashMap;


public class TabelaSimbolos {

    private final HashMap<String, Integer> simbolos = new HashMap<>();

    public int tamanho() {
        return simbolos.size();
    }

    public boolean contem(String nome) {
        return simbolos.containsKey(nome);
    }

    /** Nome de rótulo válido: letra seguida de letras, dígitos ou _. */
    public static boolean nomeValido(String nome) {
        if (nome == null || nome.isEmpty()) {
            return false;
        }

        char primeiro = nome.charAt(0);
        if (!((primeiro >= 'A' && primeiro <= 'Z') || (primeiro >= 'a' && primeiro <= 'z'))) {
            return false;
        }

        for (int i = 1; i < nome.length(); i++) {
            char c = nome.charAt(i);
            boolean letra = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
            boolean digito = c >= '0' && c <= '9';
            if (!letra && !digito && c != '_') {
                return false;
            }
        }
        return true;
    }

    /** Usada no Passo 1: erro se o nome for inválido ou já estiver definido. */
    public void inserir(String nome, int valor) {
        if (!nomeValido(nome)) {
            throw new IllegalArgumentException("Nome de símbolo inválido: " + nome);
        }
        if (simbolos.containsKey(nome)) {
            throw new IllegalArgumentException("Símbolo definido mais de uma vez: " + nome);
        }
        simbolos.put(nome, valor);
    }

    /** Usada no Passo 2: erro se o símbolo não foi definido. */
    public int buscar(String nome) {
        Integer valor = simbolos.get(nome);
        if (valor == null) {
            throw new IllegalArgumentException("Símbolo indefinido: " + nome);
        }
        return valor;
    }

    public void limpar() {
        simbolos.clear();
    }
}