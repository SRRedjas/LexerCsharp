package lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Analizador léxico simple para C#.
 * Recorre el código carácter por carácter y produce la lista de tokens.
 */
public class Lexer {

    private static final Set<String> PALABRAS_RESERVADAS = Set.of(
            // tipos (definición de variables)
            "bool", "byte", "char", "decimal", "double", "float", "int", "long",
            "object", "short", "string", "var", "void", "const",
            // literales
            "true", "false", "null",
            // control de flujo
            "if", "else", "switch", "case", "default", "for", "foreach", "in",
            "while", "do", "break", "continue", "return",
            // estructura
            "using", "namespace", "class", "struct", "interface", "enum", "new",
            "this", "base", "public", "private", "protected", "internal",
            "static", "readonly", "abstract", "virtual", "override",
            // excepciones
            "try", "catch", "finally", "throw"
    );

    // Ordenados de mayor a menor longitud para tomar siempre el operador más largo
    private static final String[] OPERADORES = {
            "<<=", ">>=", "??=",
            "==", "!=", "<=", ">=", "&&", "||", "++", "--", "+=", "-=", "*=",
            "/=", "%=", "=>", "??", "<<", ">>",
            "+", "-", "*", "/", "%", "=", "<", ">", "!", "&", "|", "^", "~", "?"
    };

    private static final String DELIMITADORES = "(){}[];,.:";

    private final String codigo;
    private int pos = 0;
    private int linea = 1;
    private int columna = 1;

    public Lexer(String codigo) {
        this.codigo = codigo;
    }

    public List<Token> analizar() {
        List<Token> tokens = new ArrayList<>();

        while (pos < codigo.length()) {
            char c = actual();

            if (Character.isWhitespace(c)) {
                avanzar();
                continue;
            }

            int inicio = pos;
            int lineaInicio = linea;
            int columnaInicio = columna;
            TokenType tipo;

            if (c == '/' && siguiente() == '/') {
                tipo = comentarioDeLinea();
            } else if (c == '/' && siguiente() == '*') {
                tipo = comentarioDeBloque();
            } else if (Character.isLetter(c) || c == '_') {
                tipo = identificador(inicio);
            } else if (Character.isDigit(c)) {
                tipo = numero();
            } else if (c == '"') {
                tipo = cadena(false);
            } else if ((c == '$' || c == '@') && siguiente() == '"') {
                avanzar(); // prefijo de cadena interpolada ($) o literal (@)
                tipo = cadena(c == '@');
            } else if (c == '\'') {
                tipo = caracter();
            } else if (DELIMITADORES.indexOf(c) >= 0) {
                avanzar();
                tipo = TokenType.DELIMITADOR;
            } else {
                tipo = operador();
            }

            tokens.add(new Token(tipo, codigo.substring(inicio, pos), lineaInicio, columnaInicio));
        }

        return tokens;
    }

    private TokenType comentarioDeLinea() {
        while (pos < codigo.length() && actual() != '\n') {
            avanzar();
        }
        return TokenType.COMENTARIO;
    }

    private TokenType comentarioDeBloque() {
        avanzar();
        avanzar();
        while (pos < codigo.length()) {
            if (actual() == '*' && siguiente() == '/') {
                avanzar();
                avanzar();
                return TokenType.COMENTARIO;
            }
            avanzar();
        }
        return TokenType.ERROR; // comentario sin cerrar
    }

    private TokenType identificador(int inicio) {
        while (pos < codigo.length() && (Character.isLetterOrDigit(actual()) || actual() == '_')) {
            avanzar();
        }
        String lexema = codigo.substring(inicio, pos);
        return PALABRAS_RESERVADAS.contains(lexema) ? TokenType.PALABRA_RESERVADA : TokenType.IDENTIFICADOR;
    }

    private TokenType numero() {
        while (pos < codigo.length() && Character.isDigit(actual())) {
            avanzar();
        }
        // parte decimal: solo si después del punto viene un dígito
        if (pos < codigo.length() && actual() == '.' && Character.isDigit(siguiente())) {
            avanzar();
            while (pos < codigo.length() && Character.isDigit(actual())) {
                avanzar();
            }
        }
        // sufijo de tipo: 1.5f, 10L, 2.0m, 3d
        if (pos < codigo.length() && "fFdDmMlLuU".indexOf(actual()) >= 0) {
            avanzar();
        }
        return TokenType.NUMERO;
    }

    private TokenType cadena(boolean literal) {
        avanzar(); // comilla de apertura
        while (pos < codigo.length()) {
            char c = actual();
            if (c == '"') {
                avanzar();
                return TokenType.CADENA;
            }
            if (c == '\n' && !literal) {
                break;
            }
            if (c == '\\' && !literal) {
                avanzar(); // salta el carácter escapado
            }
            avanzar();
        }
        return TokenType.ERROR; // cadena sin cerrar
    }

    private TokenType caracter() {
        avanzar(); // comilla de apertura
        if (pos < codigo.length() && actual() == '\\') {
            avanzar();
        }
        avanzar();
        if (pos < codigo.length() && actual() == '\'') {
            avanzar();
            return TokenType.CARACTER;
        }
        return TokenType.ERROR;
    }

    private TokenType operador() {
        for (String op : OPERADORES) {
            if (codigo.startsWith(op, pos)) {
                for (int i = 0; i < op.length(); i++) {
                    avanzar();
                }
                return TokenType.OPERADOR;
            }
        }
        avanzar();
        return TokenType.ERROR; // símbolo no reconocido
    }

    private char actual() {
        return codigo.charAt(pos);
    }

    private char siguiente() {
        return pos + 1 < codigo.length() ? codigo.charAt(pos + 1) : '\0';
    }

    private void avanzar() {
        if (pos >= codigo.length()) {
            return;
        }
        if (codigo.charAt(pos) == '\n') {
            linea++;
            columna = 1;
        } else {
            columna++;
        }
        pos++;
    }
}
