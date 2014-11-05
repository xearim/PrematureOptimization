This directory contains code that demonstrates the Common Subexpression
Elimination optimizer.

To generate the unoptimized Data Flow Graph for a file, run:
```
sh$ ./run.sh -t DFG in.dcf | dot -Tpng >out.png
```

To generate the optimized Data Flow Graph for a file, run:
```
sh$ ./run.sh -t DFG --opt=cse in.dcf | dot -Tpng >out.png
```
