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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|ByteStringer
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
name|classification
operator|.
name|InterfaceStability
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
name|protobuf
operator|.
name|generated
operator|.
name|ClusterStatusProtos
operator|.
name|LiveServerInfo
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
name|protobuf
operator|.
name|generated
operator|.
name|ClusterStatusProtos
operator|.
name|RegionInTransition
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
name|protobuf
operator|.
name|generated
operator|.
name|FSProtos
operator|.
name|HBaseVersionFileContent
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|RegionSpecifier
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|RegionSpecifierType
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|VersionedWritable
import|;
end_import

begin_comment
comment|/**  * Status information on the HBase cluster.  *<p>  *<tt>ClusterStatus</tt> provides clients with information such as:  *<ul>  *<li>The count and names of region servers in the cluster.</li>  *<li>The count and names of dead region servers in the cluster.</li>  *<li>The name of the active master for the cluster.</li>  *<li>The name(s) of the backup master(s) for the cluster, if they exist.</li>  *<li>The average cluster load.</li>  *<li>The number of regions deployed on the cluster.</li>  *<li>The number of requests since last report.</li>  *<li>Detailed region server loading and resource usage information,  *  per server and per region.</li>  *<li>Regions in transition at master</li>  *<li>The unique cluster ID</li>  *</ul>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ClusterStatus
extends|extends
name|VersionedWritable
block|{
comment|/**    * Version for object serialization.  Incremented for changes in serialized    * representation.    *<dl>    *<dt>0</dt><dd>Initial version</dd>    *<dt>1</dt><dd>Added cluster ID</dd>    *<dt>2</dt><dd>Added Map of ServerName to ServerLoad</dd>    *<dt>3</dt><dd>Added master and backupMasters</dd>    *</dl>    */
specifier|private
specifier|static
specifier|final
name|byte
name|VERSION
init|=
literal|2
decl_stmt|;
specifier|private
name|String
name|hbaseVersion
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerLoad
argument_list|>
name|liveServers
decl_stmt|;
specifier|private
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|deadServers
decl_stmt|;
specifier|private
name|ServerName
name|master
decl_stmt|;
specifier|private
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|backupMasters
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|RegionState
argument_list|>
name|intransition
decl_stmt|;
specifier|private
name|String
name|clusterId
decl_stmt|;
specifier|private
name|String
index|[]
name|masterCoprocessors
decl_stmt|;
specifier|private
name|Boolean
name|balancerOn
decl_stmt|;
comment|/**    * Constructor, for Writable    * @deprecated Used by Writables and Writables are going away.    */
annotation|@
name|Deprecated
specifier|public
name|ClusterStatus
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|ClusterStatus
parameter_list|(
specifier|final
name|String
name|hbaseVersion
parameter_list|,
specifier|final
name|String
name|clusterid
parameter_list|,
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerLoad
argument_list|>
name|servers
parameter_list|,
specifier|final
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|deadServers
parameter_list|,
specifier|final
name|ServerName
name|master
parameter_list|,
specifier|final
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|backupMasters
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|RegionState
argument_list|>
name|rit
parameter_list|,
specifier|final
name|String
index|[]
name|masterCoprocessors
parameter_list|,
specifier|final
name|Boolean
name|balancerOn
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
name|liveServers
operator|=
name|servers
expr_stmt|;
name|this
operator|.
name|deadServers
operator|=
name|deadServers
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|this
operator|.
name|backupMasters
operator|=
name|backupMasters
expr_stmt|;
name|this
operator|.
name|intransition
operator|=
name|rit
expr_stmt|;
name|this
operator|.
name|clusterId
operator|=
name|clusterid
expr_stmt|;
name|this
operator|.
name|masterCoprocessors
operator|=
name|masterCoprocessors
expr_stmt|;
name|this
operator|.
name|balancerOn
operator|=
name|balancerOn
expr_stmt|;
block|}
comment|/**    * @return the names of region servers on the dead list    */
specifier|public
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|getDeadServerNames
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableCollection
argument_list|(
name|deadServers
argument_list|)
return|;
block|}
comment|/**    * @return the number of region servers in the cluster    */
specifier|public
name|int
name|getServersSize
parameter_list|()
block|{
return|return
name|liveServers
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * @return the number of dead region servers in the cluster    */
specifier|public
name|int
name|getDeadServers
parameter_list|()
block|{
return|return
name|deadServers
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * @return the average cluster load    */
specifier|public
name|double
name|getAverageLoad
parameter_list|()
block|{
name|int
name|load
init|=
name|getRegionsCount
argument_list|()
decl_stmt|;
return|return
operator|(
name|double
operator|)
name|load
operator|/
operator|(
name|double
operator|)
name|getServersSize
argument_list|()
return|;
block|}
comment|/**    * @return the number of regions deployed on the cluster    */
specifier|public
name|int
name|getRegionsCount
parameter_list|()
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|ServerLoad
argument_list|>
name|e
range|:
name|this
operator|.
name|liveServers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|count
operator|+=
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getNumberOfRegions
argument_list|()
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
comment|/**    * @return the number of requests since last report    */
specifier|public
name|int
name|getRequestsCount
parameter_list|()
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|ServerLoad
argument_list|>
name|e
range|:
name|this
operator|.
name|liveServers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|count
operator|+=
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getTotalNumberOfRequests
argument_list|()
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
comment|/**    * @return the HBase version string as reported by the HMaster    */
specifier|public
name|String
name|getHBaseVersion
parameter_list|()
block|{
return|return
name|hbaseVersion
return|;
block|}
comment|/**    * @see java.lang.Object#equals(java.lang.Object)    */
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|ClusterStatus
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|(
name|getVersion
argument_list|()
operator|==
operator|(
operator|(
name|ClusterStatus
operator|)
name|o
operator|)
operator|.
name|getVersion
argument_list|()
operator|)
operator|&&
name|getHBaseVersion
argument_list|()
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|ClusterStatus
operator|)
name|o
operator|)
operator|.
name|getHBaseVersion
argument_list|()
argument_list|)
operator|&&
name|this
operator|.
name|liveServers
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|ClusterStatus
operator|)
name|o
operator|)
operator|.
name|liveServers
argument_list|)
operator|&&
name|this
operator|.
name|deadServers
operator|.
name|containsAll
argument_list|(
operator|(
operator|(
name|ClusterStatus
operator|)
name|o
operator|)
operator|.
name|deadServers
argument_list|)
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|this
operator|.
name|masterCoprocessors
argument_list|,
operator|(
operator|(
name|ClusterStatus
operator|)
name|o
operator|)
operator|.
name|masterCoprocessors
argument_list|)
operator|&&
name|this
operator|.
name|master
operator|.
name|equals
argument_list|(
operator|(
operator|(
name|ClusterStatus
operator|)
name|o
operator|)
operator|.
name|master
argument_list|)
operator|&&
name|this
operator|.
name|backupMasters
operator|.
name|containsAll
argument_list|(
operator|(
operator|(
name|ClusterStatus
operator|)
name|o
operator|)
operator|.
name|backupMasters
argument_list|)
return|;
block|}
comment|/**    * @see java.lang.Object#hashCode()    */
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|VERSION
operator|+
name|hbaseVersion
operator|.
name|hashCode
argument_list|()
operator|+
name|this
operator|.
name|liveServers
operator|.
name|hashCode
argument_list|()
operator|+
name|this
operator|.
name|deadServers
operator|.
name|hashCode
argument_list|()
operator|+
name|this
operator|.
name|master
operator|.
name|hashCode
argument_list|()
operator|+
name|this
operator|.
name|backupMasters
operator|.
name|hashCode
argument_list|()
return|;
block|}
comment|/** @return the object version number */
specifier|public
name|byte
name|getVersion
parameter_list|()
block|{
return|return
name|VERSION
return|;
block|}
comment|//
comment|// Getters
comment|//
comment|/**    * Returns detailed region server information: A list of    * {@link ServerName}.    * @return region server information    * @deprecated Use {@link #getServers()}    */
specifier|public
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|getServerInfo
parameter_list|()
block|{
return|return
name|getServers
argument_list|()
return|;
block|}
specifier|public
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|getServers
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableCollection
argument_list|(
name|this
operator|.
name|liveServers
operator|.
name|keySet
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Returns detailed information about the current master {@link ServerName}.    * @return current master information if it exists    */
specifier|public
name|ServerName
name|getMaster
parameter_list|()
block|{
return|return
name|this
operator|.
name|master
return|;
block|}
comment|/**    * @return the number of backup masters in the cluster    */
specifier|public
name|int
name|getBackupMastersSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|backupMasters
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * @return the names of backup masters    */
specifier|public
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|getBackupMasters
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableCollection
argument_list|(
name|this
operator|.
name|backupMasters
argument_list|)
return|;
block|}
comment|/**    * @param sn    * @return Server's load or null if not found.    */
specifier|public
name|ServerLoad
name|getLoad
parameter_list|(
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|this
operator|.
name|liveServers
operator|.
name|get
argument_list|(
name|sn
argument_list|)
return|;
block|}
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|RegionState
argument_list|>
name|getRegionsInTransition
parameter_list|()
block|{
return|return
name|this
operator|.
name|intransition
return|;
block|}
specifier|public
name|String
name|getClusterId
parameter_list|()
block|{
return|return
name|clusterId
return|;
block|}
specifier|public
name|String
index|[]
name|getMasterCoprocessors
parameter_list|()
block|{
return|return
name|masterCoprocessors
return|;
block|}
specifier|public
name|boolean
name|isBalancerOn
parameter_list|()
block|{
return|return
name|balancerOn
operator|!=
literal|null
operator|&&
name|balancerOn
return|;
block|}
specifier|public
name|Boolean
name|getBalancerOn
parameter_list|()
block|{
return|return
name|balancerOn
return|;
block|}
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
name|master
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\nNumber of backup masters: "
operator|+
name|backupMasters
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ServerName
name|serverName
range|:
name|backupMasters
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
name|sb
operator|.
name|append
argument_list|(
literal|"\nNumber of live region servers: "
operator|+
name|liveServers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ServerName
name|serverName
range|:
name|liveServers
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
name|sb
operator|.
name|append
argument_list|(
literal|"\nNumber of dead region servers: "
operator|+
name|deadServers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ServerName
name|serverName
range|:
name|deadServers
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
name|getRequestsCount
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\nNumber of regions: "
operator|+
name|getRegionsCount
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\nNumber of regions in transition: "
operator|+
name|intransition
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionState
name|state
range|:
name|intransition
operator|.
name|values
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
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**     * Convert a ClusterStatus to a protobuf ClusterStatus     *     * @return the protobuf ClusterStatus     */
specifier|public
name|ClusterStatusProtos
operator|.
name|ClusterStatus
name|convert
parameter_list|()
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
decl_stmt|;
name|builder
operator|.
name|setHbaseVersion
argument_list|(
name|HBaseVersionFileContent
operator|.
name|newBuilder
argument_list|()
operator|.
name|setVersion
argument_list|(
name|getHBaseVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|liveServers
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|ServerLoad
argument_list|>
name|entry
range|:
name|liveServers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|LiveServerInfo
operator|.
name|Builder
name|lsi
init|=
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
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|lsi
operator|.
name|setServerLoad
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|obtainServerLoadPB
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|addLiveServers
argument_list|(
name|lsi
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|deadServers
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ServerName
name|deadServer
range|:
name|deadServers
control|)
block|{
name|builder
operator|.
name|addDeadServers
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|deadServer
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|intransition
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|RegionState
argument_list|>
name|rit
range|:
name|getRegionsInTransition
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ClusterStatusProtos
operator|.
name|RegionState
name|rs
init|=
name|rit
operator|.
name|getValue
argument_list|()
operator|.
name|convert
argument_list|()
decl_stmt|;
name|RegionSpecifier
operator|.
name|Builder
name|spec
init|=
name|RegionSpecifier
operator|.
name|newBuilder
argument_list|()
operator|.
name|setType
argument_list|(
name|RegionSpecifierType
operator|.
name|REGION_NAME
argument_list|)
decl_stmt|;
name|spec
operator|.
name|setValue
argument_list|(
name|ByteStringer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rit
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|RegionInTransition
name|pbRIT
init|=
name|RegionInTransition
operator|.
name|newBuilder
argument_list|()
operator|.
name|setSpec
argument_list|(
name|spec
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|setRegionState
argument_list|(
name|rs
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|builder
operator|.
name|addRegionsInTransition
argument_list|(
name|pbRIT
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|clusterId
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
name|clusterId
argument_list|)
operator|.
name|convert
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|masterCoprocessors
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|coprocessor
range|:
name|masterCoprocessors
control|)
block|{
name|builder
operator|.
name|addMasterCoprocessors
argument_list|(
name|HBaseProtos
operator|.
name|Coprocessor
operator|.
name|newBuilder
argument_list|()
operator|.
name|setName
argument_list|(
name|coprocessor
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|master
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
name|getMaster
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|backupMasters
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ServerName
name|backup
range|:
name|backupMasters
control|)
block|{
name|builder
operator|.
name|addBackupMasters
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|backup
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|balancerOn
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setBalancerOn
argument_list|(
name|balancerOn
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
comment|/**    * Convert a protobuf ClusterStatus to a ClusterStatus    *    * @param proto the protobuf ClusterStatus    * @return the converted ClusterStatus    */
specifier|public
specifier|static
name|ClusterStatus
name|convert
parameter_list|(
name|ClusterStatusProtos
operator|.
name|ClusterStatus
name|proto
parameter_list|)
block|{
name|Map
argument_list|<
name|ServerName
argument_list|,
name|ServerLoad
argument_list|>
name|servers
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|proto
operator|.
name|getLiveServersList
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|servers
operator|=
operator|new
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|ServerLoad
argument_list|>
argument_list|(
name|proto
operator|.
name|getLiveServersList
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|LiveServerInfo
name|lsi
range|:
name|proto
operator|.
name|getLiveServersList
argument_list|()
control|)
block|{
name|servers
operator|.
name|put
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|lsi
operator|.
name|getServer
argument_list|()
argument_list|)
argument_list|,
operator|new
name|ServerLoad
argument_list|(
name|lsi
operator|.
name|getServerLoad
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|deadServers
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|proto
operator|.
name|getDeadServersList
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|deadServers
operator|=
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|(
name|proto
operator|.
name|getDeadServersList
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HBaseProtos
operator|.
name|ServerName
name|sn
range|:
name|proto
operator|.
name|getDeadServersList
argument_list|()
control|)
block|{
name|deadServers
operator|.
name|add
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|sn
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|backupMasters
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|proto
operator|.
name|getBackupMastersList
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|backupMasters
operator|=
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|(
name|proto
operator|.
name|getBackupMastersList
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HBaseProtos
operator|.
name|ServerName
name|sn
range|:
name|proto
operator|.
name|getBackupMastersList
argument_list|()
control|)
block|{
name|backupMasters
operator|.
name|add
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|sn
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|RegionState
argument_list|>
name|rit
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|proto
operator|.
name|getRegionsInTransitionList
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|rit
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|RegionState
argument_list|>
argument_list|(
name|proto
operator|.
name|getRegionsInTransitionList
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionInTransition
name|region
range|:
name|proto
operator|.
name|getRegionsInTransitionList
argument_list|()
control|)
block|{
name|String
name|key
init|=
operator|new
name|String
argument_list|(
name|region
operator|.
name|getSpec
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|RegionState
name|value
init|=
name|RegionState
operator|.
name|convert
argument_list|(
name|region
operator|.
name|getRegionState
argument_list|()
argument_list|)
decl_stmt|;
name|rit
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
name|String
index|[]
name|masterCoprocessors
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|proto
operator|.
name|getMasterCoprocessorsList
argument_list|()
operator|!=
literal|null
condition|)
block|{
specifier|final
name|int
name|numMasterCoprocessors
init|=
name|proto
operator|.
name|getMasterCoprocessorsCount
argument_list|()
decl_stmt|;
name|masterCoprocessors
operator|=
operator|new
name|String
index|[
name|numMasterCoprocessors
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numMasterCoprocessors
condition|;
name|i
operator|++
control|)
block|{
name|masterCoprocessors
index|[
name|i
index|]
operator|=
name|proto
operator|.
name|getMasterCoprocessors
argument_list|(
name|i
argument_list|)
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
block|}
return|return
operator|new
name|ClusterStatus
argument_list|(
name|proto
operator|.
name|getHbaseVersion
argument_list|()
operator|.
name|getVersion
argument_list|()
argument_list|,
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
argument_list|,
name|servers
argument_list|,
name|deadServers
argument_list|,
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|proto
operator|.
name|getMaster
argument_list|()
argument_list|)
argument_list|,
name|backupMasters
argument_list|,
name|rit
argument_list|,
name|masterCoprocessors
argument_list|,
name|proto
operator|.
name|getBalancerOn
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

