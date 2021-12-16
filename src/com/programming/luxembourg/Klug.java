package com.programming.luxembourg;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Klug {
    private static final Interpreter interpreter=new Interpreter();

    static boolean hasError=false;
    static boolean hasRuntimeError=false;

    static final private int SCRIPT_FAIL_STATUS=64;
    static final private int RUNTIME_EXIT_ERROR=70;
    static boolean isREPLMode =false;


    public static void main(String[] args) throws IOException {
        if (args.length>1){
            System.out.println("Usage: luxembourg [script]");
            System.exit(SCRIPT_FAIL_STATUS);
        }else if (args.length==1){
            runFile(args[0]);
        }
        else{
            runPrompt();
        }
    }

    private static void runFile(String file) throws IOException
    {
        byte[] bytes= Files.readAllBytes(Paths.get(file));
        run(new String(bytes, Charset.defaultCharset()));
        if (hasError) {
            System.exit(65);
        }
        if (hasRuntimeError){
            System.exit(RUNTIME_EXIT_ERROR);
        }
    }

    private static void run(String source) {
        Scanner scanner=new Scanner(source);
        List<Token> tokens=scanner.scanTokens();

        Parser parser=new Parser(tokens);
        List<Stmt> statements=parser.parse();
        if (hasError)return;
        Resolver resolver=new Resolver(interpreter);
        resolver.resolve(statements);
        if (hasError)return;
        interpreter.interpret(statements);

    }
    static void error(int line,String message){
        report(line,"",message);
    }
    private static void report(int line, String where, String message){
        System.err.println("[line " + line + "] Error " + where + ": " + message);
        hasError=true;
    }
    static void error(Token token, String message){

        if (token.type==TokenType.EOF){
            report(token.line,"at end ",message);
        }else{
            report(token.line,"at '"+token.lexme+"'",message);
        }
    }

    private static void runPrompt() throws IOException {
        isREPLMode =true;
        InputStreamReader input=new InputStreamReader(System.in);
        BufferedReader reader=new BufferedReader(input);
        while(true){
            System.out.println("> ");
            String line=reader.readLine();
            if (line==null) break;
            if (line.equals(".exit")) break;
            run(line);
            hasError=false;

        }
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage()+"\n[line "+error.token.line+"]");
        hasRuntimeError=true;

    }
}
