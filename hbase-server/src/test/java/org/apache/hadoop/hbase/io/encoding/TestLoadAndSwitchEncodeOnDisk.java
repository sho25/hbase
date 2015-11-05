begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|io
operator|.
name|encoding
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
name|client
operator|.
name|Admin
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
name|RegionLocator
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
name|Table
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
name|testclassification
operator|.
name|IOTests
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
name|testclassification
operator|.
name|MediumTests
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
name|ResultScanner
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
name|compress
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
name|io
operator|.
name|hfile
operator|.
name|CacheConfig
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
name|HRegionServer
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
name|TestMiniClusterLoadSequential
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
name|Threads
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_comment
comment|/**  * Uses the load tester  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|IOTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestLoadAndSwitchEncodeOnDisk
extends|extends
name|TestMiniClusterLoadSequential
block|{
comment|/** We do not alternate the multi-put flag in this test. */
specifier|private
specifier|static
specifier|final
name|boolean
name|USE_MULTI_PUT
init|=
literal|true
decl_stmt|;
comment|/** Un-parameterize the test */
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
index|[]
block|{
operator|new
name|Object
index|[
literal|0
index|]
block|}
argument_list|)
return|;
block|}
specifier|public
name|TestLoadAndSwitchEncodeOnDisk
parameter_list|()
block|{
name|super
argument_list|(
name|USE_MULTI_PUT
argument_list|,
name|DataBlockEncoding
operator|.
name|PREFIX
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|CacheConfig
operator|.
name|CACHE_BLOCKS_ON_WRITE_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|int
name|numKeys
parameter_list|()
block|{
return|return
literal|3000
return|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
name|TIMEOUT_MS
argument_list|)
specifier|public
name|void
name|loadTest
parameter_list|()
throws|throws
name|Exception
block|{
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|compression
operator|=
name|Compression
operator|.
name|Algorithm
operator|.
name|GZ
expr_stmt|;
comment|// used for table setup
name|super
operator|.
name|loadTest
argument_list|()
expr_stmt|;
name|HColumnDescriptor
name|hcd
init|=
name|getColumnDesc
argument_list|(
name|admin
argument_list|)
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"\nDisabling encode-on-disk. Old column descriptor: "
operator|+
name|hcd
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE
argument_list|)
decl_stmt|;
name|assertAllOnLine
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyColumnFamily
argument_list|(
name|TABLE
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"\nRe-enabling table\n"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"\nNew column descriptor: "
operator|+
name|getColumnDesc
argument_list|(
name|admin
argument_list|)
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
comment|// The table may not have all regions on line yet.  Assert online before
comment|// moving to major compact.
name|assertAllOnLine
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"\nCompacting the table\n"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|majorCompact
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
comment|// Wait until compaction completes
name|Threads
operator|.
name|sleepWithoutInterrupt
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
name|HRegionServer
name|rs
init|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
decl_stmt|;
while|while
condition|(
name|rs
operator|.
name|compactSplitThread
operator|.
name|getCompactionQueueSize
argument_list|()
operator|>
literal|0
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|50
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"\nDone with the test, shutting down the cluster\n"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertAllOnLine
parameter_list|(
specifier|final
name|Table
name|t
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|HRegionLocation
argument_list|>
name|regions
decl_stmt|;
try|try
init|(
name|RegionLocator
name|rl
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
init|)
block|{
name|regions
operator|=
name|rl
operator|.
name|getAllRegionLocations
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|HRegionLocation
name|e
range|:
name|regions
control|)
block|{
name|byte
index|[]
name|startkey
init|=
name|e
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getStartKey
argument_list|()
decl_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|(
name|startkey
argument_list|)
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
argument_list|(
name|r
operator|!=
literal|null
operator|&&
name|r
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

