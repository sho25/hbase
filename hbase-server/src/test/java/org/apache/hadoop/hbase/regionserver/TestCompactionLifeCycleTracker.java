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
name|regionserver
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|containsString
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertSame
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
name|assertThat
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
name|assertTrue
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
name|Cell
operator|.
name|Type
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
name|CellBuilderFactory
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
name|CellBuilderType
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
name|HBaseClassTestRule
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
name|ColumnFamilyDescriptorBuilder
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
name|client
operator|.
name|TableDescriptorBuilder
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
name|quotas
operator|.
name|SpaceQuotaSnapshot
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
name|quotas
operator|.
name|SpaceQuotaSnapshot
operator|.
name|SpaceQuotaStatus
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
name|quotas
operator|.
name|SpaceViolationPolicy
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
name|CompactionConfiguration
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
name|CoprocessorTests
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
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
comment|/**  * Confirm that the function of CompactionLifeCycleTracker is OK as we do not use it in our own  * code.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|CoprocessorTests
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
name|TestCompactionLifeCycleTracker
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
name|TestCompactionLifeCycleTracker
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
name|TestCompactionLifeCycleTracker
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CF1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"CF1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CF2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"CF2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"CQ"
argument_list|)
decl_stmt|;
specifier|private
name|HRegion
name|region
decl_stmt|;
specifier|private
specifier|static
name|CompactionLifeCycleTracker
name|TRACKER
init|=
literal|null
decl_stmt|;
comment|// make sure that we pass the correct CompactionLifeCycleTracker to CP hooks.
specifier|public
specifier|static
specifier|final
class|class
name|CompactionObserver
implements|implements
name|RegionObserver
implements|,
name|RegionCoprocessor
block|{
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
annotation|@
name|Override
specifier|public
name|void
name|preCompactSelection
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
name|List
argument_list|<
name|?
extends|extends
name|StoreFile
argument_list|>
name|candidates
parameter_list|,
name|CompactionLifeCycleTracker
name|tracker
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|TRACKER
operator|!=
literal|null
condition|)
block|{
name|assertSame
argument_list|(
name|tracker
argument_list|,
name|TRACKER
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|postCompactSelection
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
name|List
argument_list|<
name|?
extends|extends
name|StoreFile
argument_list|>
name|selected
parameter_list|,
name|CompactionLifeCycleTracker
name|tracker
parameter_list|,
name|CompactionRequest
name|request
parameter_list|)
block|{
if|if
condition|(
name|TRACKER
operator|!=
literal|null
condition|)
block|{
name|assertSame
argument_list|(
name|tracker
argument_list|,
name|TRACKER
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|TRACKER
operator|!=
literal|null
condition|)
block|{
name|assertSame
argument_list|(
name|tracker
argument_list|,
name|TRACKER
argument_list|)
expr_stmt|;
block|}
return|return
name|scanner
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postCompact
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
name|StoreFile
name|resultFile
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
if|if
condition|(
name|TRACKER
operator|!=
literal|null
condition|)
block|{
name|assertSame
argument_list|(
name|tracker
argument_list|,
name|TRACKER
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|CompactionConfiguration
operator|.
name|HBASE_HSTORE_COMPACTION_MIN_KEY
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
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
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|NAME
argument_list|)
operator|.
name|addColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|CF1
argument_list|)
argument_list|)
operator|.
name|addColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|CF2
argument_list|)
argument_list|)
operator|.
name|addCoprocessor
argument_list|(
name|CompactionObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|NAME
argument_list|)
init|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|row
argument_list|)
operator|.
name|add
argument_list|(
name|CellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|SHALLOW_COPY
argument_list|)
operator|.
name|setRow
argument_list|(
name|row
argument_list|)
operator|.
name|setFamily
argument_list|(
name|CF1
argument_list|)
operator|.
name|setQualifier
argument_list|(
name|QUALIFIER
argument_list|)
operator|.
name|setTimestamp
argument_list|(
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
operator|.
name|setType
argument_list|(
name|Cell
operator|.
name|Type
operator|.
name|Put
argument_list|)
operator|.
name|setValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|100
init|;
name|i
operator|<
literal|200
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|row
argument_list|)
operator|.
name|add
argument_list|(
name|CellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|SHALLOW_COPY
argument_list|)
operator|.
name|setRow
argument_list|(
name|row
argument_list|)
operator|.
name|setFamily
argument_list|(
name|CF1
argument_list|)
operator|.
name|setQualifier
argument_list|(
name|QUALIFIER
argument_list|)
operator|.
name|setTimestamp
argument_list|(
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
operator|.
name|setType
argument_list|(
name|Type
operator|.
name|Put
argument_list|)
operator|.
name|setValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|flush
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
block|}
name|region
operator|=
name|UTIL
operator|.
name|getHBaseCluster
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
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|region
operator|.
name|getStore
argument_list|(
name|CF1
argument_list|)
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|region
operator|.
name|getStore
argument_list|(
name|CF2
argument_list|)
operator|.
name|getStorefilesCount
argument_list|()
argument_list|)
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
name|region
operator|=
literal|null
expr_stmt|;
name|TRACKER
operator|=
literal|null
expr_stmt|;
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
class|class
name|Tracker
implements|implements
name|CompactionLifeCycleTracker
block|{
specifier|final
name|List
argument_list|<
name|Pair
argument_list|<
name|Store
argument_list|,
name|String
argument_list|>
argument_list|>
name|notExecutedStores
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Store
argument_list|>
name|beforeExecuteStores
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Store
argument_list|>
name|afterExecuteStores
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|completed
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|notExecuted
parameter_list|(
name|Store
name|store
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
name|notExecutedStores
operator|.
name|add
argument_list|(
name|Pair
operator|.
name|newPair
argument_list|(
name|store
argument_list|,
name|reason
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|beforeExecution
parameter_list|(
name|Store
name|store
parameter_list|)
block|{
name|beforeExecuteStores
operator|.
name|add
argument_list|(
name|store
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|afterExecution
parameter_list|(
name|Store
name|store
parameter_list|)
block|{
name|afterExecuteStores
operator|.
name|add
argument_list|(
name|store
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|completed
parameter_list|()
block|{
name|completed
operator|=
literal|true
expr_stmt|;
name|notifyAll
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|await
parameter_list|()
throws|throws
name|InterruptedException
block|{
while|while
condition|(
operator|!
name|completed
condition|)
block|{
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRequestOnRegion
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Tracker
name|tracker
init|=
operator|new
name|Tracker
argument_list|()
decl_stmt|;
name|TRACKER
operator|=
name|tracker
expr_stmt|;
name|region
operator|.
name|requestCompaction
argument_list|(
literal|"test"
argument_list|,
name|Store
operator|.
name|PRIORITY_USER
argument_list|,
literal|false
argument_list|,
name|tracker
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tracker
operator|.
name|notExecutedStores
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|CF2
argument_list|)
argument_list|,
name|tracker
operator|.
name|notExecutedStores
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|tracker
operator|.
name|notExecutedStores
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSecond
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"compaction request was cancelled"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tracker
operator|.
name|beforeExecuteStores
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|CF1
argument_list|)
argument_list|,
name|tracker
operator|.
name|beforeExecuteStores
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tracker
operator|.
name|afterExecuteStores
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|CF1
argument_list|)
argument_list|,
name|tracker
operator|.
name|afterExecuteStores
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRequestOnStore
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|Tracker
name|tracker
init|=
operator|new
name|Tracker
argument_list|()
decl_stmt|;
name|TRACKER
operator|=
name|tracker
expr_stmt|;
name|region
operator|.
name|requestCompaction
argument_list|(
name|CF1
argument_list|,
literal|"test"
argument_list|,
name|Store
operator|.
name|PRIORITY_USER
argument_list|,
literal|false
argument_list|,
name|tracker
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|notExecutedStores
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tracker
operator|.
name|beforeExecuteStores
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|CF1
argument_list|)
argument_list|,
name|tracker
operator|.
name|beforeExecuteStores
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tracker
operator|.
name|afterExecuteStores
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|CF1
argument_list|)
argument_list|,
name|tracker
operator|.
name|afterExecuteStores
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|tracker
operator|=
operator|new
name|Tracker
argument_list|()
expr_stmt|;
name|TRACKER
operator|=
name|tracker
expr_stmt|;
name|region
operator|.
name|requestCompaction
argument_list|(
name|CF2
argument_list|,
literal|"test"
argument_list|,
name|Store
operator|.
name|PRIORITY_USER
argument_list|,
literal|false
argument_list|,
name|tracker
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tracker
operator|.
name|notExecutedStores
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|CF2
argument_list|)
argument_list|,
name|tracker
operator|.
name|notExecutedStores
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|tracker
operator|.
name|notExecutedStores
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSecond
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"compaction request was cancelled"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|beforeExecuteStores
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|afterExecuteStores
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSpaceQuotaViolation
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|region
operator|.
name|getRegionServerServices
argument_list|()
operator|.
name|getRegionServerSpaceQuotaManager
argument_list|()
operator|.
name|enforceViolationPolicy
argument_list|(
name|NAME
argument_list|,
operator|new
name|SpaceQuotaSnapshot
argument_list|(
operator|new
name|SpaceQuotaStatus
argument_list|(
name|SpaceViolationPolicy
operator|.
name|NO_WRITES_COMPACTIONS
argument_list|)
argument_list|,
literal|10L
argument_list|,
literal|100L
argument_list|)
argument_list|)
expr_stmt|;
name|Tracker
name|tracker
init|=
operator|new
name|Tracker
argument_list|()
decl_stmt|;
name|TRACKER
operator|=
name|tracker
expr_stmt|;
name|region
operator|.
name|requestCompaction
argument_list|(
literal|"test"
argument_list|,
name|Store
operator|.
name|PRIORITY_USER
argument_list|,
literal|false
argument_list|,
name|tracker
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tracker
operator|.
name|notExecutedStores
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|notExecutedStores
operator|.
name|sort
argument_list|(
parameter_list|(
name|p1
parameter_list|,
name|p2
parameter_list|)
lambda|->
name|p1
operator|.
name|getFirst
argument_list|()
operator|.
name|getColumnFamilyName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|p2
operator|.
name|getFirst
argument_list|()
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|CF1
argument_list|)
argument_list|,
name|tracker
operator|.
name|notExecutedStores
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|tracker
operator|.
name|notExecutedStores
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSecond
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"space quota violation"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|CF2
argument_list|)
argument_list|,
name|tracker
operator|.
name|notExecutedStores
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|getColumnFamilyName
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|tracker
operator|.
name|notExecutedStores
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getSecond
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"space quota violation"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|beforeExecuteStores
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|afterExecuteStores
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

