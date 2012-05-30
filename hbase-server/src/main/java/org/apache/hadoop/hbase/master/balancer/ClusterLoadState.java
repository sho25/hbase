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
name|master
operator|.
name|balancer
package|;
end_package

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
name|hbase
operator|.
name|HRegionInfo
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

begin_comment
comment|/**  * Class used to hold the current state of the cluster and how balanced it is.  */
end_comment

begin_class
specifier|public
class|class
name|ClusterLoadState
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|clusterState
decl_stmt|;
specifier|private
specifier|final
name|NavigableMap
argument_list|<
name|ServerAndLoad
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|serversByLoad
decl_stmt|;
specifier|private
name|boolean
name|emptyRegionServerPresent
init|=
literal|false
decl_stmt|;
specifier|private
name|int
name|numRegions
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|numServers
init|=
literal|0
decl_stmt|;
specifier|public
name|ClusterLoadState
parameter_list|(
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|clusterState
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|numRegions
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|numServers
operator|=
name|clusterState
operator|.
name|size
argument_list|()
expr_stmt|;
name|this
operator|.
name|clusterState
operator|=
name|clusterState
expr_stmt|;
name|serversByLoad
operator|=
operator|new
name|TreeMap
argument_list|<
name|ServerAndLoad
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
comment|// Iterate so we can count regions as we build the map
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|server
range|:
name|clusterState
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|server
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|int
name|sz
init|=
name|regions
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|sz
operator|==
literal|0
condition|)
name|emptyRegionServerPresent
operator|=
literal|true
expr_stmt|;
name|numRegions
operator|+=
name|sz
expr_stmt|;
name|serversByLoad
operator|.
name|put
argument_list|(
operator|new
name|ServerAndLoad
argument_list|(
name|server
operator|.
name|getKey
argument_list|()
argument_list|,
name|sz
argument_list|)
argument_list|,
name|regions
argument_list|)
expr_stmt|;
block|}
block|}
name|Map
argument_list|<
name|ServerName
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|getClusterState
parameter_list|()
block|{
return|return
name|clusterState
return|;
block|}
name|NavigableMap
argument_list|<
name|ServerAndLoad
argument_list|,
name|List
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|>
name|getServersByLoad
parameter_list|()
block|{
return|return
name|serversByLoad
return|;
block|}
name|boolean
name|isEmptyRegionServerPresent
parameter_list|()
block|{
return|return
name|emptyRegionServerPresent
return|;
block|}
name|int
name|getNumRegions
parameter_list|()
block|{
return|return
name|numRegions
return|;
block|}
name|int
name|getNumServers
parameter_list|()
block|{
return|return
name|numServers
return|;
block|}
name|float
name|getLoadAverage
parameter_list|()
block|{
return|return
operator|(
name|float
operator|)
name|numRegions
operator|/
name|numServers
return|;
block|}
name|int
name|getMinLoad
parameter_list|()
block|{
return|return
name|getServersByLoad
argument_list|()
operator|.
name|lastKey
argument_list|()
operator|.
name|getLoad
argument_list|()
return|;
block|}
name|int
name|getMaxLoad
parameter_list|()
block|{
return|return
name|getServersByLoad
argument_list|()
operator|.
name|firstKey
argument_list|()
operator|.
name|getLoad
argument_list|()
return|;
block|}
block|}
end_class

end_unit

