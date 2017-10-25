begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
package|;
end_package

begin_comment
comment|// this is deliberately not in the o.a.h.h.regionserver package
end_comment

begin_comment
comment|// in order to make sure all required classes/method are available
end_comment

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
name|java
operator|.
name|util
operator|.
name|Optional
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
name|ConcurrentHashMap
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
name|ConcurrentMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Predicate
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
name|Cell
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
name|CellUtil
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
name|HBaseCommonTestingUtility
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
name|client
operator|.
name|Durability
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
name|Get
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|coprocessor
operator|.
name|ObserverContext
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
name|coprocessor
operator|.
name|RegionCoprocessor
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
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
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
name|coprocessor
operator|.
name|RegionObserver
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
name|DelegatingInternalScanner
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
name|FlushLifeCycleTracker
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
name|InternalScanner
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
name|regionserver
operator|.
name|RegionScanner
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
name|ScanType
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
name|ScannerContext
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
name|Store
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
name|StoreScanner
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
name|compactions
operator|.
name|CompactionLifeCycleTracker
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
name|compactions
operator|.
name|CompactionRequest
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
name|testclassification
operator|.
name|MiscTests
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
name|WALEdit
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
name|runner
operator|.
name|RunWith
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestCoprocessorScanPolicy
block|{
specifier|protected
specifier|final
specifier|static
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
name|byte
index|[]
name|F
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|Q
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|R
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
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
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|ScanObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
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
name|HBaseCommonTestingUtility
operator|.
name|BOOLEAN_PARAMETERIZED
return|;
block|}
specifier|public
name|TestCoprocessorScanPolicy
parameter_list|(
name|boolean
name|parallelSeekEnable
parameter_list|)
block|{
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getConf
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|StoreScanner
operator|.
name|STORESCANNER_PARALLEL_SEEK_ENABLE
argument_list|,
name|parallelSeekEnable
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBaseCases
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"baseCases"
argument_list|)
decl_stmt|;
if|if
condition|(
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|F
argument_list|,
literal|10
argument_list|)
decl_stmt|;
comment|// insert 3 versions
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|R
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|F
argument_list|,
name|Q
argument_list|,
name|now
argument_list|,
name|Q
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|R
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|F
argument_list|,
name|Q
argument_list|,
name|now
operator|+
literal|1
argument_list|,
name|Q
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|R
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|F
argument_list|,
name|Q
argument_list|,
name|now
operator|+
literal|2
argument_list|,
name|Q
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|R
argument_list|)
decl_stmt|;
name|g
operator|.
name|readVersions
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|compact
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// still visible after a flush/compaction
name|r
operator|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// set the version override to 2
name|p
operator|=
operator|new
name|Put
argument_list|(
name|R
argument_list|)
expr_stmt|;
name|p
operator|.
name|setAttribute
argument_list|(
literal|"versions"
argument_list|,
operator|new
name|byte
index|[]
block|{}
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|F
argument_list|,
name|tableName
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// only 2 versions now
name|r
operator|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|compact
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// still 2 versions after a flush/compaction
name|r
operator|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// insert a new version
name|p
operator|.
name|addColumn
argument_list|(
name|F
argument_list|,
name|Q
argument_list|,
name|now
operator|+
literal|3
argument_list|,
name|Q
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// still 2 versions
name|r
operator|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTTL
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testTTL"
argument_list|)
decl_stmt|;
if|if
condition|(
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|Table
name|t
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|F
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|ManualEnvironmentEdge
name|me
init|=
operator|new
name|ManualEnvironmentEdge
argument_list|()
decl_stmt|;
name|me
operator|.
name|setValue
argument_list|(
name|now
argument_list|)
expr_stmt|;
name|EnvironmentEdgeManagerTestHelper
operator|.
name|injectEdge
argument_list|(
name|me
argument_list|)
expr_stmt|;
comment|// 2s in the past
name|long
name|ts
init|=
name|now
operator|-
literal|2000
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|R
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|F
argument_list|,
name|Q
argument_list|,
name|ts
argument_list|,
name|Q
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|R
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|F
argument_list|,
name|Q
argument_list|,
name|ts
operator|+
literal|1
argument_list|,
name|Q
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// Set the TTL override to 3s
name|p
operator|=
operator|new
name|Put
argument_list|(
name|R
argument_list|)
expr_stmt|;
name|p
operator|.
name|setAttribute
argument_list|(
literal|"ttl"
argument_list|,
operator|new
name|byte
index|[]
block|{}
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|F
argument_list|,
name|tableName
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|3000L
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// these two should still be there
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|R
argument_list|)
decl_stmt|;
name|g
operator|.
name|readAllVersions
argument_list|()
expr_stmt|;
name|Result
name|r
init|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
comment|// still there?
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|flush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|compact
argument_list|(
name|tableName
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|R
argument_list|)
expr_stmt|;
name|g
operator|.
name|readAllVersions
argument_list|()
expr_stmt|;
name|r
operator|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// still there?
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// roll time forward 2s.
name|me
operator|.
name|setValue
argument_list|(
name|now
operator|+
literal|2000
argument_list|)
expr_stmt|;
comment|// now verify that data eventually does expire
name|g
operator|=
operator|new
name|Get
argument_list|(
name|R
argument_list|)
expr_stmt|;
name|g
operator|.
name|readAllVersions
argument_list|()
expr_stmt|;
name|r
operator|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
comment|// should be gone now
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|ScanObserver
implements|implements
name|RegionCoprocessor
implements|,
name|RegionObserver
block|{
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|TableName
argument_list|,
name|Long
argument_list|>
name|ttls
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|TableName
argument_list|,
name|Integer
argument_list|>
name|versions
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|// lame way to communicate with the coprocessor,
comment|// since it is loaded by a different class loader
annotation|@
name|Override
specifier|public
name|void
name|prePut
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|,
specifier|final
name|WALEdit
name|edit
parameter_list|,
specifier|final
name|Durability
name|durability
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|put
operator|.
name|getAttribute
argument_list|(
literal|"ttl"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|Cell
name|cell
init|=
name|put
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|findFirst
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|ttls
operator|.
name|put
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|cell
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toLong
argument_list|(
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|.
name|bypass
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|put
operator|.
name|getAttribute
argument_list|(
literal|"versions"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|Cell
name|cell
init|=
name|put
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|findFirst
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|versions
operator|.
name|put
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|cell
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toInt
argument_list|(
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|.
name|bypass
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|InternalScanner
name|wrap
parameter_list|(
name|Store
name|store
parameter_list|,
name|InternalScanner
name|scanner
parameter_list|)
block|{
name|Long
name|ttl
init|=
name|this
operator|.
name|ttls
operator|.
name|get
argument_list|(
name|store
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|Integer
name|version
init|=
name|this
operator|.
name|versions
operator|.
name|get
argument_list|(
name|store
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|DelegatingInternalScanner
argument_list|(
name|scanner
argument_list|)
block|{
specifier|private
name|byte
index|[]
name|row
decl_stmt|;
specifier|private
name|byte
index|[]
name|qualifier
decl_stmt|;
specifier|private
name|int
name|count
decl_stmt|;
specifier|private
name|Predicate
argument_list|<
name|Cell
argument_list|>
name|checkTtl
parameter_list|(
name|long
name|now
parameter_list|,
name|long
name|ttl
parameter_list|)
block|{
return|return
name|c
lambda|->
name|now
operator|-
name|c
operator|.
name|getTimestamp
argument_list|()
operator|>
name|ttl
return|;
block|}
specifier|private
name|Predicate
argument_list|<
name|Cell
argument_list|>
name|checkVersion
parameter_list|(
name|Cell
name|firstCell
parameter_list|,
name|int
name|version
parameter_list|)
block|{
if|if
condition|(
name|version
operator|==
literal|0
condition|)
block|{
return|return
name|c
lambda|->
literal|true
return|;
block|}
else|else
block|{
if|if
condition|(
name|row
operator|==
literal|null
operator|||
operator|!
name|CellUtil
operator|.
name|matchingRow
argument_list|(
name|firstCell
argument_list|,
name|row
argument_list|)
condition|)
block|{
name|row
operator|=
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|firstCell
argument_list|)
expr_stmt|;
comment|// reset qualifier as there is a row change
name|qualifier
operator|=
literal|null
expr_stmt|;
block|}
return|return
name|c
lambda|->
block|{
if|if
condition|(
name|qualifier
operator|!=
literal|null
operator|&&
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|c
argument_list|,
name|qualifier
argument_list|)
condition|)
block|{
if|if
condition|(
name|count
operator|>=
name|version
condition|)
block|{
return|return
literal|true
return|;
block|}
name|count
operator|++
expr_stmt|;
return|return
literal|false
return|;
block|}
else|else
block|{
comment|// qualifier switch
name|qualifier
operator|=
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|count
operator|=
literal|1
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|result
parameter_list|,
name|ScannerContext
name|scannerContext
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|moreRows
init|=
name|scanner
operator|.
name|next
argument_list|(
name|result
argument_list|,
name|scannerContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|moreRows
return|;
block|}
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|Predicate
argument_list|<
name|Cell
argument_list|>
name|predicate
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|ttl
operator|!=
literal|null
condition|)
block|{
name|predicate
operator|=
name|checkTtl
argument_list|(
name|now
argument_list|,
name|ttl
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|version
operator|!=
literal|null
condition|)
block|{
name|Predicate
argument_list|<
name|Cell
argument_list|>
name|vp
init|=
name|checkVersion
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|version
argument_list|)
decl_stmt|;
if|if
condition|(
name|predicate
operator|!=
literal|null
condition|)
block|{
name|predicate
operator|=
name|predicate
operator|.
name|and
argument_list|(
name|vp
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|predicate
operator|=
name|vp
expr_stmt|;
block|}
block|}
if|if
condition|(
name|predicate
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|removeIf
argument_list|(
name|predicate
argument_list|)
expr_stmt|;
block|}
return|return
name|moreRows
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|InternalScanner
name|preFlush
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|InternalScanner
name|scanner
parameter_list|,
name|FlushLifeCycleTracker
name|tracker
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|wrap
argument_list|(
name|store
argument_list|,
name|scanner
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InternalScanner
name|preCompact
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|InternalScanner
name|scanner
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|CompactionLifeCycleTracker
name|tracker
parameter_list|,
name|CompactionRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|wrap
argument_list|(
name|store
argument_list|,
name|scanner
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preGetOp
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Get
name|get
parameter_list|,
name|List
argument_list|<
name|Cell
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
block|{
name|TableName
name|tableName
init|=
name|c
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRegion
argument_list|()
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|Long
name|ttl
init|=
name|this
operator|.
name|ttls
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|ttl
operator|!=
literal|null
condition|)
block|{
name|get
operator|.
name|setTimeRange
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|ttl
argument_list|,
name|get
operator|.
name|getTimeRange
argument_list|()
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Integer
name|version
init|=
name|this
operator|.
name|versions
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|version
operator|!=
literal|null
condition|)
block|{
name|get
operator|.
name|readVersions
argument_list|(
name|version
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|RegionScanner
name|preScannerOpen
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|RegionScanner
name|s
parameter_list|)
throws|throws
name|IOException
block|{
name|Region
name|region
init|=
name|c
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRegion
argument_list|()
decl_stmt|;
name|TableName
name|tableName
init|=
name|region
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getTableName
argument_list|()
decl_stmt|;
name|Long
name|ttl
init|=
name|this
operator|.
name|ttls
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|ttl
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setTimeRange
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|ttl
argument_list|,
name|scan
operator|.
name|getTimeRange
argument_list|()
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Integer
name|version
init|=
name|this
operator|.
name|versions
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|version
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|readVersions
argument_list|(
name|version
argument_list|)
expr_stmt|;
block|}
return|return
name|region
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

