package edu.mit.compilers;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import antlr.ASTFactory;
import antlr.CharStreamException;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStreamException;
import antlr.collections.AST;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.ast.Method;
import edu.mit.compilers.ast.NodeMaker;
import edu.mit.compilers.ast.Program;
import edu.mit.compilers.codegen.AssemblyWriter;
import edu.mit.compilers.codegen.DataFlowIntRep;
import edu.mit.compilers.codegen.Targets;
import edu.mit.compilers.codegen.asm.Register;
import edu.mit.compilers.codegen.asm.instructions.Instruction;
import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.grammar.DecafParser;
import edu.mit.compilers.grammar.DecafParserTokenTypes;
import edu.mit.compilers.grammar.DecafScanner;
import edu.mit.compilers.grammar.DecafScannerTokenTypes;
import edu.mit.compilers.graph.DiGraph;
import edu.mit.compilers.graph.FlowGraph;
import edu.mit.compilers.regalloc.LiveRange;
import edu.mit.compilers.regalloc.RegisterAllocator;
import edu.mit.compilers.semantics.ErrorPrinter;
import edu.mit.compilers.semantics.SemanticChecker;
import edu.mit.compilers.semantics.errors.SemanticError;
import edu.mit.compilers.tools.AstPrinter;
import edu.mit.compilers.tools.CLI;
import edu.mit.compilers.tools.CLI.Action;
import edu.mit.compilers.tools.DiGraphPrinter;
import edu.mit.compilers.tools.FlowGraphPrinter;

class Main {

    private static final String MAIN_METHOD_NAME = "main";

    // TODO(jasonpr): Modify interface of CLI so we don't have to do this weird dance.
    private static final String[] OPTIMIZATION_NAMES = {"cse", "conprop", "dce", "regalloc"};

    public static void main(String[] args) {
        try {
            // Setup in and out files.
            CLI.parse(args, OPTIMIZATION_NAMES);
            InputStream inputStream = args.length == 0 ?
                    System.in : new java.io.FileInputStream(CLI.infile);
            PrintStream outputStream = CLI.outfile == null ? System.out : new java.io.PrintStream(new java.io.FileOutputStream(CLI.outfile));

            // Parse or scan.
            if (CLI.target == Action.SCAN) {
                scan(inputStream, outputStream);
            } else if (CLI.target == Action.AST) {
                printAst(inputStream, outputStream);
            } else if (CLI.target == Action.INTER) {
                semanticCheck(inputStream, outputStream);
            } else if (CLI.target == Action.CFG) {
                controlFlowGraph(inputStream, outputStream, getOptimizations());
            } else if (CLI.target == Action.PARSE) {
                parse(inputStream, outputStream);
            } else if (CLI.target == Action.ASSEMBLY ||
                    CLI.target == Action.DEFAULT) {
                genCode(inputStream, outputStream, getOptimizations());
            } else if (CLI.target == Action.DFG) {
                dataFlowGraph(inputStream, outputStream, getOptimizations());
            } else if (CLI.target == Action.PRINT_OPTS) {
                printOpts(outputStream, getOptimizations());
            } else if (CLI.target == Action.REG_DEBUG) {
                regDebug(inputStream, outputStream, getOptimizations());
            } else if (CLI.target == Action.DOMINATE) {
                printDominatorTree(inputStream,outputStream,getOptimizations());
            }
        } catch(Exception e) {
            // An unrecoverable error occurred.
            System.err.println(CLI.infile+" "+e);
        }
    }

    private static final Set<String> getOptimizations() {
        ImmutableSet.Builder<String> optimizationNames = ImmutableSet.builder();
        for (int i = 0; i < OPTIMIZATION_NAMES.length; i++) {
            if (CLI.opts[i]) {
                optimizationNames.add(OPTIMIZATION_NAMES[i]);
            }
        }
        return optimizationNames.build();
    }

