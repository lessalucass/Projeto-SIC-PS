package montador;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LeitorFonte {

    private String caminho;
    private BufferedReader leitor;
    private int numeroLinha = 0;

    /** Abre o arquivo fonte. Se já havia um aberto, ele é fechado antes. */
    public void abrir(String caminho) {
        if (caminho == null || caminho.trim().isEmpty()) {
            throw new IllegalArgumentException("Caminho do arquivo fonte não informado.");
        }
        fechar();
        try {
            this.leitor = Files.newBufferedReader(Paths.get(caminho), StandardCharsets.UTF_8);
            this.caminho = caminho;
            this.numeroLinha = 0;
        } catch (IOException e) {
            this.leitor = null;
            throw new IllegalArgumentException("Não foi possível abrir o arquivo: " + caminho);
        }
    }

  
    public String lerLinha() {
        if (leitor == null) {
            throw new IllegalArgumentException("Nenhum arquivo aberto para leitura.");
        }
        try {
            String linha = leitor.readLine();
            if (linha != null) {
                numeroLinha++;
            }
            return linha;
        } catch (IOException e) {
            throw new IllegalArgumentException("Erro ao ler o arquivo " + caminho + " (após a linha " + numeroLinha + ").");
        }
    }

    /** Volta ao início do arquivo (para o Passo 2). */
    public void reiniciar() {
        if (caminho == null) {
            throw new IllegalArgumentException("Nenhum arquivo aberto para reiniciar.");
        }
        abrir(caminho);
    }

    /** Fecha o arquivo. Pode ser chamado mais de uma vez. */
    public void fechar() {
        if (leitor != null) {
            try {
                leitor.close();
            } catch (IOException e) {
                // nada a fazer ao fechar
            }
            leitor = null;
        }
    }

    /** Número da última linha lida (1 = primeira linha). Útil nas mensagens de erro. */
    public int getNumeroLinha() {
        return numeroLinha;
    }
}