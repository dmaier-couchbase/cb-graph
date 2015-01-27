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

import com.couchbase.graph.error.DocNotFoundException;
import com.couchbase.graph.error.IdGenException;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import org.json.simple.*;

/**
 * The implementation of an Vertex
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public final class CBVertex extends CBElement implements Vertex {

    /**
     * The logger
     */
    private static final Logger LOG = Logger.getLogger(CBVertex.class.getName());
    
    /**
     * The edges those are belonging to this vertex
     */
    private JSONObject innerEdges;
    
    /**
     * The inner outgoing edges
     */
    private JSONObject innerOutgoingEdges;
    
    /**
     * The inner incoming edges
     */
    private JSONObject innerIncomingEdges;
    
     /**
     * The inner counter which is used for key generations
     */
    private static long innerIdCounter = -1;
    
    
    /**
     * The constructor which takes an id as the argument
     * @param id
     * @param graph
     * @throws com.couchbase.graph.error.DocNotFoundException
     */
    public CBVertex(Object id, Graph graph) throws DocNotFoundException {
        
        super(id, graph);
        this.cbKey = genVertexKey(id);
        this.refresh();
    }
    
    /**
     * The constructor which takes the key as an argument
     * @param key
     * @param graph
     * @throws DocNotFoundException 
     */
    public CBVertex(String key, Graph graph) throws DocNotFoundException
    {
        super(parseVertexKey(key), graph);
        this.cbKey = key;
        this.refresh();
    }
    
    
    /**
     * The edges are nested in order to access them directly via the labels
     * 
     *  {...
     *    'edges'  : 
     *       {
     *           'out':
     *               {
     *                   'label1': ['e_1', 'e_2'],
     *                   'label2': ['e_4', 'e_5']
     *               }
     *               ...
     *       }
     *   ...
     *   }
     * 
     * @param drctn
     * @param labels
     * @return 
     */
    @Override
    public Iterable<Edge> getEdges(Direction drctn, String... labels) {
        
        //The result as List        
        List<Edge> result = new ArrayList<>();
       
        try
        { 
            this.refresh();

            for (String label : labels) {

                if (drctn.equals(Direction.IN) || drctn.equals(Direction.BOTH)) {
                    JSONArray labeledInEdges = (JSONArray) innerIncomingEdges.get(label);

                    for (Object eKey : labeledInEdges) {

                        result.add(new CBEdge(eKey.toString(), graph));
                    }
                }

                if (drctn.equals(Direction.OUT) || drctn.equals(Direction.BOTH)) {
                    JSONArray labeledOutEdges = (JSONArray) innerOutgoingEdges.get(label);

                    for (Object eKey : labeledOutEdges) {

                        result.add(new CBEdge(eKey.toString(), graph));
                    }
                }

            }

        }
        catch (DocNotFoundException e)
        {
            LOG.severe(e.toString());
        }
        
        return result;
    }
    
    
    public Iterable<Edge> getAllEdges() {
        
        List<Edge> result = new ArrayList<>();
        
        try {
            
            refresh();

            for (Object oEdgeArray : this.innerIncomingEdges.values()) {

                JSONArray edgeArray = (JSONArray) oEdgeArray;

                for (Object edgeKey : edgeArray) {

                    Edge edge = new CBEdge(edgeKey.toString(), graph);

                    result.add(edge);
                }
            }
            
            for (Object oEdgeArray : this.innerOutgoingEdges.values()) {

                JSONArray edgeArray = (JSONArray) oEdgeArray;

                for (Object edgeKey : edgeArray) {

                    Edge edge = new CBEdge(edgeKey.toString(), graph);

                    result.add(edge);
                }
            }

        } catch (DocNotFoundException ex) {
            LOG.severe(ex.toString());
        }
        
        return result;
    }

    /**
     * Get the adjacent vertices of this vertex
     * 
     * @param drctn
     * @param labels
     * @return 
     */
    @Override
    public Iterable<Vertex> getVertices(Direction drctn, String... labels) {
        
        List<Vertex> result = new ArrayList<>();
        
        try {
            
            this.refresh();

            //Head = In
            //Tail = OUT
            Iterable<Edge> edges = getEdges(drctn, labels);
            
            for (Edge e : edges) {
                
                if (drctn.equals(Direction.OUT) || drctn.equals(Direction.IN)) {
                    result.add(e.getVertex(drctn));
                }

                //All edges those are connected to this node, to determine the connected
                //vertex, we have to ignore the original vertex with the exception
                //that there is a self loop
                if (drctn.equals(Direction.BOTH)) {
                    Vertex v_in = e.getVertex(Direction.IN);
                    Vertex v_out = e.getVertex(Direction.OUT);

                    //Self loop
                    if (v_in.getId().equals(this.id) && v_out.getId().equals(this.id)) {
                        result.add(this);
                    } else {
                        //Outgoing edge
                        if (v_in.getId().equals(this.id)) {
                            result.add(v_out);
                        }

                        //Incoming edge
                        if (v_out.getId().equals(this.id)) {
                            result.add(v_in);
                        }
                    }
                }                
            }
        } catch (DocNotFoundException e) {
            LOG.severe(e.toString());
        }
        
        return result;
    }

    /**
     * Not yet supported, because we ignore Indexing for now
     * 
     * @return 
     */
    @Override
    public VertexQuery query() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * To add an edge to this vertex
     * 
     * @param label
     * @param vertex
     * @return 
     */
    @Override
    public Edge addEdge(String label, Vertex vertex) {
        
        return graph.addEdge(null , this, vertex, label);
    }
        
    /**
     * To add an edge key to the vertex's adjacency list
     * 
     * @param label
     * @param edgeKey
     * @param drctn 
     */
    public void addEdgeToAdjacencyList(String label, String edgeKey, Direction drctn)
    { 
        try {
            refresh();

            //TODO: This could be abit more beautiful, just a code duplication
            if (drctn.equals(Direction.OUT) || drctn.equals(Direction.BOTH)) {
                JSONArray labeldOutEdges = (JSONArray) innerOutgoingEdges.get(label);
                
                if (labeldOutEdges == null)
                {
                    labeldOutEdges = new JSONArray();
                    
                    innerOutgoingEdges.put(label, labeldOutEdges);
                }
                
                labeldOutEdges.add(edgeKey);
            }

            if (drctn.equals(Direction.IN) || drctn.equals(Direction.BOTH)) {
                JSONArray labeledInEdges = (JSONArray) innerIncomingEdges.get(label);
                
                if (labeledInEdges  == null)
                {
                    labeledInEdges = new JSONArray();
                    
                    innerIncomingEdges.put(label, labeledInEdges);
                }
                
                labeledInEdges.add(edgeKey);
            }

            client.replace(cbKey, innerObj.toJSONString());

        } catch (DocNotFoundException e) {
            
            LOG.severe(e.toString());
        }
    }
    
    /**
     * To remove an edge from the vertexes adjacency list
     * @param label
     * @param edgeKey
     * @param drctn 
     */
    public void removeEdgeFromAdjacencyList(String label, String edgeKey, Direction drctn)
    {
         try {
            refresh();

            if (drctn.equals(Direction.OUT) || drctn.equals(Direction.BOTH)) {
                JSONArray labeldOutEdges = (JSONArray) innerOutgoingEdges.get(label);
                labeldOutEdges.remove(edgeKey);
            }

            if (drctn.equals(Direction.IN) || drctn.equals(Direction.BOTH)) {
                JSONArray labeledInEdges = (JSONArray) innerIncomingEdges.get(label);
                labeledInEdges.remove(edgeKey);
            }

            client.replace(cbKey, innerObj.toJSONString());

        } catch (DocNotFoundException e) {
            LOG.severe(e.toString());
        }
    }

    /**
     * Overrides the refresh method by making sure that the inner JSON object
     * of the edges is taken into account
     * 
     * @throws  com.couchbase.graph.error.DocNotFoundException
     * @return 
     */
    @Override
    public boolean refresh() throws DocNotFoundException {
                
        if (super.refresh())
        {
            this.innerEdges = (JSONObject) innerObj.get(CBModel.PROP_EDGES);
            this.innerOutgoingEdges = (JSONObject) innerEdges.get(CBModel.PROP_EDGES_OUT);
            this.innerIncomingEdges = (JSONObject) innerEdges.get(CBModel.PROP_EDGES_IN);
            
            return true;
        }
            
        return false;
    }

    /**
     * If we remove a vertex we have also to remove the edges those are pointing to this vertex
     * 
     * Let's say that we have:
     * 
     * x -> y
     * . -> z
     * 
     * To delete y would cause:
     * 
     * x -> 
     * . -> z
     * 
     * This is an insane state of edge x -> y. This means that we have to delete the edge x -> y but not the edge x -> z
     * 
     */
    @Override
    public void remove() {
        
        
        Iterable<Edge> allEdges = getAllEdges();
    
        for (Edge edge : allEdges) {
            
            edge.remove();
        }
        
        
        super.remove(); 
    }

    
    
    
    
    /**
     * To generate the key of a vertex by using the next generated id
     * 
     * @return 
     * @throws com.couchbase.graph.error.IdGenException 
     */
    public static String genVertexKey() throws IdGenException
    {
            genVertexId();
            return CBModel.VERTEX_KEY.replace("{1}", String.valueOf(innerIdCounter));
    }
    
    /**
     * To generate a vertex id
     * 
     * @return
     * @throws IdGenException 
     */
    public static long genVertexId() throws IdGenException
    {
        try {
            //TODO: Does not work, why
            //client.add(CBModel.VERTEX_COUNTER_KEY, 0).get();
            
            innerIdCounter = client.incr(CBModel.VERTEX_COUNTER_KEY, 1);
            
            if (innerIdCounter == -1)
            {
                innerIdCounter = 0;
                client.add(CBModel.VERTEX_COUNTER_KEY, "" + 0).get();
            }
            
            return innerIdCounter;

        } catch (InterruptedException | ExecutionException e) {
           
            throw new IdGenException(e);
        }
    }
    
    /**
     * To generate the key based on the id of a vertex
     * 
     * @param id
     * @return 
     */
    public static String genVertexKey(Object id)
    {
          return CBModel.VERTEX_KEY.replace("{1}", id.toString());
    }
    
    /**
     * Returns the id of the vertex by parsing the key
     *
     * @param vKey
     * @return 
     */
    public static Object parseVertexKey(String vKey)
    {
        return vKey.substring(CBModel.VERTEX_PREFIX.length());
    }
}
