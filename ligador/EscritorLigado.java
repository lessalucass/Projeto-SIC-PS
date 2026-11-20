package ligador;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EscritorLigado {

    // limite clássico de bytes por registro Text (T) no formato objeto SIC/XE (30 bytes = 0x1E)
    private static final int MAX_BYTES_POR_REGISTRO_T = 30;

    private EscritorLigado() {}

    private static class BlocoTexto {
        final int enderecoInicial;
        final List<Byte> bytes = new ArrayList<>();

        BlocoTexto(int enderecoInicial) {
            this.enderecoInicial = enderecoInicial;
        }
    }

    /**
     * gera o arquivo objeto ligado a partir do resultado do Passo 2.
     *
     * @param resultado     Saída produzida por Passo2.executar
     * @param caminhoSaida  Caminho do arquivo de saída
     */
    public static void escrever(Passo2.ResultadoLigacao resultado, String caminhoSaida) throws IOException {
        if (resultado == null) {
            throw new IllegalArgumentException("Resultado da ligação não pode ser nulo.");
        }

        List<BlocoTexto> blocos = agruparEmBlocosContiguos(resultado.memoriaLigada);
        Path destino = Paths.get(caminhoSaida);

        try (BufferedWriter writer = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            for (BlocoTexto bloco : blocos) {
                if (bloco.bytes.isEmpty()) {
                    continue;
                }
                StringBuilder hexBytes = new StringBuilder(bloco.bytes.size() * 2);
                for (Byte b : bloco.bytes) {
                    hexBytes.append(String.format("%02X", b & 0xFF));
                }

                String linhaT = String.format(
                        "T%06X%02X%s",
                        bloco.enderecoInicial & 0xFFFFFF,
                        bloco.bytes.size() & 0xFF,
                        hexBytes.toString()
                );
                writer.write(linhaT);
                writer.newLine();
            }

            if (resultado.modificacoesPendentes != null) {
                for (ParserObjeto.Modificacao m : resultado.modificacoesPendentes) {
                    String linhaM = String.format(
                            "M%06X%02X%c",
                            m.endereco & 0xFFFFFF,
                            m.tamanho & 0xFF,
                            m.sinal
                    );
                    writer.write(linhaM);
                    writer.newLine();
                }
            }

            String linhaE = String.format("E%06X", resultado.enderecoExecucao & 0xFFFFFF);
            writer.write(linhaE);
            writer.newLine();
        }
    }

    /**
     * funde endereços consecutivos em registros Text, quebrando quando há
     * lacuna de endereço ou quando atinge o tamanho máximo por registro.
     */
    private static List<BlocoTexto> agruparEmBlocosContiguos(Map<Integer, Byte> memoria) {
        List<BlocoTexto> blocos = new ArrayList<>();
        if (memoria == null || memoria.isEmpty()) {
            return blocos;
        }

        BlocoTexto atual = null;
        Integer proximoEnderecoEsperado = null;

        for (Map.Entry<Integer, Byte> entry : memoria.entrySet()) {
            int endereco = entry.getKey();
            byte valor = entry.getValue();

            boolean precisaNovoBloco = (atual == null)
                    || (proximoEnderecoEsperado == null)
                    || (endereco != proximoEnderecoEsperado)
                    || (atual.bytes.size() >= MAX_BYTES_POR_REGISTRO_T);

            if (precisaNovoBloco) {
                atual = new BlocoTexto(endereco);
                blocos.add(atual);
            }

            atual.bytes.add(valor);
            proximoEnderecoEsperado = endereco + 1;
        }

        return blocos;
    }
}
