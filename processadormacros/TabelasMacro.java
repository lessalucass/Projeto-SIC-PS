package processadormacros;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabelasMacro {
    
    // Tabela de nomes
    public static final Map<String, int[]> TAB_NOME = new HashMap<>();

    // Tabela de Definições
    public static final List<String> TAB_DEFINICAO = new ArrayList<>();

    //Tabela de Parâmetros
    public static final List<String> TABELA_PARAMETROS = new ArrayList<>();

    public static void inicializar(){
        TAB_NOME.clear();
        TAB_DEFINICAO.clear();
        TABELA_PARAMETROS.clear();
    }

}
