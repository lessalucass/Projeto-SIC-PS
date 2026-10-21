package montador;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EscritorObjeto {
    private BufferedWriter escritor;
    private String caminho;

    //cria e sobrescreve o arquivo de saída do módulo objeto
    public void abrir(String caminho){
        if(caminho == null || caminho.trim().isEmpty()){
            throw new IllegalArgumentException("Caminho do arquivo não informado.");
        }
        fechar();
        try {
            this.escritor = Files.newBufferedWriter(Paths.get(caminho), StandardCharsets.UTF_8);
            this.caminho = caminho;
        } catch (IOException e){
            this.escritor = null;
            throw new IllegalArgumentException("Não foi possível criar o arquivo objeto: " + caminho);
        }
    }

    //escreve uma linha de código traduzido ou registro no arquivo.
    public void escreverLinha(String conteudo){
        if(escritor == null){
            throw new IllegalArgumentException("Nenhum arquivo objeto aberto para escrita.");
        }
        if(conteudo == null || conteudo.isBlank()){
            return;
        }
        try {
            escritor.write(conteudo);
            escritor.newLine();
        } catch (IOException e){
            throw new IllegalArgumentException("Erro ao escrever no arquivo objeto " + caminho + ".");
        }
    }

    //Passa o buffer no disco e fecha o arquivo.
    public void fechar(){
        if(escritor != null){
            try {
                escritor.flush();
                escritor.close();
            } catch (IOException e) {
                //silencia exceções
            }
            escritor = null;
        }
    }
}