    // TODO(jasonpr): Javadoc.
    private static void scan(InputStream inputStream, PrintStream outputStream) throws CharStreamException {
        DecafScanner scanner =
                new DecafScanner(new DataInputStream(inputStream));
        scanner.setTrace(CLI.debug);
        Token token;
        boolean done = false;
        while (!done) {
            try {
                for (token = scanner.nextToken();
                        token.getType() != DecafParserTokenTypes.EOF;
                        token = scanner.nextToken()) {
                    String type = "";
                    String text = token.getText();
                    switch (token.getType()) {
                        // TODO: add strings for the other types here...
                        case DecafScannerTokenTypes.ID:
                            type = " IDENTIFIER";
                            break;
                        case DecafScannerTokenTypes.CHAR:
                            type = " CHARLITERAL";
                            break;
                        case DecafScannerTokenTypes.STRING:
                            type = " STRINGLITERAL";
                            break;
                        case DecafScannerTokenTypes.TRUE:  /* fall-through */
                        case DecafScannerTokenTypes.FALSE:
                            type = " BOOLEANLITERAL";
                            break;
                        case DecafScannerTokenTypes.INT_LITERAL:
                            type = " INTLITERAL";
                            break;
                    }
                    outputStream.println(token.getLine() + type + " " + text);
                }
                done = true;
            } catch(Exception e) {
                // print the error:
                System.err.println(CLI.infile + " " + e);
                scanner.consume();
            }
        }
    }

    // TODO(jasonpr): Javadoc.
    private static void parse(InputStream inputStream, PrintStream outputStream) throws RecognitionException, TokenStreamException {
        DecafParser parser = programmedParser(inputStream, outputStream);
        if(parser.getError()) {
            System.exit(1);
        }
    }

    /** Print the AST that ANTLR generated for the program, in DOT format. */
    private static void printAst(InputStream inputStream, PrintStream outputStream)
            throws RecognitionException, TokenStreamException {
        DecafParser parser = programmedParser(inputStream, outputStream);
        if (parser.getError()) {
            System.exit(1);
        }
        AST ast = parser.getAST();
        AstPrinter printer = new AstPrinter(outputStream);
        printer.print(ast);
    }

    private static void semanticCheck(InputStream inputStream, PrintStream outputStream)
            throws RecognitionException, TokenStreamException {
        Optional<Program> validProgram = semanticallyValidProgram(inputStream, outputStream);
        if (!validProgram.isPresent()) {
            System.exit(1);
        }
    }

    /** Print the optimized Control Flow Graph to outputStream in DOT format. */
    private static void controlFlowGraph(InputStream inputStream, PrintStream outputStream,
            Set<String> optimizationNames)
                    throws RecognitionException, TokenStreamException {
        Program validProgram = semanticallyValidProgram(inputStream, outputStream).get();
        ImmutableMap.Builder<String, Method> methodsBuilder = ImmutableMap.builder();
        for (Method method : validProgram.getMethods()) {
            methodsBuilder.put(method.getName(), method);
        }
        ImmutableMap<String, Method> methods = methodsBuilder.build();
        // TODO(jasonpr): Do it for everything, not just main.
        Method main = methods.get(MAIN_METHOD_NAME);
        FlowGraph<Instruction> controlFlowGraph =
                Targets.controlFlowGraph(main, optimizationNames);
        FlowGraphPrinter.print(outputStream, controlFlowGraph);
    }

    private static void dataFlowGraph(InputStream inputStream, PrintStream outputStream,
            Set<String> optimizationNames) throws RecognitionException, TokenStreamException {
        Program validProgram = semanticallyValidProgram(inputStream, outputStream).get();
        ImmutableMap.Builder<String, Method> methodsBuilder = ImmutableMap.builder();
        for (Method method : validProgram.getMethods()) {
            methodsBuilder.put(method.getName(), method);
        }
        ImmutableMap<String, Method> methods = methodsBuilder.build();
        // TODO(jasonpr): Do it for everything, not just main.
        Method main = methods.get(MAIN_METHOD_NAME);
        DataFlowIntRep ir = Targets.optimizedDataFlowIntRep(main, optimizationNames);
        FlowGraphPrinter.print(outputStream, ir.getDataFlowGraph());
    }

