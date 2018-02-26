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
name|ExecutionException
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeoutException
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
name|fs
operator|.
name|Path
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
name|Put
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
name|RegionInfo
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|wal
operator|.
name|AbstractFSWAL
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
name|LargeTests
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
name|RegionServerTests
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
name|wal
operator|.
name|WAL
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
name|wal
operator|.
name|WALFactory
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
name|ClassRule
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

begin_comment
comment|/**  * Testcase for HBASE-20066  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestSequenceIdMonotonicallyIncreasing
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestSequenceIdMonotonicallyIncreasing
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CQ
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cq"
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|IOException
block|{
name|Admin
name|admin
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|NAME
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Table
name|createTable
parameter_list|(
name|boolean
name|multiRegions
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|multiRegions
condition|)
block|{
return|return
name|UTIL
operator|.
name|createTable
argument_list|(
name|NAME
argument_list|,
name|CF
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
block|}
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|UTIL
operator|.
name|createTable
argument_list|(
name|NAME
argument_list|,
name|CF
argument_list|)
return|;
block|}
block|}
specifier|private
name|long
name|getMaxSeqId
parameter_list|(
name|HRegionServer
name|rs
parameter_list|,
name|RegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|walFile
init|=
operator|(
operator|(
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
operator|)
name|rs
operator|.
name|getWAL
argument_list|(
literal|null
argument_list|)
operator|)
operator|.
name|getCurrentFileName
argument_list|()
decl_stmt|;
name|long
name|maxSeqId
init|=
operator|-
literal|1L
decl_stmt|;
try|try
init|(
name|WAL
operator|.
name|Reader
name|reader
init|=
name|WALFactory
operator|.
name|createReader
argument_list|(
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|walFile
argument_list|,
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
init|)
block|{
for|for
control|(
init|;
condition|;
control|)
block|{
name|WAL
operator|.
name|Entry
name|entry
init|=
name|reader
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|region
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getEncodedRegionName
argument_list|()
argument_list|)
condition|)
block|{
name|maxSeqId
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxSeqId
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|maxSeqId
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSplit
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ExecutionException
throws|,
name|TimeoutException
block|{
try|try
init|(
name|Table
name|table
init|=
name|createTable
argument_list|(
literal|false
argument_list|)
init|)
block|{
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|UTIL
operator|.
name|flush
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
name|HRegionServer
name|rs
init|=
name|UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|NAME
argument_list|)
decl_stmt|;
name|RegionInfo
name|region
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|splitRegionAsync
argument_list|(
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
expr_stmt|;
name|long
name|maxSeqId
init|=
name|getMaxSeqId
argument_list|(
name|rs
argument_list|,
name|region
argument_list|)
decl_stmt|;
name|RegionLocator
name|locator
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|NAME
argument_list|)
decl_stmt|;
name|HRegionLocation
name|locA
init|=
name|locator
operator|.
name|getRegionLocation
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|HRegionLocation
name|locB
init|=
name|locator
operator|.
name|getRegionLocation
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|maxSeqId
operator|+
literal|1
argument_list|,
name|locA
operator|.
name|getSeqNum
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|maxSeqId
operator|+
literal|1
argument_list|,
name|locB
operator|.
name|getSeqNum
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMerge
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ExecutionException
throws|,
name|TimeoutException
block|{
try|try
init|(
name|Table
name|table
init|=
name|createTable
argument_list|(
literal|true
argument_list|)
init|)
block|{
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|CF
argument_list|,
name|CQ
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|UTIL
operator|.
name|flush
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
name|MiniHBaseCluster
name|cluster
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|cluster
operator|.
name|getRegions
argument_list|(
name|NAME
argument_list|)
decl_stmt|;
name|HRegion
name|regionA
init|=
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|HRegion
name|regionB
init|=
name|regions
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|HRegionServer
name|rsA
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
name|cluster
operator|.
name|getServerWith
argument_list|(
name|regionA
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|HRegionServer
name|rsB
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
name|cluster
operator|.
name|getServerWith
argument_list|(
name|regionB
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|mergeRegionsAsync
argument_list|(
name|regionA
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|regionB
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|get
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
expr_stmt|;
name|long
name|maxSeqIdA
init|=
name|getMaxSeqId
argument_list|(
name|rsA
argument_list|,
name|regionA
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|maxSeqIdB
init|=
name|getMaxSeqId
argument_list|(
name|rsB
argument_list|,
name|regionB
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionLocation
name|loc
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getRegionLocator
argument_list|(
name|NAME
argument_list|)
operator|.
name|getRegionLocation
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|Math
operator|.
name|max
argument_list|(
name|maxSeqIdA
argument_list|,
name|maxSeqIdB
argument_list|)
operator|+
literal|1
argument_list|,
name|loc
operator|.
name|getSeqNum
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

