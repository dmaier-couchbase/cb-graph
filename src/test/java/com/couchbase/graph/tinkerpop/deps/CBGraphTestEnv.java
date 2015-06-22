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

package com.couchbase.graph.tinkerpop.deps;

import com.couchbase.client.java.Bucket;
import com.couchbase.graph.CBGraph;
import com.couchbase.graph.conn.ConnectionFactory;
import com.couchbase.graph.error.ConnectionException;
import com.tinkerpop.blueprints.Graph;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author David Maier <david.maier at couchbase.com>
 */
public class CBGraphTestEnv extends GraphTest {

    @Override
    public Graph generateGraph() {
        
        try {
        
            //Create an empty graph
            Bucket bucketCon = ConnectionFactory.createCon();
            bucketCon.bucketManager().flush();
        
        } catch (ConnectionException ex) {
           
            ex.printStackTrace();
        }
        
        return new CBGraph();
    }

    @Override
    public Graph generateGraph(String graphDirectoryName) {
       
        //Ignores the directory name
        return generateGraph();
        
    }

    @Override
    public void doTestSuite(TestSuite testSuite) throws Exception {
        
       testSuite.run();

    }
    
}
