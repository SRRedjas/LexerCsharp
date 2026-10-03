package lexer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;

/**
 * Interfaz gráfica: permite escoger un archivo .cs, ver su código
 * y la tabla de tokens que produce el Lexer.
 */
public class Ventana extends JFrame {

    private static final String[] COLUMNAS = {"Línea", "Columna", "Token", "Lexema"};
    private static final Color COLOR_ERROR = new Color(255, 205, 210);

    private final JTextArea areaCodigo = new JTextArea();
    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel estado = new JLabel("Abre un archivo .cs o escribe código y presiona Analizar");
    private final JFileChooser selector = new JFileChooser(new File("."));

    private List<Token> tokens = List.of();

    public Ventana() {
        super("Analizador léxico de C#");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton botonAbrir = new JButton("Abrir archivo...");
        JButton botonAnalizar = new JButton("Analizar");
        JButton botonLimpiar = new JButton("Limpiar");
        botonAbrir.addActionListener(e -> abrirArchivo());
        botonAnalizar.addActionListener(e -> analizar());
        botonLimpiar.addActionListener(e -> limpiar());

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.add(botonAbrir);
        barra.add(botonAnalizar);
        barra.add(botonLimpiar);

        areaCodigo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        areaCodigo.setTabSize(4);
        JScrollPane panelCodigo = new JScrollPane(areaCodigo);
        panelCodigo.setBorder(BorderFactory.createTitledBorder("Código fuente"));

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setDefaultRenderer(Object.class, new RenderizadorTokens());
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        tabla.getColumnModel().getColumn(1).setMaxWidth(70);
        tabla.getSelectionModel().addListSelectionListener(e -> resaltarTokenSeleccionado());
        JScrollPane panelTokens = new JScrollPane(tabla);
        panelTokens.setBorder(BorderFactory.createTitledBorder("Tokens"));

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCodigo, panelTokens);
        division.setResizeWeight(0.5);

        estado.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        selector.setFileFilter(new FileNameExtensionFilter("Archivos de C# (*.cs)", "cs"));

        add(barra, BorderLayout.NORTH);
        add(division, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);

        setSize(1000, 650);
        setLocationRelativeTo(null);
    }

    private void abrirArchivo() {
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = selector.getSelectedFile();
        try {
            areaCodigo.setText(Files.readString(archivo.toPath()));
            areaCodigo.setCaretPosition(0);
            setTitle("Analizador léxico de C# - " + archivo.getName());
            analizar();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo leer el archivo:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void analizar() {
        tokens = new Lexer(areaCodigo.getText()).analizar();

        modelo.setRowCount(0);
        for (Token t : tokens) {
            modelo.addRow(new Object[]{t.linea(), t.columna(), t.tipo(), t.lexema()});
        }

        long errores = tokens.stream().filter(t -> t.tipo() == TokenType.ERROR).count();
        estado.setText("Total de tokens: " + tokens.size() + " | Errores: " + errores);
    }

    private void limpiar() {
        areaCodigo.setText("");
        modelo.setRowCount(0);
        tokens = List.of();
        setTitle("Analizador léxico de C#");
        estado.setText("Abre un archivo .cs o escribe código y presiona Analizar");
    }

    // Al escoger una fila de la tabla, selecciona el lexema en el código
    private void resaltarTokenSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= tokens.size()) {
            return;
        }
        Token t = tokens.get(fila);
        try {
            int inicio = areaCodigo.getLineStartOffset(t.linea() - 1) + t.columna() - 1;
            areaCodigo.requestFocusInWindow();
            areaCodigo.select(inicio, inicio + t.lexema().length());
        } catch (BadLocationException ex) {
            // el código cambió desde el último análisis: no hay nada que resaltar
        }
    }

    private static class RenderizadorTokens extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                boolean conFoco, int fila, int columna) {
            Component c = super.getTableCellRendererComponent(tabla, valor, seleccionada, conFoco, fila, columna);
            if (!seleccionada) {
                boolean esError = tabla.getValueAt(fila, 2) == TokenType.ERROR;
                c.setBackground(esError ? COLOR_ERROR : tabla.getBackground());
            }
            return c;
        }
    }
}
