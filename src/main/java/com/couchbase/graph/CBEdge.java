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

package com.couchbase.graph;

import static com.couchbase.graph.views.ViewManager.*;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.couchbase.graph.error.DocNotFoundException;
import com.couchbase.graph.helper.ZipHelper;
import com.couchbase.graph.views.ViewManager;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The implementation of an Edge
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public final class CBEdge extends CBElement implements Edge {

    private static final Logger LOG = Logger.getLogger(CBEdge.class.getName());
     
    /**
     * The inner from attribute
     */
    private String innerFrom;
    
    /**
     * The inner to attribute
     */
    private String innerTo;
    
    /**
     * The inner label attribute
     */
    private String innerLabel;
    
    
    /**
    * The constructor of an edge without label
     * @param vId1
     * @param vId2
     * @param graph
     * @throws com.couchbase.graph.error.DocNotFoundException
    */
    public CBEdge(Object vId1, Object vId2, Graph graph) throws DocNotFoundException {
        
        this(vId1, "", vId2, graph);
    }
    
    /**
    * The constructor of an edge which has a label
     * @param vId1
     * @param label
     * @param vId2
     * @param graph
     * @throws com.couchbase.graph.error.DocNotFoundException
    */
    public CBEdge(Object vId1, String label, Object vId2, Graph graph) throws DocNotFoundException {
        
        super(genEdgeId(vId1, label, vId2), graph);
        this.cbKey = genEdgeKey(vId1, label, vId2);
        this.innerFrom = CBVertex.genVertexKey(vId1);
        this.innerLabel = label;
        this.innerTo = CBVertex.genVertexKey(vId2);
        this.refresh();
    }
    
    /**
     * To constructor of an edge by it's key value
     * @param eKey
     * @param graph 
     * @throws com.couchbase.graph.error.DocNotFoundException 
     */
    public CBEdge(String eKey,  Graph graph) throws DocNotFoundException
    {  
        super(parseEdgeKey(eKey)[0],graph);
        
        this.cbKey = eKey;
        this.refresh();
    }

    @Override
    public Vertex getVertex(Direction drctn) throws IllegalArgumentException {
        
        Vertex result = null;
        
        try {
            
            refresh();
            
            if (drctn.equals(Direction.IN) || drctn.equals(Direction.OUT))
            {
                if (drctn.equals(Direction.IN))
                    return new CBVertex(innerTo, graph);
            
                if (drctn.equals(Direction.OUT))
                    return new CBVertex(innerFrom, graph);
            }
            else
            {
                throw new IllegalArgumentException("Only Ingoing and outgoing is allowed.");
            }
            
        } catch (DocNotFoundException e) {
            
            LOG.severe(e.toString());
        }
       
        return result;
    }

    /**
     * To get the label of this edge
     * @return 
     */
    @Override
    public String getLabel() {
        
        try {
            
            refresh();
        
        } catch (DocNotFoundException e) {
            
            LOG.severe(e.toString());
        }
        
        return innerLabel;
        
    }

    /**
     * To refresh the edge attributes by accessing the database
     * 
     * 
     * @throws com.couchbase.graph.error.DocNotFoundException
     * @return 
     */
    @Override
    public boolean refresh() throws DocNotFoundException {
        
        if (super.refresh())
        {   
            innerFrom = innerObj.get(CBModel.PROP_FROM).toString();
            innerTo = innerObj.get(CBModel.PROP_TO).toString();
            innerLabel = innerObj.get(CBModel.PROP_LABEL).toString();
                  
            return true;
        }
        return false;
    }

    /**
     * To remove an edge does not just mean to remove the edge object,
     * instead also the references to it needs to be removed
     * 
     */
    @Override
    public void remove() {
        
            
            //Get the vertices of the edge
            CBVertex vSource = (CBVertex) this.getVertex(Direction.OUT);
            CBVertex vTarget = (CBVertex) this.getVertex(Direction.IN);
            
            //Remove the edge from it   
            try {
                
                vSource.removeEdgeFromAdjacencyList(this.innerLabel, cbKey, Direction.OUT);
                vTarget.removeEdgeFromAdjacencyList(this.innerLabel, cbKey, Direction.IN);
                super.remove();
                
            } catch (ZipHelper.CompressionException ex) {
                
                LOG.severe(ex.toString());
            }
    }
    
   
    //-- Some helper methods
    
    /**
     * To generate the id of an edge by using the vertex id-s
     * @param vId1
     * @param label
     * @param vId2
     * @return 
     */
    public static String genEdgeId(Object vId1, String label, Object vId2)
    {
        return CBModel.EDGE_ID.replace("{1}", vId1.toString()).replace("{3}", vId2.toString()).replace("{2}", label);
    }
    
    
      
    /**
     * To generate the key of an edge by using the vertex id-s
     * @param vId1
     * @param label
     * @param vId2
     * @return 
     */
    public static String genEdgeKey(Object vId1, String label, Object vId2)
    {        
        return CBModel.EDGE_KEY.replace("{1}", vId1.toString()).replace("{3}", vId2.toString()).replace("{2}", label);
    }
   
    /**
     * Returns the parsed key
     * 
     * 0 - the id
     * 1 - the prefix
     * 2 - from
     * 3 - label
     * 4 - to
     * 
     * Returns 
     * @param eKey
     * @return 
     */
    public static String[] parseEdgeKey(String eKey)
    {
        String[] result = new String[5];
        
        
        int idStartIdx = CBModel.EDGE_PREFIX.length();
        String id = eKey.substring(idStartIdx, eKey.length());
        result[0] = id;
        result[1] = CBModel.EDGE_PREFIX;
        
        if (eKey.contains(CBModel.EDGE_DELIM + "|") && eKey.contains("|"+CBModel.EDGE_DELIM))
        {
            int fromEndIdx = id.indexOf(CBModel.EDGE_DELIM);
            String from = id.substring(0, fromEndIdx);
            result[2] = from;

            int toStartIdx = id.lastIndexOf(CBModel.EDGE_DELIM) + CBModel.EDGE_DELIM.length();
            String to = id.substring(toStartIdx, id.length());
            result[4] = to;

            int labelStartIdx = id.indexOf("|");
            int labelEndIdx = id.lastIndexOf("|");
            String label = id.substring(labelStartIdx + 1, labelEndIdx);
            result[3] = label;

        }
        
        return result;
    }
    
    
    /**
     * Queries all edge labels
     * @return 
     */
    public static Set<String> queryAllEdgeLabels()
    {
        Set<String> result = new HashSet<>();
        
        ViewResult queryResult = ViewManager.query(DESIGN_DOC, getAllEdgeLabelsViewDef().name(), null, null);
        
        for (ViewRow viewRow : queryResult) {
                
            String label = viewRow.key().toString();
            
            result.add(label);
            
        }
        
        return result;
    }
    
    
    /**
     * Queries for a specific edge label
     * @param label
     * @param graph
     * @return 
     * @throws com.couchbase.graph.error.DocNotFoundException 
     */
    public static List<Edge> queryByEdgeLabel(String label, Graph graph) throws DocNotFoundException
    {
        List<Edge> result = new ArrayList<>();
        
        ViewResult viewResult = ViewManager.query(DESIGN_DOC, getAllEdgeLabelsViewDef().name(), label, null);
        
        for (ViewRow viewRow : viewResult) {
            
            result.add(new CBEdge(viewRow.id(), graph));
        }
        
        return result;
        
    }
    
     /**
     * Queries all edges
     * 
     * @param graph
     * @return 
     * @throws com.couchbase.graph.error.DocNotFoundException 
     */
    public static List<Edge> queryAllEdges(Graph graph) throws DocNotFoundException
    {
       
        List<Edge> result = new ArrayList<>();
         
        ViewResult viewResult = ViewManager.query(DESIGN_DOC, getAllEdgesViewDef().name(), null, null);
        
        
        for (ViewRow viewRow : viewResult) {
            
            result.add(new CBEdge(viewRow.id(), graph));
        }
        
        return result;
    }
    
    /**
     * Queries all edge properties
     * 
     * @param key
     * @param value
     * @return 
     */
    public static List<Edge> queryByEdgeProp(String key, String value, Graph graph) throws DocNotFoundException
    { 
        List<Edge> result = new ArrayList<>();
        
        ViewResult viewResult = ViewManager.query(DESIGN_DOC, getAllEdgePropsViewDef().name(), genCompKey(key, value), genCompKey(key, value));
        
        for (ViewRow viewRow : viewResult) {
            
            result.add(new CBEdge(viewRow.id(), graph));
        }
        
        return result;
         
         
    }
    
    
     
}
