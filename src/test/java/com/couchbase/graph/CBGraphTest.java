package com.couchbase.graph;

/*
 * Copyright 2014 Couchbase, Inc.
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
import com.couchbase.client.java.Bucket;
import com.couchbase.graph.conn.ConnectionFactory;
import com.couchbase.graph.test.annotation.RunIf;
import com.couchbase.graph.test.checker.GraphEnabledChecker;
import com.couchbase.graph.test.runner.JUnitExtRunner;
import com.couchbase.graph.views.ViewManager;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * To test the stuff
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
@RunWith(JUnitExtRunner.class)
public class CBGraphTest {
    
    /**
     * The graph instance to test
     */
    private static Graph graph;
    
    
    /**
     * To initialize the test
     * @throws java.lang.Exception
     */
    @BeforeClass
    @RunIf(value = GraphEnabledChecker.class)
    public static void setUpClass() throws Exception {
        
        //Flush the bucket and wait until the operation is successful
        Bucket client = ConnectionFactory.getBucketCon();
        assertTrue(client.bucketManager().flush());
        assertTrue(ViewManager.deleteDesignDoc());
        
        //Init the graph
        graph = new CBGraph();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * To test if the features are available
     * @throws Exception 
     */
    @Test
    @RunIf(value = GraphEnabledChecker.class)
    public void testGetFeatures() throws Exception
    {
        boolean isPersistent = graph.getFeatures().isPersistent;
        
        assertTrue(isPersistent);
    }
    
    /**
     * To test to add a vertex without Id
     */
    @Test
    @RunIf(value = GraphEnabledChecker.class)
    public void testAddVertexWoId()
    {
        System.out.println("-- testAddVertexWoId");
        
        //Check if the id generation for vertices works in general
        System.out.println("- Check Id Generation");
        
        Vertex v0 = graph.addVertex(null);
        
        Object id = v0.getId();
        String key = ((CBVertex) v0).getCbKey();
        
        System.out.println("id = " + id);
        System.out.println("key = " + key);
        assertEquals(0L, id);
        
        Vertex v1 = graph.addVertex(null);
        id = v1.getId();
        key = ((CBVertex) v1).getCbKey();
        
        System.out.println("id = " + id);
        System.out.println("key = " + key);
        assertEquals(1L, id);
        
        //Check if an empty vertex was generated in the bucket
        System.out.println("- Check if an empty vertex object was created");
        String str = v0.toString();
        assertEquals("{edges={in={}, out={}}, props={}, type=vertex}", str);    
    }
    
    /**
     * To add a vertex with properties
     */
    @Test
    @RunIf(value = GraphEnabledChecker.class)
    public void testAddVertexWithProps()
    {
        System.out.println("-- testAddVertexWithProps");
        
        Vertex tavwp_bart = graph.addVertex("tavwp_bart");
        
        tavwp_bart.setProperty("first_name", "Bart");
        tavwp_bart.setProperty("last_name", "Simpson");
        tavwp_bart.setProperty("city", "Springfield");
        tavwp_bart.setProperty("age", 8);
        tavwp_bart.setProperty("is_student", true);
        
        Vertex tavwp_bart_2 = graph.getVertex("tavwp_bart");
        
        System.out.println(tavwp_bart_2.getId() + " = " + tavwp_bart_2.toString());
        assertEquals("{edges={in={}, out={}}, props={age=8, city=Springfield, first_name=Bart, is_student=true, last_name=Simpson}, type=vertex}", tavwp_bart.toString());
        assertEquals("{edges={in={}, out={}}, props={age=8, city=Springfield, first_name=Bart, is_student=true, last_name=Simpson}, type=vertex}", tavwp_bart_2.toString());
    }
    
    
    /**
     * To test to add a vertex with a given id
     */
    @Test
    @RunIf(value = GraphEnabledChecker.class)
    public void testAddVertexWithId()
    {
        System.out.println("-- testAddVertexWithId");
        
        Vertex v_xyz = graph.addVertex("xyz");
        
        Object id = v_xyz.getId();
        String key = ((CBVertex) v_xyz).getCbKey();
        
        System.out.println("id = " + id);
        System.out.println("key = " + key);
        
        assertEquals("xyz", id );
        assertEquals("v_xyz", key);
        
        String str = v_xyz.toString();
        assertEquals("{edges={in={}, out={}}, props={}, type=vertex}", str);    
    }
    
    
    @Test
    @RunIf(value = GraphEnabledChecker.class)
    public void testGetVertex()
    {
        System.out.println("-- testGetVertex");
        
        Vertex v_1 = graph.addVertex("tgv_1");
        
        System.out.println("v_1 = " + v_1.toString());
        
        Vertex v_1_2 = graph.getVertex("tgv_1");
        
        System.out.println("v_1_2 = " + v_1_2.toString());
        
        assertEquals(v_1.toString(), v_1_2.toString());
    }
    
    /**
     * To get all vertices
     */
    @Test
    @RunIf(value = GraphEnabledChecker.class)
    public void testGetVertices() throws Exception
    {
        System.out.println("-- testGetVertices");
        
        //First add ten vertices
        final String PREFIX = "tgvs_";
        final int NUM_OF_VERTICES = 10;
        
        for (int i = 0; i < NUM_OF_VERTICES; i++) {

           graph.addVertex(PREFIX + i);
        
        }
              
        List<String> keys = new ArrayList<>();
        
        Iterable<Vertex> vertices = graph.getVertices();
       
 
        int i = 0;
        
        for (Vertex vertex : vertices) {
            
            String id = vertex.getId().toString();
            
            System.out.println("vertex_" + i + " = " + id);
                
            if (id.startsWith(PREFIX)) keys.add(id);
            
            i++;
        }
        
        assertEquals(NUM_OF_VERTICES, keys.size());   
    }
    
    /**
     * Test to add an edge between 2 vertices
     */
    @Test
    @RunIf(value = GraphEnabledChecker.class)
    public void testAddEdgeWoId()
    {
         System.out.println("-- testAddEdgeWoId");
         
         System.out.println("- Create 3 vertices");
         
         Vertex v_tae_bart = graph.addVertex("tae_bart");
         Vertex v_tae_march = graph.addVertex("tae_march");
         Vertex v_tae_homer = graph.addVertex("tae_homer");
         
         System.out.println("- Add an edge via the Graph instance between Bart and Homer, and Bart and March");
         Edge e_1 = graph.addEdge(null, v_tae_bart, v_tae_homer, "son of");
         Edge e_2 = graph.addEdge(null, v_tae_bart, v_tae_march, "son of");
         
         //Check if the edge objects where created correctly
         System.out.println(e_1.getId() + " = " + e_1); 
         assertEquals("tae_bart->|son of|->tae_homer",e_1.getId().toString()); 
         assertEquals("{from=v_tae_bart, label=son of, props={}, to=v_tae_homer, type=edge}", e_1.toString());
        
         System.out.println(e_2.getId() + " = " + e_2.toString());
         assertEquals("tae_bart->|son of|->tae_march", e_2.getId().toString());
         assertEquals("{from=v_tae_bart, label=son of, props={}, to=v_tae_march, type=edge}", e_2.toString());
         
         //Check if the vertices were updated correctly
         System.out.println("v_tae_bart = " + v_tae_bart.toString());
         assertEquals("{edges={in={}, out={son of=[e_tae_bart->|son of|->tae_homer, e_tae_bart->|son of|->tae_march]}}, props={}, type=vertex}", v_tae_bart.toString());
         
         System.out.println("v_tae_homer = " + v_tae_homer.toString());
         assertEquals("{edges={in={son of=[e_tae_bart->|son of|->tae_homer]}, out={}}, props={}, type=vertex}", v_tae_homer.toString());
        
         System.out.println("v_tae_march = " + v_tae_march.toString());
         assertEquals("{edges={in={son of=[e_tae_bart->|son of|->tae_march]}, out={}}, props={}, type=vertex}", v_tae_march.toString());
          
    }
    
    /**
     * Test to add an edge with id, the id should be ignored
     */
    @Test
    @RunIf(value = GraphEnabledChecker.class)
    public void testAddEdgeWithId()
    {
         System.out.println("-- testAddEdgeWithId");
                
         System.out.println("- Create 2 vertices");
         
         Vertex v_tae_moe = graph.addVertex("tae_moe");
         Vertex v_tae_barney = graph.addVertex("tae_barney");
         
         System.out.println("- Add an edge via the Graph instance between Moe and Barney");
         Edge e_1 = graph.addEdge("1", v_tae_barney, v_tae_moe, "guest of");
         
         System.out.println(e_1.getId());
         
         System.out.println("e_1 = " + e_1.getId());
         assertEquals("1", e_1.getId().toString()); 
         
         //Check if the vertices have attached the incoming and outgoing edges
         System.out.println("v_tae_moe = " + v_tae_moe.toString());
         assertEquals("{edges={in={guest of=[e_1]}, out={}}, props={}, type=vertex}", v_tae_moe.toString());
                
         System.out.println("v_tae_barney = " + v_tae_barney.toString());
         assertEquals("{edges={in={}, out={guest of=[e_1]}}, props={}, type=vertex}", v_tae_barney.toString());        
    }
    
    
    @Test
    @RunIf(value = GraphEnabledChecker.class)
    public void testAddEdgeWithProps()
    {
         System.out.println("-- testAddEdgeWithProps");
                
         System.out.println("- Create 2 vertices");
         
         Vertex v_taewp_moe = graph.addVertex("taewp_moe");
         Vertex v_taewp_barney = graph.addVertex("taewp_barney");
         
         Edge e_taewp = graph.addEdge("taewp", v_taewp_moe, v_taewp_barney, "guest of");
         e_taewp.setProperty("bill", 1234);
         
         System.out.println("e_taewp = " + e_taewp.toString());
         assertEquals("{from=v_taewp_moe, label=guest of, props={bill=1234}, to=v_taewp_barney, type=edge}", e_taewp.toString()); 
         
    }
}
