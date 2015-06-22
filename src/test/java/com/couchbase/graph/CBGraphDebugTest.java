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

package com.couchbase.graph;

import com.couchbase.client.java.Bucket;
import com.couchbase.graph.conn.ConnectionFactory;
import com.couchbase.graph.test.checker.DebugEnabledChecker;
import com.couchbase.graph.test.annotation.RunIf;
import com.couchbase.graph.test.runner.JUnitExtRunner;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author David Maier <david.maier at couchbase.com>
 */
@RunWith(JUnitExtRunner.class)
public class CBGraphDebugTest {
    
    /**
     * The graph instance to test
     */
    private static Graph graph;
    
    
    @BeforeClass
    @RunIf(value = DebugEnabledChecker.class)
    public static void setUpClass() {
    
        Bucket bucket = ConnectionFactory.getBucketCon();
        
        //Init the graph
        graph = new CBGraph();
    
    }
   
   /**
    * Investigate why it does not work to get the Edge via the browser tool
    */
    @Test
    @RunIf(value = DebugEnabledChecker.class)
    public void testGetEdgesFromVertex() {
    
        //Get the edge back
        Vertex v_tae_bart2 = graph.getVertex("tae_bart");

        if (v_tae_bart2 != null) {

            Iterable<Edge> edges = v_tae_bart2.getEdges(Direction.OUT, "son of");

            for (Edge edge : edges) {

                System.out.println("eid = " + edge.getId());

            }

        }
    
    }
}
