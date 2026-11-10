package processadormacros;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Integrante 4 - Cirurgião de Argumentos (Parser de Macros)
 * Responsável por separar, limpar e validar parâmetros e argumentos
 * delimitados por vírgulas nas definições e chamadas de macros.
 */
public class ParserArgumentos {

    private static final Pattern PARAMETRO = Pattern.compile("&[A-Za-z0-9_]+");

    private ParserArgumentos() {}

    /**
     * Processa a string de parâmetros de uma definição de macro (ex: "&A, &B").
     * Converte para maiúsculas, valida a sintaxe (&NOME) e garante que não há duplicados.
     *
     * @param operando A string contendo os parâmetros
     * @return Lista de parâmetros válidos e formatados
     */
    public static List<String> extrairParametros(String operando) {
        List<String> lista = new ArrayList<>();
        if (operando == null || operando.trim().isEmpty()) {
            return lista;
        }

        for (String p : operando.split(",", -1)) {
            p = p.trim().toUpperCase();
            if (p.isEmpty()) {
                continue;
            }
            if (!PARAMETRO.matcher(p).matches()) {
                throw new IllegalArgumentException(
                        "Parâmetro de macro inválido (esperado &NOME): '" + p + "'");
            }
            if (lista.contains(p)) {
                throw new IllegalArgumentException("Parâmetro repetido na macro: " + p);
            }
            lista.add(p);
        }
        return lista;
    }

    /**
     * Processa a string de argumentos de uma chamada de macro (ex: "VALOR_A,  VALOR_B").
     * Corta nas vírgulas e remove espaços desnecessários.
     *
     * @param operando A string contendo os argumentos
     * @return Lista de argumentos limpos
     */
    public static List<String> extrairArgumentos(String operando) {
        List<String> lista = new ArrayList<>();
        if (operando == null || operando.trim().isEmpty()) {
            return lista;
        }

        for (String a : operando.split(",", -1)) {
            lista.add(a.trim());
        }
        return lista;
    }
}
