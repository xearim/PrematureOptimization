package edu.mit.compilers.semantics.errors;

import java.util.List;

import com.google.common.base.Optional;

import edu.mit.compilers.ast.*;

public class SignatureMismatchSemanticError implements SemanticError {
    private final static String ERRORNAME = "SignatureMismatchSemanticError";
    private final String programName;
    private final MethodCall methodCall;
    private final Optional<Method> method;

    public SignatureMismatchSemanticError(String progName,
            MethodCall methodCall, Optional<Method> method) {
        this.programName = progName;
        this.methodCall = methodCall;
        this.method = method;
    }

    @Override
    public String generateErrorMessage() {
        String.format("%s: %s %s; method call \"%s\" %s", ERRORNAME,
                this.programName, getLocationString(),
                this.methodCall.getName(), getContextString());
        // TODO Auto-generated method stub
        return null;
    }

    private String getLocationString() {
        LocationDescriptor mcLD = methodCall.getLocationDescriptor();
        if (method.isPresent()) {
            LocationDescriptor mLD = method.get().getLocationDescriptor();
            return String.format("methodCall: %d:%d, method %d:%d",
                    mcLD.lineNo(), mcLD.colNo(), mLD.lineNo(), mLD.colNo());
        } else {
            return String.format("%d:%d", mcLD.lineNo(),mcLD.colNo());
        }
    }

    private String getContextString() {
        if (method.isPresent()) {
            return String.format(" doesn't match signature: %s", getSignature(method.get()));
        } else {
            return " doesn't exist.";
        }
    }

    private String getSignature(Method method) {
        List<BaseType> signature = method.getParameters().getSignature();
        if (signature.size() == 0) {
            return "no parameters";
        } else {
            String sigStr = "";
            for (BaseType bt: signature) {
                if (bt == BaseType.BOOLEAN) {
                    sigStr += " boolean";
                } else if (bt == BaseType.INTEGER) {
                    sigStr += " int";
                } else {
                    // TODO(Manny): throw exception
                }
            }
            return sigStr;
        }
    }
}
