package vista;

import controlador.Controlador;
import modelo.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator; // <--- IMPORTANTE
import java.util.List;

public class VistaGrafica implements IVistaRummy {

    private JFrame frame;
    private Controlador controlador;
    private JPanel panelTapete;
    private JPanel panelMano;
    private JTextArea logArea;
    private JLabel lblTurno;

    // Colores y Fuentes
    private final Color COLOR_TAPETE = new Color(34, 100, 34); // Verde oscuro
    private final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 14);

    @Override
    public void iniciar() {
        // Configuración de la ventana principal
        frame = new JFrame("Rummy - Partida Gráfica");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // 1. PANEL SUPERIOR (Info y Log)
        JPanel panelInfo = new JPanel(new BorderLayout());
        panelInfo.setBackground(new Color(20, 60, 20));

        lblTurno = new JLabel("Conectando...", SwingConstants.CENTER);
        lblTurno.setForeground(Color.YELLOW);
        lblTurno.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTurno.setBorder(new EmptyBorder(10, 0, 10, 0));
        panelInfo.add(lblTurno, BorderLayout.NORTH);

        logArea = new JTextArea(4, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollLog = new JScrollPane(logArea);
        panelInfo.add(scrollLog, BorderLayout.CENTER);

        frame.add(panelInfo, BorderLayout.NORTH);

        // 2. PANEL CENTRAL (Tapete / Mesa)
        panelTapete = new JPanel();
        panelTapete.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        panelTapete.setBackground(COLOR_TAPETE);
        JScrollPane scrollTapete = new JScrollPane(panelTapete);
        scrollTapete.setBorder(null);
        frame.add(scrollTapete, BorderLayout.CENTER);

        // 3. PANEL INFERIOR (Mano del Jugador)
        panelMano = new JPanel();
        panelMano.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
        panelMano.setBackground(new Color(45, 130, 45)); // Verde más claro
        panelMano.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE),
                " TU MANO (Ordenada) ",
                0, 0,
                FUENTE_TITULO,
                Color.WHITE
        ));

        JScrollPane scrollMano = new JScrollPane(panelMano);
        scrollMano.setPreferredSize(new Dimension(frame.getWidth(), 180));
        scrollMano.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        frame.add(scrollMano, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    @Override
    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    // MÉTODOS VISUALES

    @Override
    public void mostrarMensaje(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[INFO] " + msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
            if (msg.contains("Esperando") || msg.contains("Turno de")) {
                lblTurno.setText(msg);
                lblTurno.setForeground(Color.WHITE);
            }
        });
    }

    @Override
    public void mostrarEstadoJugador(Jugador jugador) {
        SwingUtilities.invokeLater(() -> {
            lblTurno.setText(">>> ES TU TURNO: " + jugador.getNombre().toUpperCase() + " <<<");
            lblTurno.setForeground(Color.CYAN);

            panelMano.removeAll();

            //ORDENO LA MANO VISUALMENTE
            List<Carta> manoOrdenada = new ArrayList<>(jugador.getMano().getCartas());
            manoOrdenada.sort(Comparator.comparing(Carta::getValor)); // Ordena por Valor (As, 2... K)

            for (Carta c : manoOrdenada) {
                JPanel cartaView = crearVistaCarta(c, 0.85);
                panelMano.add(cartaView);
            }
            panelMano.revalidate();
            panelMano.repaint();
        });
    }

    @Override
    public void mostrarTapete(List<Combinacion> tapete) {
        SwingUtilities.invokeLater(() -> {
            panelTapete.removeAll();

            if (tapete.isEmpty()) {
                JLabel lblVacio = new JLabel("MESA VACÍA");
                lblVacio.setForeground(new Color(255, 255, 255, 100));
                lblVacio.setFont(new Font("SansSerif", Font.BOLD, 30));
                panelTapete.add(lblVacio);
            } else {
                int i = 0;
                for (Combinacion comb : tapete) {
                    JPanel panelComb = new JPanel();
                    panelComb.setLayout(new BoxLayout(panelComb, BoxLayout.Y_AXIS));
                    panelComb.setOpaque(false);
                    panelComb.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                    // Etiqueta del juego
                    JLabel lblIndex = new JLabel("Juego " + i);
                    lblIndex.setForeground(Color.YELLOW);
                    lblIndex.setAlignmentX(Component.CENTER_ALIGNMENT);
                    panelComb.add(lblIndex);

                    // Panel de cartas del juego
                    JPanel cartasPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, -25, 0));
                    cartasPanel.setOpaque(false);

                    // --- ORDENO LAS CARTAS DE LA COMBINACIÓN ---
                    // Esto arregla el problema visual del "Q - 10 - J"
                    List<Carta> cartasOrdenadas = new ArrayList<>(comb.getCartas());
                    cartasOrdenadas.sort(Comparator.comparing(Carta::getValor));

                    for (Carta c : cartasOrdenadas) {
                        cartasPanel.add(crearVistaCarta(c, 0.6));
                    }
                    panelComb.add(cartasPanel);

                    panelTapete.add(panelComb);
                    i++;
                }
            }
            panelTapete.revalidate();
            panelTapete.repaint();
        });
    }

    @Override
    public void mostrarCartaDescarteActual(Carta carta) {
        if (carta != null) {
            SwingUtilities.invokeLater(() ->
                    logArea.append("Carta en el pozo: " + carta + "\n")
            );
        }
    }

    @Override
    public void actualizar(Evento evento) {
        SwingUtilities.invokeLater(() -> {
            switch (evento) {
                case NUEVA_RONDA -> {
                    logArea.append("\n--- NUEVA RONDA ---\n");
                    JOptionPane.showMessageDialog(frame, "¡Arranca una nueva ronda!");
                }
                case JUGADOR_ELIMINADO -> JOptionPane.showMessageDialog(frame, "Un jugador ha sido eliminado.");
                case BAJAR_COMBINACION -> logArea.append("¡Alguien bajó juego!\n");
                case DESCARTAR -> logArea.append("Alguien descartó.\n");
                case ROBAR_MAZO -> logArea.append("Alguien robó del mazo.\n");
                default -> logArea.append("Evento: " + evento + "\n");
            }
        });
    }

    @Override
    public void mostrarGanadorRonda(Jugador ganador) {
        JOptionPane.showMessageDialog(frame, "¡Ganador de la ronda: " + ganador.getNombre() + "!");
    }

    @Override
    public void mostrarGanadorFinal(Jugador ganador) {
        JOptionPane.showMessageDialog(frame, "🏆 GANADOR FINAL DEL PARTIDO: " + ganador.getNombre() + " 🏆");
        System.exit(0);
    }

    // DISEÑO DE CARTAS

    private JPanel crearVistaCarta(Carta c, double escala) {
        int w = (int) (100 * escala);
        int h = (int) (140 * escala);

        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // 1. Fondo Blanco
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 12, 12);

                // 2. Borde Negro
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(2, 2, w - 4, h - 4, 12, 12);

                if (c == null) return;

                Color color = getColorPalo(c.getPalo());
                String textoValor = getTextoValor(c.getValor());
                String simbolo = getSimboloPalo(c.getPalo());

                g2.setColor(color);

                // Índice
                int fontSizeSmall = (int)(18 * escala);
                g2.setFont(new Font("Arial", Font.BOLD, fontSizeSmall));
                g2.drawString(textoValor, 8, (int)(22 * escala));

                g2.setFont(new Font("SansSerif", Font.PLAIN, fontSizeSmall));
                g2.drawString(simbolo, 8, (int)(38 * escala));

                // Símbolo Central
                int fontSizeBig = (int)(50 * escala);
                g2.setFont(new Font("SansSerif", Font.PLAIN, fontSizeBig));

                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(simbolo);
                int textH = fm.getAscent();
                g2.drawString(simbolo, (w - textW) / 2, (h + textH) / 2 - 5);
            }
        };
        p.setPreferredSize(new Dimension(w, h));
        p.setOpaque(false);
        return p;
    }

    private Color getColorPalo(Palo p) {
        if (p == Palo.CORAZON || p == Palo.DIAMANTE) return new Color(200, 0, 0);
        return Color.BLACK;
    }

    private String getSimboloPalo(Palo p) {
        return switch (p) {
            case CORAZON -> "♥";
            case DIAMANTE -> "♦";
            case TREBOL -> "♣";
            case PICA -> "♠";
        };
    }

    private String getTextoValor(Valor v) {
        return switch (v.toString()) {
            case "AS" -> "A";
            case "DOS" -> "2";
            case "TRES" -> "3";
            case "CUATRO" -> "4";
            case "CINCO" -> "5";
            case "SEIS" -> "6";
            case "SIETE" -> "7";
            case "OCHO" -> "8";
            case "NUEVE" -> "9";
            case "DIEZ" -> "10";
            default -> v.toString().substring(0,1);
        };
    }

    // INTERACCIÓN (BOTONES)

    @Override
    public int leerEnteroSimple() {
        String[] opciones = {"Bajar Juego", "Agregar Carta", "Terminar Turno"};
        int seleccion = JOptionPane.showOptionDialog(
                frame,
                "¿Qué jugada querés hacer?",
                "Tu Turno",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );
        return seleccion + 1;
    }

    @Override
    public int pedirOpcionRobo(Jugador jugador) {
        JDialog d = new JDialog(frame, "Robar Carta", true);
        d.setLayout(new BorderLayout());
        d.setSize(400, 250);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        p.setBackground(new Color(30, 80, 30));

        // Botón MAZO
        JButton btnMazo = new JButton("MAZO");
        btnMazo.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnMazo.setHorizontalTextPosition(SwingConstants.CENTER);
        btnMazo.setIcon(new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(new Color(150, 0, 0));
                g.fillRoundRect(x, y, 60, 90, 8, 8);
                g.setColor(Color.WHITE);
                g.drawRoundRect(x+2, y+2, 56, 86, 8, 8);
                g.drawLine(x, y, x+60, y+90);
                g.drawLine(x+60, y, x, y+90);
            }
            public int getIconWidth() { return 60; }
            public int getIconHeight() { return 90; }
        });

        // Botón DESCARTE
        JButton btnDescarte = new JButton("DESCARTE");
        btnDescarte.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnDescarte.setHorizontalTextPosition(SwingConstants.CENTER);
        btnDescarte.setIcon(new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(Color.WHITE);
                g.fillRoundRect(x, y, 60, 90, 8, 8);
                g.setColor(Color.BLACK);
                g.drawRoundRect(x, y, 60, 90, 8, 8);
                g.drawString("?", x+25, y+50);
            }
            public int getIconWidth() { return 60; }
            public int getIconHeight() { return 90; }
        });

        final int[] result = {1};
        btnMazo.addActionListener(e -> { result[0]=1; d.dispose(); });
        btnDescarte.addActionListener(e -> { result[0]=2; d.dispose(); });

        p.add(btnMazo);
        p.add(btnDescarte);

        d.add(new JLabel("¿De dónde querés robar, " + jugador.getNombre() + "?", SwingConstants.CENTER), BorderLayout.NORTH);
        d.add(p, BorderLayout.CENTER);
        d.setVisible(true);

        return result[0];
    }

    // --- Selectores Visuales ---

    private List<Integer> mostrarSelectorCartas(Jugador jugador, String titulo, boolean multiple) {
        JDialog dialog = new JDialog(frame, titulo, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(800, 450);
        dialog.setLocationRelativeTo(frame);

        JPanel panelCartas = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelCartas.setBackground(new Color(34, 100, 34));

        List<Integer> seleccionados = new ArrayList<>();
        List<JToggleButton> botones = new ArrayList<>();

        // ACÁ USO LA LISTA ORIGINAL PARA MANTENER LOS ÍNDICES CORRECTOS
        // (Visualmente pueden aparecer desordenadas en el selector, o podemos ordenar y mapear indices,
        // pero para evitar bugs de índices, mostramos como están en la mano del objeto jugador).
        List<Carta> mano = jugador.getMano().getCartas();

        for (int i = 0; i < mano.size(); i++) {
            Carta c = mano.get(i);
            JToggleButton btn = new JToggleButton();
            btn.setLayout(new BorderLayout());
            btn.add(crearVistaCarta(c, 0.8), BorderLayout.CENTER);
            btn.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
            btn.setBackground(Color.DARK_GRAY);
            btn.setFocusPainted(false);

            final int index = i;
            btn.addActionListener(e -> {
                if (btn.isSelected()) {
                    btn.setBorder(new LineBorder(Color.YELLOW, 4));
                    if (!multiple) {
                        for (JToggleButton other : botones) {
                            if (other != btn) {
                                other.setSelected(false);
                                other.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
                            }
                        }
                        seleccionados.clear();
                    }
                    if (!seleccionados.contains(index)) seleccionados.add(index);
                } else {
                    btn.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
                    seleccionados.remove((Integer) index);
                }
            });

            botones.add(btn);
            panelCartas.add(btn);
        }

        JButton btnConfirmar = new JButton("CONFIRMAR SELECCIÓN");
        btnConfirmar.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnConfirmar.setBackground(new Color(200, 200, 200));
        btnConfirmar.addActionListener(e -> {
            if (seleccionados.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Seleccioná al menos una carta.");
            } else {
                dialog.dispose();
            }
        });

        dialog.add(new JScrollPane(panelCartas), BorderLayout.CENTER);
        dialog.add(btnConfirmar, BorderLayout.SOUTH);
        dialog.setVisible(true);

        return seleccionados;
    }

    @Override
    public List<Integer> pedirIndicesCombinacion(Jugador jugador) {
        return mostrarSelectorCartas(jugador, "Seleccioná cartas para bajar", true);
    }

    @Override
    public int pedirCartaParaAgregar(Jugador jugador) {
        List<Integer> sel = mostrarSelectorCartas(jugador, "Seleccioná UNA carta para agregar", false);
        return sel.isEmpty() ? -1 : sel.get(0);
    }

    @Override
    public int pedirIndiceCartaADescartar(Jugador jugador) {
        List<Integer> sel = mostrarSelectorCartas(jugador, "Seleccioná la carta a DESCARTAR", false);
        return sel.isEmpty() ? 0 : sel.get(0);
    }

    @Override
    public int pedirCombinacionATocar(List<Combinacion> tapete) {
        if (tapete.isEmpty()) return -1;

        JDialog d = new JDialog(frame, "Elegí el juego destino", true);
        d.setSize(600, 500);
        d.setLocationRelativeTo(frame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(20, 60, 20));

        final int[] sel = {-1};

        for(int i=0; i<tapete.size(); i++) {
            Combinacion c = tapete.get(i);

            JButton b = new JButton();
            b.setLayout(new FlowLayout(FlowLayout.LEFT));
            b.setBackground(new Color(34, 139, 34));

            // Ordenamos visualmente el botón del juego también
            List<Carta> sorted = new ArrayList<>(c.getCartas());
            sorted.sort(Comparator.comparing(Carta::getValor));

            for(Carta card : sorted) {
                b.add(crearVistaCarta(card, 0.5));
            }

            int idx = i;
            b.addActionListener(e -> { sel[0] = idx; d.dispose(); });

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.YELLOW), "Juego " + i,
                    0,0, new Font("SansSerif", Font.BOLD, 12), Color.YELLOW));
            wrapper.add(b);

            p.add(wrapper);
            p.add(Box.createVerticalStrut(10));
        }

        d.add(new JScrollPane(p));
        d.setVisible(true);
        return sel[0];
    }

    // --- Métodos de diálogo simples ---
    @Override public String pedirNombreJugador(int n) { return JOptionPane.showInputDialog(frame, "Nombre Jugador " + n); }
    @Override public int pedirModoDeJuego() {
        String[] ops = {"Express", "Puntos"};
        return JOptionPane.showOptionDialog(frame, "Modo", "Config", 0,3,null,ops,ops[0]) == 1 ? 2 : 1;
    }
    @Override public int pedirCantidadJugadores() { return 2; }
    @Override public int pedirLimiteDePuntos() { return 100; }
    @Override public boolean pedirReenganche(Jugador j) { return JOptionPane.showConfirmDialog(frame, "Reenganchar?", "Fin", JOptionPane.YES_NO_OPTION) == 0; }
}