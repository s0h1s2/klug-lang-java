package com.programming.luxembourg;

class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr){
        return expr.accept(this);
    }
    private String parenthesize(String name, Expr...exprs) {
        StringBuilder builder=new StringBuilder();
        builder.append("(").append(name);
        for (Expr expr:exprs){
            builder.append(" ");
            builder.append(expr.accept(this));

        }
        builder.append(")");
        return builder.toString();

    }


    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexme,expr.left,expr.right);
        
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group",expr.expression);
    }


    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value==null) return "nil";
        return expr.value.toString();

    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexme,expr.right);

    }

    @Override
    public String visitVariableExpr(Expr.Variable variable) {
        return null;
    }

    @Override
    public String visitAssignExpr(Expr.Assign assign) {
        return null;
    }

    @Override
    public String visitLogicalExpr(Expr.Logical logical) {
        return null;
    }

    @Override
    public String visitCallExpr(Expr.Call call) {
        return null;
    }

    @Override
    public String visitGetExpr(Expr.Get get) {
        return null;
    }

    @Override
    public String visitSetExpr(Expr.Set set) {
        return null;
    }

    @Override
    public String visitThisExpr(Expr.This aThis) {
        return null;
    }

}
