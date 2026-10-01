public class Main {
    public static void main(String[] args) {
        Memoria memoria = new Memoria();
        Registradores regs = new Registradores();
        Executor executor = new Executor(memoria, regs);

        memoria.setByte(0, 0x01); 
        memoria.setByte(1, 0x00); //primeira metade do deslocamento
        memoria.setByte(2, 0x05); //segunda metade do deslocamento

        System.out.println("Acumulador (A) antes: " + regs.get(Registradores.A));
        System.out.println("PC antes: " + regs.getPC());
        
        executor.executarPasso();

        System.out.println("\n--- Depois de executar o passo ---");
        System.out.println("Acumulador (A) depois: " + regs.get(Registradores.A));
        System.out.println("PC depois: " + regs.getPC());
    }
}