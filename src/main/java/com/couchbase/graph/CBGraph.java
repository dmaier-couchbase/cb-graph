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

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.couchbase.graph.con.ConnectionFactory;
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
import org.json.simple.JSONObject;

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
    private final CouchbaseClient client;

    /**
     * The default connector
     */
    public CBGraph() {
        
        //The features of this Graph
        this.features = new CBFeatures();
        
        //Init the client connection
        this.client = ConnectionFactory.getClient();
        
        //Init the views
        ViewManager.createViews();
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

            JSONObject v = new JSONObject();
            v.put(CBModel.PROP_TYPE, CBModel.VAL_TYPE_VERTEX);

            JSONObject props = new JSONObject();
            v.put(CBModel.PROP_PROPS, props);

            JSONObject in = new JSONObject();
            JSONObject out = new JSONObject();
            JSONObject edges = new JSONObject();
            edges.put(CBModel.PROP_EDGES_IN, in);
            edges.put(CBModel.PROP_EDGES_OUT, out);

            v.put(CBModel.PROP_EDGES, edges);

            client.add(CBVertex.genVertexKey(id), v.toJSONString());

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
        
        List<Vertex> result = new ArrayList<>();
       
        ViewResponse queryResult = ViewManager.queryAllVertices();
        
        for (ViewRow v : queryResult) {

            String currKey = v.getKey();
            String currId = v.getId();
            
            LOG.log(Level.FINEST,"(id, key) = " + "({0}, {1})", new Object[]{currId, currKey});
            
            try
            {
                Vertex currV = new CBVertex(currKey, this);

                result.add(currV);
            }
            catch (DocNotFoundException e)
            {
                LOG.severe(e.toString());
            }
            
        }
        
        return result;
    }

    /**
     * To get all vertices those have a specifc key-value property
     * @param key
     * @param value
     * @return 
     */
    @Override
    public Iterable<Vertex> getVertices(String key, Object value) {
     
        List<Vertex> result = new ArrayList<>();
        
        //TODO: This requires a view for each property of a vertex
        //As an alternative we could store an additional property index
        //'$key_$value' : { 'ref':'$vKey'}
        //So the following implementation is not the best one, because it 
        //goes over all vertices  
        Iterable<Vertex> allVertices = getVertices();
        
        for (Vertex vertex : allVertices) {
        
            Object currVal = vertex.getProperty(key);
            
            if (currVal != null && currVal.equals(value))
            {
                result.add(vertex);
            }
        }
        
        return result;
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
            JSONObject edge = new JSONObject();
            edge.put(CBModel.PROP_TYPE, CBModel.VAL_TYPE_EDGE);
            edge.put(CBModel.PROP_FROM, v1CB.getCbKey());
            edge.put(CBModel.PROP_TO, v2CB.getCbKey());
            edge.put(CBModel.PROP_LABEL, label);
            
            String eKey = CBEdge.genEdgeKey(v1CB.getId(),label, v2CB.getId());
            
            //Add the edege object
            client.add(eKey, edge.toJSONString());

            //Add the edge to the outgoing adjacency list of this vertex
            v1CB.addEdgeToAdjacencyList(label, eKey, Direction.OUT);

            //Add the edge to the incoming adjacency list of the other vertex
            v2CB.addEdgeToAdjacencyList(label, eKey, Direction.IN);

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
        
        //This may cause a null pointer exception and should be tested
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
        
        List<Edge> result = new ArrayList<>();
        
        ViewResponse queryResult = ViewManager.queryAllEdges();
        
        for (ViewRow v : queryResult) {
            
            try {
            
                result.add(new CBEdge(v.getKey(), this));
            
            } catch (DocNotFoundException e) {
                
                LOG.severe(e.toString());
            }
            
        }
        
        return result;
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
        
        //See getVertices regarding the comment how to implement it better
        List<Edge> result = new ArrayList<>();
        
        Iterable<Edge> queryResult = getEdges();
        
        for (Edge edge : queryResult) {
         
            Object currVal = edge.getProperty(key);
            
            if (currVal != null && currVal.equals(value))
            {
                result.add(edge);
            }
            
        }
        
        return result;
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
       
        client.shutdown();
    }
}
