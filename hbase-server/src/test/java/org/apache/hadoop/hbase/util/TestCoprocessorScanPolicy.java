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
name|HashMap
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableSet
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
name|OptionalInt
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
name|HTableDescriptor
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
name|KeyValue
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
name|KeyValueUtil
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
name|ColumnFamilyDescriptor
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
name|HStore
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
name|KeyValueScanner
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
name|ScanInfo
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
literal|1
argument_list|)
decl_stmt|;
comment|// set the version override to 2
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
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
comment|// insert 2 versions
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
name|setMaxVersions
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
comment|// both version are still visible even after a flush/compaction
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
name|setMaxVersions
argument_list|(
literal|10
argument_list|)
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
comment|// insert a 3rd version
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
name|setMaxVersions
argument_list|(
literal|10
argument_list|)
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
comment|// still only two version visible
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
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|F
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
literal|10
argument_list|)
operator|.
name|setTimeToLive
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|desc
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
name|tableName
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
comment|// Set the TTL override to 3s
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
comment|// these two should be expired but for the override
comment|// (their ts was 2s in the past)
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
name|setMaxVersions
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
name|setMaxVersions
argument_list|(
literal|10
argument_list|)
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
name|setMaxVersions
argument_list|(
literal|10
argument_list|)
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
name|Map
argument_list|<
name|TableName
argument_list|,
name|Long
argument_list|>
name|ttls
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|TableName
argument_list|,
name|Integer
argument_list|>
name|versions
init|=
operator|new
name|HashMap
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
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|cell
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
name|kv
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|kv
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
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|kv
argument_list|)
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
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|cell
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
name|kv
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|kv
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
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|kv
argument_list|)
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
annotation|@
name|Override
specifier|public
name|InternalScanner
name|preFlushScannerOpen
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
parameter_list|,
name|InternalScanner
name|s
parameter_list|,
name|long
name|readPoint
parameter_list|)
throws|throws
name|IOException
block|{
name|HStore
name|hs
init|=
operator|(
name|HStore
operator|)
name|store
decl_stmt|;
name|Long
name|newTtl
init|=
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
if|if
condition|(
name|newTtl
operator|!=
literal|null
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"PreFlush:"
operator|+
name|newTtl
argument_list|)
expr_stmt|;
block|}
name|Integer
name|newVersions
init|=
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
name|ScanInfo
name|oldSI
init|=
name|hs
operator|.
name|getScanInfo
argument_list|()
decl_stmt|;
name|ColumnFamilyDescriptor
name|family
init|=
name|store
operator|.
name|getColumnFamilyDescriptor
argument_list|()
decl_stmt|;
name|ScanInfo
name|scanInfo
init|=
operator|new
name|ScanInfo
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|family
operator|.
name|getName
argument_list|()
argument_list|,
name|family
operator|.
name|getMinVersions
argument_list|()
argument_list|,
name|newVersions
operator|==
literal|null
condition|?
name|family
operator|.
name|getMaxVersions
argument_list|()
else|:
name|newVersions
argument_list|,
name|newTtl
operator|==
literal|null
condition|?
name|oldSI
operator|.
name|getTtl
argument_list|()
else|:
name|newTtl
argument_list|,
name|family
operator|.
name|getKeepDeletedCells
argument_list|()
argument_list|,
name|family
operator|.
name|getBlocksize
argument_list|()
argument_list|,
name|oldSI
operator|.
name|getTimeToPurgeDeletes
argument_list|()
argument_list|,
name|oldSI
operator|.
name|getComparator
argument_list|()
argument_list|,
name|family
operator|.
name|isNewVersionBehavior
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|StoreScanner
argument_list|(
name|hs
argument_list|,
name|scanInfo
argument_list|,
name|newVersions
operator|==
literal|null
condition|?
name|OptionalInt
operator|.
name|empty
argument_list|()
else|:
name|OptionalInt
operator|.
name|of
argument_list|(
name|newVersions
operator|.
name|intValue
argument_list|()
argument_list|)
argument_list|,
name|scanners
argument_list|,
name|ScanType
operator|.
name|COMPACT_RETAIN_DELETES
argument_list|,
name|store
operator|.
name|getSmallestReadPoint
argument_list|()
argument_list|,
name|HConstants
operator|.
name|OLDEST_TIMESTAMP
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InternalScanner
name|preCompactScannerOpen
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|KeyValueScanner
argument_list|>
name|scanners
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|long
name|earliestPutTs
parameter_list|,
name|InternalScanner
name|s
parameter_list|,
name|CompactionLifeCycleTracker
name|tracker
parameter_list|,
name|long
name|readPoint
parameter_list|)
throws|throws
name|IOException
block|{
name|HStore
name|hs
init|=
operator|(
name|HStore
operator|)
name|store
decl_stmt|;
name|Long
name|newTtl
init|=
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
name|newVersions
init|=
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
name|ScanInfo
name|oldSI
init|=
name|hs
operator|.
name|getScanInfo
argument_list|()
decl_stmt|;
name|ColumnFamilyDescriptor
name|family
init|=
name|store
operator|.
name|getColumnFamilyDescriptor
argument_list|()
decl_stmt|;
name|ScanInfo
name|scanInfo
init|=
operator|new
name|ScanInfo
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|family
operator|.
name|getName
argument_list|()
argument_list|,
name|family
operator|.
name|getMinVersions
argument_list|()
argument_list|,
name|newVersions
operator|==
literal|null
condition|?
name|family
operator|.
name|getMaxVersions
argument_list|()
else|:
name|newVersions
argument_list|,
name|newTtl
operator|==
literal|null
condition|?
name|oldSI
operator|.
name|getTtl
argument_list|()
else|:
name|newTtl
argument_list|,
name|family
operator|.
name|getKeepDeletedCells
argument_list|()
argument_list|,
name|family
operator|.
name|getBlocksize
argument_list|()
argument_list|,
name|oldSI
operator|.
name|getTimeToPurgeDeletes
argument_list|()
argument_list|,
name|oldSI
operator|.
name|getComparator
argument_list|()
argument_list|,
name|family
operator|.
name|isNewVersionBehavior
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|StoreScanner
argument_list|(
name|hs
argument_list|,
name|scanInfo
argument_list|,
name|newVersions
operator|==
literal|null
condition|?
name|OptionalInt
operator|.
name|empty
argument_list|()
else|:
name|OptionalInt
operator|.
name|of
argument_list|(
name|newVersions
operator|.
name|intValue
argument_list|()
argument_list|)
argument_list|,
name|scanners
argument_list|,
name|scanType
argument_list|,
name|store
operator|.
name|getSmallestReadPoint
argument_list|()
argument_list|,
name|earliestPutTs
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|KeyValueScanner
name|preStoreScannerOpen
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|,
specifier|final
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|targetCols
parameter_list|,
name|KeyValueScanner
name|s
parameter_list|,
name|long
name|readPt
parameter_list|)
throws|throws
name|IOException
block|{
name|TableName
name|tn
init|=
name|store
operator|.
name|getTableName
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|tn
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
name|HStore
name|hs
init|=
operator|(
name|HStore
operator|)
name|store
decl_stmt|;
name|Long
name|newTtl
init|=
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
name|newVersions
init|=
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
name|ScanInfo
name|oldSI
init|=
name|hs
operator|.
name|getScanInfo
argument_list|()
decl_stmt|;
name|ColumnFamilyDescriptor
name|family
init|=
name|store
operator|.
name|getColumnFamilyDescriptor
argument_list|()
decl_stmt|;
name|ScanInfo
name|scanInfo
init|=
operator|new
name|ScanInfo
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|family
operator|.
name|getName
argument_list|()
argument_list|,
name|family
operator|.
name|getMinVersions
argument_list|()
argument_list|,
name|newVersions
operator|==
literal|null
condition|?
name|family
operator|.
name|getMaxVersions
argument_list|()
else|:
name|newVersions
argument_list|,
name|newTtl
operator|==
literal|null
condition|?
name|oldSI
operator|.
name|getTtl
argument_list|()
else|:
name|newTtl
argument_list|,
name|family
operator|.
name|getKeepDeletedCells
argument_list|()
argument_list|,
name|family
operator|.
name|getBlocksize
argument_list|()
argument_list|,
name|oldSI
operator|.
name|getTimeToPurgeDeletes
argument_list|()
argument_list|,
name|oldSI
operator|.
name|getComparator
argument_list|()
argument_list|,
name|family
operator|.
name|isNewVersionBehavior
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|StoreScanner
argument_list|(
name|hs
argument_list|,
name|scanInfo
argument_list|,
name|scan
argument_list|,
name|targetCols
argument_list|,
name|readPt
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|s
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

