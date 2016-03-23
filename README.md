# CBGraph - A Graph API for Couchbase

This is an implementation of Tinkerpop's Graph API for Couchbase

> The following shows a demo application which uses CBGraph

![alt tag](https://raw.github.com/dmaier-couchbase/cb-graph-viz/master/screenshot.png)


## Configuration

You configure your Couchbase access by using a couchbase.properties file:

```
cb.con.hosts=192.168.56.104,192.168.56.105,192.168.56.106
cb.con.port=8091
cb.con.bucket.name=graph
cb.con.bucket.pwd=test
cb.timeout.op=30000
cb.admin.user=couchbase
cb.admin.pwd=couchbase
cb.view.designdoc=graph_views
cb.view.alledges=all_edges
cb.view.allvertices=all_vertices
```

## Add vertices

All you need is a new CBGraph instance in order to start adding vertices:

```
Graph graph = new CBGraph();

Vertex tavwp_bart = graph.addVertex("tavwp_bart");
        
tavwp_bart.setProperty("first_name", "Bart");
tavwp_bart.setProperty("last_name", "Simpson");
tavwp_bart.setProperty("city", "Springfield");
tavwp_bart.setProperty("age", 8);
tavwp_bart.setProperty("is_student", true);
```

## Get vertices

Use the CBGraph instance to get your vertex by it's id.

```
Vertex v_1_2 = graph.getVertex("tgv_1");
```

## Add edges

Here a code example how to add edges to vertices:

```
Vertex v_tae_moe = graph.addVertex("tae_moe");
Vertex v_tae_barney = graph.addVertex("tae_barney");
         
Edge e_1 = graph.addEdge("e_1", v_tae_barney, v_tae_moe, "guest of");
```
## Follow edges

Here a simple example how to follow edges in order to find vertices:

```
Iterable<Vertex> guests = v_tae_moe.getVertices(Direction.OUT, "guest of");
```


## How it is stored in Couchbase

The data is stored as JSON documents in Couchbase. The format is human readable.

This is the key of a vertex:

```
v_tavwp_bart
```

This is the key of an edge:

```
e_tae_barney->|guest of|->tae_moe
```

This is the JSON string of a vertex:

```
{
        "edges":{
                     "in":{},
                     "out":{}
        },
        "type": "vertex",
        "props":{
                "city":"Springfield",
                "last_name":"Simpson",
                "first_name":"Bart",
                "is_student":true,
                "age":8
        }
}
```

This is the JSON string of a vertex with edges:

```
{
        "edges":{
                "in": {
                        "son of": ["e_tae_bart->|son of|->tae_homer"]
                },
                "out":{}
        },
        "type": "vertex",
        "props": {}
}
```

This is the JSON stirng of an edge:

```
{
        "to": "v_tae_homer",
        "label": "son of",
        "from":
        "v_tae_bart",
        "type": "edge"
}
```

## Compression

The latest version of CBGraph supports adjacency list compression. Vertices can become quite big if they have a huge amount of incoming or outgoing edges (such a vertex is called a supernode). One of the limitations which such a supernode introduces is that it just takes longer to transfer a e.g. a 10MB vertex over the wire than e.g. a 1KB one. In order support such supernodes better by reducing the network latency, two optimization steps were introduced for CBGraph.

1. Compress the adjacency lists by still storing it at the vertex (as base64 string). The base64 encoding causes that the lists are taking a bit more space for small vertices but you save a lot (saw up to 50% with UUID-s as vertex id-s) for supernodes.
2. Externalize and compress the adjacency list as a binary (How much can be saved depends on the gzip compression, but it's even better than with embedding)


> Important:
> A Graph which was previously created uncompressed can not be handled if compression is enabled later and vice versa. 
> So the decision which compression mode should be used is a life time decision.
> The recommendation is to load your data again into a second compressed graph.

There are the following switches in the 'graph.properties' file:

* graph.compression.enabled
* graph.compression.binary

Here the settings for the different modes:

* *(1)* The following means that compression is disabled:

```
graph.compression.enabled=false
```

* *(2)* The following would enable compressed adjacency lists by storing them at the vertex as base64 string:

```
graph.compression.enabled=true
graph.compression.binary=false
```

* *(3)* The following would enable compressed adjacency lists by storing them as seperated binary documents:

```
graph.compression.enabled=true
graph.compression.binary=false
```

The inner (physical) document model then changes dependent on your choice:

* *(1)* Adjacency lists as embedded JSON documents

```
"v_$id" : {
...
        "edges" : {...}
...
}
```

* *(2)* Adjacency lists as embedded String (gzipped and base64 encoded)

```
"v_$id ": {
...
        "edges" : "base64 encoded string"
...
}
```

* *(3)* Adjacency lists are externalized and compressed as binary (gzipped)

```
"v_$id" : {
...
        "edges" : "al_$id"
...
}

"al_$id" : $bin
```

Which mode is the prefered one depends. Mode 1 allows you to access you the underlying documents in a more human readable form. If you have supernodes then option 3 brings the most benefit because you save the base64 encoding overhead (regarding space).
