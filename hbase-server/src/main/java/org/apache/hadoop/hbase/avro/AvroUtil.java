begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Schema
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|generic
operator|.
name|GenericArray
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|generic
operator|.
name|GenericData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|util
operator|.
name|Utf8
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ClusterStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HColumnDescriptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HServerAddress
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HServerLoad
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HTableDescriptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|KeyValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ServerName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AClusterStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AColumn
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AColumnValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|ACompressionAlgorithm
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|ADelete
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AFamilyDescriptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AGet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIllegalArgument
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|APut
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|ARegionLoad
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AResult
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AResultEntry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AScan
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AServerAddress
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AServerInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AServerLoad
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|ATableDescriptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Delete
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Get
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Put
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Result
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|Scan
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|io
operator|.
name|hfile
operator|.
name|Compression
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_class
annotation|@
name|Deprecated
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|AvroUtil
block|{
comment|//
comment|// Cluster metadata
comment|//
specifier|static
specifier|public
name|AServerAddress
name|hsaToASA
parameter_list|(
name|HServerAddress
name|hsa
parameter_list|)
throws|throws
name|IOException
block|{
name|AServerAddress
name|asa
init|=
operator|new
name|AServerAddress
argument_list|()
decl_stmt|;
name|asa
operator|.
name|hostname
operator|=
operator|new
name|Utf8
argument_list|(
name|hsa
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
name|asa
operator|.
name|inetSocketAddress
operator|=
operator|new
name|Utf8
argument_list|(
name|hsa
operator|.
name|getInetSocketAddress
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|asa
operator|.
name|port
operator|=
name|hsa
operator|.
name|getPort
argument_list|()
expr_stmt|;
return|return
name|asa
return|;
block|}
specifier|static
specifier|public
name|ARegionLoad
name|hrlToARL
parameter_list|(
name|HServerLoad
operator|.
name|RegionLoad
name|rl
parameter_list|)
throws|throws
name|IOException
block|{
name|ARegionLoad
name|arl
init|=
operator|new
name|ARegionLoad
argument_list|()
decl_stmt|;
name|arl
operator|.
name|memStoreSizeMB
operator|=
name|rl
operator|.
name|getMemStoreSizeMB
argument_list|()
expr_stmt|;
name|arl
operator|.
name|name
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|rl
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|arl
operator|.
name|storefileIndexSizeMB
operator|=
name|rl
operator|.
name|getStorefileIndexSizeMB
argument_list|()
expr_stmt|;
name|arl
operator|.
name|storefiles
operator|=
name|rl
operator|.
name|getStorefiles
argument_list|()
expr_stmt|;
name|arl
operator|.
name|storefileSizeMB
operator|=
name|rl
operator|.
name|getStorefileSizeMB
argument_list|()
expr_stmt|;
name|arl
operator|.
name|stores
operator|=
name|rl
operator|.
name|getStores
argument_list|()
expr_stmt|;
return|return
name|arl
return|;
block|}
specifier|static
specifier|public
name|AServerLoad
name|hslToASL
parameter_list|(
name|HServerLoad
name|hsl
parameter_list|)
throws|throws
name|IOException
block|{
name|AServerLoad
name|asl
init|=
operator|new
name|AServerLoad
argument_list|()
decl_stmt|;
name|asl
operator|.
name|load
operator|=
name|hsl
operator|.
name|getLoad
argument_list|()
expr_stmt|;
name|asl
operator|.
name|maxHeapMB
operator|=
name|hsl
operator|.
name|getMaxHeapMB
argument_list|()
expr_stmt|;
name|asl
operator|.
name|memStoreSizeInMB
operator|=
name|hsl
operator|.
name|getMemStoreSizeInMB
argument_list|()
expr_stmt|;
name|asl
operator|.
name|numberOfRegions
operator|=
name|hsl
operator|.
name|getNumberOfRegions
argument_list|()
expr_stmt|;
name|asl
operator|.
name|numberOfRequests
operator|=
name|hsl
operator|.
name|getNumberOfRequests
argument_list|()
expr_stmt|;
name|Collection
argument_list|<
name|HServerLoad
operator|.
name|RegionLoad
argument_list|>
name|regionLoads
init|=
name|hsl
operator|.
name|getRegionsLoad
argument_list|()
operator|.
name|values
argument_list|()
decl_stmt|;
name|Schema
name|s
init|=
name|Schema
operator|.
name|createArray
argument_list|(
name|ARegionLoad
operator|.
name|SCHEMA$
argument_list|)
decl_stmt|;
name|GenericData
operator|.
name|Array
argument_list|<
name|ARegionLoad
argument_list|>
name|aregionLoads
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|regionLoads
operator|!=
literal|null
condition|)
block|{
name|aregionLoads
operator|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|ARegionLoad
argument_list|>
argument_list|(
name|regionLoads
operator|.
name|size
argument_list|()
argument_list|,
name|s
argument_list|)
expr_stmt|;
for|for
control|(
name|HServerLoad
operator|.
name|RegionLoad
name|rl
range|:
name|regionLoads
control|)
block|{
name|aregionLoads
operator|.
name|add
argument_list|(
name|hrlToARL
argument_list|(
name|rl
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|aregionLoads
operator|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|ARegionLoad
argument_list|>
argument_list|(
literal|0
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
name|asl
operator|.
name|regionsLoad
operator|=
name|aregionLoads
expr_stmt|;
name|asl
operator|.
name|storefileIndexSizeInMB
operator|=
name|hsl
operator|.
name|getStorefileIndexSizeInMB
argument_list|()
expr_stmt|;
name|asl
operator|.
name|storefiles
operator|=
name|hsl
operator|.
name|getStorefiles
argument_list|()
expr_stmt|;
name|asl
operator|.
name|storefileSizeInMB
operator|=
name|hsl
operator|.
name|getStorefileSizeInMB
argument_list|()
expr_stmt|;
name|asl
operator|.
name|usedHeapMB
operator|=
name|hsl
operator|.
name|getUsedHeapMB
argument_list|()
expr_stmt|;
return|return
name|asl
return|;
block|}
specifier|static
specifier|public
name|AServerInfo
name|hsiToASI
parameter_list|(
name|ServerName
name|sn
parameter_list|,
name|HServerLoad
name|hsl
parameter_list|)
throws|throws
name|IOException
block|{
name|AServerInfo
name|asi
init|=
operator|new
name|AServerInfo
argument_list|()
decl_stmt|;
name|asi
operator|.
name|infoPort
operator|=
operator|-
literal|1
expr_stmt|;
name|asi
operator|.
name|load
operator|=
name|hslToASL
argument_list|(
name|hsl
argument_list|)
expr_stmt|;
name|asi
operator|.
name|serverAddress
operator|=
name|hsaToASA
argument_list|(
operator|new
name|HServerAddress
argument_list|(
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|,
name|sn
operator|.
name|getPort
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|asi
operator|.
name|serverName
operator|=
operator|new
name|Utf8
argument_list|(
name|sn
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|asi
operator|.
name|startCode
operator|=
name|sn
operator|.
name|getStartcode
argument_list|()
expr_stmt|;
return|return
name|asi
return|;
block|}
specifier|static
specifier|public
name|AClusterStatus
name|csToACS
parameter_list|(
name|ClusterStatus
name|cs
parameter_list|)
throws|throws
name|IOException
block|{
name|AClusterStatus
name|acs
init|=
operator|new
name|AClusterStatus
argument_list|()
decl_stmt|;
name|acs
operator|.
name|averageLoad
operator|=
name|cs
operator|.
name|getAverageLoad
argument_list|()
expr_stmt|;
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|deadServerNames
init|=
name|cs
operator|.
name|getDeadServerNames
argument_list|()
decl_stmt|;
name|Schema
name|stringArraySchema
init|=
name|Schema
operator|.
name|createArray
argument_list|(
name|Schema
operator|.
name|create
argument_list|(
name|Schema
operator|.
name|Type
operator|.
name|STRING
argument_list|)
argument_list|)
decl_stmt|;
name|GenericData
operator|.
name|Array
argument_list|<
name|CharSequence
argument_list|>
name|adeadServerNames
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|deadServerNames
operator|!=
literal|null
condition|)
block|{
name|adeadServerNames
operator|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|CharSequence
argument_list|>
argument_list|(
name|deadServerNames
operator|.
name|size
argument_list|()
argument_list|,
name|stringArraySchema
argument_list|)
expr_stmt|;
for|for
control|(
name|ServerName
name|deadServerName
range|:
name|deadServerNames
control|)
block|{
name|adeadServerNames
operator|.
name|add
argument_list|(
operator|new
name|Utf8
argument_list|(
name|deadServerName
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|adeadServerNames
operator|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|CharSequence
argument_list|>
argument_list|(
literal|0
argument_list|,
name|stringArraySchema
argument_list|)
expr_stmt|;
block|}
name|acs
operator|.
name|deadServerNames
operator|=
name|adeadServerNames
expr_stmt|;
name|acs
operator|.
name|deadServers
operator|=
name|cs
operator|.
name|getDeadServers
argument_list|()
expr_stmt|;
name|acs
operator|.
name|hbaseVersion
operator|=
operator|new
name|Utf8
argument_list|(
name|cs
operator|.
name|getHBaseVersion
argument_list|()
argument_list|)
expr_stmt|;
name|acs
operator|.
name|regionsCount
operator|=
name|cs
operator|.
name|getRegionsCount
argument_list|()
expr_stmt|;
name|acs
operator|.
name|requestsCount
operator|=
name|cs
operator|.
name|getRequestsCount
argument_list|()
expr_stmt|;
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|hserverInfos
init|=
name|cs
operator|.
name|getServers
argument_list|()
decl_stmt|;
name|Schema
name|s
init|=
name|Schema
operator|.
name|createArray
argument_list|(
name|AServerInfo
operator|.
name|SCHEMA$
argument_list|)
decl_stmt|;
name|GenericData
operator|.
name|Array
argument_list|<
name|AServerInfo
argument_list|>
name|aserverInfos
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|hserverInfos
operator|!=
literal|null
condition|)
block|{
name|aserverInfos
operator|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|AServerInfo
argument_list|>
argument_list|(
name|hserverInfos
operator|.
name|size
argument_list|()
argument_list|,
name|s
argument_list|)
expr_stmt|;
for|for
control|(
name|ServerName
name|hsi
range|:
name|hserverInfos
control|)
block|{
name|aserverInfos
operator|.
name|add
argument_list|(
name|hsiToASI
argument_list|(
name|hsi
argument_list|,
name|cs
operator|.
name|getLoad
argument_list|(
name|hsi
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|aserverInfos
operator|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|AServerInfo
argument_list|>
argument_list|(
literal|0
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
name|acs
operator|.
name|serverInfos
operator|=
name|aserverInfos
expr_stmt|;
name|acs
operator|.
name|servers
operator|=
name|cs
operator|.
name|getServers
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
return|return
name|acs
return|;
block|}
comment|//
comment|// Table metadata
comment|//
specifier|static
specifier|public
name|ATableDescriptor
name|htdToATD
parameter_list|(
name|HTableDescriptor
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|ATableDescriptor
name|atd
init|=
operator|new
name|ATableDescriptor
argument_list|()
decl_stmt|;
name|atd
operator|.
name|name
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|HColumnDescriptor
argument_list|>
name|families
init|=
name|table
operator|.
name|getFamilies
argument_list|()
decl_stmt|;
name|Schema
name|afdSchema
init|=
name|Schema
operator|.
name|createArray
argument_list|(
name|AFamilyDescriptor
operator|.
name|SCHEMA$
argument_list|)
decl_stmt|;
name|GenericData
operator|.
name|Array
argument_list|<
name|AFamilyDescriptor
argument_list|>
name|afamilies
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|families
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|afamilies
operator|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|AFamilyDescriptor
argument_list|>
argument_list|(
name|families
operator|.
name|size
argument_list|()
argument_list|,
name|afdSchema
argument_list|)
expr_stmt|;
for|for
control|(
name|HColumnDescriptor
name|hcd
range|:
name|families
control|)
block|{
name|AFamilyDescriptor
name|afamily
init|=
name|hcdToAFD
argument_list|(
name|hcd
argument_list|)
decl_stmt|;
name|afamilies
operator|.
name|add
argument_list|(
name|afamily
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|afamilies
operator|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|AFamilyDescriptor
argument_list|>
argument_list|(
literal|0
argument_list|,
name|afdSchema
argument_list|)
expr_stmt|;
block|}
name|atd
operator|.
name|families
operator|=
name|afamilies
expr_stmt|;
name|atd
operator|.
name|maxFileSize
operator|=
name|table
operator|.
name|getMaxFileSize
argument_list|()
expr_stmt|;
name|atd
operator|.
name|memStoreFlushSize
operator|=
name|table
operator|.
name|getMemStoreFlushSize
argument_list|()
expr_stmt|;
name|atd
operator|.
name|rootRegion
operator|=
name|table
operator|.
name|isRootRegion
argument_list|()
expr_stmt|;
name|atd
operator|.
name|metaRegion
operator|=
name|table
operator|.
name|isMetaRegion
argument_list|()
expr_stmt|;
name|atd
operator|.
name|metaTable
operator|=
name|table
operator|.
name|isMetaTable
argument_list|()
expr_stmt|;
name|atd
operator|.
name|readOnly
operator|=
name|table
operator|.
name|isReadOnly
argument_list|()
expr_stmt|;
name|atd
operator|.
name|deferredLogFlush
operator|=
name|table
operator|.
name|isDeferredLogFlush
argument_list|()
expr_stmt|;
return|return
name|atd
return|;
block|}
specifier|static
specifier|public
name|HTableDescriptor
name|atdToHTD
parameter_list|(
name|ATableDescriptor
name|atd
parameter_list|)
throws|throws
name|IOException
throws|,
name|AIllegalArgument
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|atd
operator|.
name|name
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|atd
operator|.
name|families
operator|!=
literal|null
operator|&&
name|atd
operator|.
name|families
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|AFamilyDescriptor
name|afd
range|:
name|atd
operator|.
name|families
control|)
block|{
name|htd
operator|.
name|addFamily
argument_list|(
name|afdToHCD
argument_list|(
name|afd
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|atd
operator|.
name|maxFileSize
operator|!=
literal|null
condition|)
block|{
name|htd
operator|.
name|setMaxFileSize
argument_list|(
name|atd
operator|.
name|maxFileSize
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|atd
operator|.
name|memStoreFlushSize
operator|!=
literal|null
condition|)
block|{
name|htd
operator|.
name|setMemStoreFlushSize
argument_list|(
name|atd
operator|.
name|memStoreFlushSize
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|atd
operator|.
name|readOnly
operator|!=
literal|null
condition|)
block|{
name|htd
operator|.
name|setReadOnly
argument_list|(
name|atd
operator|.
name|readOnly
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|atd
operator|.
name|deferredLogFlush
operator|!=
literal|null
condition|)
block|{
name|htd
operator|.
name|setDeferredLogFlush
argument_list|(
name|atd
operator|.
name|deferredLogFlush
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|atd
operator|.
name|rootRegion
operator|!=
literal|null
operator|||
name|atd
operator|.
name|metaRegion
operator|!=
literal|null
operator|||
name|atd
operator|.
name|metaTable
operator|!=
literal|null
condition|)
block|{
name|AIllegalArgument
name|aie
init|=
operator|new
name|AIllegalArgument
argument_list|()
decl_stmt|;
name|aie
operator|.
name|message
operator|=
operator|new
name|Utf8
argument_list|(
literal|"Can't set root or meta flag on create table."
argument_list|)
expr_stmt|;
throw|throw
name|aie
throw|;
block|}
return|return
name|htd
return|;
block|}
comment|//
comment|// Family metadata
comment|//
specifier|static
specifier|public
name|AFamilyDescriptor
name|hcdToAFD
parameter_list|(
name|HColumnDescriptor
name|hcd
parameter_list|)
throws|throws
name|IOException
block|{
name|AFamilyDescriptor
name|afamily
init|=
operator|new
name|AFamilyDescriptor
argument_list|()
decl_stmt|;
name|afamily
operator|.
name|name
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|hcd
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|compressionAlgorithm
init|=
name|hcd
operator|.
name|getCompressionType
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|compressionAlgorithm
operator|==
literal|"LZO"
condition|)
block|{
name|afamily
operator|.
name|compression
operator|=
name|ACompressionAlgorithm
operator|.
name|LZO
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|compressionAlgorithm
operator|==
literal|"GZ"
condition|)
block|{
name|afamily
operator|.
name|compression
operator|=
name|ACompressionAlgorithm
operator|.
name|GZ
expr_stmt|;
block|}
else|else
block|{
name|afamily
operator|.
name|compression
operator|=
name|ACompressionAlgorithm
operator|.
name|NONE
expr_stmt|;
block|}
name|afamily
operator|.
name|maxVersions
operator|=
name|hcd
operator|.
name|getMaxVersions
argument_list|()
expr_stmt|;
name|afamily
operator|.
name|blocksize
operator|=
name|hcd
operator|.
name|getBlocksize
argument_list|()
expr_stmt|;
name|afamily
operator|.
name|inMemory
operator|=
name|hcd
operator|.
name|isInMemory
argument_list|()
expr_stmt|;
name|afamily
operator|.
name|timeToLive
operator|=
name|hcd
operator|.
name|getTimeToLive
argument_list|()
expr_stmt|;
name|afamily
operator|.
name|blockCacheEnabled
operator|=
name|hcd
operator|.
name|isBlockCacheEnabled
argument_list|()
expr_stmt|;
return|return
name|afamily
return|;
block|}
specifier|static
specifier|public
name|HColumnDescriptor
name|afdToHCD
parameter_list|(
name|AFamilyDescriptor
name|afd
parameter_list|)
throws|throws
name|IOException
block|{
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|afd
operator|.
name|name
argument_list|)
argument_list|)
decl_stmt|;
name|ACompressionAlgorithm
name|compressionAlgorithm
init|=
name|afd
operator|.
name|compression
decl_stmt|;
if|if
condition|(
name|compressionAlgorithm
operator|==
name|ACompressionAlgorithm
operator|.
name|LZO
condition|)
block|{
name|hcd
operator|.
name|setCompressionType
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|LZO
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|compressionAlgorithm
operator|==
name|ACompressionAlgorithm
operator|.
name|GZ
condition|)
block|{
name|hcd
operator|.
name|setCompressionType
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|GZ
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hcd
operator|.
name|setCompressionType
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|afd
operator|.
name|maxVersions
operator|!=
literal|null
condition|)
block|{
name|hcd
operator|.
name|setMaxVersions
argument_list|(
name|afd
operator|.
name|maxVersions
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|afd
operator|.
name|blocksize
operator|!=
literal|null
condition|)
block|{
name|hcd
operator|.
name|setBlocksize
argument_list|(
name|afd
operator|.
name|blocksize
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|afd
operator|.
name|inMemory
operator|!=
literal|null
condition|)
block|{
name|hcd
operator|.
name|setInMemory
argument_list|(
name|afd
operator|.
name|inMemory
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|afd
operator|.
name|timeToLive
operator|!=
literal|null
condition|)
block|{
name|hcd
operator|.
name|setTimeToLive
argument_list|(
name|afd
operator|.
name|timeToLive
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|afd
operator|.
name|blockCacheEnabled
operator|!=
literal|null
condition|)
block|{
name|hcd
operator|.
name|setBlockCacheEnabled
argument_list|(
name|afd
operator|.
name|blockCacheEnabled
argument_list|)
expr_stmt|;
block|}
return|return
name|hcd
return|;
block|}
comment|//
comment|// Single-Row DML (Get)
comment|//
comment|// TODO(hammer): More concise idiom than if not null assign?
specifier|static
specifier|public
name|Get
name|agetToGet
parameter_list|(
name|AGet
name|aget
parameter_list|)
throws|throws
name|IOException
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|aget
operator|.
name|row
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|aget
operator|.
name|columns
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|AColumn
name|acolumn
range|:
name|aget
operator|.
name|columns
control|)
block|{
if|if
condition|(
name|acolumn
operator|.
name|qualifier
operator|!=
literal|null
condition|)
block|{
name|get
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acolumn
operator|.
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acolumn
operator|.
name|qualifier
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|get
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acolumn
operator|.
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|aget
operator|.
name|timestamp
operator|!=
literal|null
condition|)
block|{
name|get
operator|.
name|setTimeStamp
argument_list|(
name|aget
operator|.
name|timestamp
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|aget
operator|.
name|timerange
operator|!=
literal|null
condition|)
block|{
name|get
operator|.
name|setTimeRange
argument_list|(
name|aget
operator|.
name|timerange
operator|.
name|minStamp
argument_list|,
name|aget
operator|.
name|timerange
operator|.
name|maxStamp
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|aget
operator|.
name|maxVersions
operator|!=
literal|null
condition|)
block|{
name|get
operator|.
name|setMaxVersions
argument_list|(
name|aget
operator|.
name|maxVersions
argument_list|)
expr_stmt|;
block|}
return|return
name|get
return|;
block|}
comment|// TODO(hammer): Pick one: Timestamp or TimeStamp
specifier|static
specifier|public
name|AResult
name|resultToAResult
parameter_list|(
name|Result
name|result
parameter_list|)
block|{
name|AResult
name|aresult
init|=
operator|new
name|AResult
argument_list|()
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|result
operator|.
name|getRow
argument_list|()
decl_stmt|;
name|aresult
operator|.
name|row
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|row
operator|!=
literal|null
condition|?
name|row
else|:
operator|new
name|byte
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|Schema
name|s
init|=
name|Schema
operator|.
name|createArray
argument_list|(
name|AResultEntry
operator|.
name|SCHEMA$
argument_list|)
decl_stmt|;
name|GenericData
operator|.
name|Array
argument_list|<
name|AResultEntry
argument_list|>
name|entries
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|resultKeyValues
init|=
name|result
operator|.
name|list
argument_list|()
decl_stmt|;
if|if
condition|(
name|resultKeyValues
operator|!=
literal|null
operator|&&
name|resultKeyValues
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|entries
operator|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|AResultEntry
argument_list|>
argument_list|(
name|resultKeyValues
operator|.
name|size
argument_list|()
argument_list|,
name|s
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|resultKeyValue
range|:
name|resultKeyValues
control|)
block|{
name|AResultEntry
name|entry
init|=
operator|new
name|AResultEntry
argument_list|()
decl_stmt|;
name|entry
operator|.
name|family
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|resultKeyValue
operator|.
name|getFamily
argument_list|()
argument_list|)
expr_stmt|;
name|entry
operator|.
name|qualifier
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|resultKeyValue
operator|.
name|getQualifier
argument_list|()
argument_list|)
expr_stmt|;
name|entry
operator|.
name|value
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|resultKeyValue
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|entry
operator|.
name|timestamp
operator|=
name|resultKeyValue
operator|.
name|getTimestamp
argument_list|()
expr_stmt|;
name|entries
operator|.
name|add
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|entries
operator|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|AResultEntry
argument_list|>
argument_list|(
literal|0
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
name|aresult
operator|.
name|entries
operator|=
name|entries
expr_stmt|;
return|return
name|aresult
return|;
block|}
comment|//
comment|// Single-Row DML (Put)
comment|//
specifier|static
specifier|public
name|Put
name|aputToPut
parameter_list|(
name|APut
name|aput
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|aput
operator|.
name|row
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|AColumnValue
name|acv
range|:
name|aput
operator|.
name|columnValues
control|)
block|{
if|if
condition|(
name|acv
operator|.
name|timestamp
operator|!=
literal|null
condition|)
block|{
name|put
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acv
operator|.
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acv
operator|.
name|qualifier
argument_list|)
argument_list|,
name|acv
operator|.
name|timestamp
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acv
operator|.
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|put
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acv
operator|.
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acv
operator|.
name|qualifier
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acv
operator|.
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|put
return|;
block|}
comment|//
comment|// Single-Row DML (Delete)
comment|//
specifier|static
specifier|public
name|Delete
name|adeleteToDelete
parameter_list|(
name|ADelete
name|adelete
parameter_list|)
throws|throws
name|IOException
block|{
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|adelete
operator|.
name|row
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|adelete
operator|.
name|columns
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|AColumn
name|acolumn
range|:
name|adelete
operator|.
name|columns
control|)
block|{
if|if
condition|(
name|acolumn
operator|.
name|qualifier
operator|!=
literal|null
condition|)
block|{
name|delete
operator|.
name|deleteColumns
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acolumn
operator|.
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acolumn
operator|.
name|qualifier
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|delete
operator|.
name|deleteFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acolumn
operator|.
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|delete
return|;
block|}
comment|//
comment|// Multi-row DML (Scan)
comment|//
specifier|static
specifier|public
name|Scan
name|ascanToScan
parameter_list|(
name|AScan
name|ascan
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
if|if
condition|(
name|ascan
operator|.
name|startRow
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setStartRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ascan
operator|.
name|startRow
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ascan
operator|.
name|stopRow
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setStopRow
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ascan
operator|.
name|stopRow
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ascan
operator|.
name|columns
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|AColumn
name|acolumn
range|:
name|ascan
operator|.
name|columns
control|)
block|{
if|if
condition|(
name|acolumn
operator|.
name|qualifier
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|addColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acolumn
operator|.
name|family
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acolumn
operator|.
name|qualifier
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|acolumn
operator|.
name|family
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|ascan
operator|.
name|timestamp
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setTimeStamp
argument_list|(
name|ascan
operator|.
name|timestamp
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ascan
operator|.
name|timerange
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setTimeRange
argument_list|(
name|ascan
operator|.
name|timerange
operator|.
name|minStamp
argument_list|,
name|ascan
operator|.
name|timerange
operator|.
name|maxStamp
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ascan
operator|.
name|maxVersions
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|ascan
operator|.
name|maxVersions
argument_list|)
expr_stmt|;
block|}
return|return
name|scan
return|;
block|}
comment|// TODO(hammer): Better to return null or empty array?
specifier|static
specifier|public
name|GenericArray
argument_list|<
name|AResult
argument_list|>
name|resultsToAResults
parameter_list|(
name|Result
index|[]
name|results
parameter_list|)
block|{
name|Schema
name|s
init|=
name|Schema
operator|.
name|createArray
argument_list|(
name|AResult
operator|.
name|SCHEMA$
argument_list|)
decl_stmt|;
name|GenericData
operator|.
name|Array
argument_list|<
name|AResult
argument_list|>
name|aresults
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|results
operator|!=
literal|null
operator|&&
name|results
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|aresults
operator|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|AResult
argument_list|>
argument_list|(
name|results
operator|.
name|length
argument_list|,
name|s
argument_list|)
expr_stmt|;
for|for
control|(
name|Result
name|result
range|:
name|results
control|)
block|{
name|aresults
operator|.
name|add
argument_list|(
name|resultToAResult
argument_list|(
name|result
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|aresults
operator|=
operator|new
name|GenericData
operator|.
name|Array
argument_list|<
name|AResult
argument_list|>
argument_list|(
literal|0
argument_list|,
name|s
argument_list|)
expr_stmt|;
block|}
return|return
name|aresults
return|;
block|}
block|}
end_class

end_unit

