package com.programming.luxembourg.methods;

import com.programming.luxembourg.Interpreter;
import com.programming.luxembourg.Interfaces.LoxCallable;

import java.util.List;

public class Clock implements LoxCallable
{
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double)System.currentTimeMillis()/1000;
    }

    @Override
    public int arity() {
        return 0;
    }
}
