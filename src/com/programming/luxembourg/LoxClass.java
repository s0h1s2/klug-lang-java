package com.programming.luxembourg;

import com.programming.luxembourg.Interfaces.KlugInstance;
import com.programming.luxembourg.Interfaces.LoxCallable;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable, KlugInstance {
    final String name;
    private final Map<String, LoxFunction> methods;
    private final Map<String,Object> fields;

    LoxClass(String name, Map<String, LoxFunction> methods,Map<String,Object> fields){

        this.name = name;
        this.methods = methods;
        this.fields=fields;

    }

    @Override
    public String toString() {
        return this.name;

    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance=new LoxInstance(this);
        for (Map.Entry<String,Object> field:fields.entrySet()){
            instance.set(field.getKey(),field.getValue());
        }
        LoxFunction initializer=this.findMethod("init");
        if (initializer!=null){
            initializer.bind(instance).call(interpreter,arguments);
        }
        return instance;

    }

    @Override
    public int arity() {
        LoxFunction initializer=this.findMethod("init");
        if (initializer==null){
            return 0;
        }
        return initializer.arity();

    }
    public boolean isInstance(){
        return false;
    }
    public LoxFunction findMethod(String name) {
        if (methods.containsKey(name)){
            return methods.get(name);
        }
        return null;
    }
}
