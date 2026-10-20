package montador;

public class Operandos {

    public static int getValorOperando(String operandoTexto, TabelaSimbolos tabelaSimbolos) {
        if (operandoTexto == null || operandoTexto.trim().isEmpty()) {
            return 0;
        }

        String op = operandoTexto.trim();

        // 1. Limpeza de sintaxe dos modos de endereçamento (isolamento da entidade)
        // Remove prefixo imediato (#) ou indireto (@)
        if (op.startsWith("#") || op.startsWith("@")) {
            op = op.substring(1).trim();
        }

        // Remove sufixo de indexado (,X ou ,x)
        if (op.toUpperCase().endsWith(",X")) {
            op = op.substring(0, op.length() - 2).trim();
        }

        // 2. Trata sintaxe de constante Hexadecimal do Beck: X'HEX' (ex: X'F1')
        if (op.toUpperCase().startsWith("X'") && op.endsWith("'") && op.length() > 3) {
            String hexContent = op.substring(2, op.length() - 1);
            try {
                return Integer.parseInt(hexContent, 16);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Literal hexadecimal inválido (X'..'): " + operandoTexto);
            }
        }

        // Trata sintaxe de constante Caractere do Beck: C'TEXTO' (ex: C'EOF')
        if (op.toUpperCase().startsWith("C'") && op.endsWith("'") && op.length() > 3) {
            String textContent = op.substring(2, op.length() - 1);
            return converterStringParaIntASCII(textContent);
        }

        // 3. Trata notações tradicionais de Hexadecimal ($HEX ou 0xHEX)
        if (op.startsWith("$")) {
            try {
                return Integer.parseInt(op.substring(1), 16);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Literal hexadecimal inválido ($): " + operandoTexto);
            }
        }
        if (op.toLowerCase().startsWith("0x")) {
            try {
                return Integer.parseInt(op.substring(2), 16);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Literal hexadecimal inválido (0x): " + operandoTexto);
            }
        }

        // 4. Trata aspas simples diretas para caractere: 'A' ou 'ABC'
        if (op.startsWith("'") && op.endsWith("'") && op.length() >= 3) {
            String textContent = op.substring(1, op.length() - 1);
            return converterStringParaIntASCII(textContent);
        }

        // 5. Trata literais numéricos decimais simples (ex: 100, 4096, 0)
        if (eNumeroDecimal(op)) {
            try {
                return Integer.parseInt(op);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Número decimal fora dos limites de 32 bits: " + operandoTexto);
            }
        }

        // 6. Se não for número nem literal formato string, é um SÍMBOLO (rótulo)
        if (tabelaSimbolos != null && tabelaSimbolos.contem(op)) {
            return tabelaSimbolos.buscar(op);
        }

        // Se o símbolo não foi encontrado na Tabela de Símbolos
        throw new IllegalArgumentException("Símbolo não encontrado na Tabela de Símbolos: " + op);
    }

    /*Converte caracteres ASCII concatenados para valor numérico inteiro.*/
    private static int converterStringParaIntASCII(String str) {
        if (str.length() == 1) {
            return (int) str.charAt(0);
        }
        int valor = 0;
        for (char c : str.toCharArray()) {
            valor = (valor << 8) | ((int) c & 0xFF);
        }
        return valor;
    }

    /*Verifica se a string é formada exclusivamente por dígitos numéricos (com sinal opcional).*/
    private static boolean eNumeroDecimal(String str) {
        if (str == null || str.isEmpty()) return false;
        int inicio = (str.charAt(0) == '-' || str.charAt(0) == '+') ? 1 : 0;
        if (inicio >= str.length()) return false;

        for (int i = inicio; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
