
package ligador;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ValidadorSemantico {

    // Memoria SIC/XE: 1 MiB (enderecos de 0 a FFFFF)
    private static final long LIMITE_MEMORIA = 0x100000L;

    // Guarda onde cada simbolo foi definido
    private static final Map<String, String> simbolos = new HashMap<>();

    // Guarda referencias externas encontradas
    private static final List<Referencia> referencias = new ArrayList<>();

    // Guarda todos os erros encontrados
    private static final List<String> erros = new ArrayList<>();

    private static class Referencia {
        String nome;
        String arquivo;
        int linha;

        Referencia(String nome, String arquivo, int linha) {
            this.nome = nome;
            this.arquivo = arquivo;
            this.linha = linha;
        }
    }

    // Metodo principal chamado pelo Integrante 7
    public static void validar(List<String> arquivos, int progAddr) {

        simbolos.clear();
        referencias.clear();
        erros.clear();

        if (arquivos == null || arquivos.isEmpty()) {
            throw new IllegalArgumentException(
                    "Nenhum arquivo objeto informado."
            );
        }

        if (progAddr < 0 || (long) progAddr >= LIMITE_MEMORIA) {
            throw new IllegalArgumentException(
                    "PROGADDR fora da memoria SIC/XE."
            );
        }

        long csAddr = progAddr;

        // Primeira varredura: coleta definicoes e referencias
        for (String arquivo : arquivos) {

            int tamanhoSecao = 0;
            int inicioSecao = 0;
            int numeroLinha = 0;
            boolean encontrouHeader = false;
            boolean encontrouEnd = false;

            try {
                List<String> linhas = Files.readAllLines(Paths.get(arquivo));

                for (String linha : linhas) {
                    numeroLinha++;

                    linha = linha.trim();

                    if (linha.isEmpty()) {
                        continue;
                    }

                    if (encontrouEnd) {
                        erros.add("Registro apos E em " + arquivo
                                + ", linha " + numeroLinha);
                        continue;
                    }

                    char tipo = linha.charAt(0);

                    try {
                        if (!encontrouHeader && tipo != 'H') {
                            erros.add("Registro antes do Header em "
                                    + arquivo + ", linha " + numeroLinha);
                            continue;
                        }

                        switch (tipo) {

                            case 'H': {
                                if (encontrouHeader) {
                                    erros.add("Header duplicado em "
                                            + arquivo + ", linha " + numeroLinha);
                                    break;
                                }

                                ParserObjeto.Header h = ParserObjeto.parseH(linha);

                                encontrouHeader = true;
                                tamanhoSecao = h.tamanho;
                                inicioSecao = h.inicio;

                                adicionarSimbolo(h.nome, arquivo, numeroLinha);

                                if (tamanhoSecao < 0 ||
                                        csAddr + tamanhoSecao > LIMITE_MEMORIA) {
                                    erros.add("Secao " + h.nome
                                            + " ultrapassa o limite da memoria SIC/XE.");
                                }

                                break;
                            }

                            case 'D': {
                                for (ParserObjeto.Simbolo s : ParserObjeto.parseD(linha)) {

                                    adicionarSimbolo(s.nome, arquivo, numeroLinha);

                                    long deslocamento = (long) s.endereco - inicioSecao;

                                    if (deslocamento < 0 ||
                                            deslocamento >= tamanhoSecao) {
                                        erros.add("Simbolo " + s.nome
                                                + " definido fora da secao em "
                                                + arquivo + ", linha " + numeroLinha);
                                    }
                                }
                                break;
                            }

                            case 'R': {
                                for (String nome : ParserObjeto.parseR(linha)) {
                                    adicionarReferencia(nome, arquivo, numeroLinha);
                                }
                                break;
                            }

                            case 'T': {
                                ParserObjeto.Text t = ParserObjeto.parseT(linha);

                                long deslocamento = (long) t.inicio - inicioSecao;

                                if (deslocamento < 0 || t.tamanho < 0 ||
                                        deslocamento + t.tamanho > tamanhoSecao) {
                                    erros.add("Registro T fora dos limites da secao em "
                                            + arquivo + ", linha " + numeroLinha);
                                }
                                break;
                            }

                            case 'M': {
                                ParserObjeto.Modificacao m = ParserObjeto.parseM(linha);

                                long deslocamento = (long) m.endereco - inicioSecao;
                                long bytesAfetados = (m.tamanho + 1L) / 2L;

                                if (m.tamanho <= 0 ||
                                        deslocamento < 0 ||
                                        deslocamento + bytesAfetados > tamanhoSecao) {
                                    erros.add("Registro M fora dos limites da secao em "
                                            + arquivo + ", linha " + numeroLinha);
                                }

                                if (m.sinal != '+' && m.sinal != '-') {
                                    erros.add("Sinal invalido no registro M em "
                                            + arquivo + ", linha " + numeroLinha);
                                }

                                if (m.simbolo != null && !m.simbolo.isEmpty()) {
                                    adicionarReferencia(
                                            m.simbolo, arquivo, numeroLinha
                                    );
                                }
                                break;
                            }

                            case 'E': {
                                int enderecoExecucao = ParserObjeto.parseE(linha);

                                if (enderecoExecucao != -1) {
                                    long deslocamento =
                                            (long) enderecoExecucao - inicioSecao;

                                    if (deslocamento < 0 ||
                                            deslocamento >= tamanhoSecao) {
                                        erros.add("Endereco de execucao invalido em "
                                                + arquivo + ", linha " + numeroLinha);
                                    }
                                }

                                encontrouEnd = true;
                                break;
                            }

                            default:
                                erros.add("Tipo de registro desconhecido em "
                                        + arquivo + ", linha " + numeroLinha
                                        + ": " + tipo);
                        }

                    } catch (RuntimeException e) {
                        erros.add("Registro invalido em " + arquivo
                                + ", linha " + numeroLinha
                                + ": " + e.getMessage());
                    }
                }

                if (!encontrouHeader) {
                    erros.add("Arquivo sem registro H: " + arquivo);
                }

                if (!encontrouEnd) {
                    erros.add("Arquivo sem registro E: " + arquivo);
                }

            } catch (IOException e) {
                erros.add("Nao foi possivel ler o arquivo: " + arquivo
                        + " (" + e.getMessage() + ")");
            }

            csAddr += tamanhoSecao;
        }

        // Segunda verificacao: todas as referencias devem existir
        verificarReferencias();

        // Verificacao final da memoria
        if (csAddr > LIMITE_MEMORIA) {
            erros.add("Programa ultrapassa a memoria SIC/XE. "
                    + "Ultimo endereco exclusivo: "
                    + Long.toHexString(csAddr).toUpperCase());
        }

        // Exibe todos os erros antes de interromper
        if (!erros.isEmpty()) {
            StringBuilder mensagem = new StringBuilder();

            mensagem.append("Falha na validacao semantica:\n");

            for (String erro : erros) {
                mensagem.append(" - ").append(erro).append("\n");
            }

            throw new IllegalArgumentException(mensagem.toString());
        }

        System.out.println("Validacao semantica concluida com sucesso.");
        System.out.println("Nenhum simbolo duplicado encontrado.");
        System.out.println("Todas as referencias externas foram resolvidas.");
        System.out.println("Programa dentro dos limites da memoria SIC/XE.");
    }

    // Valida nomes repetidos, ignorando maiusculas/minusculas
    private static void adicionarSimbolo(
            String nome, String arquivo, int linha) {

        if (nome == null || nome.trim().isEmpty()) {
            erros.add("Simbolo com nome vazio em "
                    + arquivo + ", linha " + linha);
            return;
        }

        nome = nome.trim().toUpperCase();

        if (simbolos.containsKey(nome)) {
            erros.add("Simbolo externo duplicado: " + nome
                    + " | Primeira definicao: " + simbolos.get(nome)
                    + " | Nova definicao: " + arquivo
                    + ", linha " + linha);
        } else {
            simbolos.put(nome, arquivo + ", linha " + linha);
        }
    }

    // Armazena referencias para verificar depois
    private static void adicionarReferencia(
            String nome, String arquivo, int linha) {

        if (nome == null || nome.trim().isEmpty()) {
            erros.add("Referencia com nome vazio em "
                    + arquivo + ", linha " + linha);
            return;
        }

        referencias.add(new Referencia(
                nome.trim().toUpperCase(), arquivo, linha
        ));
    }

    // Verifica se cada referencia possui uma definicao
    private static void verificarReferencias() {

        Set<String> reportadas = new HashSet<>();

        for (Referencia ref : referencias) {

            if (!simbolos.containsKey(ref.nome)) {

                String chave = ref.nome + "|" + ref.arquivo + "|" + ref.linha;

                if (reportadas.add(chave)) {
                    erros.add("Referencia externa nao resolvida: "
                            + ref.nome + " em " + ref.arquivo
                            + ", linha " + ref.linha);
                }
            }
        }
    }
}
