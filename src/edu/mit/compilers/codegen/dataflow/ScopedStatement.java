package edu.mit.compilers.codegen.dataflow;

import edu.mit.compilers.ast.Scope;
import edu.mit.compilers.ast.Statement;

public class ScopedStatement {

    private final Statement statement;
    private final Scope scope;
    
    public ScopedStatement(Statement statement, Scope scope) {
        this.statement = statement;
        this.scope = scope;
    }
    
    public Statement getStatement() {
        return statement;
    }
    
    public Scope getScope() {
        return scope;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        result = prime * result
                + ((statement == null) ? 0 : statement.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ScopedStatement)) {
            return false;
        }
        ScopedStatement other = (ScopedStatement) obj;
        if (scope == null) {
            if (other.scope != null) {
                return false;
            }
        } else if (!scope.equals(other.scope)) {
            return false;
        }
        if (statement == null) {
            if (other.statement != null) {
                return false;
            }
        } else if (!statement.equals(other.statement)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ScopedStatement [statement=" + statement + ", scope=" + scope
                + "]";
    }
}
