
/*
 Utilitários de máscara de bits da arquitetura SIC/XE.
 Convenção: todo valor é armazenado SEM sinal, já mascarado.
 Use signed24/signed48 apenas quando a operação exigir sinal (SUB, COMP, DIV, SHIFTR...).
 */
public final class Mascaras {
    public static final int  MASK_24 = 0xFFFFFF;
    public static final long MASK_48 = 0xFFFFFFFFFFFFL;
    private static final int  SIGN_24 = 0x800000;
    private static final long SIGN_48 = 0x800000000000L;
    private Mascaras() {}

    /*Trunca para 24 bits (sem sinal).*/
    public static int to24(long v){ 
        return (int) (v & MASK_24);
        }

    /*Trunca para 48 bits (sem sinal). */
    public static long to48(long v){
        return v & MASK_48; 
        }

    /*Interpreta os 24 bits como inteiro com sinal (complemento de 2).*/
    public static int signed24(int v) {
        v &= MASK_24;
        return (v & SIGN_24) != 0 ? v | 0xFF000000 : v;
    }

    /*Interpreta os 48 bits como inteiro com sinal (complemento de 2).*/
    public static long signed48(long v) {
        v &= MASK_48;
        return (v & SIGN_48) != 0 ? v | ~MASK_48 : v;
    }

    /*Hexadecimal de 6 dígitos (24 bits), para a interface.*/
    public static String hex24(int v) { return String.format("%06X", v & MASK_24); }

    /*Hexadecimal de 12 dígitos (48 bits), para a interface.*/
    public static String hex48(long v) { return String.format("%012X", v & MASK_48); }
}