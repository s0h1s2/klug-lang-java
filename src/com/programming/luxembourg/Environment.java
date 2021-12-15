package com.programming.luxembourg;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String,Object> values=new HashMap<>();
    final Environment enclosing;
    Environment(){
        this.enclosing=null;

    }
    Environment(Environment enclosing){
        this.enclosing=enclosing;
    }
    void define(String name,Object value){
        values.put(name,value);

    }
    Object get(Token name){
        if (values.containsKey(name.lexme)){
            return values.get(name.lexme);

        }
        if (enclosing!=null)return enclosing.get(name);

        throw new RuntimeError(name,"Undefined variable '"+name.lexme+"'.");

    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexme)){
            values.put(name.lexme,value);
            return ;

        }
        if (enclosing!=null){
            enclosing.assign(name,value);
            return ;
            
        }
        throw new RuntimeError(name,"Undefined variable '"+name.lexme+"'.");

    }
    public void incrementBy(Token name, Object value) {
        if (values.containsKey(name.lexme)){
            if (values.get(name.lexme) instanceof Double){
                values.put(name.lexme,(double)value+(double) values.get(name.lexme));;
            }
        }
        if (enclosing!=null){
            enclosing.assign(name,value);
            return ;

        }
        throw new RuntimeError(name,"Undefined variable '"+name.lexme+"'.");

    }

    public Object getAt(Integer distance, String name) {
        return ancestor(distance).values.get(name);

    }

    private Environment ancestor(Integer distance) {
        Environment environment=this;
        for (int i=0;i<distance;i++){
            environment=environment.enclosing;

        }
        return environment;

    }

    public void assignAt(Integer distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexme,value);

    }
}
