package edu.mit.compilers.graph;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@RunWith(JUnit4.class)
public class GraphsTest {

    // Some re-usable nodes for creating test graphs.
    private static final Node<String> N0 = Node.of("Node 0");
    private static final Node<String> N1 = Node.of("Node 1");
    private static final Node<String> N2 = Node.of("Node 2");
    private static final Node<String> N3 = Node.of("Node 3");
    private static final Node<String> N4 = Node.of("Node 4");

    @Test
    public void testColoringSucceeds() throws Exception {
        Graph<String> graph = Graph.<String>builder()
                .link(N0, N1)
                .link(N0, N2)
                .link(N0, N3)
                .link(N1, N2)
                .link(N1, N3)
                .link(N2, N4)
                .link(N3, N4)
                .build();
        Set<Integer> colors = ImmutableSet.of(1, 2, 3);

        assertGoodTotalColoring(Graphs.colored(graph, colors), graph, colors);
    }

    @Test
    public void testPartialColoring() throws Exception {
        // It's impossible to color a total graph with fewer colors than there are nodes.
        Graph<String> graph = Graphs.totalGraph(ImmutableList.of(N0, N1, N2, N3, N4));
        Set<Integer> colors = ImmutableSet.of(0, 1, 2, 3);
        assertGoodPartialColoring(Graphs.colored(graph, colors), graph, colors);
    }

    private static <T, C> void
            assertGoodTotalColoring(Map<Node<T>, C> coloring, Graph<T> graph, Set<C> colors) {
        assertGoodColoring(coloring, graph, colors, true);
    }

    private static <T, C> void
            assertGoodPartialColoring(Map<Node<T>, C> coloring, Graph<T> graph, Set<C> colors) {
        assertGoodColoring(coloring, graph, colors, false);
    }

    /**
     * Assert that the coloring is valid.
     * @param coloring The coloring to verify.
     * @param graph The graph that was colored.
     * @param colors The available colors.
     * @param total Whether every node must be colored.  (If false, then uncolored nodes are
     *     considered to be compatible with all colors.  If true, then uncolored nodes cause
     *     a failed assertion.)
     */
    private static <T, C> void
            assertGoodColoring(Map<Node<T>, C> coloring, Graph<T> graph, Set<C> colors, boolean total) {
        for (Node<T> node : graph.getNodes()) {
            if (!coloring.containsKey(node)) {
                if (total) {
                    Assert.fail();
                } else {
                    continue;
                }
            }
            C color = coloring.get(node);
            assertThat("Color is one of the provided choices.", colors, hasItem(color));
            for (Node<T> neighbor : graph.getSuccessors(node)) {
                if (!coloring.containsKey(neighbor)) {
                    // Don't do the comparison at all.  We'll detect the missing color
                    // in the outer loop.
                    continue;
                }
                C neighborColor = coloring.get(neighbor);
                assertThat("Neighbors cannot have same color.",
                        neighborColor, not(equalTo(color)));
            }
        }
    }
}

