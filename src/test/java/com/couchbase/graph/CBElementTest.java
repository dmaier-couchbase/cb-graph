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

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.graph.helper.JSONHelper;
import com.couchbase.graph.deps.annotation.RunIf;
import com.couchbase.graph.deps.checker.GraphEnabledChecker;
import com.couchbase.graph.deps.runner.JUnitExtRunner;
import com.tinkerpop.blueprints.Graph;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author David Maier <david.maier at couchbase.com>
 */
@RunWith(JUnitExtRunner.class)
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

    }
    
    /**
     * Purpose of this test is to make sure that the 
     */
    @Test
    @RunIf(value = GraphEnabledChecker.class)
    public void testToString() {
    
        System.out.println("-- testToString");
        
        String in = "{\"edges\":{\"in\":{},\"out\":{}},\"type\":\"vertex\",\"props\":{\"city\":\"Springfield\",\"last_name\":\"Simpson\",\"first_name\":\"Bart\",\"is_student\":true,\"age\":8}}";
  
        JsonObject obj =  JsonObject.fromJson(in);
        
        System.out.println(in);
        

        //TODO: Add assertion
        System.out.println( JSONHelper.sort(obj));
        
    }
}
