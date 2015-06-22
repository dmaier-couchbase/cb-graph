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

import com.couchbase.graph.tinkerpop.deps.CBGraphTestEnv;
import com.couchbase.graph.tinkerpop.deps.GraphTest;
import com.couchbase.client.java.Bucket;
import com.couchbase.graph.CBGraph;
import com.couchbase.graph.conn.ConnectionFactory;
import com.couchbase.graph.test.annotation.RunIf;
import com.couchbase.graph.test.checker.GraphEnabledChecker;
import com.couchbase.graph.views.ViewManager;
import com.tinkerpop.blueprints.Graph;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author David Maier <david.maier at couchbase.com>
 */
public class GraphTestSuiteTest {
    
    private static Graph graph;
    
    
    @BeforeClass
    @RunIf(value = GraphEnabledChecker.class)
    public static void setUpClass() throws InterruptedException {
        
        //Flush the bucket and wait until the operation is successful
        Bucket client = ConnectionFactory.getBucketCon();
        assertTrue(client.bucketManager().flush());
        assertTrue(ViewManager.deleteDesignDoc());
        Thread.sleep(5000);
        graph = new CBGraph();
    }
    

    @Test
    @RunIf(value = GraphEnabledChecker.class)
    public void testRunTestSuite()
    {
        GraphTest testEnv = new CBGraphTestEnv();
        GraphTestSuite suite = new GraphTestSuite(testEnv);
        
        
        suite.testAddingVerticesAndEdges();
        suite.testAutotypingOfProperties();
        suite.testConcurrentModification();
        
    }
}
