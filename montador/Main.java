
package maquinavirtual;

import javax.swing.SwingUtilities;
import montador.*;

public class Main {
    public static void main(String[] args) {
        Memoria memoria = new Memoria();
        Registradores regs = new Registradores();
        Executor executor = new Executor(memoria, regs);
        if (args.length > 0) {
            LeitorFonte leitor = new LeitorFonte();
            try {
                leitor.abrir(args[0]);
                TabelaSimbolos tabela = new TabelaSimbolos();
                int[] limites = Passo1.executar(leitor, tabela);
                ModuloObjeto modulo = Passo2.executar(leitor, tabela, limites[0]);
                modulo.carregar(memoria);
                regs.setPC(modulo.getEnderecoInicial());
                int fimPrograma = Math.max(
                        limites[1],
                        modulo.getEnderecoFinal()
                );
                executor.setEnderecoFinal(fimPrograma);
                // Define o endereco de retorno do programa principal
                regs.set(Registradores.L, fimPrograma);
                if (args.length > 1) {
                    EscritorObjeto escritor = new EscritorObjeto();
                    try { escritor.abrir(args[1]); modulo.escrever(escritor); }
                    finally { escritor.fechar(); }
                }
                System.out.println("Montagem concluida. Entrada: " + modulo.getEnderecoInicial());
            } catch (RuntimeException e) {
                System.err.println("Falha na montagem: " + e.getMessage()); return;
            } finally { leitor.fechar(); }
        } else {
            memoria.setByte(0, 0x01); memoria.setByte(1, 0); memoria.setByte(2, 5);
            executor.setEnderecoFinal(3);
        }
        SwingUtilities.invokeLater(() -> new Interface(memoria, regs, executor).setVisible(true));
    }
}
