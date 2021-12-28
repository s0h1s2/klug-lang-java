package com.programming.luxembourg;

import com.programming.luxembourg.Interfaces.LoxCallable;


import java.util.List;

public class LoxFunction implements LoxCallable {

    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    LoxFunction(Stmt.Function declaration,Environment closure,boolean isInitializer){
        this.declaration=declaration;
        this.closure=closure;
        this.isInitializer=isInitializer;
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
            if (isInitializer) return closure.getAt(0,"this");

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

    public LoxFunction bind(LoxInstance loxInstance) {
        Environment environment=new Environment();
        environment.define("this",loxInstance);
        return new LoxFunction(declaration,environment,isInitializer);


    }
}
