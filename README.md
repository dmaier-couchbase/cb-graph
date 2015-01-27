# A Graph API for Couchbase

This is an implementation of Tinkerpop's Graph API for Couchbase

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
## Folloe edges

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
{\"edges\":{\"in\":{},\"out\":{}},\"type\":\"vertex\",\"props\":{\"city\":\"Springfield\",\"last_name\":\"Simpson\",\"first_name\":\"Bart\",\"is_student\":true,\"age\":8}}
```

This is the JSON string of a vertex with edges:

```
{\"edges\":{\"in\":{\"son of\":[\"e_tae_bart->|son of|->tae_homer\"]},\"out\":{}},\"type\":\"vertex\",\"props\":{}}
```

This is the JSON stirng of an edge:

```
{\"to\":\"v_tae_homer\",\"label\":\"son of\",\"from\":\"v_tae_bart\",\"type\":\"edge\"}
```
