package edu.mit.compilers.regalloc;

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import edu.mit.compilers.codegen.dataflow.ScopedStatement;
import edu.mit.compilers.graph.Graph;
import edu.mit.compilers.graph.Graphs;
import edu.mit.compilers.graph.Node;

public class Web implements Iterable<DefUseChain> {
    private final Set<DefUseChain> defUseChains;

    public Web(Iterable<DefUseChain> defUseChains) {
        this.defUseChains = ImmutableSet.copyOf(defUseChains);
    }

    public Set<DefUseChain> getDefUseChains() {
        return defUseChains;
    }

    @Override
    public Iterator<DefUseChain> iterator() {
        return defUseChains.iterator();
    }

    /**
     * Gets the set of webs for some def-use chains.
     *
     * <p>Assumes that all the def-use chains are for the same variable.
     * @param defUseChains
     * @return
     */
    public Set<Web> webs(Set<DefUseChain> defUseChains) {
        // We build up a "same-web" graph, where two def-use chains are connected
        // if they belong to the same web.
        Graph.Builder<DefUseChain> sameWebGraphBuilder = Graph.builder();

        // We find "same-web" edges by mapping DFG nodes to the def-use nodes they
        // are a part of.
        Multimap<Node<ScopedStatement>, Node<DefUseChain>> duNodes = HashMultimap.create();

        for (DefUseChain duChain : defUseChains) {
            Node<DefUseChain> duNode = Node.of(duChain);

            for (Node<ScopedStatement> end :
                    ImmutableList.of(duChain.getDef(), duChain.getUse())) {
                if (duNodes.containsKey(end)) {
                    for (Node<DefUseChain> sameEndNode : duNodes.get(end)) {
                        // One end of this node is equal to one end of sameEndNode.
                        sameWebGraphBuilder.link(duNode, sameEndNode);
                    }
                }
                duNodes.put(end, duNode);
            }
        }

        // Each connected component of the "same-web" graph is a web.
        Graph<DefUseChain> duChainGraph = sameWebGraphBuilder.build();
        ImmutableSet.Builder<Web> websBuilder = ImmutableSet.builder();
        for (Set<Node<DefUseChain>> web : Graphs.connectedComponents(duChainGraph)) {
            websBuilder.add(new Web(Node.values(web)));
        }
        return websBuilder.build();
    }
}
