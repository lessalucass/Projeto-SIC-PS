package ligador;

import java.util.HashMap;
import java.util.Map;

public class TabelaSimbolosGlobais {

    public static class DadosSimbolo{
        public int endereco;
        public String modo;  //modo de relocabilidade(relativo ou absoluto)
    
        public DadosSimbolo(int endereco, String modo){
            this.endereco = endereco;
            this.modo = modo;
        }
    }
    
    //Mapeia o nome da variável global -> DadosSimbolo(endereço e modo)
    private static final Map<String, DadosSimbolo> TABELA = new HashMap<>();

    public static void inicializar(){
        TABELA.clear();
    }

    //Usado pelo Passo 1 para unificar os símbolos encontrados 
    public static void inserir (String nome, int endereco, String modo){
        if (nome == null || nome.trim().isEmpty()){
            throw new IllegalArgumentException("Nome do símbolo não pode ser vazio.");
        }

        nome = nome.trim().toUpperCase();

        if(TABELA.containsKey(nome)){
            throw new IllegalArgumentException("Erro ligador: Símbolo global duplicado:" + nome); 
        }
        TABELA.put(nome, new DadosSimbolo(endereco, modo));
    }

    //Usado pelo Passo 2 
    public static int buscarEndereco(String nome){
        if (nome == null) return -1;

        nome = nome.trim().toUpperCase();
        DadosSimbolo dados = TABELA.get(nome);

        if(dados == null){
            throw new IllegalArgumentException("Erro ligador");
        }
        return dados.endereco;
    }
  
    public static String buscarModo(String nome){
        if (nome == null){
            return null;
        }
        nome = nome.trim().toUpperCase();
        DadosSimbolo dados = TABELA.get(nome);

        if (dados == null){
            throw new IllegalArgumentException("Erro ligador.");
        }
        return dados.modo;
    }

    public static boolean existe(String nome){
        if(nome== null){
            return false;
        }
        return TABELA.containsKey(nome.trim().toUpperCase());
    }
}
