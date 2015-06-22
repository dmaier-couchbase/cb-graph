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

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.Stale;
import com.couchbase.client.java.view.View;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.graph.cfg.ConfigManager;
import com.couchbase.graph.conn.ConnectionFactory;
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
    public static final Bucket client = ConnectionFactory.getBucketCon();
    
    /**
     * The default design document name
     */
    public static final String DESIGN_DOC = ConfigManager.getCbConfig().getDesignDoc();
 
   
    //Available view definitions
    private static ViewDef allEdgesViewDef;
    private static ViewDef allEdgeLabelsViewDef;
    private static ViewDef allVerticesViewDef;
    private static ViewDef allVertexPropsViewDef;
    private static ViewDef allEdgePropsViewDef;
    
    
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

            
            List<View> views = new ArrayList<>();
            
            for (View view : viewDefs) {
                
                views.add(view);
            }
            
            DesignDocument designDoc = DesignDocument.create(designDocName, views );
        
            
            client.bucketManager().insertDesignDocument(designDoc);
            

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
        defs.add(getAllEdgeLabelsViewDef());
        defs.add(getAllVertexPropsViewDef());
        defs.add(getAllEdgePropsViewDef());
        
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
       DesignDocument designDoc = null;   
       
       designDoc = client.bucketManager().getDesignDocument(name);
       if (designDoc != null) return true;
     
        return false;
    }
    
    /**
     * Check if the default design document exists
     */
    public static boolean designDocExists()
    {
        return designDocExists(DESIGN_DOC);
    }
    
    /**
     * To delete a design document, if the design document was not existent also
     * true is returned
     * 
     * @param designDoc
     * @return 
     */
    public static boolean deleteDesignDoc(String designDoc)
    {
        if (designDocExists())
        {
            return client.bucketManager().removeDesignDocument(designDoc);
        }
        
        return true;
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
     * To get the all edge labels view definition
     * 
     * @return 
     */
    public static ViewDef getAllEdgeLabelsViewDef() {
        if (allEdgeLabelsViewDef == null)
              allEdgeLabelsViewDef = new AllEdgeLabelsViewDef();
        
        return allEdgeLabelsViewDef;
    }

    /**
     * To get the all vertex properties view definition
     * @return 
     */
    public static ViewDef getAllVertexPropsViewDef() {
        if (allVertexPropsViewDef == null)
            allVertexPropsViewDef = new AllVertexPropsViewDef();
        
        return allVertexPropsViewDef;
    }
    
    /**
     * To get the all edge properties view definition
     * @return 
     */
    public static ViewDef getAllEdgePropsViewDef() {
        if (allEdgePropsViewDef == null)
            allEdgePropsViewDef = new AllEdgePropsViewDef();
        
        return allEdgePropsViewDef;
    }

    
    /**
     * Queries all documents of a view with an optional range parameter
     * 
     * @param designDocName
     * @param viewName
     * @param startKey
     * @param endKey
     * @return 
     */
    public static ViewResult query(String designDocName, String viewName, String startKey, String endKey)
    {
        ViewResult result = null;
        
        //Perform the query
        ViewQuery query = ViewQuery.from(designDocName, viewName).inclusiveEnd(true).stale(Stale.FALSE);
                
        if (startKey != null)
        {
            query = query.startKey(startKey);
        }
        
        if (endKey != null)
        {
            query = query.endKey(endKey);
        }
        
        result = client.query(query);
        
        return result;
    }
    
    /**
     * Queries all edges
     * 
     * @return 
     */
    public static ViewResult queryAllEdges()
    {
        return query(DESIGN_DOC, getAllEdgesViewDef().name(), null, null);
    }
    
    /**
     * Queries all vertices
     * 
     * @return 
     */
    public static ViewResult queryAllVertices()
    {
        return query(DESIGN_DOC, getAllVerticesViewDef().name(), null, null);
    }
    
    /**
     * Queries all edge labels
     * @return 
     */
    public static ViewResult queryAllEdgeLabels()
    {
        return query(DESIGN_DOC, getAllEdgeLabelsViewDef().name(), null, null);
    }
    
    /**
     * Queries for a specific edge label
     * @param label
     * @return 
     */
    public static ViewResult queryAllEdgeLabels(String label)
    {
        return query(DESIGN_DOC, getAllEdgeLabelsViewDef().name(), label, null);
    }
    
    /**
     * Queries all vertex properties
     * @return 
     */
    public static ViewResult queryAllVertexProps()
    {
        return query(DESIGN_DOC, getAllVertexPropsViewDef().name(),null, null);
    }
    
    /**
     * Queries all edge properties
     * 
     * @param key
     * @param value
     * @return 
     */
    public static ViewResult queryAllEdgeProps(String key, String value)
    {
        return query(DESIGN_DOC, getAllVertexPropsViewDef().name(), genCompKey(key, value), genCompKey(key, value));
    }
    
    /**
     * Queries for a specific property
     * @param key
     * @param value
     * @return 
     */
    public static ViewResult queryAllVertexProps(String key, String value)
    {
        return query(DESIGN_DOC, getAllVertexPropsViewDef().name(),genCompKey(key, value), genCompKey(key, value));
    }
    
    /**
     * To generate a compound key from a KV-pair
     * 
     * @param key
     * @param value
     * @return 
     */
    private static String genCompKey(String key, String value)
    {
        return "[" + key + "," + value + "]";
    }
}
