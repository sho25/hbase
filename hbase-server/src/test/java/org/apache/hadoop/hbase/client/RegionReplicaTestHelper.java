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
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
name|HBaseTestingUtility
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
name|HConstants
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
name|NotServingRegionException
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
name|TableName
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
name|Waiter
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
name|Waiter
operator|.
name|ExplainingPredicate
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
name|regionserver
operator|.
name|Region
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
name|JVMClusterUtil
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|RegionReplicaTestHelper
block|{
specifier|private
name|RegionReplicaTestHelper
parameter_list|()
block|{   }
comment|// waits for all replicas to have region location
specifier|static
name|void
name|waitUntilAllMetaReplicasAreReady
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|ConnectionRegistry
name|registry
parameter_list|)
block|{
name|Configuration
name|conf
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|int
name|regionReplicaCount
init|=
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|META_REPLICAS_NUM
argument_list|,
name|HConstants
operator|.
name|DEFAULT_META_REPLICA_NUM
argument_list|)
decl_stmt|;
name|Waiter
operator|.
name|waitFor
argument_list|(
name|conf
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.client.sync.wait.timeout.msec"
argument_list|,
literal|60000
argument_list|)
argument_list|,
literal|200
argument_list|,
literal|true
argument_list|,
operator|new
name|ExplainingPredicate
argument_list|<
name|IOException
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|String
name|explainFailure
parameter_list|()
block|{
return|return
literal|"Not all meta replicas get assigned"
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
block|{
try|try
block|{
name|RegionLocations
name|locs
init|=
name|registry
operator|.
name|getMetaRegionLocations
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|locs
operator|.
name|size
argument_list|()
operator|<
name|regionReplicaCount
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|regionReplicaCount
condition|;
name|i
operator|++
control|)
block|{
name|HRegionLocation
name|loc
init|=
name|locs
operator|.
name|getRegionLocation
argument_list|(
name|i
argument_list|)
decl_stmt|;
comment|// Wait until the replica is served by a region server. There could be delay between
comment|// the replica being available to the connection and region server opening it.
name|Optional
argument_list|<
name|ServerName
argument_list|>
name|rsCarryingReplica
init|=
name|getRSCarryingReplica
argument_list|(
name|util
argument_list|,
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getTable
argument_list|()
argument_list|,
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|rsCarryingReplica
operator|.
name|isPresent
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|TestZKConnectionRegistry
operator|.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get meta region locations"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|static
name|Optional
argument_list|<
name|ServerName
argument_list|>
name|getRSCarryingReplica
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|int
name|replicaId
parameter_list|)
block|{
return|return
name|util
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|t
lambda|->
name|t
operator|.
name|getRegionServer
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
name|rs
lambda|->
name|rs
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|r
lambda|->
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getReplicaId
argument_list|()
operator|==
name|replicaId
argument_list|)
argument_list|)
operator|.
name|findAny
argument_list|()
operator|.
name|map
argument_list|(
name|rs
lambda|->
name|rs
operator|.
name|getServerName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Return the new location.    */
specifier|static
name|ServerName
name|moveRegion
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|HRegionLocation
name|currentLoc
parameter_list|)
throws|throws
name|Exception
block|{
name|ServerName
name|serverName
init|=
name|currentLoc
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|RegionInfo
name|regionInfo
init|=
name|currentLoc
operator|.
name|getRegion
argument_list|()
decl_stmt|;
name|TableName
name|tableName
init|=
name|regionInfo
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|int
name|replicaId
init|=
name|regionInfo
operator|.
name|getReplicaId
argument_list|()
decl_stmt|;
name|ServerName
name|newServerName
init|=
name|util
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|t
lambda|->
name|t
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
name|sn
lambda|->
operator|!
name|sn
operator|.
name|equals
argument_list|(
name|serverName
argument_list|)
argument_list|)
operator|.
name|findAny
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|move
argument_list|(
name|regionInfo
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|newServerName
argument_list|)
expr_stmt|;
name|util
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
operator|new
name|ExplainingPredicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
name|Optional
argument_list|<
name|ServerName
argument_list|>
name|newServerName
init|=
name|getRSCarryingReplica
argument_list|(
name|util
argument_list|,
name|tableName
argument_list|,
name|replicaId
argument_list|)
decl_stmt|;
return|return
name|newServerName
operator|.
name|isPresent
argument_list|()
operator|&&
operator|!
name|newServerName
operator|.
name|get
argument_list|()
operator|.
name|equals
argument_list|(
name|serverName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|explainFailure
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" is still on "
operator|+
name|serverName
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|newServerName
return|;
block|}
interface|interface
name|Locator
block|{
name|RegionLocations
name|getRegionLocations
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|int
name|replicaId
parameter_list|,
name|boolean
name|reload
parameter_list|)
throws|throws
name|Exception
function_decl|;
name|void
name|updateCachedLocationOnError
parameter_list|(
name|HRegionLocation
name|loc
parameter_list|,
name|Throwable
name|error
parameter_list|)
throws|throws
name|Exception
function_decl|;
block|}
specifier|static
name|void
name|testLocator
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|Locator
name|locator
parameter_list|)
throws|throws
name|Exception
block|{
name|RegionLocations
name|locs
init|=
name|locator
operator|.
name|getRegionLocations
argument_list|(
name|tableName
argument_list|,
name|RegionReplicaUtil
operator|.
name|DEFAULT_REPLICA_ID
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|locs
operator|.
name|size
argument_list|()
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|HRegionLocation
name|loc
init|=
name|locs
operator|.
name|getRegionLocation
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|loc
argument_list|)
expr_stmt|;
name|ServerName
name|serverName
init|=
name|getRSCarryingReplica
argument_list|(
name|util
argument_list|,
name|tableName
argument_list|,
name|i
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|serverName
argument_list|,
name|loc
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ServerName
name|newServerName
init|=
name|moveRegion
argument_list|(
name|util
argument_list|,
name|locs
operator|.
name|getDefaultRegionLocation
argument_list|()
argument_list|)
decl_stmt|;
comment|// The cached location should not be changed
name|assertEquals
argument_list|(
name|locs
operator|.
name|getDefaultRegionLocation
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|,
name|locator
operator|.
name|getRegionLocations
argument_list|(
name|tableName
argument_list|,
name|RegionReplicaUtil
operator|.
name|DEFAULT_REPLICA_ID
argument_list|,
literal|false
argument_list|)
operator|.
name|getDefaultRegionLocation
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// should get the new location when reload = true
name|assertEquals
argument_list|(
name|newServerName
argument_list|,
name|locator
operator|.
name|getRegionLocations
argument_list|(
name|tableName
argument_list|,
name|RegionReplicaUtil
operator|.
name|DEFAULT_REPLICA_ID
argument_list|,
literal|true
argument_list|)
operator|.
name|getDefaultRegionLocation
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// the cached location should be replaced
name|assertEquals
argument_list|(
name|newServerName
argument_list|,
name|locator
operator|.
name|getRegionLocations
argument_list|(
name|tableName
argument_list|,
name|RegionReplicaUtil
operator|.
name|DEFAULT_REPLICA_ID
argument_list|,
literal|false
argument_list|)
operator|.
name|getDefaultRegionLocation
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|ServerName
name|newServerName1
init|=
name|moveRegion
argument_list|(
name|util
argument_list|,
name|locs
operator|.
name|getRegionLocation
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|ServerName
name|newServerName2
init|=
name|moveRegion
argument_list|(
name|util
argument_list|,
name|locs
operator|.
name|getRegionLocation
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
comment|// The cached location should not be change
name|assertEquals
argument_list|(
name|locs
operator|.
name|getRegionLocation
argument_list|(
literal|1
argument_list|)
operator|.
name|getServerName
argument_list|()
argument_list|,
name|locator
operator|.
name|getRegionLocations
argument_list|(
name|tableName
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
operator|.
name|getRegionLocation
argument_list|(
literal|1
argument_list|)
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// clear the cached location for replica 1
name|locator
operator|.
name|updateCachedLocationOnError
argument_list|(
name|locs
operator|.
name|getRegionLocation
argument_list|(
literal|1
argument_list|)
argument_list|,
operator|new
name|NotServingRegionException
argument_list|()
argument_list|)
expr_stmt|;
comment|// the cached location for replica 2 should not be changed
name|assertEquals
argument_list|(
name|locs
operator|.
name|getRegionLocation
argument_list|(
literal|2
argument_list|)
operator|.
name|getServerName
argument_list|()
argument_list|,
name|locator
operator|.
name|getRegionLocations
argument_list|(
name|tableName
argument_list|,
literal|2
argument_list|,
literal|false
argument_list|)
operator|.
name|getRegionLocation
argument_list|(
literal|2
argument_list|)
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// should get the new location as we have cleared the old location
name|assertEquals
argument_list|(
name|newServerName1
argument_list|,
name|locator
operator|.
name|getRegionLocations
argument_list|(
name|tableName
argument_list|,
literal|1
argument_list|,
literal|false
argument_list|)
operator|.
name|getRegionLocation
argument_list|(
literal|1
argument_list|)
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// as we will get the new location for replica 2 at once, we should also get the new location
comment|// for replica 2
name|assertEquals
argument_list|(
name|newServerName2
argument_list|,
name|locator
operator|.
name|getRegionLocations
argument_list|(
name|tableName
argument_list|,
literal|2
argument_list|,
literal|false
argument_list|)
operator|.
name|getRegionLocation
argument_list|(
literal|2
argument_list|)
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|assertReplicaDistributed
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|Table
name|t
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|t
operator|.
name|getDescriptor
argument_list|()
operator|.
name|getRegionReplication
argument_list|()
operator|<=
literal|1
condition|)
block|{
return|return;
block|}
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionInfos
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|rs
range|:
name|util
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|regionInfos
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Region
name|r
range|:
name|rs
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getRegions
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
control|)
block|{
if|if
condition|(
name|contains
argument_list|(
name|regionInfos
argument_list|,
name|r
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
condition|)
block|{
name|fail
argument_list|(
literal|"Replica regions should be assigned to different region servers"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|regionInfos
operator|.
name|add
argument_list|(
name|r
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
specifier|static
name|boolean
name|contains
parameter_list|(
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regionInfos
parameter_list|,
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
for|for
control|(
name|RegionInfo
name|info
range|:
name|regionInfos
control|)
block|{
if|if
condition|(
name|RegionReplicaUtil
operator|.
name|isReplicasForSameRegion
argument_list|(
name|info
argument_list|,
name|regionInfo
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

