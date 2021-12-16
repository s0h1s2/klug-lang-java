package com.programming.luxembourg;
import java.io.IOException;
import java.nio.file.Files;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.programming.luxembourg.TokenType.*;

import com.programming.luxembourg.methods.Clock;
import com.programming.luxembourg.methods.Pow;
import com.programming.luxembourg.methods.Time;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>
{
    public Environment globals=new Environment();
    private Environment environment=globals;
    private final Map<Expr,Integer> locals=new HashMap<>();

    private boolean shouldStop=false;
    Interpreter(){
        globals.define("clock", new Clock());
        globals.define("time", new Time());
        globals.define("pow", new Pow());


    }
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
            Object value=evaluate(stmt.expression);
            if (Klug.isREPLMode){
                System.out.println(stringify(value));
            }
            return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value=evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;

    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value=null;
        if (stmt.initializer!=null){
            value=evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexme,value);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block block) {
        executeBlock(block.statements,new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))){
            execute(stmt.thenBranch);
        }else if (stmt.elseBranch!=null){
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition)) && !shouldStop){
            execute(stmt.body);
        }
        shouldStop=false;
        return null;

    }

    @Override
    public Void visitBreakStmt(Stmt.Break aBreak) {
        shouldStop=true;
        return null;

    }

    @Override
    public Void visitFunctionStmt(Stmt.Function function) {
        LoxFunction loxFunction=new LoxFunction(function,environment);
        environment.define(function.name.lexme,loxFunction);
        return null;

    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value=null;
        if (stmt.value!=null){
            value=evaluate(stmt.value);

        }
        throw new Return(value);


    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt)
    {
        environment.define(stmt.name.lexme,null);
        Map<String,LoxFunction> methods=new HashMap<>();
        for (Stmt.Function method:stmt.methods){
            LoxFunction function=new LoxFunction(method,environment);

            methods.put(method.name.lexme,function);;


        }
        LoxClass lclass=new LoxClass(stmt.name.lexme,methods);

        environment.assign(stmt.name,lclass);
        return null;
    }

    @Override
    public Void visitImportStatement(Stmt.Import anImport)  {
        try{
            String source=new String(Files.readAllBytes(Paths.get(anImport.path.toString())));
            Scanner scanner=new Scanner(source);
            List<Token> tokens=scanner.scanTokens();
            Parser parser=new Parser(tokens);
            List<Stmt> statements= parser.parse();
            Resolver resolver=new Resolver(this);
            resolver.resolve(statements);
            for (int i=0;i<statements.size();i++){
                execute(statements.get(i));

            }
        }
        catch (IOException e) {
            System.out.println("Module couldn't be found.");

            System.exit(70);
        }
        return null;
    }


    public void executeBlock(List<Stmt> statements, Environment environment)
    {
        Environment prev=this.environment;
        try {
            this.environment=environment;
            for (Stmt statement:statements){
                execute(statement);

            }
        }finally {
            this.environment=prev;
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left=evaluate(expr.left);
        Object right=evaluate(expr.right);
        switch (expr.operator.type){
            case MINUS :
                checkNumberOperand(expr.operator,right);
                return (double)left-(double)right;
            case SLASH:
                checkNumberOperands(expr.operator,left,right);
                if ((double)right==0){
                    throw new RuntimeError(expr.operator,"Division by zero");
                }
                return (double)left/(double)right;
            case STAR:
                checkNumberOperands(expr.operator,left,right);
                return (double)left*(double)right;
            case MODULO:
                return (double)left%(double)right;
            case EXPONENT:
                return Math.pow((double) left,(double) right);

            case PLUS:
                if (left instanceof Double && right instanceof Double){
                    return (double)left+(double) right;
                }
                if (left instanceof String && right instanceof String){
                    return (String)left+(String)right;
                }
                if (left instanceof String && right instanceof Double){
                    return (String)left+stringify(right);

                }
                if (left instanceof Double && right instanceof String){
                    return stringify(left)+(String) right;
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");

            case GREATER:
                checkNumberOperands(expr.operator,left,right);

                return (double)left>(double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator,left,right);
                return (double)left>=(double)right;
            case LESS:
                checkNumberOperands(expr.operator,left,right);
                return (double)left<(double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator,left,right);
                return (double)left<=(double)right;
            case EQUAL_EQUAL:
                return isEqual(left,right);
            case BANG_EQUAL:
                return !isEqual(left,right);

        }
        return null;

    }

    private boolean isEqual(Object left, Object right) {
        if (left==null && right==null)return true;
        if (left==null)return false;
        return left.equals(right);

    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);

    }

    private Object evaluate(Expr expression) {
        return expression.accept(this);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;

    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right=evaluate(expr.right);

        switch (expr.operator.type){
            case MINUS :
                return -(double)right;
            case BANG:
                return !isTruthy(right);
        }
        return null;

    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name,expr);
    }



    @Override
    public Object visitAssignExpr(Expr.Assign expr)
    {
        Object value=evaluate(expr.value);
        Integer distance=locals.get(expr);
        if (distance!=null){
            environment.assignAt(distance,expr.name,value);
        }else{
            globals.assign(expr.name,value);
        }
        return value;

    }

    @Override
    public Object visitLogicalExpr(Expr.Logical logical) {
        Object left=evaluate(logical.left);
        if (logical.operator.type== OR){
            if (isTruthy(left))return left;

        }else{
            if (!isTruthy(left))return left;

        }
        return evaluate(logical.right);

    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee=evaluate(expr.callee);
        List<Object> arguments=new ArrayList<>();
        for (Expr argument:expr.arguments){
            arguments.add(evaluate(argument));

        }
        if (!(callee instanceof LoxCallable)){
            throw new RuntimeError(expr.paren,"Call only functions and classes");
        }
        LoxCallable function=(LoxCallable) callee;
        if (arguments.size()!=function.arity()){
            throw new RuntimeError(expr.paren,"Expected "+function.arity()+" arguments but got "+arguments.size()+".");

        }
        return function.call(this,arguments);
    }

    @Override
    public Object visitGetExpr(Expr.Get get) {
        Object object=evaluate(get.object);
        if (object instanceof LoxInstance){
            return ((LoxInstance) object).get(get.name);

        }
        throw new RuntimeError(get.name,"Only instance have property access.");
    }

    @Override
    public Object visitSetExpr(Expr.Set set) {
        Object object=evaluate(set.object);
        if (!(object instanceof LoxInstance)){
            throw new RuntimeError(set.name,"Only instances have fields.");

        }
        Object value=evaluate(set.value);
        ((LoxInstance)object).set(set.name,value);
        return null;
    }

    @Override
    public Object visitThisExpr(Expr.This aThis) {
//        return lookUpVariable(aThis.keyword,aThis);
        return "not implemented yet";

    }

    private boolean isTruthy(Object object) {
        if (object==null)
        {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        return true;

    }
    private void  checkNumberOperand(Token operator,Object operand){
        if (operand instanceof Double){
            return ;
        }
        throw new RuntimeError(operator,"Operand must be a number");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double){
            return;
        }
        throw new RuntimeError(operator,"Operands must be numbers.");

    }
    void interpret(List<Stmt> statements){

        try{
            for (Stmt statement:statements){
                    execute(statement);
            }


        }catch(RuntimeError error){
            Klug.runtimeError(error);
        }

    }

    private void execute(Stmt statement) {
        statement.accept(this);

    }

    private String stringify(Object value) {
        if (value==null)return "nil";
        if (value instanceof Double){
            String text=value.toString();
            if (text.endsWith(".0")){
                text=text.substring(0,text.length()-2);

            }
            return text;

        }
        return value.toString();

    }


    public void resolve(Expr variable, int depth) {

        locals.put(variable,depth);

    }
    private Object lookUpVariable(Token name, Expr.Variable expr) {
        Integer distance=locals.get(expr);
        if (distance!=null){
            System.out.println(distance);
            System.out.println(name.lexme);
            System.out.println(environment.getAt(distance,name.lexme));


            return environment.getAt(distance,name.lexme);
        }else{
            return globals.get(name);
        }
    }

}
