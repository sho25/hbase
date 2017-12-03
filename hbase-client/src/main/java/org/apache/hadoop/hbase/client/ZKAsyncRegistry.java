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
name|client
package|;
end_package

begin_import
import|import static
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
name|RegionInfo
operator|.
name|DEFAULT_REPLICA_ID
import|;
end_import

begin_import
import|import static
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
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
import|;
end_import

begin_import
import|import static
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
name|RegionReplicaUtil
operator|.
name|getRegionInfoForDefaultReplica
import|;
end_import

begin_import
import|import static
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
name|RegionReplicaUtil
operator|.
name|getRegionInfoForReplica
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|zookeeper
operator|.
name|ZKMetadata
operator|.
name|removeMetaData
import|;
end_import

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
name|util
operator|.
name|concurrent
operator|.
name|CompletableFuture
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|mutable
operator|.
name|MutableInt
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configuration
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
name|ClusterId
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
name|HRegionLocation
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
name|RegionLocations
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
name|exceptions
operator|.
name|DeserializationException
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
name|master
operator|.
name|RegionState
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
name|Pair
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
name|zookeeper
operator|.
name|ReadOnlyZKClient
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
name|zookeeper
operator|.
name|ZNodePaths
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
import|;
end_import

begin_comment
comment|/**  * Fetch the registry data from zookeeper.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|ZKAsyncRegistry
implements|implements
name|AsyncRegistry
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ZKAsyncRegistry
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ReadOnlyZKClient
name|zk
decl_stmt|;
specifier|private
specifier|final
name|ZNodePaths
name|znodePaths
decl_stmt|;
name|ZKAsyncRegistry
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|znodePaths
operator|=
operator|new
name|ZNodePaths
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|zk
operator|=
operator|new
name|ReadOnlyZKClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|private
interface|interface
name|Converter
parameter_list|<
name|T
parameter_list|>
block|{
name|T
name|convert
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|Exception
function_decl|;
block|}
specifier|private
parameter_list|<
name|T
parameter_list|>
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|getAndConvert
parameter_list|(
name|String
name|path
parameter_list|,
name|Converter
argument_list|<
name|T
argument_list|>
name|converter
parameter_list|)
block|{
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|future
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|zk
operator|.
name|get
argument_list|(
name|path
argument_list|)
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|data
parameter_list|,
name|error
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|error
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|future
operator|.
name|complete
argument_list|(
name|converter
operator|.
name|convert
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|future
return|;
block|}
specifier|private
specifier|static
name|String
name|getClusterId
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|DeserializationException
block|{
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|data
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|data
operator|=
name|removeMetaData
argument_list|(
name|data
argument_list|)
expr_stmt|;
return|return
name|ClusterId
operator|.
name|parseFrom
argument_list|(
name|data
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|String
argument_list|>
name|getClusterId
parameter_list|()
block|{
return|return
name|getAndConvert
argument_list|(
name|znodePaths
operator|.
name|clusterIdZNode
argument_list|,
name|ZKAsyncRegistry
operator|::
name|getClusterId
argument_list|)
return|;
block|}
annotation|@
name|VisibleForTesting
name|ReadOnlyZKClient
name|getZKClient
parameter_list|()
block|{
return|return
name|zk
return|;
block|}
specifier|private
specifier|static
name|ZooKeeperProtos
operator|.
name|MetaRegionServer
name|getMetaProto
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|data
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|data
operator|=
name|removeMetaData
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|int
name|prefixLen
init|=
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
return|return
name|ZooKeeperProtos
operator|.
name|MetaRegionServer
operator|.
name|parser
argument_list|()
operator|.
name|parseFrom
argument_list|(
name|data
argument_list|,
name|prefixLen
argument_list|,
name|data
operator|.
name|length
operator|-
name|prefixLen
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|void
name|tryComplete
parameter_list|(
name|MutableInt
name|remaining
parameter_list|,
name|HRegionLocation
index|[]
name|locs
parameter_list|,
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
name|future
parameter_list|)
block|{
name|remaining
operator|.
name|decrement
argument_list|()
expr_stmt|;
if|if
condition|(
name|remaining
operator|.
name|intValue
argument_list|()
operator|>
literal|0
condition|)
block|{
return|return;
block|}
name|future
operator|.
name|complete
argument_list|(
operator|new
name|RegionLocations
argument_list|(
name|locs
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Pair
argument_list|<
name|RegionState
operator|.
name|State
argument_list|,
name|ServerName
argument_list|>
name|getStateAndServerName
parameter_list|(
name|ZooKeeperProtos
operator|.
name|MetaRegionServer
name|proto
parameter_list|)
block|{
name|RegionState
operator|.
name|State
name|state
decl_stmt|;
if|if
condition|(
name|proto
operator|.
name|hasState
argument_list|()
condition|)
block|{
name|state
operator|=
name|RegionState
operator|.
name|State
operator|.
name|convert
argument_list|(
name|proto
operator|.
name|getState
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|state
operator|=
name|RegionState
operator|.
name|State
operator|.
name|OPEN
expr_stmt|;
block|}
name|HBaseProtos
operator|.
name|ServerName
name|snProto
init|=
name|proto
operator|.
name|getServer
argument_list|()
decl_stmt|;
return|return
name|Pair
operator|.
name|newPair
argument_list|(
name|state
argument_list|,
name|ServerName
operator|.
name|valueOf
argument_list|(
name|snProto
operator|.
name|getHostName
argument_list|()
argument_list|,
name|snProto
operator|.
name|getPort
argument_list|()
argument_list|,
name|snProto
operator|.
name|getStartCode
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
name|getMetaRegionLocation
parameter_list|()
block|{
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
name|future
init|=
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|HRegionLocation
index|[]
name|locs
init|=
operator|new
name|HRegionLocation
index|[
name|znodePaths
operator|.
name|metaReplicaZNodes
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|MutableInt
name|remaining
init|=
operator|new
name|MutableInt
argument_list|(
name|locs
operator|.
name|length
argument_list|)
decl_stmt|;
name|znodePaths
operator|.
name|metaReplicaZNodes
operator|.
name|forEach
argument_list|(
parameter_list|(
name|replicaId
parameter_list|,
name|path
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|replicaId
operator|==
name|DEFAULT_REPLICA_ID
condition|)
block|{
name|getAndConvert
argument_list|(
name|path
argument_list|,
name|ZKAsyncRegistry
operator|::
name|getMetaProto
argument_list|)
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|proto
parameter_list|,
name|error
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|error
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|proto
operator|==
literal|null
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"Meta znode is null"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|Pair
argument_list|<
name|RegionState
operator|.
name|State
argument_list|,
name|ServerName
argument_list|>
name|stateAndServerName
init|=
name|getStateAndServerName
argument_list|(
name|proto
argument_list|)
decl_stmt|;
if|if
condition|(
name|stateAndServerName
operator|.
name|getFirst
argument_list|()
operator|!=
name|RegionState
operator|.
name|State
operator|.
name|OPEN
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"Meta region is in state "
operator|+
name|stateAndServerName
operator|.
name|getFirst
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|locs
index|[
name|DEFAULT_REPLICA_ID
index|]
operator|=
operator|new
name|HRegionLocation
argument_list|(
name|getRegionInfoForDefaultReplica
argument_list|(
name|FIRST_META_REGIONINFO
argument_list|)
argument_list|,
name|stateAndServerName
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
name|tryComplete
argument_list|(
name|remaining
argument_list|,
name|locs
argument_list|,
name|future
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|getAndConvert
argument_list|(
name|path
argument_list|,
name|ZKAsyncRegistry
operator|::
name|getMetaProto
argument_list|)
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|proto
parameter_list|,
name|error
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|future
operator|.
name|isDone
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to fetch "
operator|+
name|path
argument_list|,
name|error
argument_list|)
expr_stmt|;
name|locs
index|[
name|replicaId
index|]
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|proto
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Meta znode for replica "
operator|+
name|replicaId
operator|+
literal|" is null"
argument_list|)
expr_stmt|;
name|locs
index|[
name|replicaId
index|]
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|Pair
argument_list|<
name|RegionState
operator|.
name|State
argument_list|,
name|ServerName
argument_list|>
name|stateAndServerName
init|=
name|getStateAndServerName
argument_list|(
name|proto
argument_list|)
decl_stmt|;
if|if
condition|(
name|stateAndServerName
operator|.
name|getFirst
argument_list|()
operator|!=
name|RegionState
operator|.
name|State
operator|.
name|OPEN
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Meta region for replica "
operator|+
name|replicaId
operator|+
literal|" is in state "
operator|+
name|stateAndServerName
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
name|locs
index|[
name|replicaId
index|]
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|locs
index|[
name|replicaId
index|]
operator|=
operator|new
name|HRegionLocation
argument_list|(
name|getRegionInfoForReplica
argument_list|(
name|FIRST_META_REGIONINFO
argument_list|,
name|replicaId
argument_list|)
argument_list|,
name|stateAndServerName
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|tryComplete
argument_list|(
name|remaining
argument_list|,
name|locs
argument_list|,
name|future
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|future
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Integer
argument_list|>
name|getCurrentNrHRS
parameter_list|()
block|{
return|return
name|zk
operator|.
name|exists
argument_list|(
name|znodePaths
operator|.
name|rsZNode
argument_list|)
operator|.
name|thenApply
argument_list|(
name|s
lambda|->
name|s
operator|!=
literal|null
condition|?
name|s
operator|.
name|getNumChildren
argument_list|()
else|:
literal|0
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|ZooKeeperProtos
operator|.
name|Master
name|getMasterProto
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|data
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|data
operator|=
name|removeMetaData
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|int
name|prefixLen
init|=
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
return|return
name|ZooKeeperProtos
operator|.
name|Master
operator|.
name|parser
argument_list|()
operator|.
name|parseFrom
argument_list|(
name|data
argument_list|,
name|prefixLen
argument_list|,
name|data
operator|.
name|length
operator|-
name|prefixLen
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|ServerName
argument_list|>
name|getMasterAddress
parameter_list|()
block|{
return|return
name|getAndConvert
argument_list|(
name|znodePaths
operator|.
name|masterAddressZNode
argument_list|,
name|ZKAsyncRegistry
operator|::
name|getMasterProto
argument_list|)
operator|.
name|thenApply
argument_list|(
name|proto
lambda|->
block|{
if|if
condition|(
name|proto
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|HBaseProtos
operator|.
name|ServerName
name|snProto
init|=
name|proto
operator|.
name|getMaster
argument_list|()
decl_stmt|;
return|return
name|ServerName
operator|.
name|valueOf
argument_list|(
name|snProto
operator|.
name|getHostName
argument_list|()
argument_list|,
name|snProto
operator|.
name|getPort
argument_list|()
argument_list|,
name|snProto
operator|.
name|getStartCode
argument_list|()
argument_list|)
return|;
block|}
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Integer
argument_list|>
name|getMasterInfoPort
parameter_list|()
block|{
return|return
name|getAndConvert
argument_list|(
name|znodePaths
operator|.
name|masterAddressZNode
argument_list|,
name|ZKAsyncRegistry
operator|::
name|getMasterProto
argument_list|)
operator|.
name|thenApply
argument_list|(
name|proto
lambda|->
name|proto
operator|!=
literal|null
condition|?
name|proto
operator|.
name|getInfoPort
argument_list|()
else|:
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|zk
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

