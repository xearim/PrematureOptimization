package edu.mit.compilers.graph;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.mit.compilers.graph.Graphs.UncolorableGraphException;

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

        assertGoodColoring(Graphs.colored(graph, colors), graph, colors);
    }

    @Test(expected=UncolorableGraphException.class)
    public void testColoringFails() throws Exception {
        // It's impossible to color a total graph with fewer colors than there are nodes.
        Graphs.colored(
                Graphs.totalGraph(ImmutableList.of(N0, N1, N2, N3, N4)),
                ImmutableSet.of(0, 1, 2, 3));
    }

    private static <T, C> void
            assertGoodColoring(Map<Node<T>, C> coloring, Graph<T> graph, Set<C> colors) {
        for (Node<T> node : graph.getNodes()) {
            C color = coloring.get(node);
            assertThat("Color is one of the provided choices.", colors, hasItem(color));
            for (Node<T> neighbor : graph.getSuccessors(node)) {
                C neighborColor = coloring.get(neighbor);
                assertThat("Neighbors cannot have same color.",
                        neighborColor, not(equalTo(color)));
            }
        }
    }
}

