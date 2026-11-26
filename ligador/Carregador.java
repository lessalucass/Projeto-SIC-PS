package ligador;

public class Carregador {
    /* Aplica os registros M pendentes (relocação simples) nos bytes ligados */

    public static int aplicarRelocacao(Passo2.ResultadoLigacao resultado){
        if(resultado.modificacoesPendentes == null) return 0;

        for(ParserObjeto.Modificacao m : resultado.modificacoesPendentes){
            Passo2.aplicarModificacao(resultado.memoriaLigada, m.endereco, m.tamanho,m.sinal, m.ajuste);
        }
        return resultado.modificacoesPendentes.size();
    }

}
