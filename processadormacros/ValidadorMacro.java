
package processadormacros;

import java.util.List;
import montador.Analisador;

public class ValidadorMacro {

    private ValidadorMacro() {}

    // Verifica se a macro possui um nome válido
    public static void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Erro: nome da macro não informado.");
        }

        if (!nome.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException(
                    "Erro: nome de macro inválido: " + nome);
        }
    }

    // Verifica se a macro já foi definida
    public static void validarDuplicidade(String nome) {
        validarNome(nome);

        if (TabelasMacro.TAB_NOME.containsKey(
                nome.toUpperCase())) {
            throw new IllegalArgumentException(
                    "Erro: macro " + nome +
                            " já foi definida.");
        }
    }

    // Verifica se a quantidade de argumentos está correta
    public static void validarArgumentos(
            String nome, int esperados, int recebidos) {

        if (esperados != recebidos) {
            throw new IllegalArgumentException(
                    "Erro na macro " + nome +
                            ": esperados " + esperados +
                            " argumento(s), recebidos " +
                            recebidos + ".");
        }
    }

    // Verifica se a definição terminou com MEND
    public static void validarMend(
            String nome, int nivel, int linhaAbertura) {

        if (nivel > 0) {
            throw new IllegalArgumentException(
                    "Erro: macro " + nome +
                            " iniciada na linha " + linhaAbertura +
                            " sem MEND correspondente.");
        }
    }

    // Verifica a sintaxe dos parâmetros da definição
    public static void validarParametros(String operando) {
        ParserArgumentos.extrairParametros(operando);
    }

    // Valida uma definição antes de cadastrá-la
    public static void validarDefinicao(String linha) {
        String nome = Analisador.getRotulo(linha);

        validarNome(nome);
        validarDuplicidade(nome);

        validarParametros(Analisador.getOperando(linha));
    }

    // Valida uma chamada de macro
    public static void validarChamada(String linha) {
        String nome = Analisador.getOperacao(linha);

        if (nome == null) {
            throw new IllegalArgumentException(
                    "Erro: operação não informada.");
        }

        int[] dados = TabelasMacro.TAB_NOME.get(
                nome.toUpperCase());

        if (dados == null) {
            throw new IllegalArgumentException(
                    "Erro: macro " + nome +
                            " não está definida.");
        }

        List<String> argumentos =
                ParserArgumentos.extrairArgumentos(
                        Analisador.getOperando(linha));

        validarArgumentos(
                nome, dados[2], argumentos.size());

        for (String argumento : argumentos) {
            if (argumento.isEmpty()) {
                throw new IllegalArgumentException(
                        "Erro: argumento vazio na macro " +
                                nome + ".");
            }
        }
    }
}
