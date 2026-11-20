package ligador;

import java.util.List;
import montador.LeitorFonte;

public class Passo1 {

    // percorre os arquivos objeto lendo so os registros H e D
    // e monta a tabela de simbolos globais
    public static int[] executar(List<String> arquivos, int progAddr) {
        TabelaSimbolosGlobais.inicializar();

        int csAddr = progAddr;

        for (String arquivo : arquivos) {
            LeitorFonte leitor = new LeitorFonte();
            leitor.abrir(arquivo);
            int tamanhoSecao = 0;

            try {
                String linha;
                while ((linha = leitor.lerLinha()) != null) {
                    if (linha.trim().isEmpty()) {
                        continue;
                    }

                    char tipo = linha.charAt(0);

                    if (tipo == 'H') {
                        ParserObjeto.Header cabecalho = ParserObjeto.parseH(linha);
                        tamanhoSecao = cabecalho.tamanho;
                        TabelaSimbolosGlobais.inserir(cabecalho.nome, csAddr, "R");
                    } else if (tipo == 'D') {
                        for (ParserObjeto.Simbolo simbolo : ParserObjeto.parseD(linha)) {
                            TabelaSimbolosGlobais.inserir(simbolo.nome, csAddr + simbolo.endereco, "R");
                        }
                    } else if (tipo == 'E') {
                        break;
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Erro na linha " + leitor.getNumeroLinha() + " de " + arquivo + ": " + e.getMessage());
            } finally {
                leitor.fechar();
            }

            // a proxima secao comeca depois desta
            csAddr += tamanhoSecao;
        }

        // retorna o endereco inicial e o proximo endereco livre
        return new int[] { progAddr, csAddr };
    }
}
