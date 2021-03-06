package com.programming.luxembourg;

import com.programming.luxembourg.Types.ClassType;
import com.programming.luxembourg.Types.FunctionType;

import java.util.*;

public class Resolver implements Expr.Visitor<Void>,Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private Stack<Map<String, Boolean>> scopes=new Stack<>();
    private FunctionType currentFunction=FunctionType.NONE;
    private ClassType currentClass=ClassType.NONE;
    private boolean isLoop=false;


    Resolver(Interpreter interpreter){
        this.interpreter = interpreter;
    }
    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);

        resolve(expr.right);

        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);

        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);

        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable variable) {
        if (!scopes.isEmpty() && scopes.peek().get(variable.name.lexme)==Boolean.FALSE){
            Klug.error(variable.name,"Can't read local variable in its own initializer");
        }

        resolveLocal(variable,variable.name);
        
        return null;

    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr,expr.name);

        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical logical) {
        resolve(logical.left);
        resolve(logical.right);

        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call call) {
        resolve(call.callee);
        for (Expr argument:call.arguments){
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get get) {
        resolve(get.object);

        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set set) {
        resolve(set.value);
        resolve(set.object);

        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This aThis) {
        if (currentClass==ClassType.NONE){
            Klug.error(aThis.keyword,"can't use 'this' outside of class");
            return null;
        }
        resolveLocal(aThis,aThis.keyword);
        return null;
    }

    @Override
    public Void visitArrayList(Expr.ArrayList arrayList) {
        for(Expr item:arrayList.exprs){
            resolve(item);
        }
        return null;
    }

    @Override
    public Void visitSubscriptGet(Expr.SubscriptGet subscriptGet) {
        this.resolve(subscriptGet.object);
        this.resolve(subscriptGet.index);

        return null;

    }


    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);

        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        for (Expr expression:stmt.expressions){
            resolve(expression);
        }

        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer!=null){
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }


    @Override
    public Void visitBlockStmt(Stmt.Block block) {
        beginScope();
        resolve(block.statements);
        endScope();
        return null;

    }

    @Override
    public Void visitIfStmt(Stmt.If anIf) {
        resolve(anIf.condition);
        resolve(anIf.thenBranch);
        if (anIf.elseBranch!=null){
            resolve(anIf.elseBranch);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While aWhile) {
        isLoop=true;

        resolve(aWhile.condition);
        resolve(aWhile.body);
        isLoop=false;

        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break aBreak) {
        if (!isLoop){
            Klug.error(aBreak.keyword,"'break' must be used inside loop.");
            return null;
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function function) {
        declare(function.name);
        define(function.name);
        resolveFunction(function,FunctionType.FUNCTION);

        return null;
    }


    @Override
    public Void visitReturnStmt(Stmt.Return aReturn) {
        if (currentFunction==FunctionType.NONE){
            Klug.error(aReturn.keyword,"Can't return from top-level code.");
        }

        if (aReturn.value!=null){
            if (currentFunction==FunctionType.INITIALIZER){
                Klug.error(aReturn.keyword,"can't return value from initializer.");
                return null;
            }
            resolve(aReturn.value);

        }
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class aClass) {
        ClassType enclosingClass=currentClass;
        currentClass=ClassType.CLASS;

        declare(aClass.name);
        define(aClass.name);
        beginScope();
        scopes.peek().put("this",true);

        for (Stmt.Function method : aClass.methods){
            FunctionType declaration=FunctionType.METHOD;
            if(method.name.lexme.equals("init")){
                declaration=FunctionType.INITIALIZER;

            }
            resolveFunction(method,declaration);
        }
        endScope();
        currentClass=enclosingClass;

        return null;
    }

    @Override
    public Void visitImportStatement(Stmt.Import anImport) {
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue aContinue) {
        if (!isLoop){
            Klug.error(aContinue.keyword,"'continue' must be used inside loop.");
            return null;

        }
        return null;
    }

    private void beginScope() {
        scopes.push(new HashMap<String,Boolean>());
    }
    void resolve(List<Stmt> statements) {
        for (Stmt stmt:statements){
            resolve(stmt);
        }
    }
    private void resolve(Stmt statement) {
        statement.accept(this);

    }
    private void resolve(Expr expression) {
        expression.accept(this);

    }

    private void endScope() {
        scopes.pop();

    }
    private void define(Token name) {
        if (scopes.isEmpty()){
            return;
        }
        scopes.peek().put(name.lexme,true);

    }

    private void declare(Token name) {
        if (scopes.isEmpty()){
            return ;
        }
        Map<String,Boolean> scope=scopes.peek();
        if (scope.containsKey(name.lexme)){
            Klug.error(name,"Already a variable name in this scope");

        }
        scope.put(name.lexme,false);

    }

    private void resolveLocal(Expr variable, Token name) {
        for (int i=scopes.size()-1;i>=0;i--){
            if (scopes.get(i).containsKey(name.lexme)){
                interpreter.resolve(variable,scopes.size()-1-i);
                return ;
            }
        }
    }
    private void resolveFunction(Stmt.Function function,FunctionType funType) {

        FunctionType enclosingFunction=currentFunction;
        currentFunction=funType;
        beginScope();
            for (Token param:function.params){
                declare(param);
                define(param);

            }

            resolve(function.body);
            endScope();
            currentFunction=enclosingFunction;

    }


}