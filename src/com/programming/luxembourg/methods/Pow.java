package com.programming.luxembourg.methods;

import com.programming.luxembourg.*;

import java.util.List;

public class Pow implements LoxCallable {

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        if (arguments.get(0) instanceof Double && arguments.get(1) instanceof Double){
            int x=(int)Math.floor((Double) arguments.get(0));
            int y=(int)Math.floor((Double) arguments.get(1));

            return Math.pow(x,y);
        }
//        return throw RuntimeError();
        return null;


    }

    @Override
    public int arity() {
        return 2;

    }
}
