package montador;

import java.util.*;


public class TabelasMontador {
    
    //Tabela de Pseudo-Instruções (POT)
    private static final Set<String> POT = new HashSet<>();

    //Tabela de Instruções de Máquina (MOT)
    private static final Map<String, int[]> MOT = new HashMap<>();

    static {
        POT.add("START");
        POT.add("END");
        POT.add("BYTE");
        POT.add("WORD");
        POT.add("RESB");
        POT.add("RESW");
        POT.add("EQU");
        POT.add("LTORG");
        POT.add("BASE");
        POT.add("NOBASE");
        POT.add("ORG");
        POT.add("DC");
        POT.add("DS");

        // Instruções de Formato 2
        MOT.put("ADDR",  new int[]{ 0x90, 2 });
        MOT.put("CLEAR", new int[]{ 0xB4, 2 });
        MOT.put("COMPR", new int[]{ 0xA0, 2 });
        MOT.put("DIVR",  new int[]{ 0x9C, 2 });
        MOT.put("MULR",  new int[]{ 0x98, 2 });
        MOT.put("RMO",   new int[]{ 0xAC, 2 });
        MOT.put("SHIFTL",new int[]{ 0xA4, 2 });
        MOT.put("SHIFTR",new int[]{ 0xA8, 2 });
        MOT.put("SUBR",  new int[]{ 0x94, 2 });
        MOT.put("TIXR",  new int[]{ 0xB8, 2 });

        // Instruções de Formato 3
        MOT.put("ADD",   new int[]{ 0x18, 3 });
        MOT.put("AND",   new int[]{ 0x40, 3 });
        MOT.put("COMP",  new int[]{ 0x28, 3 });
        MOT.put("DIV",   new int[]{ 0x24, 3 });
        MOT.put("J",     new int[]{ 0x3C, 3 });
        MOT.put("JEQ",   new int[]{ 0x30, 3 }); 
        MOT.put("JGT",   new int[]{ 0x34, 3 });
        MOT.put("JLT",   new int[]{ 0x38, 3 });
        MOT.put("JSUB",  new int[]{ 0x48, 3 });
        MOT.put("LDA",   new int[]{ 0x00, 3 });
        MOT.put("LDB",   new int[]{ 0x68, 3 });
        MOT.put("LDCH",  new int[]{ 0x50, 3 });
        MOT.put("LDL",   new int[]{ 0x08, 3 });
        MOT.put("LDS",   new int[]{ 0x6C, 3 });
        MOT.put("LDT",   new int[]{ 0x74, 3 });
        MOT.put("LDX",   new int[]{ 0x04, 3 });
        MOT.put("MUL",   new int[]{ 0x20, 3 });
        MOT.put("OR",    new int[]{ 0x44, 3 });
        MOT.put("RSUB",  new int[]{ 0x4C, 3 });
        MOT.put("STA",   new int[]{ 0x0C, 3 });
        MOT.put("STB",   new int[]{ 0x78, 3 });
        MOT.put("STCH",  new int[]{ 0x54, 3 });
        MOT.put("STL",   new int[]{ 0x14, 3 });
        MOT.put("STS",   new int[]{ 0x7C, 3 });
        MOT.put("STT",   new int[]{ 0x84, 3 });
        MOT.put("STX",   new int[]{ 0x10, 3 });
        MOT.put("SUB",   new int[]{ 0x1C, 3 });
        MOT.put("TIX",   new int[]{ 0x2C, 3 });
    }

        public static boolean PseudoOp(String operacao){
            if(operacao == null) return false;
            return POT.contains(operacao.trim().toUpperCase());
        }

        public static boolean Instrucao(String operacao){
            if (operacao == null) return false;
            String nome = operacao.trim().toUpperCase();
            if (nome.startsWith("+")){
                nome = nome.substring(1);
            } 
            return MOT.containsKey(nome);
        }
 
        public static int getOpcode(String operacao) {
            if (operacao == null) return -1;

            String nome = operacao.trim().toUpperCase();

            if (nome.startsWith("+")){
                nome = nome.substring(1);
            }

            int[] dados = MOT.get(nome);

            if (dados != null){
                return dados[0];
            } else {
                return -1;
            }
        }

        public static int getTamanho(String operacao) {
        if (operacao == null) return 0;

        String nome = operacao.trim().toUpperCase();
        
        boolean formato4 = false;

        if (nome.startsWith("+")){
            formato4 = true;
            nome =  nome.substring(1);
        }
         
        int[] dados = MOT.get(nome);

        if (dados == null) return 0;

       int tamanhoBase = dados[1];

       if(formato4 == true && tamanhoBase == 3){
            return 4;
       } else{
            return tamanhoBase;
       }

    }
}
