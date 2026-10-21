package montador;

import maquinavirtual.Registradores;

/** Gera os bytes das instrucoes SIC/XE suportadas pelo montador. */
public class GeradorCodigo {
    private GeradorCodigo() {}

    /** Sem base configurada: instrucoes fora do alcance do PC exigem formato 4. */
    public static byte[] gerar(String instrucao, String operando, int lc, TabelaSimbolos tabela) {
        return gerar(instrucao, operando, lc, tabela, null);
    }

    /** base: endereco carregado no registrador B, ou null se indisponivel. */
    public static byte[] gerar(String instrucao, String operando, int lc,
                               TabelaSimbolos tabela, Integer base) {
        if (instrucao == null || !TabelasMontador.Instrucao(instrucao))
            throw new IllegalArgumentException("Instrucao desconhecida: " + instrucao);
        String nome = instrucao.trim().toUpperCase();
        int tamanho = TabelasMontador.getTamanho(nome);
        int opcode = TabelasMontador.getOpcode(nome);
        String op = operando == null ? "" : operando.trim();
        if (nome.startsWith("+") && tamanho != 4)
            throw new IllegalArgumentException("Formato 4 invalido para: " + nome);
        if (tamanho == 2) return formato2(nome, opcode, op);
        if (tamanho != 3 && tamanho != 4)
            throw new IllegalArgumentException("Formato nao suportado: " + nome);

        if (nome.equals("RSUB")) {
            if (!op.isEmpty()) throw new IllegalArgumentException("RSUB nao recebe operando");
            return new byte[]{(byte)(opcode | 3), 0, 0};
        }
        if (op.isEmpty()) throw new IllegalArgumentException("Operando ausente: " + nome);

        boolean imediato = op.startsWith("#");
        boolean indireto = op.startsWith("@");
        boolean indexado = op.toUpperCase().endsWith(",X");
        if (indexado && (imediato || indireto))
            throw new IllegalArgumentException("Indexacao permitida apenas no modo simples: " + op);
        String alvoTexto = op;
        if (imediato || indireto) alvoTexto = alvoTexto.substring(1).trim();
        if (indexado) alvoTexto = alvoTexto.substring(0, alvoTexto.length()-2).trim();
        int alvo = Operandos.getValorOperando(op, tabela);
        boolean constante = alvoTexto.matches("[+-]?[0-9]+") ||
                alvoTexto.matches("(?i)(0x[0-9a-f]+|\\$[0-9a-f]+|X'[0-9a-f]+')");
        int ni = imediato ? 1 : (indireto ? 2 : 3);
        int byte1 = opcode | ni;
        int xbpe = indexado ? 8 : 0;

        if (tamanho == 4) {
            if (alvo < 0 || alvo > 0xFFFFF)
                throw new IllegalArgumentException("Endereco fora de 20 bits: " + op);
            xbpe |= 1;
            return new byte[]{(byte)byte1, (byte)((xbpe << 4) | ((alvo >> 16) & 15)),
                    (byte)(alvo >> 8), (byte)alvo};
        }

        int disp;
        if (imediato && constante) {
            if (alvo < -2048 || alvo > 4095)
                throw new IllegalArgumentException("Constante imediata nao cabe no formato 3: " + op);
            disp = alvo;
        } else {
            int relativo = alvo - (lc + 3);
            if (relativo >= -2048 && relativo <= 2047) {
                disp = relativo;
                xbpe |= 2;
            } else if (base != null && alvo - base >= 0 && alvo - base <= 4095) {
                disp = alvo - base;
                xbpe |= 4;
            } else {
                throw new IllegalArgumentException("Alvo fora do alcance PC/base; use formato 4: " + op);
            }
        }
        return new byte[]{(byte)byte1, (byte)((xbpe << 4) | ((disp >> 8) & 15)), (byte)disp};
    }

    private static byte[] formato2(String nome, int opcode, String op) {
        String[] partes = op.isEmpty() ? new String[0] : op.split(",", -1);
        boolean unico = nome.equals("CLEAR") || nome.equals("TIXR");
        if (partes.length != (unico ? 1 : 2))
            throw new IllegalArgumentException("Quantidade de registradores invalida em " + nome);
        int r1 = Registradores.numero(partes[0].trim());
        int r2 = unico ? 0 : Registradores.numero(partes[1].trim());
        if (r1 < 0 || r2 < 0)
            throw new IllegalArgumentException("Registrador desconhecido em " + nome + ": " + op);
        return new byte[]{(byte)opcode, (byte)((r1 << 4) | r2)};
    }
}
