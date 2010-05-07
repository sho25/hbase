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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableMap
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
name|io
operator|.
name|VersionedWritable
import|;
end_import

begin_comment
comment|/**  * Status information on the HBase cluster.  *<p>  *<tt>ClusterStatus</tt> provides clients with information such as:  *<ul>  *<li>The count and names of region servers in the cluster.</li>  *<li>The count and names of dead region servers in the cluster.</li>  *<li>The average cluster load.</li>  *<li>The number of regions deployed on the cluster.</li>  *<li>The number of requests since last report.</li>  *<li>Detailed region server loading and resource usage information,  *  per server and per region.</li>  *<li>Regions in transition at master</li>  *</ul>  */
end_comment

begin_class
specifier|public
class|class
name|ClusterStatus
extends|extends
name|VersionedWritable
block|{
specifier|private
specifier|static
specifier|final
name|byte
name|VERSION
init|=
literal|0
decl_stmt|;
specifier|private
name|String
name|hbaseVersion
decl_stmt|;
specifier|private
name|Collection
argument_list|<
name|HServerInfo
argument_list|>
name|liveServerInfo
decl_stmt|;
specifier|private
name|Collection
argument_list|<
name|String
argument_list|>
name|deadServers
decl_stmt|;
specifier|private
name|NavigableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|intransition
decl_stmt|;
comment|/**    * Constructor, for Writable    */
specifier|public
name|ClusterStatus
parameter_list|()
block|{   }
comment|/**    * @return the names of region servers in the cluster    */
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|getServerNames
parameter_list|()
block|{
name|ArrayList
argument_list|<
name|String
argument_list|>
name|names
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|liveServerInfo
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HServerInfo
name|server
range|:
name|liveServerInfo
control|)
block|{
name|names
operator|.
name|add
argument_list|(
name|server
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|names
return|;
block|}
comment|/**    * @return the names of region servers on the dead list    */
specifier|public
name|Collection
argument_list|<
name|String
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
name|getServers
parameter_list|()
block|{
return|return
name|liveServerInfo
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
literal|0
decl_stmt|;
for|for
control|(
name|HServerInfo
name|server
range|:
name|liveServerInfo
control|)
block|{
name|load
operator|+=
name|server
operator|.
name|getLoad
argument_list|()
operator|.
name|getLoad
argument_list|()
expr_stmt|;
block|}
return|return
operator|(
name|double
operator|)
name|load
operator|/
operator|(
name|double
operator|)
name|liveServerInfo
operator|.
name|size
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
name|HServerInfo
name|server
range|:
name|liveServerInfo
control|)
block|{
name|count
operator|+=
name|server
operator|.
name|getLoad
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
name|HServerInfo
name|server
range|:
name|liveServerInfo
control|)
block|{
name|count
operator|+=
name|server
operator|.
name|getLoad
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
comment|/**    * @param version the HBase version string    */
specifier|public
name|void
name|setHBaseVersion
parameter_list|(
name|String
name|version
parameter_list|)
block|{
name|hbaseVersion
operator|=
name|version
expr_stmt|;
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
name|liveServerInfo
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
name|liveServerInfo
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
name|liveServerInfo
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
comment|/**    * Returns detailed region server information: A list of    * {@link HServerInfo}, containing server load and resource usage    * statistics as {@link HServerLoad}, containing per-region    * statistics as {@link HServerLoad.RegionLoad}.    * @return region server information    */
specifier|public
name|Collection
argument_list|<
name|HServerInfo
argument_list|>
name|getServerInfo
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableCollection
argument_list|(
name|liveServerInfo
argument_list|)
return|;
block|}
comment|//
comment|// Setters
comment|//
specifier|public
name|void
name|setServerInfo
parameter_list|(
name|Collection
argument_list|<
name|HServerInfo
argument_list|>
name|serverInfo
parameter_list|)
block|{
name|this
operator|.
name|liveServerInfo
operator|=
name|serverInfo
expr_stmt|;
block|}
specifier|public
name|void
name|setDeadServers
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|deadServers
parameter_list|)
block|{
name|this
operator|.
name|deadServers
operator|=
name|deadServers
expr_stmt|;
block|}
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
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
name|void
name|setRegionsInTransition
parameter_list|(
specifier|final
name|NavigableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|m
parameter_list|)
block|{
name|this
operator|.
name|intransition
operator|=
name|m
expr_stmt|;
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
name|liveServerInfo
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HServerInfo
name|server
range|:
name|liveServerInfo
control|)
block|{
name|server
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
name|String
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
name|String
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
name|out
operator|.
name|writeUTF
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|liveServerInfo
operator|=
operator|new
name|ArrayList
argument_list|<
name|HServerInfo
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
name|HServerInfo
name|info
init|=
operator|new
name|HServerInfo
argument_list|()
decl_stmt|;
name|info
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|liveServerInfo
operator|.
name|add
argument_list|(
name|info
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
name|String
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
name|in
operator|.
name|readUTF
argument_list|()
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
name|String
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
name|String
name|value
init|=
name|in
operator|.
name|readUTF
argument_list|()
decl_stmt|;
name|this
operator|.
name|intransition
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
block|}
end_class

end_unit

