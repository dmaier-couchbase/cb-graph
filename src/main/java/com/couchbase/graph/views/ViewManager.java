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
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewDesign;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.graph.cfg.ConfigManager;
import com.couchbase.graph.con.ConnectionFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 *  This class helps to access the existing views 
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public class ViewManager {
    
    private static final Logger LOG = Logger.getLogger(ViewManager.class.getName());
    
    /**
     * The client instance to use
     */
    public static final CouchbaseClient client = ConnectionFactory.getClient();
    
    /**
     * The default design document name
     */
    public static final String DESIGN_DOC = ConfigManager.getCbConfig().getDesignDoc();
 
   
    //Available view definitions
    private static ViewDef allEdgesViewDef = null;
    private static ViewDef allVerticesViewDef = null;
    
    
    /**
     * To create a view
     * 
     * @param designDocName 
     * @param viewDefs 
     */
    public static void createViews(String designDocName, List<ViewDef> viewDefs)
    {      
        //Check if the Design document is available, otherwise create it         
        if (!designDocExists(designDocName)) {
            DesignDocument designDoc = new DesignDocument(designDocName);

            for (ViewDef viewDef : viewDefs) {
                
                ViewDesign viewDesign;
                
                //Derive the ViewDesign
                if (!viewDef.hasReduceFunc) {
                    viewDesign = new ViewDesign(viewDef.getName(), viewDef.getMapFunc());
                } else {
                    viewDesign = new ViewDesign(designDocName, viewDef.getMapFunc(), viewDef.getReduceFunc());
                }

                designDoc.getViews().add(viewDesign);
            }

            client.createDesignDoc(designDoc);
        }
    }
    
    /**
     * Create a view within the default design document
     * 
     * @param viewDefs
     */
    public static void createViews(List<ViewDef> viewDefs)
    {
           createViews(DESIGN_DOC, viewDefs);
    }
    
    
    /**
     * Create the default views
     */
    public static void createViews()
    {
        List<ViewDef> defs = new ArrayList<>();
        
        defs.add(getAllEdgesViewDef());
        defs.add(getAllVerticesViewDef());
        
        createViews(defs);
    }
    
    
    /**
     * Check if the design document is accessible
     * 
     * @param name
     * @return 
     */
    public static boolean designDocExists(String name)
    {
        //Check if the Design document is available, otherwise create it
        DesignDocument designDoc = null;
        
        try
        {
            designDoc = client.getDesignDoc(name);
        
        }catch (Exception e)
        {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if the default design document exists
     */
    public static boolean designDocExists()
    {
        return designDocExists(DESIGN_DOC);
    }
    
    /**
     * To delete a design document
     * 
     * @param designDoc
     * @return 
     */
    public static boolean deleteDesignDoc(String designDoc)
    {
        return client.deleteDesignDoc(designDoc);
    }
    
    /**
     * To delete the default design document
     * 
     * @return 
     */
    public static boolean deleteDesignDoc()
    {
       return deleteDesignDoc(DESIGN_DOC);
    }
    
    /**
     * To access the all edges view
     * @return 
     */
    public static View getAllEdgesView()
    {
        return client.getView(DESIGN_DOC, getAllEdgesViewDef().getName());
    }
    
    /**
     * To access the all vertices view
     * @return 
     */
    public static View getAllVerticesView()
    {
        return client.getView(DESIGN_DOC, getAllVerticesViewDef().getName());
    }
    
    /**
     * To get the all edges view definition
     * @return 
     */
    public static ViewDef getAllEdgesViewDef()
    {
        if (allEdgesViewDef == null)
            allEdgesViewDef = new AllEdgesViewDef();
        
        return allEdgesViewDef;
    }
    
    /**
     * To get the all vertices view definition
     * @return 
     */
    public static ViewDef getAllVerticesViewDef()
    {
        if (allVerticesViewDef == null)
            allVerticesViewDef = new AllVerticesViewDef();
        
        return allVerticesViewDef;
    }
    
    
    /**
     * Queries all documents of a view
     * 
     * @param designDocName
     * @param viewName
     * @return 
     */
    public static ViewResponse queryAll(String designDocName, String viewName)
    {
        ViewResponse result = null;
        
        //Perform the query
        View view = client.getView(designDocName, viewName);
        Query query = new Query();
        query.setIncludeDocs(false);
        query.setStale(Stale.FALSE);
       
        boolean viewAccessible = false;
        int counter = 5;
        
        while (!viewAccessible && counter != 0)
        {
            try
            {
                result = client.query(view, query);
                viewAccessible = true;
                
                LOG.finest("The view is now accessible");

            }
            catch (Exception e)
            {
                LOG.finest("The view is not yet accessible");
                
                try {
                    
                    Thread.sleep(1000);
                
                } catch (InterruptedException ex) {
                    //Do nothing
                }
                
                counter-=1;
            }
        }
        
        return result;
    }
    
    /**
     * Queries all edges
     * 
     * @return 
     */
    public static ViewResponse queryAllEdges()
    {
        return queryAll(DESIGN_DOC, getAllEdgesViewDef().getName());
    }
    
    /**
     * Queries all vertices
     * 
     * @return 
     */
    public static ViewResponse queryAllVertices()
    {
        return queryAll(DESIGN_DOC, getAllVerticesViewDef().getName());
    }
    
}
