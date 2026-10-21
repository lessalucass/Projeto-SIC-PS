
package montador;

import java.util.Map;
import java.util.TreeMap;
import maquinavirtual.Memoria;

public class ModuloObjeto {
    private final TreeMap<Integer, Byte> bytes = new TreeMap<>();
    private int entrada;

    public void colocar(int endereco, byte[] dados) {
        if (endereco < 0 || dados == null) throw new IllegalArgumentException("Bloco invalido");
        for (int i = 0; i < dados.length; i++) {
            if (bytes.containsKey(endereco + i)) throw new IllegalArgumentException("Endereco sobreposto: " + (endereco + i));
            bytes.put(endereco + i, dados[i]);
        }
    }
    public void fechar(int enderecoInicial) { entrada = enderecoInicial; }
    public int getEnderecoInicial() { return entrada; }
    public int getEnderecoFinal() { return bytes.isEmpty() ? entrada : bytes.lastKey() + 1; }
    public void carregar(Memoria memoria) {
        for (Map.Entry<Integer, Byte> e : bytes.entrySet()) memoria.setByte(e.getKey(), e.getValue() & 255);
    }
    public void escrever(EscritorObjeto escritor) {
        for (Map.Entry<Integer, Byte> e : bytes.entrySet())
            escritor.escreverLinha(String.format("%06X %02X", e.getKey(), e.getValue() & 255));
    }
}
