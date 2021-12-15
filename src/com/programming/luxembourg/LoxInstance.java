package com.programming.luxembourg;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private LoxClass loxClass;
    private final Map<String,Object> fields=new HashMap<>();

    LoxInstance(LoxClass loxClass){
        this.loxClass = loxClass;
    }
    Object get(Token name){
        if (fields.containsKey(name.lexme)){
            return fields.get(name.lexme);

        }
        LoxFunction method=loxClass.findMethod(name.lexme);
        if (method!=null)return method.bind(this);


        throw new RuntimeError(name,"undefined property '"+name.lexme+"'.");

    }
    @Override
    public String toString() {
        return loxClass+" instance";

    }

    public void set(Token name, Object value) {
        fields.put(name.lexme,value);

    }
}