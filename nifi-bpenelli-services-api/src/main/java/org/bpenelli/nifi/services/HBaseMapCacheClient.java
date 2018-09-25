/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bpenelli.nifi.services;

import java.io.IOException;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.distributed.cache.client.DistributedMapCacheClient;
import org.apache.nifi.distributed.cache.client.Serializer;

/**
 * This interface defines an API that can be used for interacting with a
 * Distributed Cache that functions similarly to a {@link java.util.Map Map}.
 *
 */
@Tags({"client", "cluster", "map", "cache", "hbase", "bpenelli"})
@CapabilityDescription("Provides the ability to communicate with a DistributedMapCacheServer. This allows "
        + "multiple nodes to coordinate state with a single remote entity.")
public interface HBaseMapCacheClient extends DistributedMapCacheClient {

	/**
     * Updates the value of the specified key, but only if
     * the current value is the same as checkValue, serializing
     * the value with the given
     * {@link Serializer}s.
     *
     * @param <K> type of key
     * @param <V> type of value and checkValue
     * @param key the key of the map entry to update
     * @param value the value to update to the map if and only if current 
     * value = checkValue
     * @param keySerializer key serializer
     * @param valueSerializer value serializer
     * @return true if the value was updated to the cache, false if the 
     * current value in the cache doesn't match checkValue
     *
     * @throws IOException if unable to communicate with the remote instance
     */

	<K, V> boolean checkAndPut(final K key, final V value, final V checkValue, final Serializer<K> keySerializer, final Serializer<V> valueSerializer) throws IOException;    
}