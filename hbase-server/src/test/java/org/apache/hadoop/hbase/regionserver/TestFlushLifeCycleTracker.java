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
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
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
name|assertNull
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
name|io
operator|.
name|InterruptedIOException
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
name|CountDownLatch
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
name|CellBuilder
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
comment|/**  * Confirm that the function of FlushLifeCycleTracker is OK as we do not use it in our own code.  */
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
name|TestFlushLifeCycleTracker
block|{
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
name|TestFlushLifeCycleTracker
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
name|CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"CF"
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
name|FlushLifeCycleTracker
name|TRACKER
decl_stmt|;
specifier|private
specifier|static
specifier|volatile
name|CountDownLatch
name|ARRIVE
decl_stmt|;
specifier|private
specifier|static
specifier|volatile
name|CountDownLatch
name|BLOCK
decl_stmt|;
specifier|public
specifier|static
specifier|final
class|class
name|FlushObserver
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
name|preFlush
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|FlushLifeCycleTracker
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
name|postFlush
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|FlushLifeCycleTracker
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
name|postFlush
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
name|FlushLifeCycleTracker
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
comment|// inject here so we can make a flush request to fail because of we already have a flush
comment|// ongoing.
name|CountDownLatch
name|arrive
init|=
name|ARRIVE
decl_stmt|;
if|if
condition|(
name|arrive
operator|!=
literal|null
condition|)
block|{
name|arrive
operator|.
name|countDown
argument_list|()
expr_stmt|;
try|try
block|{
name|BLOCK
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|InterruptedIOException
argument_list|()
throw|;
block|}
block|}
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|Tracker
implements|implements
name|FlushLifeCycleTracker
block|{
specifier|private
name|String
name|reason
decl_stmt|;
specifier|private
name|boolean
name|beforeExecutionCalled
decl_stmt|;
specifier|private
name|boolean
name|afterExecutionCalled
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
specifier|synchronized
name|void
name|notExecuted
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
name|this
operator|.
name|reason
operator|=
name|reason
expr_stmt|;
name|completed
operator|=
literal|true
expr_stmt|;
name|notifyAll
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|beforeExecution
parameter_list|()
block|{
name|this
operator|.
name|beforeExecutionCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|afterExecution
parameter_list|()
block|{
name|this
operator|.
name|afterExecutionCalled
operator|=
literal|true
expr_stmt|;
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
name|CF
argument_list|)
argument_list|)
operator|.
name|addCoprocessor
argument_list|(
name|FlushObserver
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
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
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
argument_list|,
literal|true
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
name|CF
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
name|CellBuilder
operator|.
name|DataType
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
block|}
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
name|requestFlush
argument_list|(
name|tracker
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|tracker
operator|.
name|reason
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|beforeExecutionCalled
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|afterExecutionCalled
argument_list|)
expr_stmt|;
comment|// request flush on a region with empty memstore should still success
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
name|requestFlush
argument_list|(
name|tracker
argument_list|)
expr_stmt|;
name|tracker
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|tracker
operator|.
name|reason
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|beforeExecutionCalled
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker
operator|.
name|afterExecutionCalled
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNotExecuted
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
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
argument_list|,
literal|true
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
name|CF
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
name|CellBuilder
operator|.
name|DataType
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
block|}
comment|// here we may have overlap when calling the CP hooks so we do not assert on TRACKER
name|Tracker
name|tracker1
init|=
operator|new
name|Tracker
argument_list|()
decl_stmt|;
name|ARRIVE
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|BLOCK
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|region
operator|.
name|requestFlush
argument_list|(
name|tracker1
argument_list|)
expr_stmt|;
name|ARRIVE
operator|.
name|await
argument_list|()
expr_stmt|;
name|Tracker
name|tracker2
init|=
operator|new
name|Tracker
argument_list|()
decl_stmt|;
name|region
operator|.
name|requestFlush
argument_list|(
name|tracker2
argument_list|)
expr_stmt|;
name|tracker2
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|tracker2
operator|.
name|reason
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|tracker2
operator|.
name|beforeExecutionCalled
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|tracker2
operator|.
name|afterExecutionCalled
argument_list|)
expr_stmt|;
name|BLOCK
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|tracker1
operator|.
name|await
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|tracker1
operator|.
name|reason
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker1
operator|.
name|beforeExecutionCalled
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tracker1
operator|.
name|afterExecutionCalled
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

