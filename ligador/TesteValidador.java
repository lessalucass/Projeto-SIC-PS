
package ligador;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class TesteValidador {

    public static void main(String[] args) {

        System.out.println("Pasta de execucao: "
                + System.getProperty("user.dir"));

        try {
            // Criando primeiro arquivo objeto
            String objeto1 =
                    "H^MOD1  ^000000^000006\n" +
                            "D^VAR1  ^000000\n" +
                            "R^VAR2\n" +
                            "T^000000^06^141033281030\n" +
                            "M^000001^05^+VAR2\n" +
                            "E^000000\n";

            // Criando segundo arquivo objeto
            String objeto2 =
                    "H^MOD2  ^000000^000006\n" +
                            "D^VAR2  ^000000\n" +
                            "T^000000^06^141033281030\n" +
                            "E\n";

            // Salva os arquivos na pasta de execucao
            Files.writeString(Path.of("teste1.obj"), objeto1);
            Files.writeString(Path.of("teste2.obj"), objeto2);

            System.out.println("Arquivos de teste criados!");

            // Lista dos arquivos objeto
            List<String> arquivos = Arrays.asList(
                    "teste1.obj",
                    "teste2.obj"
            );

            int progAddr = 0x1000;

            // Executa a validacao
            ValidadorSemantico.validar(arquivos, progAddr);

            System.out.println("Programa aprovado pelo validador!");

        } catch (IOException e) {
            System.err.println("Erro ao criar arquivos: "
                    + e.getMessage());

        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println("Ligacao interrompida.");
        }
    }
}
