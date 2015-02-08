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

import com.couchbase.client.CouchbaseClient;
import com.couchbase.graph.con.ConnectionFactory;
import com.couchbase.graph.views.ViewManager;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author David Maier <david.maier at couchbase.com>
 */
public class CBElementTest {
    
    /**
     * The graph instance to test
     */
    private static Graph graph;
    
    /**
     * Flush the test bucket and delete the views before running the tests
     * 
     * @throws Exception 
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        
        CouchbaseClient client = ConnectionFactory.getClient();
        assertTrue(client.flush().get());
        assertTrue(ViewManager.deleteDesignDoc());
        graph = new CBGraph();
    }
    
    /**
     * Purpose of this test is to make sure that the 
     */
    @Test
    public void testToString() {
    
        System.out.println("-- testToString");
        
        //Make sure the element is there. In this case it is a vertex.
        Vertex v = graph.addVertex("te_1");
                
        v.setProperty("birthday", 123456789);
        v.setProperty("z", "value_z");
        v.setProperty("b", "value_b");
        v.setProperty("age", 34);
        v.setProperty("a", "value_a");
        v.setProperty("y", "value_y");

        System.out.println("v = " + v.toString());
        assertEquals("{\"edges\":{\"in\":{},\"out\":{}},\"props\":{\"a\":\"value_a\",\"b\":\"value_b\",\"y\":\"value_y\",\"z\":\"value_z\"},\"type\":\"vertex\"}", v.toString());
    }
}
