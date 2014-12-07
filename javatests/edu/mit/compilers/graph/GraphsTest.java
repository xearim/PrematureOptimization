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

import com.google.common.collect.ImmutableSet;

@RunWith(JUnit4.class)
public class GraphsTest {
    @Test
    public void testColoringSucceeds() throws Exception {
        Node<String> s0 = Node.of("s0");
        Node<String> s1 = Node.of("s1");
        Node<String> s2 = Node.of("s2");
        Node<String> s3 = Node.of("s3");
        Node<String> s4 = Node.of("s4");

        Graph<String> graph = Graph.<String>builder()
                .link(s0, s1)
                .link(s0, s2)
                .link(s0, s3)
                .link(s1, s2)
                .link(s1, s3)
                .link(s2, s4)
                .link(s3, s4)
                .build();
        Set<Integer> colors = ImmutableSet.of(1, 2, 3);

        assertGoodColoring(Graphs.colored(graph, colors), graph, colors);
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

