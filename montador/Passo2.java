
package montador;

import java.nio.charset.StandardCharsets;

public class Passo2 {
    public static ModuloObjeto executar(LeitorFonte leitor, TabelaSimbolos tabela, int inicio) {
        leitor.reiniciar();
        ModuloObjeto modulo = new ModuloObjeto();
        int lc = inicio, entrada = inicio;
        Integer base = null;
        try {
            String linha;
            while ((linha = leitor.lerLinha()) != null) {
                if (Analisador.linhaVazia(linha)) continue;
                String instrucao = Analisador.getOperacao(linha).toUpperCase();
                String op = Analisador.getOperando(linha);
                switch (instrucao) {
                    case "START": if (!op.isEmpty()) lc = Operandos.getValorOperando(op, tabela); break;
                    case "END":
                        if (!op.isEmpty()) entrada = Operandos.getValorOperando(op, tabela);
                        modulo.fechar(entrada); return modulo;
                    case "EQU": case "LTORG": break;
                    case "BASE": base = Operandos.getValorOperando(op, tabela); break;
                    case "NOBASE": base = null; break;
                    case "ORG": lc = Operandos.getValorOperando(op, tabela); break;
                    case "RESB":
                    case "DS": lc += reserva(op, tabela); break;
                    case "RESW": lc += 3 * reserva(op, tabela); break;
                    case "WORD":
                    case "DC": {
                        int v = Operandos.getValorOperando(op, tabela);
                        modulo.colocar(lc, new byte[]{(byte)(v >> 16), (byte)(v >> 8), (byte)v});
                        lc += 3; break;
                    }
                    case "BYTE": {
                        byte[] b = literal(op, tabela);
                        modulo.colocar(lc, b); lc += b.length; break;
                    }
                    default: {
                        byte[] b = GeradorCodigo.gerar(instrucao, op, lc, tabela, base);
                        modulo.colocar(lc, b); lc += b.length;
                    }
                }
            }
            modulo.fechar(entrada); return modulo;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Erro na linha " + leitor.getNumeroLinha() + ": " + e.getMessage(), e);
        }
    }
    private static int reserva(String op, TabelaSimbolos tabela) {
        int n = Operandos.getValorOperando(op, tabela);
        if (n < 0) throw new IllegalArgumentException("Reserva negativa");
        return n;
    }
    private static byte[] literal(String op, TabelaSimbolos tabela) {
        if (op.matches("(?i)C'.*'")) return op.substring(2, op.length()-1).getBytes(StandardCharsets.US_ASCII);
        if (op.matches("(?i)X'[0-9a-f]*'")) {
            String hex = op.substring(2, op.length()-1);
            byte[] b = new byte[(hex.length()+1)/2];
            for (int i=0; i<b.length; i++) {
                int from = i*2, to = Math.min(from+2,hex.length());
                b[i] = (byte)Integer.parseInt(hex.substring(from,to),16);
            }
            return b;
        }
        int v = Operandos.getValorOperando(op,tabela);
        if (v < 0 || v > 255) throw new IllegalArgumentException("BYTE fora do intervalo");
        return new byte[]{(byte)v};
    }
}
