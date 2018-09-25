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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.ConfigurationContext;

//import org.apache.nifi.distributed.cache.client.DistributedMapCacheClient;
import org.apache.nifi.distributed.cache.client.Serializer;
import org.apache.nifi.reporting.InitializationException;

import java.nio.charset.StandardCharsets;
import org.apache.nifi.hbase.HBaseClientService;
import org.apache.nifi.hbase.HBase_1_1_2_ClientMapCacheService;
import org.apache.nifi.hbase.put.PutColumn;
import org.bpenelli.nifi.services.HBaseMapCacheClient;


@Tags({"cache", "state", "map", "cluster", "hbase", "bpenelli"})
@SeeAlso(classNames = {"org.apache.nifi.hbase.HBase_1_1_2_ClientService"})
@CapabilityDescription("Provides the ability to use an HBase table as a cache. "
	+ " Unlike a DistributedMapCache, this provides a checkAndPut method, offering the ability to perform concurrency safe update operations."
    + " Uses a HBase_1_1_2_ClientService controller to communicate with HBase.")

public class HBaseMapCacheClientService extends HBase_1_1_2_ClientMapCacheService implements HBaseMapCacheClient {

	// Other threads may call @OnEnabled so these are marked volatile to ensure other class methods read the updated value
	private volatile HBaseClientService hbaseClientService;
    private volatile String hBaseCacheTableName;
    private volatile String hBaseColumnFamily;
    private volatile byte[] hBaseColumnFamilyBytes;
    private volatile String hBaseColumnQualifier;
    private volatile byte[] hBaseColumnQualifierBytes;

    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException{
    	for (PropertyDescriptor item : context.getProperties().keySet()) {
    		if (item.getName().equals("HBase Client Service")) {
    			this.hbaseClientService = context.getProperty(item).asControllerService(HBaseClientService.class);
    		} else if (item.getName().equals("HBase Cache Table Name")) {
    			this.hBaseCacheTableName = context.getProperty(item).evaluateAttributeExpressions().getValue();
    		} else if (item.getName().equals("HBase Column Family")) {
    			this.hBaseColumnFamily = context.getProperty(item).evaluateAttributeExpressions().getValue();
    		} else if (item.getName().equals("HBase Column Qualifier")) {
    			this.hBaseColumnQualifier = context.getProperty(item).evaluateAttributeExpressions().getValue();
    		}
    	}
        hBaseColumnFamilyBytes = this.hBaseColumnFamily.getBytes(StandardCharsets.UTF_8);
        hBaseColumnQualifierBytes = this.hBaseColumnQualifier.getBytes(StandardCharsets.UTF_8);
    }

    private <T> byte[] serialize(final T value, final Serializer<T> serializer) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(value, baos);
        return baos.toByteArray();
    }

    @Override
    public <K, V> boolean checkAndPut(final K key, final V value, final V checkValue, final Serializer<K> keySerializer, final Serializer<V> valueSerializer) throws IOException {

        final byte[] rowIdBytes = serialize(key, keySerializer);
        final byte[] valueBytes = serialize(value, valueSerializer);
        final byte[] checkBytes = serialize(checkValue, valueSerializer);
        final PutColumn putColumn = new PutColumn(hBaseColumnFamilyBytes, hBaseColumnQualifierBytes, valueBytes);

        return this.hbaseClientService.checkAndPut(hBaseCacheTableName, rowIdBytes, hBaseColumnFamilyBytes, hBaseColumnQualifierBytes, checkBytes, putColumn);
    }
}