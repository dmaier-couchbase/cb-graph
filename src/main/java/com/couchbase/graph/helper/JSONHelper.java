/*
 * Copyright 2015 Couchbase, Inc.
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

package com.couchbase.graph.helper;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Just some additional helper methods for JSON
 * 
 * @author David Maier <david.maier at couchbase.com>
 */
public class JSONHelper {
    
    
    /**
     * Takes a JSON object as the input and returns a sorted map which represents
     * the JSON object
     * 
     * @param obj
     * @return 
     */
    public static Map sort(JsonObject obj)
    {
        
        Map<String, Object> map = obj.toMap();

        TreeMap<String, Object> treeMap = CollectionHelper.sortMap(map);
        
        return treeMap;
        
    }
    
    /**
     * Removes an object from a JsonArray
     * @param in
     * @param obj
     * @return 
     */
   public static JsonArray remove(JsonArray in, Object obj)
   {
       List<Object> list = in.toList();
       
       list.remove(obj);
       
       return JsonArray.from(list);
       
   }
}
