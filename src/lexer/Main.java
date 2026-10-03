package lexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) throws IOException {
        // Sin argumentos abre la interfaz gráfica; con una ruta imprime los tokens en consola
        if (args.length == 0) {
            SwingUtilities.invokeLater(() -> new Ventana().setVisible(true));
            return;
        }

        String codigo = Files.readString(Path.of(args[0]));

        List<Token> tokens = new Lexer(codigo).analizar();

        System.out.printf("%-6s %-4s %-18s %s%n", "LINEA", "COL", "TOKEN", "LEXEMA");
        System.out.println("-".repeat(60));
        for (Token t : tokens) {
            System.out.printf("%-6d %-4d %-18s %s%n", t.linea(), t.columna(), t.tipo(), t.lexema());
        }

        long errores = tokens.stream().filter(t -> t.tipo() == TokenType.ERROR).count();
        System.out.println("-".repeat(60));
        System.out.println("Total de tokens: " + tokens.size() + " | Errores: " + errores);
    }
}
