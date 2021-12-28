package com.programming.luxembourg.Interfaces;

import com.programming.luxembourg.Interpreter;

import java.util.List;

public interface LoxCallable {
     Object call(Interpreter interpreter, List<Object> arguments);

     int arity();
}
