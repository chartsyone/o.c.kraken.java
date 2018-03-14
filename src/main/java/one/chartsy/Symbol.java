/* Copyright 2018 by Mariusz Bernacki. PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND
 * and under the terms and conditions of the Apache License, Version 2.0. */
package one.chartsy;

import java.io.Serializable;

public class Symbol {
    /** The symbol name. */
    private final String name;
    /** The unique symbol reference ID. */
    private Serializable refId;


    public Symbol(String name) {
        this.name = name;
    }

    public Symbol(SymbolInformation info) {
        this.name = info.getName();
        this.refId = info.getRefId();
    }

    public String getName() {
        return name;
    }

    /**
     * Gives the symbol reference ID. Currently not used - reserved for future use.
     * 
     * @return
     */
    public String getRefIdAsString() {
        Serializable refId = this.refId;
        return (refId instanceof String)? (String) refId : null;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
