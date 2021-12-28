package com.programming.luxembourg;

import com.programming.luxembourg.Interpreter;
import com.programming.luxembourg.LoxCallable;


import java.util.List;

public class LoxFunction implements LoxCallable {

    private final Stmt.Function declaration;
    private final Environment closure;

    LoxFunction(Stmt.Function declaration,Environment closure){
        this.declaration=declaration;
        this.closure=closure;

    }
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment=new Environment(closure);
        for (int i=0;i<declaration.params.size();i++){
            environment.define(declaration.params.get(i).lexme,arguments.get(i));
        }
        try{
            interpreter.executeBlock(declaration.body,environment);
        }catch (Return returnValue){
            return returnValue.value;
        }

        return null;
    }

    @Override
    public int arity() {
        return this.declaration.params.size();

    }

    @Override
    public String toString() {
        return "<fn "+declaration.name.lexme+">";

    }

    public Object bind(LoxInstance loxInstance) {
        Environment environment=new Environment();
        environment.define("this",loxInstance);
        return new LoxFunction(declaration,environment);


    }
}
