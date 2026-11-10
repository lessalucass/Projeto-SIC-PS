package processadormacros;

import montador.Analisador;
import montador.LeitorFonte;

/** Teste manual da Parte 3 (Expansor). Uso: java -cp out processadormacros.TesteExpansor arquivo.asm */
public class TesteExpansor {
    public static void main(String[] args) {
        TabelasMacro.inicializar();
        LeitorFonte leitor = new LeitorFonte();
        leitor.abrir(args[0]);
        String linha;
        while ((linha = leitor.lerLinha()) != null) {
            if (Analisador.getOperacao(linha).equals("MACRO")) {
                Definidor.definir(linha, leitor);
            } else if (Expansor.ehChamada(linha)) {
                for (String saida : Expansor.expandir(linha)) {
                    System.out.println(saida);
                }
            }
        }
        leitor.fechar();
    }
}