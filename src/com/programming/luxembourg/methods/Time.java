package com.programming.luxembourg.methods;

import com.programming.luxembourg.Interpreter;
import com.programming.luxembourg.LoxCallable;

import java.util.List;
import java.util.Date;

public class Time implements LoxCallable {
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Date date=new Date();
        return date;
    }

    @Override
    public int arity() {
        return 0;
    }
}
