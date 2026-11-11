package montador;

public class InstrucaoInfo{
    private final String mnemonico;
    private final int opcode;
    private final int formatoPadrao;
    private final int numOperandos;

    public InstrucaoInfo(String mnemonico, int opcode, int formatoPadrao, int numOperandos) {
        this.mnemonico = mnemonico;
        this.opcode = opcode;
        this.formatoPadrao = formatoPadrao;
        this.numOperandos = numOperandos;
    }

    public String getMnemonico(){
        return mnemonico;
    }

    public int getOpcode(){
        return opcode;
    }

    public int getFormatoPadrao(){
        return formatoPadrao;
    }

    public int getNumOperandos(){
        return numOperandos;
    }
}