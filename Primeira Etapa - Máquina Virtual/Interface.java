import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Interface  extends JFrame{
    private Memoria memoria;
    private Registradores regs;
    private Executor executor;

    private JTextArea telaRegistradores;
    private JButton botaoNext;

    public Interface(Memoria mem, Registradores r, Executor exec){
        this.memoria = mem;
        this.regs = r;
        this.executor = exec; 

        setTitle("Simulador SIC/XE");
        setSize(300,350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        telaRegistradores = new JTextArea();
        telaRegistradores.setEditable(false);
        telaRegistradores.setFont(new Font("Monospaced", Font.BOLD, 16));
    
        add(new JScrollPane(telaRegistradores), BorderLayout.CENTER);

        botaoNext = new JButton("Executar Próximo Passo");
        botaoNext.setFont(new Font("Arial",Font.BOLD,14));

        botaoNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                boolean executou = executor.executarPasso();
                atualizarTela();

                if (!executou || executor.atingiuFim()){
                    botaoNext.setEnabled(false);
                    botaoNext.setText("Fim da Execução");
                }
            }
        });

        add(botaoNext, BorderLayout.SOUTH);
        atualizarTela();
    
    }

    private void atualizarTela(){
        StringBuilder texto = new StringBuilder();
        texto.append("--- CPU ---\n\n");

        texto.append(" A  : ").append(regs.hex(Registradores.A)).append("\n");
        texto.append(" X  : ").append(regs.hex(Registradores.X)).append("\n");
        texto.append(" L  : ").append(regs.hex(Registradores.L)).append("\n");
        texto.append(" B  : ").append(regs.hex(Registradores.B)).append("\n");
        texto.append(" S  : ").append(regs.hex(Registradores.S)).append("\n");
        texto.append(" T  : ").append(regs.hex(Registradores.T)).append("\n");
        texto.append(" PC : ").append(regs.hex(Registradores.PC)).append("\n");
        texto.append(" SW : ").append(regs.hex(Registradores.SW)).append("\n");
    
        telaRegistradores.setText(texto.toString());
    }

    
    
}
