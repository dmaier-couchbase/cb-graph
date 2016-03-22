/*
 * Copyright 2016 david.
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
import com.couchbase.graph.cfg.ConfigManager;
import com.couchbase.graph.cfg.GraphConfig;
import com.couchbase.graph.conn.ConnectionFactory;
import com.couchbase.graph.deps.annotation.RunIf;
import com.couchbase.graph.deps.checker.GraphEnabledChecker;
import com.couchbase.graph.deps.runner.JUnitExtRunner;
import com.couchbase.graph.views.ViewManager;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author David Maier <david.maier at couchbase.com>
 */
@RunWith(JUnitExtRunner.class)
public class CBVertexCompressionTest {
    
     /**
     * The graph instance to test
     */
    private static Graph graph;
    
    private static final GraphConfig CFG = ConfigManager.getGraphConfig();
    
    
    /**
     * Flush the test bucket and delete the views before running the tests
     * 
     * @throws Exception 
     */
    @BeforeClass
    public static void setUpClass() throws Exception {

        Bucket client = ConnectionFactory.getBucketCon();
        assertTrue(client.bucketManager().flush());
        assertTrue(ViewManager.deleteDesignDoc());
      
        //Init the graph
        graph = new CBGraph();
    }
    
    
    @Test
    @RunIf(value = GraphEnabledChecker.class)
    public void testIfUncompressedWorks() {
    
        System.out.println("-- testIfUncompressedWorks");
        
        if (!CFG.isCompressionEnabled()) {
        
            //Add a vertex and an edge
            Vertex v = graph.addVertex("v_tuc");
            Vertex v2 = graph.addVertex("v_2_tuc");
            v.addEdge("friend of", v2);
           
            v = graph.getVertex("v_tuc");
            v2 = graph.getVertex("v_2_tuc");
           
            System.out.println("v = " + v.toString());
            System.out.println("v2 = " + v2.toString());
            
            assertEquals("{edges={in={}, out={friend of=[e_v_tuc->|friend of|->v_2_tuc]}}, props={}, type=vertex}", v.toString());
            assertEquals("{edges={in={friend of=[e_v_tuc->|friend of|->v_2_tuc]}, out={}}, props={}, type=vertex}", v2.toString());
        
        } else {
            
            System.out.println("Skipping test because compression is enabled.");
        } 
        
    }
    
    @Test
    @RunIf(value = GraphEnabledChecker.class)
    public void testVertexCompression() {
    
        System.out.println("-- testVertexCompression");
        
         
        if (CFG.isCompressionEnabled()) {
            
            //Add a vertex and an edge
            Vertex v = graph.addVertex("v_tc");
            Vertex v2 = graph.addVertex("v_2_tc");
            v.addEdge("friend of", v2);
           
            v = graph.getVertex("v_tc");
            v2 = graph.getVertex("v_2_tc");
           
            System.out.println("v = " + v.toString());
            System.out.println("v2 = " + v2.toString());
            
        } else {
   
            System.out.println("Skipping test because compression is not enabled.");
        }
   
    }
    
    @Test
    @RunIf(value = GraphEnabledChecker.class)
    public void testCompressionImpactForSuperNode() {
        
        System.out.println("-- testCompressionImpactForSuperNode");
        
        String data = "123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790";
       
        
        Vertex root = graph.addVertex("root");
        
        for (int i = 0; i < 5000; i++) {
            
            Vertex v = graph.addVertex(UUID.randomUUID().toString());
            System.out.println("id = " +v.getId());  
            v.setProperty("data", data);
            graph.addEdge(null, root, v, "neighbor of" );
        }
        
        root = graph.getVertex("root");
     
        System.out.println("size = " + root.toString().length());
    }
}
