/*
 * Copyright 2015 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.graph.tinkerpop;


import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import java.util.Collection;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class TestSuite extends BaseTest {

    protected GraphTest graphTest;

    public TestSuite() {
    }

    public TestSuite(final GraphTest graphTest) {
        this.graphTest = graphTest;
    }

    protected void vertexCount(final Graph graph, int expectedCount) {
        if (graph.getFeatures().supportsVertexIteration) assertEquals(count(graph.getVertices()), expectedCount);
    }

    protected void containsVertices(final Graph graph, final Collection<Vertex> vertices) {
        for (Vertex v : vertices) {
            Vertex vp = graph.getVertex(v.getId());
            if (vp == null || !vp.getId().equals(v.getId()))
                fail("Graph does not contain vertex: '" + v + "'");
        }
    }

    protected void edgeCount(final Graph graph, int expectedCount) {
        if (graph.getFeatures().supportsEdgeIteration) assertEquals(count(graph.getEdges()), expectedCount);
    }


}
