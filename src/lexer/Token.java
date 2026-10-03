package lexer;

public record Token(TokenType tipo, String lexema, int linea, int columna) {
}
