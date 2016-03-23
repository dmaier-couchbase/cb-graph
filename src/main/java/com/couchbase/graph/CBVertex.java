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

import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.buffer.Unpooled;
import com.couchbase.client.java.document.BinaryDocument;
import static com.couchbase.graph.views.ViewManager.*;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.couchbase.graph.cfg.ConfigManager;
import com.couchbase.graph.cfg.GraphConfig;
import com.couchbase.graph.error.DocNotFoundException;
import com.couchbase.graph.error.IdGenException;
import com.couchbase.graph.helper.CollectionHelper;
import com.couchbase.graph.helper.JSONHelper;
import com.couchbase.graph.helper.ZipHelper;
import com.couchbase.graph.views.ViewManager;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.DefaultVertexQuery;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ListSelectionEvent;
import org.apache.commons.lang.time.StopWatch;
import rx.Observable;

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
     * The associated Graph configuration
     */
    private GraphConfig cfg = ConfigManager.getGraphConfig();
    
    /**
     * The edges those are belonging to this vertex
     */
    private JsonObject innerEdges;
    
    /**
     * The inner outgoing edges
     */
    private JsonObject innerOutgoingEdges;
    
    /**
     * The inner incoming edges
     */
    private JsonObject innerIncomingEdges;
    
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
       
        //If no label is given then get the edges for all labels
        if (labels.length == 0) labels = CBEdge.queryAllEdgeLabels().toArray(new String[]{});
        
        try
        { 
            this.refresh();

            for (String label : labels) {

                if (drctn.equals(Direction.IN) || drctn.equals(Direction.BOTH)) {
                    
                    JsonArray labeledInEdges = innerIncomingEdges.getArray(label);

                    result.addAll(edgesFromJsonArr(result, labeledInEdges, graph));
                }

                if (drctn.equals(Direction.OUT) || drctn.equals(Direction.BOTH)) {
                    JsonArray labeledOutEdges = innerOutgoingEdges.getArray(label);

                    result.addAll(edgesFromJsonArr(result, labeledOutEdges, graph));
                }

            }

        }
        catch (DocNotFoundException e)
        {
            LOG.severe(e.toString());
        }
        
        return result;
    }
    
    /**
     * Iterate over an edge array by returning a list of edges
     * @param result
     * @param edgeArr
     * @param graph
     * @return
     * @throws DocNotFoundException 
     */
    private List<Edge> edgesFromJsonArr(List<Edge> result, JsonArray edgeArr, Graph graph) throws DocNotFoundException
    {
        StopWatch sw = new StopWatch();
        sw.start();
                
         if (edgeArr != null )
         {
             //Multi-get instead of multiple gets
             Iterator<Edge>  it = Observable.from(edgeArr.toList())
                    .flatMap(key -> client.async().get(key.toString()))
                    .map(doc -> CBEdge.fromJson(doc.content(), graph)).toBlocking().getIterator();
            
             return CollectionHelper.copyIterator(it);
         }
        
         sw.stop();
         
         LOG.log(Level.FINEST, "Time to get edges: {0}", sw.getTime());
         
         return result;
    }
    
    
    public Iterable<Edge> getAllEdges() {
        
        List<Edge> result = new ArrayList<>();
        
        try {
            
            refresh();

            Set<String> labels = innerIncomingEdges.getNames();
            
            for (String label : labels) {
             
                JsonArray edgeArray = innerIncomingEdges.getArray(label);
                
                result = edgesFromJsonArr(result, edgeArray, graph);
                
            }
            
            
            labels = innerOutgoingEdges.getNames();
            
            for (String label : labels) {
             
                JsonArray edgeArray = innerOutgoingEdges.getArray(label);
                
                result = edgesFromJsonArr(result, edgeArray, graph);
                
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
                    
                    result.add(e.getVertex(invertDirection(drctn)));
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
                        
                        //Incoming edge
                        if (v_in.getId().equals(this.id)) {
                            result.add(v_out);
                        }

                        //Outgoing edge
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

    public Direction invertDirection(Direction dir) {
        
        if (dir.equals(Direction.IN)) return Direction.OUT;
        if (dir.equals(Direction.OUT)) return Direction.IN;
        
        return Direction.BOTH;
    }
    
    
    /**
     * Not yet supported, because we ignore Indexing for now
     * 
     * @return 
     */
    @Override
    public VertexQuery query() {
        
        return new DefaultVertexQuery(this);
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
    public void addEdgeToAdjacencyList(String label, String edgeKey, Direction drctn) throws ZipHelper.CompressionException
    { 
        try {
            refresh();

            //TODO: This could be abit more beautiful, just a code duplication
            if (drctn.equals(Direction.OUT) || drctn.equals(Direction.BOTH)) {
                JsonArray labeldOutEdges = innerOutgoingEdges.getArray(label);
                
                if (labeldOutEdges == null)
                {
                    labeldOutEdges = JsonArray.empty();
                    
                    innerOutgoingEdges.put(label, labeldOutEdges);
                }
                
                labeldOutEdges.add(edgeKey);
            }

            if (drctn.equals(Direction.IN) || drctn.equals(Direction.BOTH)) {
                JsonArray labeledInEdges = innerIncomingEdges.getArray(label);
                
                if (labeledInEdges  == null)
                {
                    labeledInEdges = JsonArray.empty();
                    
                    innerIncomingEdges.put(label, labeledInEdges);
                }
                
                labeledInEdges.add(edgeKey);
            }

            JsonObject toReplace = compressEdges();
            client.replace(JsonDocument.create(cbKey, toReplace));

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
    public void removeEdgeFromAdjacencyList(String label, String edgeKey, Direction drctn) throws ZipHelper.CompressionException
    {
         try {
            refresh();

            if (drctn.equals(Direction.OUT) || drctn.equals(Direction.BOTH)) {
                JsonArray labeldOutEdges = innerOutgoingEdges.getArray(label);
                labeldOutEdges = JSONHelper.remove(labeldOutEdges, edgeKey);  
                
                if (labeldOutEdges.size() != 0)
                    innerOutgoingEdges.put(label, labeldOutEdges);
                else
                    innerOutgoingEdges.removeKey(label);
            }

            if (drctn.equals(Direction.IN) || drctn.equals(Direction.BOTH)) {
                JsonArray labeledInEdges = innerIncomingEdges.getArray(label);
                labeledInEdges = JSONHelper.remove(labeledInEdges, edgeKey);
                
                if (labeledInEdges.size() != 0)
                    innerIncomingEdges.put(label, labeledInEdges);
                else
                    innerIncomingEdges.removeKey(label);
            }

            JsonObject toReplace = compressEdges();
            client.replace(JsonDocument.create(cbKey, toReplace));

        } catch (DocNotFoundException e) {
            LOG.severe(e.toString());
        }
    }
    
    /**
     * If compression enabled then the edge lists need to be compressed and
     * stored as a string. If compression is disabled then this method just
     * returns the innerObj without doing the decompression.
     *
     * @return
     * @throws com.couchbase.graph.helper.ZipHelper.CompressionException
     */
    public JsonObject compressEdges() throws ZipHelper.CompressionException {

        JsonObject result = innerObj;

        if (cfg.isCompressionEnabled()) {

            byte[] compr = ZipHelper.compress(innerEdges.toString());
            
            if (!cfg.isCompressedAsBinary()) {
                
                String innerEdgesStr = ZipHelper.comprBytesToString(compr);
                result.put(CBModel.PROP_EDGES, innerEdgesStr);
               
            } else {
             
                String alKey = genAdjacencyListKey(id);
                BinaryDocument al = BinaryDocument.create(alKey, Unpooled.copiedBuffer(compr));
                client.upsert(al);
                result.put(CBModel.PROP_EDGES, alKey);
            }
        }

        return result;
    }

    
    /**
     * If compression is enabled then the edge lists are compressed and need to
     * be decompressed before. If compression is disabled, then this method will
     * just return the already decompressed edge list.
     *
     * @return
     * @throws com.couchbase.graph.helper.ZipHelper.DecompressionException
     */
    public JsonObject decompressEdges() throws ZipHelper.DecompressionException, IOException {

        JsonObject result;

        Object edgeLists = innerObj.get(CBModel.PROP_EDGES);

        if (cfg.isCompressionEnabled()) {

            byte[] compr;
            
            //The edge lists are then encoded as a string
            if (!cfg.isCompressedAsBinary()) {
            
                String comprStr = (String) edgeLists;
                compr = ZipHelper.comprStringToBytes(comprStr);

            }
            else {
                
                BinaryDocument al = client.get((String) edgeLists,BinaryDocument.class);
                ByteBuf buffer = al.content();
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
                for (int i = 0; i < buffer.capacity(); i ++) {
                    byte b = buffer.getByte(i);
                    baos.write(b);
                }
                
                baos.close();
                
                compr = baos.toByteArray();          
            }
            
           String decompr = ZipHelper.decompress(compr);
           result = JsonObject.fromJson(decompr);

        } else {

            result = (JsonObject) edgeLists;
        }

        return result;
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

            try {
            
                this.innerEdges =  decompressEdges();
            
            } catch (ZipHelper.DecompressionException | IOException ex) {
                
                LOG.severe(ex.toString());
                return false;                
            }
            
            this.innerOutgoingEdges = innerEdges.getObject(CBModel.PROP_EDGES_OUT);
            this.innerIncomingEdges = innerEdges.getObject(CBModel.PROP_EDGES_IN);
            
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
     * To generate the key of the adjacency list which belongs to this vertex
     * 
     * @return
     * @throws IdGenException 
     */
    public static String genAdjacencyListKey() throws IdGenException
    {
        genVertexId();
        return CBModel.AL_KEY.replace("{1}", String.valueOf(innerIdCounter));
    }
    
    /**
     * To generate a vertex id
     * 
     * @return
     * @throws IdGenException 
     */
    public static long genVertexId() throws IdGenException
    {
        try 
        {
            innerIdCounter = client.counter(CBModel.VERTEX_COUNTER_KEY, 1).content();
            return innerIdCounter;
        }
        catch(Exception e)
        {
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
     * To generate the key of the adjacency list which belongs to this vertex
     * 
     * @param id
     * @return 
     */
    public static String genAdjacencyListKey(Object id) {
    
        return CBModel.AL_KEY.replace("{1}", id.toString());
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
    
    
     /**
     * Queries all vertices
     * 
     * @param graph
     * @return 
     * @throws com.couchbase.graph.error.DocNotFoundException 
     */
    public static List<Vertex> queryAllVertices(Graph graph) throws DocNotFoundException
    {
        List<Vertex> result = new ArrayList<>();
                
        
        ViewResult viewResult = ViewManager.query(DESIGN_DOC, getAllVerticesViewDef().name(), null, null);
        
        for (ViewRow viewRow : viewResult) {
            
            result.add(new CBVertex(viewRow.id(), graph));
        }
        
        return result;
    }
    
    /**
     * Queries for a specific property
     * @param key
     * @param value
     * @param graph
     * @return 
     * @throws com.couchbase.graph.error.DocNotFoundException 
     */
    public static List<Vertex> queryByVertexProp(String key, String value, Graph graph) throws DocNotFoundException
    {
        List<Vertex> result = new ArrayList<>();
        
        ViewResult viewResult = ViewManager.query(DESIGN_DOC, getAllVertexPropsViewDef().name(),genCompKey(key, value), genCompKey(key, value));
        
        for (ViewRow viewRow : viewResult) {
            
            result.add(new CBVertex(viewRow.id(), graph));
        }
        
        return result;
        
    }
    
   
}
