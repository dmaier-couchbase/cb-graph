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

package com.couchbase.graph.views;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.graph.cfg.ConfigManager;
import com.couchbase.graph.con.ConnectionFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author David Maier <david.maier at couchbase.com>
 */
public class ViewManagerTest {
    
    /**
     * To initialize the test
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        
        //Flush the bucket and wait until the operation is successful
        CouchbaseClient client = ConnectionFactory.getClient();
        assertTrue(client.flush().get());
        
        client.deleteDesignDoc(ConfigManager.getCbConfig().getDesignDoc());
     
    }
    
    @Test
    public void testCreateView() {
            
        ViewManager.createViews();
        
        String edge_view = ViewManager.getAllEdgesView().getViewName();
        String vertex_view = ViewManager.getAllVerticesView().getViewName();
        
        System.out.println("edge view = " + edge_view);
        assertEquals(AllEdgesViewDef.VIEW_ALL_EDGES, edge_view);
        
        System.out.println("vertex view = " + vertex_view);
        assertEquals(AllVerticesViewDef.VIEW_ALL_VERTICES, vertex_view);
    }
    
    @Test
    public void testQueryAll()
    {
        
    }
}
