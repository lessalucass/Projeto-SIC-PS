
/**
 * Banco de registradores do SIC/XE (9 registradores).
 * A, X, L, B, S, T, PC, SW: 24 bits (int mascarado).
 * F: 48 bits (long mascarado).
 */
public class Registradores {

    //deixei assim pq n tava conseguindo visualizar direito
    public static final int A = 0;
    public static final int X = 1;
    public static final int L = 2;
    public static final int B = 3;
    public static final int S = 4;
    public static final int T = 5;
    public static final int F = 6;
    public static final int PC = 8;
    public static final int SW = 9;

    /*Código condicional (CC) gravado nos bits 7..6 do SW (padrão do Beck).*/
    public static final int CC_MENOR = 0b00;
    public static final int CC_IGUAL = 0b01;
    public static final int CC_MAIOR = 0b10;
    private static final int CC_SHIFT = 6;
    private static final int CC_MASK  = 0b11 << CC_SHIFT;

    private static final String[] NOMES = {"A", "X", "L", "B", "S", "T", "F", null, "PC", "SW"};

    private final int[] r24 = new int[10]; // índice 6 (F) e 7 não usados aqui
    private long f48 = 0;

    public static boolean valido(int n) {return n >= 0 && n <= 9 && n != 7;}

    public static String nome(int n) {return valido(n) ? NOMES[n] : "?";}

    /** Retorna -1 se o nome não existir. */
    public static int numero(String nome) {
        for (int i = 0; i < NOMES.length; i++)
            if (NOMES[i] != null && NOMES[i].equalsIgnoreCase(nome)) return i;
        return -1;
    }

    //registradores de 24 bits
    public int get(int n) {
        checar24(n);
        return r24[n];
    }

    public void set(int n, int valor) {
        checar24(n);
        r24[n] = Mascaras.to24(valor);
    }

    private void checar24(int n) {
        if (!valido(n) || n == F)
            throw new IllegalArgumentException("Registrador inválido (24 bits): " + n);
    }

    //registrador F (48 bits)
    public long getF() { return f48; }
    public void setF(long valor) { f48 = Mascaras.to48(valor); }

    //atalhos
    public int getPC() { return r24[PC]; }
    public void setPC(int v) { r24[PC] = Mascaras.to24(v); }

    /*Compara a com b (com sinal de 24 bits) e grava o CC no SW.*/
    public void setCCcomparando(int a, int b) {
        int x = Mascaras.signed24(a), y = Mascaras.signed24(b);
        setCC(x < y ? CC_MENOR : (x == y ? CC_IGUAL : CC_MAIOR));
    }

    public void setCC(int cc) {
        r24[SW] = Mascaras.to24((r24[SW] & ~CC_MASK) | ((cc & 0b11) << CC_SHIFT));
    }

    public int getCC() { return (r24[SW] & CC_MASK) >> CC_SHIFT; }

    public void reset() {
        java.util.Arrays.fill(r24, 0);
        f48 = 0;
    }

    /*Representação em hex para a interface visual.*/
    public String hex(int n) {
        return n == F ? Mascaras.hex48(f48) : Mascaras.hex24(get(n));
    }
}