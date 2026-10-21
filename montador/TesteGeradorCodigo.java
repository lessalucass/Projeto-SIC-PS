
package montador;

public class TesteGeradorCodigo {

    private static int aprovados = 0;
    private static int reprovados = 0;

    public static void main(String[] args) {
        TabelaSimbolos tabela = new TabelaSimbolos();

        tabela.inserir("ALVO", 0x1009);
        tabela.inserir("DISTANTE", 0x3000);

        System.out.println("=== TESTES DA PARTE 6 ===");

        // Formato 2
        testar("Formato 2 - CLEAR X",
                "B4 10", "CLEAR", "X", 0, tabela, null);

        testar("Formato 2 - COMPR A,X",
                "A0 01", "COMPR", "A,X", 0, tabela, null);

        // Formato 3: imediato
        testar("Formato 3 - LDA #5",
                "01 00 05", "LDA", "#5", 0, tabela, null);

        // Formato 3: relativo ao PC
        testar("PC-relativo - LDA ALVO",
                "03 20 06", "LDA", "ALVO", 0x1000, tabela, null);

        // Formato 3: indexado
        testar("Indexado - LDA ALVO,X",
                "03 A0 06", "LDA", "ALVO,X", 0x1000, tabela, null);

        // Formato 3: base-relativo
        testar("Base-relativo - LDA DISTANTE",
                "03 40 00", "LDA", "DISTANTE",
                0x1000, tabela, 0x3000);

        // Formato 4
        testar("Formato 4 - +LDA #4096",
                "01 10 10 00", "+LDA", "#4096", 0, tabela, null);

        // Instrução sem operando
        testar("RSUB",
                "4F 00 00", "RSUB", "", 0, tabela, null);

        // Testes de erro
        testarErro("Instrucao desconhecida",
                "INVALIDA", "A", 0, tabela);

        testarErro("Operando ausente",
                "LDA", "", 0, tabela);

        testarErro("Indexacao invalida",
                "LDA", "#5,X", 0, tabela);

        System.out.println("\n=== RESULTADO FINAL ===");
        System.out.println("Aprovados: " + aprovados);
        System.out.println("Reprovados: " + reprovados);
    }

    private static void testar(String descricao, String esperado,
                               String instrucao, String operando,
                               int lc, TabelaSimbolos tabela, Integer base) {
        try {
            byte[] codigo = GeradorCodigo.gerar(
                    instrucao, operando, lc, tabela, base);

            StringBuilder resultado = new StringBuilder();
            for (byte b : codigo) {
                if (resultado.length() > 0) resultado.append(" ");
                resultado.append(String.format("%02X", b & 0xFF));
            }

            if (esperado.equals(resultado.toString())) {
                System.out.println("[OK] " + descricao);
                aprovados++;
            } else {
                System.out.println("[FALHOU] " + descricao);
                System.out.println("  Esperado: " + esperado);
                System.out.println("  Obtido:   " + resultado);
                reprovados++;
            }
        } catch (Exception e) {
            System.out.println("[ERRO] " + descricao);
            System.out.println("  " + e.getMessage());
            reprovados++;
        }
    }

    private static void testarErro(String descricao,
                                   String instrucao, String operando,
                                   int lc, TabelaSimbolos tabela) {
        try {
            GeradorCodigo.gerar(instrucao, operando, lc, tabela);
            System.out.println("[FALHOU] " + descricao);
            reprovados++;
        } catch (IllegalArgumentException e) {
            System.out.println("[OK] " + descricao);
            aprovados++;
        }
    }
}
