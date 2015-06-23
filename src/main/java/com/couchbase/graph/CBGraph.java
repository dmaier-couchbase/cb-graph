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

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.couchbase.graph.cfg.ConfigManager;
import com.couchbase.graph.conn.ConnectionFactory;
import com.couchbase.graph.error.DocNotFoundException;
import com.couchbase.graph.error.IdGenException;
import com.couchbase.graph.views.ViewManager;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Blueprints implementation for Couchbase Server
 * 
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public class CBGraph implements Graph {

    /**
     * The Logger
     */
    private final static Logger LOG = Logger.getLogger(CBGraph.class.getName());
      
    /**
     * The features those are supported
     */
    private final Features features;
    
    /**
     * The client to use to connect to the Couchbase cluster
     */
    private final Bucket client;
    
    /**
     * Provides the information if the Graph is closed, multiple Graphs can share
     * one connection so we don't close the underlying connection
     */
    private boolean closed;

    /**
     * The default connector
     */
    public CBGraph() {
        
        //The features of this Graph
        this.features = new CBFeatures();
        
        //Init the client connection
        this.client = ConnectionFactory.getBucketCon();
        
        //The Graph is per default open
        this.closed = false;
        
        //Init the views
        if (ConfigManager.getCbConfig().isViewAutoCreateEnabled() && !ViewManager.designDocExists()) ViewManager.createViews();
    }
    
    
    /**
     * To get the features of this Graph implementation
     * 
     * @return 
     */
    @Override
    public Features getFeatures() {
        
        return this.features;
    }

    /**
     * To add a vertex. It returns null if the vertex could not be added.
     * 
     * @param id
     * @return 
     */
    @Override
    public Vertex addVertex(Object id) {
        
        Vertex result = null;

        try {
            
            if (id == null) {
                id = CBVertex.genVertexId();
            }

            
            JsonObject v = JsonObject.empty();
            v.put(CBModel.PROP_TYPE, CBModel.VAL_TYPE_VERTEX);

            JsonObject props = JsonObject.empty();
            v.put(CBModel.PROP_PROPS, props);

            JsonObject in = JsonObject.empty();
            JsonObject out = JsonObject.empty();
            JsonObject edges = JsonObject.empty();
                    
            edges.put(CBModel.PROP_EDGES_IN, in);
            edges.put(CBModel.PROP_EDGES_OUT, out);

            v.put(CBModel.PROP_EDGES, edges);

            JsonDocument doc = JsonDocument.create(CBVertex.genVertexKey(id), v);
            client.insert(doc);
            
            result = new CBVertex(id, this);

        } catch (IdGenException | DocNotFoundException ex) {
            
            LOG.severe(ex.toString());
            return result;
        }

        return result;
    }

    /**
     * To get a vertex by the id. It returns null if the vertex could not
     * be retrieved.
     * 
     * @param id
     * @return 
     */
    @Override
    public Vertex getVertex(Object id) {
        
        Vertex result = null;
        
        try
        {
            result = new CBVertex(id, this);
        }
        catch (DocNotFoundException e)
        {
            LOG.severe(e.toString());
        }
        
        return result; 
    }

    /**
     * To remove a vertex
     * 
     * @param vertex 
     */
    @Override
    public void removeVertex(Vertex vertex) {
        
        vertex.remove();
    }

    /**
     * To get all vertices
     * 
     * @return 
     */
    @Override
    public Iterable<Vertex> getVertices() {
        
        try {
            
            return CBVertex.queryAllVertices(this);
            
        } catch (DocNotFoundException ex) {
            
            LOG.log(Level.SEVERE, "Could not get all vertices: {0}", ex.getMessage());
        }
        
        return null;
    }

    /**
     * To get all vertices those have a specifc key-value property
     * @param key
     * @param value
     * @return 
     */
    @Override
    public Iterable<Vertex> getVertices(String key, Object value) {
     
        try
        {
           return CBVertex.queryByVertexProp(key, value.toString(), this);
        }
        catch (DocNotFoundException ex)
        {
            LOG.log(Level.SEVERE, "Could not get the vertices by property: {0}", ex.getMessage());
        }

        return null;
    }

    /**
     * To add an edge between two vertices with a specific label
     * 
     * Our implementation ignores the id of the edge because it is derived from
     * the id-s of the vertices
     *
     * @param id
     * @param v1
     * @param v2
     * @param label
     * @return
     */
    @Override
    public Edge addEdge(Object id, Vertex v1, Vertex v2, String label) {

        Edge result = null;

        try {

            //The vertices as Couchbase vertices
            CBVertex v1CB = (CBVertex) v1;
            CBVertex v2CB = (CBVertex) v2;

            v1CB.refresh();
            v2CB.refresh();

            //Create a new edge document
            JsonObject edge = JsonObject.empty();
            edge.put(CBModel.PROP_PROPS, JsonObject.empty());
            edge.put(CBModel.PROP_TYPE, CBModel.VAL_TYPE_EDGE);
            edge.put(CBModel.PROP_FROM, v1CB.getCbKey());
            edge.put(CBModel.PROP_TO, v2CB.getCbKey());
            edge.put(CBModel.PROP_LABEL, label);
            
            String eKey;
                    
            if (id == null)
             eKey = CBEdge.genEdgeKey(v1CB.getId(),label, v2CB.getId());
            else
             eKey = CBModel.EDGE_PREFIX + id; 
            
            //Add the edege object
            client.insert(JsonDocument.create(eKey, edge));

            //Add the edge to the outgoing adjacency list of this vertex
            v1CB.addEdgeToAdjacencyList(label, eKey, Direction.OUT);

            //Add the edge to the incoming adjacency list of the other vertex
            v2CB.addEdgeToAdjacencyList(label, eKey, Direction.IN);
            
            if (id == null)
                result = new CBEdge(eKey, this);
            else
                result = new CBEdge(eKey, this);

        } catch (DocNotFoundException e) {

            LOG.severe(e.toString());
        }

        return result;
    }

    /**
     * To get an edge by it's id, otherwise return null
     * @param id
     * @return 
     */
    @Override
    public Edge getEdge(Object id) {
       
        Edge result = null;
        
        //TODO: This may cause a null pointer exception and should be tested
        String eKey = CBModel.EDGE_PREFIX + id.toString();
       
        try {
            
            result = new CBEdge(eKey, this);
        
        } catch (DocNotFoundException e) {
        
            LOG.severe(e.toString());
        
        }
        
        return result;
    }

    /**
     * To remove an edge means to remove it also from the vertices
     * 
     * @param edge 
     */
    @Override
    public void removeEdge(Edge edge) {
        
        edge.remove();
    }

    /**
     * To get all edges
     * 
     * @return 
     */
    @Override
    public Iterable<Edge> getEdges() {
        
        try
        {
            return CBEdge.queryAllEdges(this);
        }
        catch (DocNotFoundException ex)
        {
            LOG.log(Level.SEVERE, "Could not get the edges: {0}", ex.getMessage());
        }
        
        return null;
    }

    /**
     * To get all edges with the given key-value pair
     * 
     * @param key
     * @param value
     * @return 
     */
    @Override
    public Iterable<Edge> getEdges(String key, Object value) {
        
        
        try {
            
            return CBEdge.queryByEdgeProp(key, value.toString(), this);
         
        } catch (DocNotFoundException ex) {
            
            LOG.log(Level.SEVERE, "Could not get the edges by property: {0}", ex.getMessage());
        }
        
        return null;
    }

    /**
     * Not yet implemented
     * 
     * @return 
     */
    @Override
    public GraphQuery query() {
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * To shutdown the Graph
     */
    @Override
    public void shutdown() {
      
        this.closed = true;
    }

    /**
     * Get the status if the Graph was shutdown
     * @return 
     */
    public boolean isClosed() {
        return closed;
    }
    
    
}
