package maquinavirtual;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Memoria memoria = new Memoria();
        Registradores regs = new Registradores();
        Executor executor = new Executor(memoria, regs);

        memoria.setByte(0, 0x01); 
        memoria.setByte(1, 0x00); //primeira metade do deslocamento
        memoria.setByte(2, 0x05); //segunda metade do deslocamento

        executor.setEnderecoFinal(3);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run(){
                Interface janela = new Interface(memoria,regs, executor);
            
                janela.setVisible(true);
            }
        });
        
    }
}