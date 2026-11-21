package ligador;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import maquinavirtual.Executor;
import maquinavirtual.Interface;
import maquinavirtual.Memoria;
import maquinavirtual.Registradores;

/**
 * Parte 7 do Ligador - Modulo Integrador e Interface de Carga (Main).
 *
 * Uso:
 *   java -cp out ligador.Ligador [-p PROGADDR_HEX] [-m relocador|carregador] [-o saida.obj] a.obj b.obj ...
 *
 *   -p  endereco de carga do programa, PROGADDR (hexadecimal, padrao 0)
 *   -m  relocador   (padrao) Ligador-Relocador: Passo 2 resolve TUDO e o resultado
 *                   vai direto para o EscritorLigado, que grava o arquivo absoluto
 *       carregador  Ligador + Carregador: Passo 2 resolve so os simbolos externos,
 *                   mantem a relocacao simples pendente e entrega os dados brutos
 *                   ao Carregador, que carrega na memoria do simulador (sem arquivo)
 *   -o  arquivo de saida do modo relocador (padrao ligado.obj)
 *
 * Fluxo:
 *   1. le e valida os argumentos
 *   2. ValidadorSemantico.validar   (Parte 6): duplicados, referencias, limite de memoria
 *   3. Passo1.executar              (Parte 3): preenche a TabelaSimbolosGlobais (ESTAB)
 *   4. Passo2.executar              (Parte 4): T e M; true = relocador, false = carregador
 *   5. EscritorLigado.escrever      (Parte 5) OU carga na Memoria do simulador
 *
 * Erros das outras partes sao IllegalArgumentException com mensagem em portugues.
 */
public class Ligador {

    private enum Modo { RELOCADOR, CARREGADOR }

    private static class Opcoes {
        int progaddr = 0;
        Modo modo = Modo.RELOCADOR;
        String saida = "ligado.obj";
        final List<String> arquivos = new ArrayList<>();
    }

    private Ligador() {}

    public static void main(String[] args) {
        int codigo = executar(args);
        if (codigo != 0) {
            System.exit(codigo);
        }
    }

    /** Roda o ligador inteiro. Devolve 0 se deu certo, 1 se houve erro. */
    static int executar(String[] args) {
        try {
            Opcoes op = lerArgumentos(args);
            validarArquivos(op.arquivos);

            ValidadorSemantico.validar(op.arquivos, op.progaddr);

            int[] limites = Passo1.executar(op.arquivos, op.progaddr);
            int tamanho = limites[1] - limites[0];

            boolean relocacaoCompleta = (op.modo == Modo.RELOCADOR);
            Passo2.ResultadoLigacao resultado =
                    Passo2.executar(op.arquivos, op.progaddr, relocacaoCompleta);

            System.out.printf("Ligacao concluida. PROGADDR=%06X tamanho=%d entrada=%06X%n",
                    op.progaddr, tamanho, resultado.enderecoExecucao);

            if (op.modo == Modo.RELOCADOR) {
                try {
                    EscritorLigado.escrever(resultado, op.saida);
                } catch (IOException e) {
                    throw new IllegalArgumentException(
                            "Nao foi possivel gravar o arquivo ligado: " + op.saida);
                }
                System.out.println("Arquivo ligado gravado em: " + op.saida);
            } else {
                carregarEExecutar(resultado, limites[1]);
            }
            return 0;
        } catch (RuntimeException e) {
            System.err.println("Falha na ligacao: " + e.getMessage());
            return 1;
        }
    }

    /** Modo carregador: joga os bytes na memoria e abre o simulador, como o Main da maquina virtual. */
    private static void carregarEExecutar(Passo2.ResultadoLigacao resultado, int fimPrograma) {
        Memoria memoria = new Memoria();
        Registradores regs = new Registradores();
        Executor executor = new Executor(memoria, regs);

        int ultimo = fimPrograma;
        for (Map.Entry<Integer, Byte> e : resultado.memoriaLigada.entrySet()) {
            memoria.setByte(e.getKey(), e.getValue() & 0xFF);
            ultimo = Math.max(ultimo, e.getKey() + 1);
        }

        int pendentes = resultado.modificacoesPendentes == null
                ? 0 : resultado.modificacoesPendentes.size();
        if (pendentes > 0) {
            System.out.println("Aviso: " + pendentes + " registro(s) M de relocacao simples "
                    + "pendente(s): o Carregador ainda precisa aplica-los.");
        }

        regs.setPC(resultado.enderecoExecucao);
        executor.setEnderecoFinal(ultimo);
        regs.set(Registradores.L, ultimo);
        SwingUtilities.invokeLater(() -> new Interface(memoria, regs, executor).setVisible(true));
    }

    private static Opcoes lerArgumentos(String[] args) {
        Opcoes op = new Opcoes();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "-p":
                    op.progaddr = lerEndereco(valorDe(args, ++i, a));
                    break;
                case "-o":
                    op.saida = valorDe(args, ++i, a);
                    break;
                case "-m":
                    String m = valorDe(args, ++i, a).toLowerCase();
                    if (m.equals("relocador") || m.equals("ligador")) {
                        op.modo = Modo.RELOCADOR;
                    } else if (m.equals("carregador")) {
                        op.modo = Modo.CARREGADOR;
                    } else {
                        throw new IllegalArgumentException(
                                "Modo invalido: '" + m + "' (use relocador ou carregador).");
                    }
                    break;
                default:
                    if (a.startsWith("-")) {
                        throw new IllegalArgumentException("Opcao desconhecida: " + a + "\n" + uso());
                    }
                    op.arquivos.add(a);
            }
        }
        return op;
    }

    private static String valorDe(String[] args, int i, String opcao) {
        if (i >= args.length) {
            throw new IllegalArgumentException("A opcao " + opcao + " precisa de um valor.");
        }
        return args[i];
    }

    /** Aceita 1000, 0x1000 ou $1000 (hexadecimal). */
    private static int lerEndereco(String texto) {
        String t = texto.trim();
        if (t.toLowerCase().startsWith("0x")) {
            t = t.substring(2);
        } else if (t.startsWith("$")) {
            t = t.substring(1);
        }
        try {
            int valor = Integer.parseInt(t, 16);
            if (valor < 0) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("PROGADDR invalido (esperado hexadecimal): " + texto);
        }
    }

    private static void validarArquivos(List<String> arquivos) {
        if (arquivos.isEmpty()) {
            throw new IllegalArgumentException("Nenhum arquivo objeto informado.\n" + uso());
        }
        Set<String> vistos = new HashSet<>();
        for (String caminho : arquivos) {
            File f = new File(caminho);
            if (!f.isFile()) {
                throw new IllegalArgumentException("Arquivo objeto nao encontrado: " + caminho);
            }
            if (!vistos.add(f.getAbsolutePath())) {
                throw new IllegalArgumentException("Arquivo objeto repetido: " + caminho);
            }
        }
    }

    private static String uso() {
        return "Uso: java -cp out ligador.Ligador [-p PROGADDR_HEX] [-m relocador|carregador] "
                + "[-o saida.obj] a.obj b.obj ...";
    }
}