package com.alibaba.druid.sql.lexer;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class LexerTest {
    
    private static Dictionary dictionary = new Dictionary();
    
    private Lexer lexer;
    
    @BeforeClass
    public static void fillDictionary() {
        dictionary.fill();
    }
    
    @Test
    public void assertNextTokenForWhitespace() {
        lexer = new Lexer("Select  \t \n * from \r\n TABLE_XXX \t", dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("Select"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.STAR));
        assertThat(lexer.getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.FROM));
        assertThat(lexer.getLiterals(), is("from"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.EOF));
    }
    
    @Test
    public void assertNextTokenForVariable() {
        assertNextTokenForVariable("$", true);
        assertNextTokenForVariable("@", true);
        assertNextTokenForVariable(":", true);
        assertNextTokenForVariable("@", false);
        assertNextTokenForVariable("@@", false);
        assertNextTokenForVariable(":", false);
    }
    
    private void assertNextTokenForVariable(final String variantSymbol, final boolean withBrace) {
        String leftBrace = withBrace ? "{" : "";
        String rightBrace = withBrace ? "}" : "";
        lexer = new Lexer(String.format("SELECT %s%sx1%s FROM TABLE_XXX WHERE COLUMN_XXX= %s%sx2%s", variantSymbol, leftBrace, rightBrace, variantSymbol, leftBrace, rightBrace), dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.VARIANT));
        assertThat(lexer.getLiterals(), is(variantSymbol + leftBrace + "x1" + rightBrace));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        assertThat(lexer.getLiterals(), is("TABLE_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.WHERE));
        assertThat(lexer.getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("COLUMN_XXX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.EQ));
        assertThat(lexer.getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.VARIANT));
        assertThat(lexer.getLiterals(), is(variantSymbol + leftBrace + "x2" + rightBrace));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.EOF));
    }
    
    @Test
    public void assertNextTokenForOrderBy() {
        lexer = new Lexer("SELECT * FROM ORDER  ORDER \t  BY XX DESC", dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.STAR));
        assertThat(lexer.getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("ORDER"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.ORDER));
        assertThat(lexer.getLiterals(), is("ORDER"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.BY));
        assertThat(lexer.getLiterals(), is("BY"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.DESC));
        assertThat(lexer.getLiterals(), is("DESC"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.EOF));
    }
    
    @Test
    public void assertNextTokenForGroupBy() {
        lexer = new Lexer("SELECT * FROM GROUP  Group \n  By XX DESC", dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.STAR));
        assertThat(lexer.getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("GROUP"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.GROUP));
        assertThat(lexer.getLiterals(), is("Group"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.BY));
        assertThat(lexer.getLiterals(), is("By"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.DESC));
        assertThat(lexer.getLiterals(), is("DESC"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.EOF));
    }
    
    @Test
    public void assertNextTokenForNumber() {
        assertNextTokenForNumber("0x1e", DataType.LITERAL_HEX);
        assertNextTokenForNumber("0x-1e", DataType.LITERAL_HEX);
        assertNextTokenForNumber("123", DataType.LITERAL_INT);
        assertNextTokenForNumber("123.0", DataType.LITERAL_FLOAT);
        assertNextTokenForNumber("123e4", DataType.LITERAL_FLOAT);
        assertNextTokenForNumber("123E4", DataType.LITERAL_FLOAT);
        assertNextTokenForNumber("123e+4", DataType.LITERAL_FLOAT);
        assertNextTokenForNumber("123E+4", DataType.LITERAL_FLOAT);
        assertNextTokenForNumber("123e-4", DataType.LITERAL_FLOAT);
        assertNextTokenForNumber("123E-4", DataType.LITERAL_FLOAT);
        assertNextTokenForNumber(".5", DataType.LITERAL_FLOAT);
        assertNextTokenForNumber("123f", DataType.BINARY_FLOAT);
        assertNextTokenForNumber("123F", DataType.BINARY_FLOAT);
        assertNextTokenForNumber(".5F", DataType.BINARY_FLOAT);
        assertNextTokenForNumber("123d", DataType.BINARY_DOUBLE);
        assertNextTokenForNumber("123D", DataType.BINARY_DOUBLE);
    }
    
    private void assertNextTokenForNumber(final String number, final Token expectedToken) {
        lexer = new Lexer(String.format("SELECT * FROM XXX_TABLE WHERE XX=%s AND YY=%s", number, number), dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.STAR));
        assertThat(lexer.getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XXX_TABLE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.WHERE));
        assertThat(lexer.getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.EQ));
        assertThat(lexer.getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(expectedToken));
        assertThat(lexer.getLiterals(), is(number));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.AND));
        assertThat(lexer.getLiterals(), is("AND"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("YY"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.EQ));
        assertThat(lexer.getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is(expectedToken));
        assertThat(lexer.getLiterals(), is(number));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.EOF));
    }
    
    @Test
    public void assertNextTokenForString() {
        assertNextTokenForString("'xxx'");
        assertNextTokenForString("'x''x''''x'");
    }
    
    private void assertNextTokenForString(final String str) {
        lexer = new Lexer(String.format("SELECT * FROM XXX_TABLE WHERE XX=%s AND YY=%s", str, str), dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.STAR));
        assertThat(lexer.getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XXX_TABLE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.WHERE));
        assertThat(lexer.getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.EQ));
        assertThat(lexer.getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.LITERAL_CHARS));
        assertThat(lexer.getLiterals(), is(str.substring(1, str.length() - 1)));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.AND));
        assertThat(lexer.getLiterals(), is("AND"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("YY"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.EQ));
        assertThat(lexer.getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.LITERAL_CHARS));
        assertThat(lexer.getLiterals(), is(str.substring(1, str.length() - 1)));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.EOF));
    }
    
    @Test
    public void assertNextTokenForAlias() {
        assertNextTokenForAlias("xyz");
    }
    
    private void assertNextTokenForAlias(final String str) {
        lexer = new Lexer(String.format("SELECT * FROM XXX_TABLE AS \"%s\"", str), dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.STAR));
        assertThat(lexer.getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XXX_TABLE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.AS));
        assertThat(lexer.getLiterals(), is("AS"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.LITERAL_ALIAS));
        assertThat(lexer.getLiterals(), is(str));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.EOF));
    }
    
    @Test
    public void assertNextTokenForSingleLineComment() {
        assertNextTokenForSingleLineComment("--xyz");
        assertNextTokenForSingleLineComment("//xyz");
    }
    
    private void assertNextTokenForSingleLineComment(final String comment) {
        lexer = new Lexer(String.format("SELECT * FROM XXX_TABLE %s \n WHERE XX=1 %s", comment, comment), dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.STAR));
        assertThat(lexer.getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XXX_TABLE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.LINE_COMMENT));
        assertThat(lexer.getLiterals().trim(), is(comment));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.WHERE));
        assertThat(lexer.getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XX"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.EQ));
        assertThat(lexer.getLiterals(), is("="));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.LITERAL_INT));
        assertThat(lexer.getLiterals(), is("1"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.LINE_COMMENT));
        assertThat(lexer.getLiterals().trim(), is(comment));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.EOF));
    }
    
    @Test
    public void assertNextTokenForMultipleLineComment() {
        assertNextTokenForMultipleLineComment("/*--xyz", "//xyz*/");
    }
    
    private void assertNextTokenForMultipleLineComment(final String commentStart, final String commentEnd) {
        lexer = new Lexer(String.format("SELECT * FROM XXX_TABLE %s \n WHERE XX=1 %s WHERE YY>2 %s %s", commentStart, commentEnd, commentStart, commentEnd), dictionary);
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.SELECT));
        assertThat(lexer.getLiterals(), is("SELECT"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.STAR));
        assertThat(lexer.getLiterals(), is("*"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.FROM));
        assertThat(lexer.getLiterals(), is("FROM"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("XXX_TABLE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.MULTI_LINE_COMMENT));
        assertThat(lexer.getLiterals().trim(), is(commentStart + " \n WHERE XX=1 " + commentEnd));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DefaultKeyword.WHERE));
        assertThat(lexer.getLiterals(), is("WHERE"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.IDENTIFIER));
        assertThat(lexer.getLiterals(), is("YY"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) Symbol.GT));
        assertThat(lexer.getLiterals(), is(">"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.LITERAL_INT));
        assertThat(lexer.getLiterals(), is("2"));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.MULTI_LINE_COMMENT));
        assertThat(lexer.getLiterals().trim(), is(commentStart + " " + commentEnd));
        lexer.nextToken();
        assertThat(lexer.getToken(), is((Token) DataType.EOF));
    }
}
