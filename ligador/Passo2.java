package ligador;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import montador.LeitorFonte;

/**
 * Parte 4 - Passo 2 do Ligador (Motor de Relocação e Ligação).
 * Lê os arquivos objeto pela segunda vez, carrega os bytes dos registros T
 * e aplica as modificações dos registros M consultando a TabelaSimbolosGlobais.
 */
public class Passo2 {

    /**
     * Estrutura de retorno contendo os bytes consolidados na memória,
     * o endereço de execução inicial (registro E) e eventuais registros M
     * pendentes (caso o modo seja apenas Ligador, sem relocação final).
     */
    public static class ResultadoLigacao {
        public final Map<Integer, Byte> memoriaLigada;
        public final int enderecoExecucao;
        public final List<ParserObjeto.Modificacao> modificacoesPendentes;

        public ResultadoLigacao(Map<Integer, Byte> memoriaLigada, int enderecoExecucao,
                                List<ParserObjeto.Modificacao> modificacoesPendentes) {
            this.memoriaLigada = memoriaLigada;
            this.enderecoExecucao = enderecoExecucao;
            this.modificacoesPendentes = modificacoesPendentes;
        }
    }

    /**
     * Executa a segunda passagem do Ligador.
     *
     * @param arquivos Lista com os caminhos dos arquivos objeto (.obj)
     * @param progAddr Endereço inicial de carga (PROGADDR)
     * @param relocacaoCompleta true = Ligador-Relocador (resolve tudo);
     *                          false = Apenas Ligador (resolve externos, mantém relocação simples para o Carregador)
     * @return Objeto contendo o mapa ordenado de endereços -> bytes e o endereço de execução
     */
    public static ResultadoLigacao executar(List<String> arquivos, int progAddr, boolean relocacaoCompleta) {
        // TreeMap mantém os bytes ordenados pelo endereço de memória real
        Map<Integer, Byte> memoria = new TreeMap<>();
        List<ParserObjeto.Modificacao> pendentes = new ArrayList<>();

        int csAddr = progAddr;
        int execAddr = progAddr;
        boolean primeiroEndEncontrado = false;

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
                        ParserObjeto.Header h = ParserObjeto.parseH(linha);
                        tamanhoSecao = h.tamanho;

                    } else if (tipo == 'T') {
                        ParserObjeto.Text t = ParserObjeto.parseT(linha);
                        int enderecoBase = csAddr + t.inicio;

                        // Converte cada par de caracteres hexadecimais em 1 byte na memória
                        for (int i = 0; i < t.tamanho; i++) {
                            String hexByte = t.bytes.substring(i * 2, i * 2 + 2);
                            byte valorByte = (byte) Integer.parseInt(hexByte, 16);
                            memoria.put(enderecoBase + i, valorByte);
                        }

                    } else if (tipo == 'M') {
                        ParserObjeto.Modificacao m = ParserObjeto.parseM(linha);
                        int enderecoAlvo = csAddr + m.endereco;

                        boolean ehSimboloExterno = (m.simbolo != null && !m.simbolo.isEmpty());

                        if (ehSimboloExterno) {
                            // Ligação: busca o endereço absoluto do símbolo externo na ESTAB
                            int valorSimbolo = TabelaSimbolosGlobais.buscarEndereco(m.simbolo);
                            aplicarModificacao(memoria, enderecoAlvo, m.tamanho, m.sinal, valorSimbolo);

                        } else {
                            // Relocação simples (referência interna da própria seção)
                            if (relocacaoCompleta) {
                                aplicarModificacao(memoria, enderecoAlvo, m.tamanho, m.sinal, csAddr);
                            } else {
                                // Se for apenas Ligador, ajusta o endereço do registro M e repassa ao Carregador
                                ParserObjeto.Modificacao modAjustada = new ParserObjeto.Modificacao();
                                modAjustada.endereco = enderecoAlvo;
                                modAjustada.tamanho = m.tamanho;
                                modAjustada.sinal = m.sinal;
                                modAjustada.simbolo = null;
                                pendentes.add(modAjustada);
                            }
                        }

                    } else if (tipo == 'E') {
                        int endE = ParserObjeto.parseE(linha);
                        if (endE >= 0 && !primeiroEndEncontrado) {
                            execAddr = csAddr + endE;
                            primeiroEndEncontrado = true;
                        }
                        break;
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Erro no Passo 2 na linha " + leitor.getNumeroLinha() + " de " + arquivo + ": " + e.getMessage());
            } finally {
                leitor.fechar();
            }

            csAddr += tamanhoSecao;
        }

        return new ResultadoLigacao(memoria, execAddr, pendentes);
    }

    /**
     * Lê os bytes afetados na memória, aplica a soma/subtração respeitando
     * a largura em meios-bytes (nibbles) e grava o resultado de volta (Big-Endian).
     */
    private static void aplicarModificacao(Map<Integer, Byte> memoria, int endereco,
                                           int meiosBytes, char sinal, int valorAjuste) {
        // 5 meios-bytes (20 bits - Formato 4) ou 6 meios-bytes (24 bits - WORD) ocupam 3 bytes
        int numBytes = (meiosBytes + 1) / 2;

        // 1. Reconstrói o valor inteiro atual a partir dos bytes na memória (Big-Endian)
        int valorAtual = 0;
        for (int i = 0; i < numBytes; i++) {
            int b = memoria.getOrDefault(endereco + i, (byte) 0) & 0xFF;
            valorAtual = (valorAtual << 8) | b;
        }

        // 2. Isola apenas os bits modificáveis (ex: 20 bits -> 0x0FFFFF, 24 bits -> 0xFFFFFF)
        int bitsTotais = meiosBytes * 4;
        int mascaraCampo = (bitsTotais >= 32) ? 0xFFFFFFFF : ((1 << bitsTotais) - 1);
        int bitsPreservados = valorAtual & ~mascaraCampo; // Preserva os 4 bits de nixbpe se for Formato 4 (5 nibbles)
        int campoAlvo = valorAtual & mascaraCampo;

        // 3. Aplica a soma ou subtração
        if (sinal == '-') {
            campoAlvo -= valorAjuste;
        } else {
            campoAlvo += valorAjuste;
        }

        int novoValor = bitsPreservados | (campoAlvo & mascaraCampo);

        // 4. Escreve os bytes atualizados de volta na memória (Big-Endian)
        for (int i = numBytes - 1; i >= 0; i--) {
            memoria.put(endereco + i, (byte) (novoValor & 0xFF));
            novoValor >>= 8;
        }
    }
}
