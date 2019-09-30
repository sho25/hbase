begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|Nullable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|RegionStatesCount
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|UnsafeByteOperations
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
name|ProtobufUtil
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
name|ClusterStatusProtos
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
name|ClusterStatusProtos
operator|.
name|Option
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
name|FSProtos
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ClusterMetricsBuilder
block|{
specifier|public
specifier|static
name|ClusterStatusProtos
operator|.
name|ClusterStatus
name|toClusterStatus
parameter_list|(
name|ClusterMetrics
name|metrics
parameter_list|)
block|{
name|ClusterStatusProtos
operator|.
name|ClusterStatus
operator|.
name|Builder
name|builder
init|=
name|ClusterStatusProtos
operator|.
name|ClusterStatus
operator|.
name|newBuilder
argument_list|()
operator|.
name|addAllBackupMasters
argument_list|(
name|metrics
operator|.
name|getBackupMasterNames
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ProtobufUtil
operator|::
name|toServerName
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|addAllDeadServers
argument_list|(
name|metrics
operator|.
name|getDeadServerNames
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ProtobufUtil
operator|::
name|toServerName
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|addAllLiveServers
argument_list|(
name|metrics
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|s
lambda|->
name|ClusterStatusProtos
operator|.
name|LiveServerInfo
operator|.
name|newBuilder
argument_list|()
operator|.
name|setServer
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|s
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setServerLoad
argument_list|(
name|ServerMetricsBuilder
operator|.
name|toServerLoad
argument_list|(
name|s
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|addAllMasterCoprocessors
argument_list|(
name|metrics
operator|.
name|getMasterCoprocessorNames
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|n
lambda|->
name|HBaseProtos
operator|.
name|Coprocessor
operator|.
name|newBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|n
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|addAllRegionsInTransition
argument_list|(
name|metrics
operator|.
name|getRegionStatesInTransition
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|r
lambda|->
name|ClusterStatusProtos
operator|.
name|RegionInTransition
operator|.
name|newBuilder
argument_list|()
operator|.
name|setSpec
argument_list|(
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|newBuilder
argument_list|()
operator|.
name|setType
argument_list|(
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|RegionSpecifierType
operator|.
name|REGION_NAME
argument_list|)
operator|.
name|setValue
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|r
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|setRegionState
argument_list|(
name|r
operator|.
name|convert
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setMasterInfoPort
argument_list|(
name|metrics
operator|.
name|getMasterInfoPort
argument_list|()
argument_list|)
operator|.
name|addAllServersName
argument_list|(
name|metrics
operator|.
name|getServersName
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ProtobufUtil
operator|::
name|toServerName
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|addAllTableRegionStatesCount
argument_list|(
name|metrics
operator|.
name|getTableRegionStatesCount
argument_list|()
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|status
lambda|->
name|ClusterStatusProtos
operator|.
name|TableRegionStatesCount
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTableName
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoTableName
argument_list|(
operator|(
name|status
operator|.
name|getKey
argument_list|()
operator|)
argument_list|)
argument_list|)
operator|.
name|setRegionStatesCount
argument_list|(
name|ProtobufUtil
operator|.
name|toTableRegionStatesCount
argument_list|(
name|status
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|metrics
operator|.
name|getMasterName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setMaster
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
operator|(
name|metrics
operator|.
name|getMasterName
argument_list|()
operator|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|metrics
operator|.
name|getBalancerOn
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setBalancerOn
argument_list|(
name|metrics
operator|.
name|getBalancerOn
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|metrics
operator|.
name|getClusterId
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setClusterId
argument_list|(
operator|new
name|ClusterId
argument_list|(
name|metrics
operator|.
name|getClusterId
argument_list|()
argument_list|)
operator|.
name|convert
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|metrics
operator|.
name|getHBaseVersion
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setHbaseVersion
argument_list|(
name|FSProtos
operator|.
name|HBaseVersionFileContent
operator|.
name|newBuilder
argument_list|()
operator|.
name|setVersion
argument_list|(
name|metrics
operator|.
name|getHBaseVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|ClusterMetrics
name|toClusterMetrics
parameter_list|(
name|ClusterStatusProtos
operator|.
name|ClusterStatus
name|proto
parameter_list|)
block|{
name|ClusterMetricsBuilder
name|builder
init|=
name|ClusterMetricsBuilder
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setLiveServerMetrics
argument_list|(
name|proto
operator|.
name|getLiveServersList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toMap
argument_list|(
name|e
lambda|->
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|e
operator|.
name|getServer
argument_list|()
argument_list|)
argument_list|,
name|ServerMetricsBuilder
operator|::
name|toServerMetrics
argument_list|)
argument_list|)
argument_list|)
operator|.
name|setDeadServerNames
argument_list|(
name|proto
operator|.
name|getDeadServersList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ProtobufUtil
operator|::
name|toServerName
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setBackerMasterNames
argument_list|(
name|proto
operator|.
name|getBackupMastersList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ProtobufUtil
operator|::
name|toServerName
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setRegionsInTransition
argument_list|(
name|proto
operator|.
name|getRegionsInTransitionList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ClusterStatusProtos
operator|.
name|RegionInTransition
operator|::
name|getRegionState
argument_list|)
operator|.
name|map
argument_list|(
name|RegionState
operator|::
name|convert
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setMasterCoprocessorNames
argument_list|(
name|proto
operator|.
name|getMasterCoprocessorsList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|HBaseProtos
operator|.
name|Coprocessor
operator|::
name|getName
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setServerNames
argument_list|(
name|proto
operator|.
name|getServersNameList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ProtobufUtil
operator|::
name|toServerName
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setTableRegionStatesCount
argument_list|(
name|proto
operator|.
name|getTableRegionStatesCountList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toMap
argument_list|(
name|e
lambda|->
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|e
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|,
name|e
lambda|->
name|ProtobufUtil
operator|.
name|toTableRegionStatesCount
argument_list|(
name|e
operator|.
name|getRegionStatesCount
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|proto
operator|.
name|hasClusterId
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setClusterId
argument_list|(
name|ClusterId
operator|.
name|convert
argument_list|(
name|proto
operator|.
name|getClusterId
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasHbaseVersion
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setHBaseVersion
argument_list|(
name|proto
operator|.
name|getHbaseVersion
argument_list|()
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasMaster
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setMasterName
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|proto
operator|.
name|getMaster
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasBalancerOn
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setBalancerOn
argument_list|(
name|proto
operator|.
name|getBalancerOn
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|proto
operator|.
name|hasMasterInfoPort
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setMasterInfoPort
argument_list|(
name|proto
operator|.
name|getMasterInfoPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Convert ClusterStatusProtos.Option to ClusterMetrics.Option    * @param option a ClusterStatusProtos.Option    * @return converted ClusterMetrics.Option    */
specifier|public
specifier|static
name|ClusterMetrics
operator|.
name|Option
name|toOption
parameter_list|(
name|ClusterStatusProtos
operator|.
name|Option
name|option
parameter_list|)
block|{
switch|switch
condition|(
name|option
condition|)
block|{
case|case
name|HBASE_VERSION
case|:
return|return
name|ClusterMetrics
operator|.
name|Option
operator|.
name|HBASE_VERSION
return|;
case|case
name|LIVE_SERVERS
case|:
return|return
name|ClusterMetrics
operator|.
name|Option
operator|.
name|LIVE_SERVERS
return|;
case|case
name|DEAD_SERVERS
case|:
return|return
name|ClusterMetrics
operator|.
name|Option
operator|.
name|DEAD_SERVERS
return|;
case|case
name|REGIONS_IN_TRANSITION
case|:
return|return
name|ClusterMetrics
operator|.
name|Option
operator|.
name|REGIONS_IN_TRANSITION
return|;
case|case
name|CLUSTER_ID
case|:
return|return
name|ClusterMetrics
operator|.
name|Option
operator|.
name|CLUSTER_ID
return|;
case|case
name|MASTER_COPROCESSORS
case|:
return|return
name|ClusterMetrics
operator|.
name|Option
operator|.
name|MASTER_COPROCESSORS
return|;
case|case
name|MASTER
case|:
return|return
name|ClusterMetrics
operator|.
name|Option
operator|.
name|MASTER
return|;
case|case
name|BACKUP_MASTERS
case|:
return|return
name|ClusterMetrics
operator|.
name|Option
operator|.
name|BACKUP_MASTERS
return|;
case|case
name|BALANCER_ON
case|:
return|return
name|ClusterMetrics
operator|.
name|Option
operator|.
name|BALANCER_ON
return|;
case|case
name|SERVERS_NAME
case|:
return|return
name|ClusterMetrics
operator|.
name|Option
operator|.
name|SERVERS_NAME
return|;
case|case
name|MASTER_INFO_PORT
case|:
return|return
name|ClusterMetrics
operator|.
name|Option
operator|.
name|MASTER_INFO_PORT
return|;
case|case
name|TABLE_TO_REGIONS_COUNT
case|:
return|return
name|ClusterMetrics
operator|.
name|Option
operator|.
name|TABLE_TO_REGIONS_COUNT
return|;
comment|// should not reach here
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid option: "
operator|+
name|option
argument_list|)
throw|;
block|}
block|}
comment|/**    * Convert ClusterMetrics.Option to ClusterStatusProtos.Option    * @param option a ClusterMetrics.Option    * @return converted ClusterStatusProtos.Option    */
specifier|public
specifier|static
name|ClusterStatusProtos
operator|.
name|Option
name|toOption
parameter_list|(
name|ClusterMetrics
operator|.
name|Option
name|option
parameter_list|)
block|{
switch|switch
condition|(
name|option
condition|)
block|{
case|case
name|HBASE_VERSION
case|:
return|return
name|ClusterStatusProtos
operator|.
name|Option
operator|.
name|HBASE_VERSION
return|;
case|case
name|LIVE_SERVERS
case|:
return|return
name|ClusterStatusProtos
operator|.
name|Option
operator|.
name|LIVE_SERVERS
return|;
case|case
name|DEAD_SERVERS
case|:
return|return
name|ClusterStatusProtos
operator|.
name|Option
operator|.
name|DEAD_SERVERS
return|;
case|case
name|REGIONS_IN_TRANSITION
case|:
return|return
name|ClusterStatusProtos
operator|.
name|Option
operator|.
name|REGIONS_IN_TRANSITION
return|;
case|case
name|CLUSTER_ID
case|:
return|return
name|ClusterStatusProtos
operator|.
name|Option
operator|.
name|CLUSTER_ID
return|;
case|case
name|MASTER_COPROCESSORS
case|:
return|return
name|ClusterStatusProtos
operator|.
name|Option
operator|.
name|MASTER_COPROCESSORS
return|;
case|case
name|MASTER
case|:
return|return
name|ClusterStatusProtos
operator|.
name|Option
operator|.
name|MASTER
return|;
case|case
name|BACKUP_MASTERS
case|:
return|return
name|ClusterStatusProtos
operator|.
name|Option
operator|.
name|BACKUP_MASTERS
return|;
case|case
name|BALANCER_ON
case|:
return|return
name|ClusterStatusProtos
operator|.
name|Option
operator|.
name|BALANCER_ON
return|;
case|case
name|SERVERS_NAME
case|:
return|return
name|Option
operator|.
name|SERVERS_NAME
return|;
case|case
name|MASTER_INFO_PORT
case|:
return|return
name|ClusterStatusProtos
operator|.
name|Option
operator|.
name|MASTER_INFO_PORT
return|;
case|case
name|TABLE_TO_REGIONS_COUNT
case|:
return|return
name|ClusterStatusProtos
operator|.
name|Option
operator|.
name|TABLE_TO_REGIONS_COUNT
return|;
comment|// should not reach here
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid option: "
operator|+
name|option
argument_list|)
throw|;
block|}
block|}
comment|/**    * Convert a list of ClusterStatusProtos.Option to an enum set of ClusterMetrics.Option    * @param options the pb options    * @return an enum set of ClusterMetrics.Option    */
specifier|public
specifier|static
name|EnumSet
argument_list|<
name|ClusterMetrics
operator|.
name|Option
argument_list|>
name|toOptions
parameter_list|(
name|List
argument_list|<
name|ClusterStatusProtos
operator|.
name|Option
argument_list|>
name|options
parameter_list|)
block|{
return|return
name|options
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ClusterMetricsBuilder
operator|::
name|toOption
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toCollection
argument_list|(
parameter_list|()
lambda|->
name|EnumSet
operator|.
name|noneOf
argument_list|(
name|ClusterMetrics
operator|.
name|Option
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Convert an enum set of ClusterMetrics.Option to a list of ClusterStatusProtos.Option    * @param options the ClusterMetrics options    * @return a list of ClusterStatusProtos.Option    */
specifier|public
specifier|static
name|List
argument_list|<
name|ClusterStatusProtos
operator|.
name|Option
argument_list|>
name|toOptions
parameter_list|(
name|EnumSet
argument_list|<
name|ClusterMetrics
operator|.
name|Option
argument_list|>
name|options
parameter_list|)
block|{
return|return
name|options
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|ClusterMetricsBuilder
operator|::
name|toOption
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ClusterMetricsBuilder
name|newBuilder
parameter_list|()
block|{
return|return
operator|new
name|ClusterMetricsBuilder
argument_list|()
return|;
block|}
annotation|@
name|Nullable
specifier|private
name|String
name|hbaseVersion
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ServerName
argument_list|>
name|deadServerNames
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerMetrics
argument_list|>
name|liveServerMetrics
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Nullable
specifier|private
name|ServerName
name|masterName
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ServerName
argument_list|>
name|backupMasterNames
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
specifier|private
name|List
argument_list|<
name|RegionState
argument_list|>
name|regionsInTransition
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
annotation|@
name|Nullable
specifier|private
name|String
name|clusterId
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|masterCoprocessorNames
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
annotation|@
name|Nullable
specifier|private
name|Boolean
name|balancerOn
decl_stmt|;
specifier|private
name|int
name|masterInfoPort
decl_stmt|;
specifier|private
name|List
argument_list|<
name|ServerName
argument_list|>
name|serversName
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|TableName
argument_list|,
name|RegionStatesCount
argument_list|>
name|tableRegionStatesCount
init|=
name|Collections
operator|.
name|emptyMap
argument_list|()
decl_stmt|;
specifier|private
name|ClusterMetricsBuilder
parameter_list|()
block|{   }
specifier|public
name|ClusterMetricsBuilder
name|setHBaseVersion
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|this
operator|.
name|hbaseVersion
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ClusterMetricsBuilder
name|setDeadServerNames
parameter_list|(
name|List
argument_list|<
name|ServerName
argument_list|>
name|value
parameter_list|)
block|{
name|this
operator|.
name|deadServerNames
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ClusterMetricsBuilder
name|setLiveServerMetrics
parameter_list|(
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerMetrics
argument_list|>
name|value
parameter_list|)
block|{
name|liveServerMetrics
operator|.
name|putAll
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ClusterMetricsBuilder
name|setMasterName
parameter_list|(
name|ServerName
name|value
parameter_list|)
block|{
name|this
operator|.
name|masterName
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ClusterMetricsBuilder
name|setBackerMasterNames
parameter_list|(
name|List
argument_list|<
name|ServerName
argument_list|>
name|value
parameter_list|)
block|{
name|this
operator|.
name|backupMasterNames
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ClusterMetricsBuilder
name|setRegionsInTransition
parameter_list|(
name|List
argument_list|<
name|RegionState
argument_list|>
name|value
parameter_list|)
block|{
name|this
operator|.
name|regionsInTransition
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ClusterMetricsBuilder
name|setClusterId
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|this
operator|.
name|clusterId
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ClusterMetricsBuilder
name|setMasterCoprocessorNames
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|value
parameter_list|)
block|{
name|this
operator|.
name|masterCoprocessorNames
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ClusterMetricsBuilder
name|setBalancerOn
parameter_list|(
annotation|@
name|Nullable
name|Boolean
name|value
parameter_list|)
block|{
name|this
operator|.
name|balancerOn
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ClusterMetricsBuilder
name|setMasterInfoPort
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|this
operator|.
name|masterInfoPort
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ClusterMetricsBuilder
name|setServerNames
parameter_list|(
name|List
argument_list|<
name|ServerName
argument_list|>
name|serversName
parameter_list|)
block|{
name|this
operator|.
name|serversName
operator|=
name|serversName
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ClusterMetricsBuilder
name|setTableRegionStatesCount
parameter_list|(
name|Map
argument_list|<
name|TableName
argument_list|,
name|RegionStatesCount
argument_list|>
name|tableRegionStatesCount
parameter_list|)
block|{
name|this
operator|.
name|tableRegionStatesCount
operator|=
name|tableRegionStatesCount
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|public
name|ClusterMetrics
name|build
parameter_list|()
block|{
return|return
operator|new
name|ClusterMetricsImpl
argument_list|(
name|hbaseVersion
argument_list|,
name|deadServerNames
argument_list|,
name|liveServerMetrics
argument_list|,
name|masterName
argument_list|,
name|backupMasterNames
argument_list|,
name|regionsInTransition
argument_list|,
name|clusterId
argument_list|,
name|masterCoprocessorNames
argument_list|,
name|balancerOn
argument_list|,
name|masterInfoPort
argument_list|,
name|serversName
argument_list|,
name|tableRegionStatesCount
argument_list|)
return|;
block|}
specifier|private
specifier|static
class|class
name|ClusterMetricsImpl
implements|implements
name|ClusterMetrics
block|{
annotation|@
name|Nullable
specifier|private
specifier|final
name|String
name|hbaseVersion
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|ServerName
argument_list|>
name|deadServerNames
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerMetrics
argument_list|>
name|liveServerMetrics
decl_stmt|;
annotation|@
name|Nullable
specifier|private
specifier|final
name|ServerName
name|masterName
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|ServerName
argument_list|>
name|backupMasterNames
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|RegionState
argument_list|>
name|regionsInTransition
decl_stmt|;
annotation|@
name|Nullable
specifier|private
specifier|final
name|String
name|clusterId
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|masterCoprocessorNames
decl_stmt|;
annotation|@
name|Nullable
specifier|private
specifier|final
name|Boolean
name|balancerOn
decl_stmt|;
specifier|private
specifier|final
name|int
name|masterInfoPort
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|ServerName
argument_list|>
name|serversName
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|TableName
argument_list|,
name|RegionStatesCount
argument_list|>
name|tableRegionStatesCount
decl_stmt|;
name|ClusterMetricsImpl
parameter_list|(
name|String
name|hbaseVersion
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|deadServerNames
parameter_list|,
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerMetrics
argument_list|>
name|liveServerMetrics
parameter_list|,
name|ServerName
name|masterName
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|backupMasterNames
parameter_list|,
name|List
argument_list|<
name|RegionState
argument_list|>
name|regionsInTransition
parameter_list|,
name|String
name|clusterId
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|masterCoprocessorNames
parameter_list|,
name|Boolean
name|balancerOn
parameter_list|,
name|int
name|masterInfoPort
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|serversName
parameter_list|,
name|Map
argument_list|<
name|TableName
argument_list|,
name|RegionStatesCount
argument_list|>
name|tableRegionStatesCount
parameter_list|)
block|{
name|this
operator|.
name|hbaseVersion
operator|=
name|hbaseVersion
expr_stmt|;
name|this
operator|.
name|deadServerNames
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|deadServerNames
argument_list|)
expr_stmt|;
name|this
operator|.
name|liveServerMetrics
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|liveServerMetrics
argument_list|)
expr_stmt|;
name|this
operator|.
name|masterName
operator|=
name|masterName
expr_stmt|;
name|this
operator|.
name|backupMasterNames
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|backupMasterNames
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionsInTransition
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|regionsInTransition
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterId
operator|=
name|clusterId
expr_stmt|;
name|this
operator|.
name|masterCoprocessorNames
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|masterCoprocessorNames
argument_list|)
expr_stmt|;
name|this
operator|.
name|balancerOn
operator|=
name|balancerOn
expr_stmt|;
name|this
operator|.
name|masterInfoPort
operator|=
name|masterInfoPort
expr_stmt|;
name|this
operator|.
name|serversName
operator|=
name|serversName
expr_stmt|;
name|this
operator|.
name|tableRegionStatesCount
operator|=
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|tableRegionStatesCount
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getHBaseVersion
parameter_list|()
block|{
return|return
name|hbaseVersion
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ServerName
argument_list|>
name|getDeadServerNames
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|deadServerNames
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerMetrics
argument_list|>
name|getLiveServerMetrics
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|liveServerMetrics
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getMasterName
parameter_list|()
block|{
return|return
name|masterName
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ServerName
argument_list|>
name|getBackupMasterNames
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|backupMasterNames
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|RegionState
argument_list|>
name|getRegionStatesInTransition
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|regionsInTransition
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getClusterId
parameter_list|()
block|{
return|return
name|clusterId
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getMasterCoprocessorNames
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|masterCoprocessorNames
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Boolean
name|getBalancerOn
parameter_list|()
block|{
return|return
name|balancerOn
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getMasterInfoPort
parameter_list|()
block|{
return|return
name|masterInfoPort
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ServerName
argument_list|>
name|getServersName
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|serversName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|TableName
argument_list|,
name|RegionStatesCount
argument_list|>
name|getTableRegionStatesCount
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|tableRegionStatesCount
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|1024
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Master: "
operator|+
name|getMasterName
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|backupMastersSize
init|=
name|getBackupMasterNames
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\nNumber of backup masters: "
operator|+
name|backupMastersSize
argument_list|)
expr_stmt|;
if|if
condition|(
name|backupMastersSize
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|ServerName
name|serverName
range|:
name|getBackupMasterNames
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"\n  "
operator|+
name|serverName
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|serversSize
init|=
name|getLiveServerMetrics
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
name|serversNameSize
init|=
name|getServersName
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\nNumber of live region servers: "
operator|+
operator|(
name|serversSize
operator|>
literal|0
condition|?
name|serversSize
else|:
name|serversNameSize
operator|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|serversSize
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|ServerName
name|serverName
range|:
name|getLiveServerMetrics
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"\n  "
operator|+
name|serverName
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|serversNameSize
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|ServerName
name|serverName
range|:
name|getServersName
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"\n  "
operator|+
name|serverName
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|deadServerSize
init|=
name|getDeadServerNames
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\nNumber of dead region servers: "
operator|+
name|deadServerSize
argument_list|)
expr_stmt|;
if|if
condition|(
name|deadServerSize
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|ServerName
name|serverName
range|:
name|getDeadServerNames
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"\n  "
operator|+
name|serverName
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"\nAverage load: "
operator|+
name|getAverageLoad
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\nNumber of requests: "
operator|+
name|getRequestCount
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\nNumber of regions: "
operator|+
name|getRegionCount
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|ritSize
init|=
name|getRegionStatesInTransition
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\nNumber of regions in transition: "
operator|+
name|ritSize
argument_list|)
expr_stmt|;
if|if
condition|(
name|ritSize
operator|>
literal|0
condition|)
block|{
for|for
control|(
name|RegionState
name|state
range|:
name|getRegionStatesInTransition
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"\n  "
operator|+
name|state
operator|.
name|toDescriptiveString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

