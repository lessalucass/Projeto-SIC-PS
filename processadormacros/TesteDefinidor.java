package processadormacros;

import java.util.Arrays;
import montador.Analisador;
import montador.LeitorFonte;

/** Teste manual da Parte 2 (Definidor). Uso: java -cp out processadormacros.TesteDefinidor arquivo.asm */
public class TesteDefinidor {
    public static void main(String[] args) {
        TabelasMacro.inicializar();
        LeitorFonte leitor = new LeitorFonte();
        leitor.abrir(args[0]);
        String linha;
        while ((linha = leitor.lerLinha()) != null) {
            if (Analisador.getOperacao(linha).equals("MACRO")) {
                try {
                    System.out.println("definida: " + Definidor.definir(linha, leitor));
                } catch (IllegalArgumentException e) {
                    System.out.println("ERRO: " + e.getMessage());
                }
            }
        }
        leitor.fechar();
        System.out.println("\n--- TAB_NOME (nome -> [inicio, fim, numParametros]) ---");
        TabelasMacro.TAB_NOME.forEach((k, v) -> System.out.println(k + " -> " + Arrays.toString(v)));
        System.out.println("\n--- TAB_DEFINICAO ---");
        for (int i = 0; i < TabelasMacro.TAB_DEFINICAO.size(); i++) {
            System.out.println(i + ": " + TabelasMacro.TAB_DEFINICAO.get(i));
        }
    }
}