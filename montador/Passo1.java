package montador;

public class Passo1 {

    /**
     * Executa o Passo 1 do Montador SIC/XE:
     * - Calcula o Contador de Localização (LC).
     * - Popula a Tabela de Símbolos.
     * 
     * @param leitor LeitorFonte já aberto no arquivo .asm
     * @param tabelaSimbolos Instância da TabelaSimbolos
     * @return Um vetor de inteiros contendo [enderecoInicial, enderecoFinal]
     */
    public static int[] executar(LeitorFonte leitor, TabelaSimbolos tabelaSimbolos) {
        tabelaSimbolos.limpar();
        
        int lc = 0;
        int enderecoInicial = 0;

        try {
            String linha;
            while ((linha = leitor.lerLinha()) != null) {
                // Pula linhas vazias ou apenas com comentários
                if (Analisador.linhaVazia(linha)) {
                    continue;
                }

                String rotulo = Analisador.getRotulo(linha);
                String operacao = Analisador.getOperacao(linha);
                String operando = Analisador.getOperando(linha);

                // 1. Tratamento da diretiva START
                if (operacao.equalsIgnoreCase("START")) {
                    if (!operando.isEmpty()) {
                        enderecoInicial = Operandos.getValorOperando(operando, null);
                        lc = enderecoInicial;
                    }
                    if (rotulo != null && !rotulo.isEmpty()) {
                        tabelaSimbolos.inserir(rotulo, lc);
                    }
                    continue;
                }

                // 2. Tratamento especial para EQU 
                // (O rótulo do EQU recebe o valor avaliado do operando, e o LC não avança por ele)
                if (operacao.equalsIgnoreCase("EQU")) {
                    int valorEqu;
                    if (operando.equalsIgnoreCase("*")) {
                        valorEqu = lc;
                    } else {
                        valorEqu = Operandos.getValorOperando(operando, tabelaSimbolos);
                    }
                    if (rotulo != null && !rotulo.isEmpty()) {
                        tabelaSimbolos.inserir(rotulo, valorEqu);
                    }
                    continue; 
                }

                // 3. Para instruções normais e outras pseudo-instruções, 
                // o rótulo é inserido com o valor do LC atual antes de somar o tamanho
                if (rotulo != null && !rotulo.isEmpty()) {
                    tabelaSimbolos.inserir(rotulo, lc);
                }

                // 4. Tratamento de Fim de Código (END)
                if (operacao.equalsIgnoreCase("END")) {
                    break;
                }

                // 5. Tratamento de Pseudo-Instruções (POT)
                if (TabelasMontador.PseudoOp(operacao)) {
                    lc = tratarPseudoInstrucao(operacao, operando, lc, tabelaSimbolos);
                } 
                // 6. Tratamento de Instruções de Máquina (MOT)
                else if (TabelasMontador.Instrucao(operacao)) {
                    int tamanho = TabelasMontador.getTamanho(operacao);
                    if (tamanho <= 0) {
                        throw new IllegalArgumentException("Tamanho de instrução inválido para: " + operacao);
                    }
                    lc += tamanho;
                } 
                else {
                    throw new IllegalArgumentException("Operação desconhecida ou inválida: " + operacao);
                }
            }
        } catch (IllegalArgumentException e) {
            // Captura o erro e anexa o número da linha conforme exigido pelo trabalho
            throw new IllegalArgumentException("Erro na linha " + leitor.getNumeroLinha() + ": " + e.getMessage());
        }

        return new int[] { enderecoInicial, lc };
    }

    private static int tratarPseudoInstrucao(String operacao, String operando, int lc, TabelaSimbolos tabelaSimbolos) {
        String opUpper = operacao.toUpperCase();

        switch (opUpper) {
            case "ORG":
                if (operando.isEmpty()) {
                    throw new IllegalArgumentException("Diretiva ORG requer um operando.");
                }
                return Operandos.getValorOperando(operando, tabelaSimbolos);

            case "RESW":
                if (operando.isEmpty()) {
                    throw new IllegalArgumentException("Diretiva RESW requer um operando numérico.");
                }
                int qtdW = Operandos.getValorOperando(operando, null);
                return lc + (qtdW * 3);

            case "RESB":
                if (operando.isEmpty()) {
                    throw new IllegalArgumentException("Diretiva RESB requer um operando numérico.");
                }
                int qtdB = Operandos.getValorOperando(operando, null);
                return lc + qtdB;

            case "WORD":
                return lc + 3;

            case "BYTE":
                int tamanhoByte = calcularTamanhoByte(operando);
                return lc + tamanhoByte;

            case "BASE":
            case "NOBASE":
            case "LTORG":
                return lc;

            default:
                throw new IllegalArgumentException("Pseudo-instrução não suportada no Passo 1: " + operacao);
        }
    }

    private static int calcularTamanhoByte(String operando) {
        if (operando == null || operando.isEmpty()) {
            return 0;
        }
        String op = operando.trim();
        // Constantes do tipo C'TEXTO'
        if (op.toUpperCase().startsWith("C'") && op.endsWith("'")) {
            return op.length() - 3; 
        }
        // Constantes do tipo X'HEX'
        if (op.toUpperCase().startsWith("X'") && op.endsWith("'")) {
            int numHexChars = op.length() - 3;
            return (numHexChars + 1) / 2; 
        }
        return 1;
    }
}