    private static void genCode(InputStream inputStream, PrintStream outputStream,
            Set<String> optimizationNames) throws RecognitionException, TokenStreamException {
        Optional<Program> programAST = semanticallyValidProgram(inputStream, outputStream);
        if (!programAST.isPresent()) {
            System.exit(1);
        }
        AssemblyWriter.writeAttAssembly(programAST.get(), outputStream, optimizationNames);
    }

    private static void regDebug(InputStream inputStream, PrintStream outputStream,
            Set<String> optimizationNames) throws RecognitionException, TokenStreamException {
        Program validProgram = semanticallyValidProgram(inputStream, outputStream).get();

        // Print out all allocations, over all methods.
        for (Method method : validProgram.getMethods()) {
            DataFlowIntRep ir =
                    Targets.optimizedDataFlowIntRep(method, optimizationNames);
            for (Entry<LiveRange, Register> entry :
                    RegisterAllocator.allocations(ir.getDataFlowGraph()).entrySet()) {
                outputStream.printf("%s: (%s : %s)\n",
                        method.getName(),
                        entry.getKey().getScopedVariable(),
                        entry.getValue());
            }
        }
    }

    /**
     * Check whether the program is semantically valid.
     *
     * @param program The AST-like IR of the program to check.
     * @param outputStream A PrintStream to which errors should be printed.
     * @return Whether the program is semantically valid.
     */
    private static boolean isSemanticallyValid(Program program, PrintStream outputStream) {
        // TODO(manny): Implement!
        SemanticChecker sc = new SemanticChecker(program);
        List<SemanticError> errors = sc.checkProgram();
        if (errors.size() == 0) {
            return true;
        } else {
            ErrorPrinter ep = new ErrorPrinter(errors, outputStream);
            ep.print();
            return false;
        }
    }


    /**
     * Makes a DecafParser, use it to parse the program from inputStream, and return that it.
     *
     * <p>The returned parser might be in an error state.  This function does not react to any
     * parser errors.
     */
    private static DecafParser programmedParser(InputStream inputStream, PrintStream outputStream)
            throws RecognitionException, TokenStreamException {
        DecafScanner scanner =
                new DecafScanner(new DataInputStream(inputStream));
        DecafParser parser = new DecafParser(scanner);
        // Need a custom ASTFactory with replaced nodes to actually get line info
        ASTFactory factory = new ASTFactory();
        factory.setASTNodeClass(AntlrASTWithLines.class);
        parser.setASTFactory(factory);
        parser.setTrace(CLI.debug);
        parser.program();
        return parser;
    }

    /**
     * Gets the semantically valid Program AST Node for the program on the inputStream.
     *
     * <p>Returns Optional.absent() for an invalid program.
     */
    private static Optional<Program>
    semanticallyValidProgram(InputStream inputStream, PrintStream outputStream)
            throws RecognitionException, TokenStreamException {
        DecafParser parser = programmedParser(inputStream, outputStream);
        if (parser.getError()) {
            // It didn't even parse!
            return Optional.absent();
        }
        AST ast = parser.getAST();
        Program program = NodeMaker.program(ast);

        return isSemanticallyValid(program, outputStream)
                ? Optional.of(program)
                        : Optional.<Program>absent();
    }

    private static void printOpts(PrintStream outputStream, Set<String> optimizations) {
        for (String optimization : optimizations) {
            outputStream.println(optimization);
        }
    }

    private static void printDominatorTree(InputStream inputStream,
            PrintStream outputStream,
            Set<String> optimizationNames) throws RecognitionException, TokenStreamException {

        Program validProgram = semanticallyValidProgram(inputStream, outputStream).get();
        ImmutableMap.Builder<String, Method> methodsBuilder = ImmutableMap.builder();
        for (Method method : validProgram.getMethods()) {
            methodsBuilder.put(method.getName(), method);
        }
        ImmutableMap<String, Method> methods = methodsBuilder.build();

        // TODO(jasonpr): Do it for everything, not just main.
        Method main = methods.get(MAIN_METHOD_NAME);
        DiGraph<ScopedStatement> tree = Targets.dominatorTree(main, optimizationNames);

        DiGraphPrinter.<ScopedStatement>print(outputStream,tree);
    }
}
