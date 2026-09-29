/*
 Memória do SIC/XE: vetor de bytes, endereçamento por byte, big-endian.
 Palavra = 3 bytes (24 bits). Palavra de ponto flutuante = 6 bytes (48 bits).
 Tamanho padrão: 2^20 bytes (1 MB), o espaço endereçável do SIC/XE (mínimo do enunciado: 1 KB).
 */

public class Memoria {
    public static final int TAMANHO_MINIMO = 1024;
    public static final int TAMANHO_PADRAO = 1 << 20;
    private final byte[] dados;

    public Memoria() { this(TAMANHO_PADRAO); }

    public Memoria(int tamanho) {
        if (tamanho < TAMANHO_MINIMO)
            throw new IllegalArgumentException("Memória mínima: 1 KB");
        this.dados = new byte[tamanho];
    }

    public int tamanho() { return dados.length; }

    private void checar(int endereco, int n) {
        if (endereco < 0 || endereco > dados.length - n)
            throw new IndexOutOfBoundsException(String.format("Acesso inválido: endereço %06X (+%d bytes), memória de %d bytes", endereco, n, dados.length));
    }

    //byte (LDCH, STCH, carga de programa)
    public int getByte(int end) {
        checar(end, 1);
        return dados[end] & 0xFF;
    }

    public void setByte(int end, int valor) {
        checar(end, 1);
        dados[end] = (byte) valor;
    }

    //palavra de 3 bytes: m..m+2

    public int getWord(int end) {
        checar(end, 3);
        return ((dados[end] & 0xFF) << 16)|((dados[end + 1] & 0xFF) << 8)|(dados[end + 2] & 0xFF);
    }

    public void setWord(int end, int valor) {
        checar(end, 3);
        valor = Mascaras.to24(valor);
        dados[end]= (byte) (valor >> 16);
        dados[end + 1]= (byte) (valor >> 8);
        dados[end + 2]= (byte) valor;
    }

    //palavra de 6 bytes (ponto flutuante): m..m+5

    public long getWord48(int end) {
        checar(end, 6);
        long v = 0;
        for (int i = 0; i < 6; i++) v = (v << 8) | (dados[end + i] & 0xFF);
        return v;
    }

    public void setWord48(int end, long valor) {
        checar(end, 6);
        valor = Mascaras.to48(valor);
        for (int i = 5; i >= 0; i--) {
            dados[end + i] = (byte) valor;
            valor >>= 8;
        }
    }

    /*Carrega um bloco de bytes a partir de um endereço. */
    public void carregar(int endereco, byte[] bloco) {
        checar(endereco, bloco.length);
        System.arraycopy(bloco, 0, dados, endereco, bloco.length);
    }

    public void limpar(){ 
        java.util.Arrays.fill(dados, (byte) 0);
        }
}