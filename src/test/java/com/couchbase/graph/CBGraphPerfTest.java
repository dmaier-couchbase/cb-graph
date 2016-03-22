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
import com.couchbase.graph.deps.annotation.RunIf;
import com.couchbase.graph.deps.checker.PerfEnabledChecker;
import com.couchbase.graph.deps.runner.JUnitExtRunner;
import com.couchbase.graph.views.ViewManager;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import java.util.UUID;
import org.apache.commons.lang.time.StopWatch;
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
public class CBGraphPerfTest {
    
    /**
     * The graph instance to test
     */
    private static Graph graph;
    
    
    /**
     * To initialize the test
     * @throws java.lang.Exception
     */
    @BeforeClass
    @RunIf(value = PerfEnabledChecker.class)
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
     * To add 1000 vertices
     */
    @Test
    @RunIf(value = PerfEnabledChecker.class)
    public void testAdd1000Vertices()
    {
        System.out.println("-- testAdd1000Vertices");
        
        //Generate some random id-s
        String[] uuids = new String[1000];
        
        for (int i = 0; i < 1000; i++) {
          
            String uuid = UUID.randomUUID().toString();
            uuids[i] = uuid;
        }
        
        StopWatch sw = new StopWatch();
        sw.start();
       
        //For each UUID add a vertex
        
        for (String uuid : uuids) {
        
            graph.addVertex(uuid);    
        }
       
        sw.stop();
        
        System.out.println("1000 verices added in " + sw.getTime() + " ms");
        
        System.out.println("- Proof that the last one was added");
        Vertex v = graph.getVertex(uuids[uuids.length-1]);
        assertNotNull(v);
        
    }
    
    
    @Test
    @RunIf(value = PerfEnabledChecker.class)
    public void testCreateBinaryTreeWithDepthOf10()
    {   
        System.out.println("-- testCreateBinaryTreeWithDepthOf10");
        
        //Create a root node
        String uuid = UUID.randomUUID().toString();
        
        Vertex root = graph.addVertex(uuid);
      
        StopWatch sw = new StopWatch();
        sw.start();
        
        add2Vertices(root, 0, 10);
        //add2Vertices(root, 0, 14);
    
        sw.stop();
        System.out.println("Binary tree with a depth of 10 added in " + sw.getTime() + " ms");
    
       
        //Traverse the tree  
        sw = new StopWatch();
        sw.start();
        
        for (int i = 0; i < 10; i++) {
            
             Vertex left = root.getVertices(Direction.OUT, "to left").iterator().next();
             root = left;
        }
        
        sw.stop();
        System.out.println("Traversed tree in " + sw.getTime() + " ms");
    }
    
    @Test
    @RunIf(value = PerfEnabledChecker.class)
    public void traverse50KEdges()
    {
        System.out.println("-- traverse50KEdges");
        
        int COUNT = 10000;
        String data = "123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790123456790";
       
        //Create a graph of neighbors and second neighbors
        System.out.println("Creating graph ...");
        
        Vertex root = graph.addVertex(UUID.randomUUID().toString());
        
        for (int i = 0; i < COUNT; i++) {
            
            Vertex v = graph.addVertex(UUID.randomUUID().toString());
            System.out.println("id = " +v.getId());  
            v.setProperty("data", data);
              
            graph.addEdge(null, root, v, "neighbor of" );
            
        }
        
        //Traverse the graph
        System.out.println("Traversing graph ..."); 
        
        StopWatch sw = new StopWatch();
        sw.start();

        Iterable<Vertex> vertices =  root.getVertices(Direction.OUT);
        
        for ( Vertex v : vertices) {

            System.out.println(v.getId());

        }

        sw.stop();
        System.out.println("Traversed tree in " + sw.getTime() + " ms");
    }
    
    
    /**
     * A helper method to add 2 vertices to a root vertex
     * 
     * @param root 
     * @param currDepth 
     * @param maxDepth 
     */
    public void add2Vertices(Vertex root, int currDepth, int maxDepth)
    {
        System.out.println("root = " + root.getId());
        
        //Add the 2 vertices
        String lUuid = "l_" + UUID.randomUUID().toString();
        String rUuid = "r_" + UUID.randomUUID().toString();
        
        Vertex v_l = graph.addVertex(lUuid);
        Vertex v_r = graph.addVertex(rUuid);
        
        root.addEdge("to left", v_l);
        root.addEdge("to right", v_r);

        currDepth = currDepth + 1;
        
        //Leave the recursion
        if (currDepth < maxDepth )
        {
            add2Vertices(v_l, currDepth, maxDepth);
            add2Vertices(v_r, currDepth, maxDepth);
        }
    }
}
