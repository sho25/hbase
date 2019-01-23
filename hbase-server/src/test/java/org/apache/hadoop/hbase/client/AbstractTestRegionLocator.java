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
name|assertArrayEquals
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
name|assertEquals
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
name|Collections
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
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractTestRegionLocator
block|{
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"Locator"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|int
name|REGION_REPLICATION
init|=
literal|3
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
index|[]
name|SPLIT_KEYS
decl_stmt|;
specifier|protected
specifier|static
name|void
name|startClusterAndCreateTable
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|TableDescriptor
name|td
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|setRegionReplication
argument_list|(
name|REGION_REPLICATION
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|SPLIT_KEYS
operator|=
operator|new
name|byte
index|[
literal|9
index|]
index|[]
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
literal|9
condition|;
name|i
operator|++
control|)
block|{
name|SPLIT_KEYS
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|td
argument_list|,
name|SPLIT_KEYS
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|balancerSwitch
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDownAfterTest
parameter_list|()
throws|throws
name|IOException
block|{
name|clearCache
argument_list|()
expr_stmt|;
block|}
specifier|private
name|byte
index|[]
name|getStartKey
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|index
operator|==
literal|0
condition|?
name|HConstants
operator|.
name|EMPTY_START_ROW
else|:
name|SPLIT_KEYS
index|[
name|index
operator|-
literal|1
index|]
return|;
block|}
specifier|private
name|byte
index|[]
name|getEndKey
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|index
operator|==
name|SPLIT_KEYS
operator|.
name|length
condition|?
name|HConstants
operator|.
name|EMPTY_END_ROW
else|:
name|SPLIT_KEYS
index|[
name|index
index|]
return|;
block|}
specifier|private
name|void
name|assertStartKeys
parameter_list|(
name|byte
index|[]
index|[]
name|startKeys
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|SPLIT_KEYS
operator|.
name|length
operator|+
literal|1
argument_list|,
name|startKeys
operator|.
name|length
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
name|startKeys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertArrayEquals
argument_list|(
name|getStartKey
argument_list|(
name|i
argument_list|)
argument_list|,
name|startKeys
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|assertEndKeys
parameter_list|(
name|byte
index|[]
index|[]
name|endKeys
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|SPLIT_KEYS
operator|.
name|length
operator|+
literal|1
argument_list|,
name|endKeys
operator|.
name|length
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
name|endKeys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertArrayEquals
argument_list|(
name|getEndKey
argument_list|(
name|i
argument_list|)
argument_list|,
name|endKeys
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStartEndKeys
parameter_list|()
throws|throws
name|IOException
block|{
name|assertStartKeys
argument_list|(
name|getStartKeys
argument_list|()
argument_list|)
expr_stmt|;
name|assertEndKeys
argument_list|(
name|getEndKeys
argument_list|()
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|byte
index|[]
index|[]
argument_list|,
name|byte
index|[]
index|[]
argument_list|>
name|startEndKeys
init|=
name|getStartEndKeys
argument_list|()
decl_stmt|;
name|assertStartKeys
argument_list|(
name|startEndKeys
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
name|assertEndKeys
argument_list|(
name|startEndKeys
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertRegionLocation
parameter_list|(
name|HRegionLocation
name|loc
parameter_list|,
name|int
name|index
parameter_list|,
name|int
name|replicaId
parameter_list|)
block|{
name|RegionInfo
name|region
init|=
name|loc
operator|.
name|getRegion
argument_list|()
decl_stmt|;
name|byte
index|[]
name|startKey
init|=
name|getStartKey
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|startKey
argument_list|,
name|region
operator|.
name|getStartKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|getEndKey
argument_list|(
name|index
argument_list|)
argument_list|,
name|region
operator|.
name|getEndKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|replicaId
argument_list|,
name|region
operator|.
name|getReplicaId
argument_list|()
argument_list|)
expr_stmt|;
name|ServerName
name|expected
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
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
name|TABLE_NAME
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|Region
operator|::
name|getRegionInfo
argument_list|)
operator|.
name|anyMatch
argument_list|(
name|r
lambda|->
name|r
operator|.
name|containsRow
argument_list|(
name|startKey
argument_list|)
operator|&&
name|r
operator|.
name|getReplicaId
argument_list|()
operator|==
name|replicaId
argument_list|)
argument_list|)
operator|.
name|findFirst
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|loc
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetRegionLocation
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|SPLIT_KEYS
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|replicaId
init|=
literal|0
init|;
name|replicaId
operator|<
name|REGION_REPLICATION
condition|;
name|replicaId
operator|++
control|)
block|{
name|assertRegionLocation
argument_list|(
name|getRegionLocation
argument_list|(
name|getStartKey
argument_list|(
name|i
argument_list|)
argument_list|,
name|replicaId
argument_list|)
argument_list|,
name|i
argument_list|,
name|replicaId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetRegionLocations
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|SPLIT_KEYS
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locs
init|=
name|getRegionLocations
argument_list|(
name|getStartKey
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|REGION_REPLICATION
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
name|replicaId
init|=
literal|0
init|;
name|replicaId
operator|<
name|REGION_REPLICATION
condition|;
name|replicaId
operator|++
control|)
block|{
name|assertRegionLocation
argument_list|(
name|locs
operator|.
name|get
argument_list|(
name|replicaId
argument_list|)
argument_list|,
name|i
argument_list|,
name|replicaId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetAllRegionLocations
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|locs
init|=
name|getAllRegionLocations
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|REGION_REPLICATION
operator|*
operator|(
name|SPLIT_KEYS
operator|.
name|length
operator|+
literal|1
operator|)
argument_list|,
name|locs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|locs
argument_list|,
parameter_list|(
name|l1
parameter_list|,
name|l2
parameter_list|)
lambda|->
block|{
name|int
name|c
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|l1
operator|.
name|getRegion
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|l2
operator|.
name|getRegion
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
block|{
return|return
name|c
return|;
block|}
return|return
name|Integer
operator|.
name|compare
argument_list|(
name|l1
operator|.
name|getRegion
argument_list|()
operator|.
name|getReplicaId
argument_list|()
argument_list|,
name|l2
operator|.
name|getRegion
argument_list|()
operator|.
name|getReplicaId
argument_list|()
argument_list|)
return|;
block|}
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
operator|<=
name|SPLIT_KEYS
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|replicaId
init|=
literal|0
init|;
name|replicaId
operator|<
name|REGION_REPLICATION
condition|;
name|replicaId
operator|++
control|)
block|{
name|assertRegionLocation
argument_list|(
name|locs
operator|.
name|get
argument_list|(
name|i
operator|*
name|REGION_REPLICATION
operator|+
name|replicaId
argument_list|)
argument_list|,
name|i
argument_list|,
name|replicaId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|protected
specifier|abstract
name|byte
index|[]
index|[]
name|getStartKeys
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|protected
specifier|abstract
name|byte
index|[]
index|[]
name|getEndKeys
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|protected
specifier|abstract
name|Pair
argument_list|<
name|byte
index|[]
index|[]
argument_list|,
name|byte
index|[]
index|[]
argument_list|>
name|getStartEndKeys
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|protected
specifier|abstract
name|HRegionLocation
name|getRegionLocation
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|int
name|replicaId
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|protected
specifier|abstract
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|getRegionLocations
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|protected
specifier|abstract
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|getAllRegionLocations
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|protected
specifier|abstract
name|void
name|clearCache
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

