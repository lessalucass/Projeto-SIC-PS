package processadormacros;
import java.util.List;
import montador.Analisador;
import montador.EscritorObjeto;
import montador.LeitorFonte;

//Classe que lê o ficheiro fonte, decide o fluxo de definição, expansão ou escrita normal, e grava o resultado final em MASMAPRG.ASM
public class ProcessadorMacros {
    public static void main(String[] args){
        if(args.length < 1){
            System.out.println("Uso: java processadormacros.ProcessadorMacros <arquivo_fonte.asm>");
            return;
        }
        String arquivoEntrada = args[0];
        String arquivoSaida = "MASMAPRG.ASM";

        processar(arquivoEntrada, arquivoSaida);
    }

    public static void processar(String caminhoEntrada, String caminhoSaida){
        TabelasMacro.inicializar();
        LeitorFonte leitor = new LeitorFonte();
        EscritorObjeto escritor = new EscritorObjeto();

        try{
            leitor.abrir(caminhoEntrada);
            escritor.abrir(caminhoSaida);

            String linha;
            while((linha = leitor.lerLinha()) != null){
                if(Analisador.linhaVazia(linha)){
                    continue;
                }

                String op = Analisador.getOperacao(linha);

                if(op != null && op.equalsIgnoreCase("MACRO")){
                    //encontrou o início da definição: o definidor lê até MEND
                    Definidor.definir(linha, leitor);
                } else if(Expansor.ehChamada(linha)){
                    //encontrou chamada de macro: expanse e escreve todas as linhas resultantes
                    List<String> linhasExpandidas = Expansor.expandir(linha);
                    for(String linhaExp : linhasExpandidas){
                        escritor.escreverLinha(linhaExp);
                    }
                }else{
                    //código Assembly normal ou diretiva: repassa para o ficheiro
                    escritor.escreverLinha(linha);
                }
            }
            System.out.println("Macro-Processamento concluído e ficheiro gerado!");
        }catch (Exception e){
            System.out.println("Erro no Macro-Processamento: " + e.getMessage());
        }finally{
            leitor.fechar();
            escritor.fechar();
        }
    }
}
