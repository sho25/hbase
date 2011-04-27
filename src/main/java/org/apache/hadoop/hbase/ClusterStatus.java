begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|AssignmentManager
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
name|io
operator|.
name|VersionedWritable
import|;
end_import

begin_comment
comment|/**  * Status information on the HBase cluster.  *<p>  *<tt>ClusterStatus</tt> provides clients with information such as:  *<ul>  *<li>The count and names of region servers in the cluster.</li>  *<li>The count and names of dead region servers in the cluster.</li>  *<li>The average cluster load.</li>  *<li>The number of regions deployed on the cluster.</li>  *<li>The number of requests since last report.</li>  *<li>Detailed region server loading and resource usage information,  *  per server and per region.</li>  *<li>Regions in transition at master</li>  *<li>The unique cluster ID</li>  *</ul>  */
end_comment

begin_class
specifier|public
class|class
name|ClusterStatus
extends|extends
name|VersionedWritable
block|{
comment|/**    * Version for object serialization.  Incremented for changes in serialized    * representation.    *<dl>    *<dt>0</dt><dd>initial version</dd>    *<dt>1</dt><dd>added cluster ID</dd>    *<dt>2</dt><dd>Added Map of ServerName to ServerLoad</dd>    *</dl>    */
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
name|HServerLoad
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
comment|/**    * Constructor, for Writable    */
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
name|HServerLoad
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
name|Map
argument_list|<
name|String
argument_list|,
name|RegionState
argument_list|>
name|rit
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
name|HServerLoad
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
name|HServerLoad
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
name|getNumberOfRequests
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
name|deadServers
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
name|deadServers
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
name|deadServers
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
comment|/**    * @param sn    * @return Server's load or null if not found.    */
specifier|public
name|HServerLoad
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
comment|//
comment|// Writable
comment|//
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|hbaseVersion
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|getServersSize
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|HServerLoad
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
name|out
operator|.
name|writeUTF
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|deadServers
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ServerName
name|server
range|:
name|deadServers
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|server
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|intransition
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
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
name|e
range|:
name|this
operator|.
name|intransition
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeUTF
argument_list|(
name|clusterId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|hbaseVersion
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|int
name|count
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|this
operator|.
name|liveServers
operator|=
operator|new
name|HashMap
argument_list|<
name|ServerName
argument_list|,
name|HServerLoad
argument_list|>
argument_list|(
name|count
argument_list|)
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|String
name|str
init|=
name|in
operator|.
name|readUTF
argument_list|()
decl_stmt|;
name|HServerLoad
name|hsl
init|=
operator|new
name|HServerLoad
argument_list|()
decl_stmt|;
name|hsl
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|liveServers
operator|.
name|put
argument_list|(
operator|new
name|ServerName
argument_list|(
name|str
argument_list|)
argument_list|,
name|hsl
argument_list|)
expr_stmt|;
block|}
name|count
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|deadServers
operator|=
operator|new
name|ArrayList
argument_list|<
name|ServerName
argument_list|>
argument_list|(
name|count
argument_list|)
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|deadServers
operator|.
name|add
argument_list|(
operator|new
name|ServerName
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|count
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|this
operator|.
name|intransition
operator|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|RegionState
argument_list|>
argument_list|()
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
name|in
operator|.
name|readUTF
argument_list|()
decl_stmt|;
name|RegionState
name|regionState
init|=
operator|new
name|RegionState
argument_list|()
decl_stmt|;
name|regionState
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|intransition
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|regionState
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|clusterId
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

