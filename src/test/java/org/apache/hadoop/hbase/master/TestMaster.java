begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

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
name|MiniHBaseCluster
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
name|catalog
operator|.
name|MetaReader
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
name|HBaseAdmin
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
name|HTable
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
name|executor
operator|.
name|EventHandler
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
name|executor
operator|.
name|EventHandler
operator|.
name|EventHandlerListener
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
name|executor
operator|.
name|EventHandler
operator|.
name|EventType
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
name|List
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
name|CountDownLatch
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
name|TimeUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
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
name|*
import|;
end_import

begin_class
specifier|public
class|class
name|TestMaster
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
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
name|TestMaster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|TABLENAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TestMaster"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILYNAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeAllTests
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Start a cluster of two regionservers.
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterAllTests
parameter_list|()
throws|throws
name|IOException
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMasterOpsWhileSplitting
parameter_list|()
throws|throws
name|Exception
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
name|HMaster
name|m
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|HBaseAdmin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLENAME
argument_list|,
name|FAMILYNAME
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TABLENAME
argument_list|)
argument_list|,
name|FAMILYNAME
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|>
name|tableRegions
init|=
name|MetaReader
operator|.
name|getTableRegionsAndLocations
argument_list|(
name|m
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|TABLENAME
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Regions after load: "
operator|+
name|Joiner
operator|.
name|on
argument_list|(
literal|','
argument_list|)
operator|.
name|join
argument_list|(
name|tableRegions
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tableRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|tableRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|,
name|tableRegions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|getEndKey
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now trigger a split and stop when the split is in progress
name|CountDownLatch
name|split
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|CountDownLatch
name|proceed
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|RegionSplitListener
name|list
init|=
operator|new
name|RegionSplitListener
argument_list|(
name|split
argument_list|,
name|proceed
argument_list|)
decl_stmt|;
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|executorService
operator|.
name|registerListener
argument_list|(
name|EventType
operator|.
name|RS_ZK_REGION_SPLIT
argument_list|,
name|list
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Splitting table"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|split
argument_list|(
name|TABLENAME
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for split result to be about to open"
argument_list|)
expr_stmt|;
name|split
operator|.
name|await
argument_list|(
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Making sure we can call getTableRegions while opening"
argument_list|)
expr_stmt|;
name|tableRegions
operator|=
name|MetaReader
operator|.
name|getTableRegionsAndLocations
argument_list|(
name|m
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|TABLENAME
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Regions: "
operator|+
name|Joiner
operator|.
name|on
argument_list|(
literal|','
argument_list|)
operator|.
name|join
argument_list|(
name|tableRegions
argument_list|)
argument_list|)
expr_stmt|;
comment|// We have three regions because one is split-in-progress
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|tableRegions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Making sure we can call getTableRegionClosest while opening"
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|pair
init|=
name|m
operator|.
name|getTableRegionForRow
argument_list|(
name|TABLENAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cde"
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Result is: "
operator|+
name|pair
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|tableRegionFromName
init|=
name|MetaReader
operator|.
name|getRegion
argument_list|(
name|m
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|pair
operator|.
name|getFirst
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|tableRegionFromName
operator|.
name|getFirst
argument_list|()
argument_list|,
name|pair
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|proceed
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|RegionSplitListener
implements|implements
name|EventHandlerListener
block|{
name|CountDownLatch
name|split
decl_stmt|,
name|proceed
decl_stmt|;
specifier|public
name|RegionSplitListener
parameter_list|(
name|CountDownLatch
name|split
parameter_list|,
name|CountDownLatch
name|proceed
parameter_list|)
block|{
name|this
operator|.
name|split
operator|=
name|split
expr_stmt|;
name|this
operator|.
name|proceed
operator|=
name|proceed
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|afterProcess
parameter_list|(
name|EventHandler
name|event
parameter_list|)
block|{
if|if
condition|(
name|event
operator|.
name|getEventType
argument_list|()
operator|!=
name|EventType
operator|.
name|RS_ZK_REGION_SPLIT
condition|)
block|{
return|return;
block|}
try|try
block|{
name|split
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|proceed
operator|.
name|await
argument_list|(
literal|60
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ie
argument_list|)
throw|;
block|}
return|return;
block|}
annotation|@
name|Override
specifier|public
name|void
name|beforeProcess
parameter_list|(
name|EventHandler
name|event
parameter_list|)
block|{     }
block|}
block|}
end_class

end_unit

