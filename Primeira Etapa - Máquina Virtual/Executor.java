public class Executor {
    private Memoria memoria;
    private Registradores regs;

    public Executor(Memoria memoria, Registradores regs) {
        this.memoria = memoria;
        this.regs = regs;
    }

    public void executarPasso() {
        int pcAtual = regs.getPC();
        int byte1 = memoria.getByte(pcAtual);
        
        //os 6 primeiros bits definem a operação nas instruções formato 3 e 4
        int opcode = byte1 & 0xFC; 
        int opcodePuro = byte1 & 0xFF; //para identificar Formato 2
        
        //lê as flags N e I do primeiro byte
        boolean n = (byte1 & 0x02) != 0;
        boolean i = (byte1 & 0x01) != 0;

        int formato = descobrirFormato(opcodePuro);
        int enderecoAlvo = 0;
        int valorOperando = 0;

        //Tratamento de formato 2
        if (formato == 2) {
            int byte2 = memoria.getByte(pcAtual + 1);
            int reg1 = (byte2 & 0xF0) >> 4;
            int reg2 = (byte2 & 0x0F);
            regs.setPC(pcAtual + 2);
            executarFormato2(opcodePuro, reg1, reg2);
            return; // Encerra o passo aqui
        }

        //Tratamento de formato 3 e 4
        int byte2 = memoria.getByte(pcAtual + 1);
        boolean x = (byte2 & 0x80) != 0;
        boolean b = (byte2 & 0x40) != 0;
        boolean p = (byte2 & 0x20) != 0;
        boolean e = (byte2 & 0x10) != 0; //se e=1, é Formato 4 

        int byte3 = memoria.getByte(pcAtual + 2);
        
        if (e) {
            //formato 4: endereço de 20 bits
            int byte4 = memoria.getByte(pcAtual + 3);
            enderecoAlvo = ((byte2 & 0x0F) << 16) | (byte3 << 8) | byte4;
            regs.setPC(pcAtual + 4);
        } else {
            //formato 3: deslocamento de 12 bits
            int disp = ((byte2 & 0x0F) << 8) | byte3;
            if ((disp & 0x800) != 0) disp |= 0xFFFFF000; //extensão de sinal se for negativo
            
            enderecoAlvo = disp;
            regs.setPC(pcAtual + 3); //próxima instrução
            
            //cálculos de endereçamento relativo
            if (p) enderecoAlvo += regs.getPC(); //PC
            else if (b) enderecoAlvo += regs.get(Registradores.B); //base
        }

        //adiciona índice X, se a flag estiver ativa
        if (x) enderecoAlvo += regs.get(Registradores.X);

        //Resolução do modo de endereçamento
        if (i && !n) {
            valorOperando = enderecoAlvo; //imediato: o valor é o próprio endereço
        } else if (n && !i) {
            enderecoAlvo = memoria.getWord(enderecoAlvo); //indireto: busca o endereço real na memória
            valorOperando = memoria.getWord(enderecoAlvo);
        } else {
            //SIC padrão ou simples SIC/XE
            valorOperando = memoria.getWord(enderecoAlvo);
        }


        //Execução do formato 3 e 4
        switch (opcode) {
            case 0x00: //LDA - carrega A
                regs.set(Registradores.A, valorOperando); break;
            case 0x04: //LDX - carrega X
                regs.set(Registradores.X, valorOperando); break;
            case 0x68: //LDB - carrega B
                regs.set(Registradores.B, valorOperando); break;
            case 0x0C: //STA - armazena A
                memoria.setWord(enderecoAlvo, regs.get(Registradores.A)); break;
            case 0x10: //STX - armazena X
                memoria.setWord(enderecoAlvo, regs.get(Registradores.X)); break;
            case 0x18: //ADD - soma em A
                regs.set(Registradores.A, regs.get(Registradores.A) + valorOperando); break;
            case 0x1C: //SUB - subtrai de A
                regs.set(Registradores.A, regs.get(Registradores.A) - valorOperando); break;
            case 0x28: //COMP - compara com A
                regs.setCCcomparando(regs.get(Registradores.A), valorOperando); break;
            case 0x3C: //J - salto incondicional
                regs.setPC(enderecoAlvo); break;
            case 0x30: //JEQ - salto se Igual (=)
                if (regs.getCC() == Registradores.CC_IGUAL) regs.setPC(enderecoAlvo); break;
            case 0x34: //JGT - salto se Maior (>)
                if (regs.getCC() == Registradores.CC_MAIOR) regs.setPC(enderecoAlvo); break;
            case 0x38: //JLT - salto se Menor (<)
                if (regs.getCC() == Registradores.CC_MENOR) regs.setPC(enderecoAlvo); break;
            case 0x48: //JSUB - salto para subrotina
                regs.set(Registradores.L, regs.getPC()); //salva o retorno em L
                regs.setPC(enderecoAlvo); break;
            case 0x4C: //RSUB - retorno de subrotina
                regs.setPC(regs.get(Registradores.L)); break; //retorna para o endereço em L
            default:
                System.out.println("Instrução F3/F4 não implementada: " + Integer.toHexString(opcode));
                break;
        }
    }

    private void executarFormato2(int opcode, int r1, int r2) {
        switch (opcode) {
            case 0xA0: //COMPR - compara dois registradores
                regs.setCCcomparando(regs.get(r1), regs.get(r2));
                break;
            case 0xB8: //TIXR - incrementa X e compara
                regs.set(Registradores.X, regs.get(Registradores.X) + 1);
                regs.setCCcomparando(regs.get(Registradores.X), regs.get(r1));
                break;
            case 0x90: //ADDR - Soma r1 em r2
                regs.set(r2, regs.get(r2) + regs.get(r1));
                break;
            case 0x94: //SUBR - Subtrai r1 de r2
                regs.set(r2, regs.get(r2) - regs.get(r1));
                break;
            case 0xB4: //CLEAR - limpa registrador 
                regs.set(r1, 0);
                break;
            default:
                System.out.println("Instrução F2 não implementada: " + Integer.toHexString(opcode));
                break;
        }
    }

    //para identificar se é Formato 2
    private int descobrirFormato(int opcodePuro) {
        // Tabela de opcodes de Formato 2 comuns[cite: 7]
        if (opcodePuro == 0x90 || opcodePuro == 0xB4 || opcodePuro == 0xA0 || 
            opcodePuro == 0x9C || opcodePuro == 0x98 || opcodePuro == 0xAC || 
            opcodePuro == 0x94 || opcodePuro == 0xB8) {
            return 2;
        }
        return 3; //retorna 3 e pode virar 4 se a flag 'e' for verdadeira
    }
}