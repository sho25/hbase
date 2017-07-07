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
name|tool
package|;
end_package

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
name|*
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
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|Appender
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|LogManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|log4j
operator|.
name|spi
operator|.
name|LoggingEvent
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
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
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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
name|rules
operator|.
name|TestName
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
name|mockito
operator|.
name|ArgumentMatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|runners
operator|.
name|MockitoJUnitRunner
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
name|ExecutorService
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
name|ScheduledThreadPoolExecutor
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
name|assertNotEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyLong
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|eq
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|isA
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|argThat
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
name|mockito
operator|.
name|Mockito
operator|.
name|*
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|MockitoJUnitRunner
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCanaryTool
block|{
specifier|private
name|HBaseTestingUtility
name|testingUtility
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|testingUtility
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|testingUtility
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|LogManager
operator|.
name|getRootLogger
argument_list|()
operator|.
name|addAppender
argument_list|(
name|mockAppender
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
name|Exception
block|{
name|testingUtility
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|LogManager
operator|.
name|getRootLogger
argument_list|()
operator|.
name|removeAppender
argument_list|(
name|mockAppender
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Mock
name|Appender
name|mockAppender
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testBasicZookeeperCanaryWorks
parameter_list|()
throws|throws
name|Exception
block|{
name|Integer
name|port
init|=
name|Iterables
operator|.
name|getOnlyElement
argument_list|(
name|testingUtility
operator|.
name|getZkCluster
argument_list|()
operator|.
name|getClientPortList
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|testingUtility
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|,
literal|"localhost:"
operator|+
name|port
operator|+
literal|"/hbase"
argument_list|)
expr_stmt|;
name|ExecutorService
name|executor
init|=
operator|new
name|ScheduledThreadPoolExecutor
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|Canary
operator|.
name|ZookeeperStdOutSink
name|sink
init|=
name|spy
argument_list|(
operator|new
name|Canary
operator|.
name|ZookeeperStdOutSink
argument_list|()
argument_list|)
decl_stmt|;
name|Canary
name|canary
init|=
operator|new
name|Canary
argument_list|(
name|executor
argument_list|,
name|sink
argument_list|)
decl_stmt|;
name|String
index|[]
name|args
init|=
block|{
literal|"-t"
block|,
literal|"10000"
block|,
literal|"-zookeeper"
block|}
decl_stmt|;
name|ToolRunner
operator|.
name|run
argument_list|(
name|testingUtility
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|canary
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|String
name|baseZnode
init|=
name|testingUtility
operator|.
name|getConfiguration
argument_list|()
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_ZNODE_PARENT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_ZOOKEEPER_ZNODE_PARENT
argument_list|)
decl_stmt|;
name|verify
argument_list|(
name|sink
argument_list|,
name|atLeastOnce
argument_list|()
argument_list|)
operator|.
name|publishReadTiming
argument_list|(
name|eq
argument_list|(
name|baseZnode
argument_list|)
argument_list|,
name|eq
argument_list|(
literal|"localhost:"
operator|+
name|port
argument_list|)
argument_list|,
name|anyLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasicCanaryWorks
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|testingUtility
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|)
decl_stmt|;
comment|// insert some test rows
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|iBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|iBytes
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN
argument_list|,
name|iBytes
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|ExecutorService
name|executor
init|=
operator|new
name|ScheduledThreadPoolExecutor
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Canary
operator|.
name|RegionStdOutSink
name|sink
init|=
name|spy
argument_list|(
operator|new
name|Canary
operator|.
name|RegionStdOutSink
argument_list|()
argument_list|)
decl_stmt|;
name|Canary
name|canary
init|=
operator|new
name|Canary
argument_list|(
name|executor
argument_list|,
name|sink
argument_list|)
decl_stmt|;
name|String
index|[]
name|args
init|=
block|{
literal|"-writeSniffing"
block|,
literal|"-t"
block|,
literal|"10000"
block|,
name|name
operator|.
name|getMethodName
argument_list|()
block|}
decl_stmt|;
name|ToolRunner
operator|.
name|run
argument_list|(
name|testingUtility
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|canary
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"verify no read error count"
argument_list|,
literal|0
argument_list|,
name|canary
operator|.
name|getReadFailures
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"verify no write error count"
argument_list|,
literal|0
argument_list|,
name|canary
operator|.
name|getWriteFailures
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|sink
argument_list|,
name|atLeastOnce
argument_list|()
argument_list|)
operator|.
name|publishReadTiming
argument_list|(
name|isA
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
argument_list|,
name|isA
argument_list|(
name|HRegionInfo
operator|.
name|class
argument_list|)
argument_list|,
name|isA
argument_list|(
name|HColumnDescriptor
operator|.
name|class
argument_list|)
argument_list|,
name|anyLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadTableTimeouts
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
index|[]
name|tableNames
init|=
operator|new
name|TableName
index|[
literal|2
index|]
decl_stmt|;
name|tableNames
index|[
literal|0
index|]
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"1"
argument_list|)
expr_stmt|;
name|tableNames
index|[
literal|1
index|]
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"2"
argument_list|)
expr_stmt|;
comment|// Create 2 test tables.
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|2
condition|;
name|j
operator|++
control|)
block|{
name|Table
name|table
init|=
name|testingUtility
operator|.
name|createTable
argument_list|(
name|tableNames
index|[
name|j
index|]
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|)
decl_stmt|;
comment|// insert some test rows
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|iBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
operator|+
name|j
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|iBytes
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN
argument_list|,
name|iBytes
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
block|}
name|ExecutorService
name|executor
init|=
operator|new
name|ScheduledThreadPoolExecutor
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Canary
operator|.
name|RegionStdOutSink
name|sink
init|=
name|spy
argument_list|(
operator|new
name|Canary
operator|.
name|RegionStdOutSink
argument_list|()
argument_list|)
decl_stmt|;
name|Canary
name|canary
init|=
operator|new
name|Canary
argument_list|(
name|executor
argument_list|,
name|sink
argument_list|)
decl_stmt|;
name|String
name|configuredTimeoutStr
init|=
name|tableNames
index|[
literal|0
index|]
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"="
operator|+
name|Long
operator|.
name|MAX_VALUE
operator|+
literal|","
operator|+
name|tableNames
index|[
literal|1
index|]
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|"=0"
decl_stmt|;
name|String
index|[]
name|args
init|=
block|{
literal|"-readTableTimeouts"
block|,
name|configuredTimeoutStr
block|,
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"1"
block|,
name|name
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"2"
block|}
decl_stmt|;
name|ToolRunner
operator|.
name|run
argument_list|(
name|testingUtility
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|canary
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|sink
argument_list|,
name|times
argument_list|(
name|tableNames
operator|.
name|length
argument_list|)
argument_list|)
operator|.
name|initializeAndGetReadLatencyForTable
argument_list|(
name|isA
argument_list|(
name|String
operator|.
name|class
argument_list|)
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|assertNotEquals
argument_list|(
literal|"verify non-null read latency"
argument_list|,
literal|null
argument_list|,
name|sink
operator|.
name|getReadLatencyMap
argument_list|()
operator|.
name|get
argument_list|(
name|tableNames
index|[
name|i
index|]
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
literal|"verify non-zero read latency"
argument_list|,
literal|0L
argument_list|,
name|sink
operator|.
name|getReadLatencyMap
argument_list|()
operator|.
name|get
argument_list|(
name|tableNames
index|[
name|i
index|]
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// One table's timeout is set for 0 ms and thus, should lead to an error.
name|verify
argument_list|(
name|mockAppender
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|doAppend
argument_list|(
name|argThat
argument_list|(
operator|new
name|ArgumentMatcher
argument_list|<
name|LoggingEvent
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|matches
parameter_list|(
name|Object
name|argument
parameter_list|)
block|{
return|return
operator|(
operator|(
name|LoggingEvent
operator|)
name|argument
operator|)
operator|.
name|getRenderedMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"exceeded the configured read timeout."
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockAppender
argument_list|,
name|times
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|doAppend
argument_list|(
name|argThat
argument_list|(
operator|new
name|ArgumentMatcher
argument_list|<
name|LoggingEvent
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|matches
parameter_list|(
name|Object
name|argument
parameter_list|)
block|{
return|return
operator|(
operator|(
name|LoggingEvent
operator|)
name|argument
operator|)
operator|.
name|getRenderedMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"The configured read timeout was"
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWriteTableTimeout
parameter_list|()
throws|throws
name|Exception
block|{
name|ExecutorService
name|executor
init|=
operator|new
name|ScheduledThreadPoolExecutor
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Canary
operator|.
name|RegionStdOutSink
name|sink
init|=
name|spy
argument_list|(
operator|new
name|Canary
operator|.
name|RegionStdOutSink
argument_list|()
argument_list|)
decl_stmt|;
name|Canary
name|canary
init|=
operator|new
name|Canary
argument_list|(
name|executor
argument_list|,
name|sink
argument_list|)
decl_stmt|;
name|String
index|[]
name|args
init|=
block|{
literal|"-writeSniffing"
block|,
literal|"-writeTableTimeout"
block|,
name|String
operator|.
name|valueOf
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
block|}
decl_stmt|;
name|ToolRunner
operator|.
name|run
argument_list|(
name|testingUtility
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|canary
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
literal|"verify non-null write latency"
argument_list|,
literal|null
argument_list|,
name|sink
operator|.
name|getWriteLatency
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
literal|"verify non-zero write latency"
argument_list|,
literal|0L
argument_list|,
name|sink
operator|.
name|getWriteLatency
argument_list|()
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|mockAppender
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|doAppend
argument_list|(
name|argThat
argument_list|(
operator|new
name|ArgumentMatcher
argument_list|<
name|LoggingEvent
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|matches
parameter_list|(
name|Object
name|argument
parameter_list|)
block|{
return|return
operator|(
operator|(
name|LoggingEvent
operator|)
name|argument
operator|)
operator|.
name|getRenderedMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"The configured write timeout was"
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//no table created, so there should be no regions
annotation|@
name|Test
specifier|public
name|void
name|testRegionserverNoRegions
parameter_list|()
throws|throws
name|Exception
block|{
name|runRegionserverCanary
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|mockAppender
argument_list|)
operator|.
name|doAppend
argument_list|(
name|argThat
argument_list|(
operator|new
name|ArgumentMatcher
argument_list|<
name|LoggingEvent
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|matches
parameter_list|(
name|Object
name|argument
parameter_list|)
block|{
return|return
operator|(
operator|(
name|LoggingEvent
operator|)
name|argument
operator|)
operator|.
name|getRenderedMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Regionserver not serving any regions"
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//by creating a table, there shouldn't be any region servers not serving any regions
annotation|@
name|Test
specifier|public
name|void
name|testRegionserverWithRegions
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|testingUtility
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|)
expr_stmt|;
name|runRegionserverCanary
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|mockAppender
argument_list|,
name|never
argument_list|()
argument_list|)
operator|.
name|doAppend
argument_list|(
name|argThat
argument_list|(
operator|new
name|ArgumentMatcher
argument_list|<
name|LoggingEvent
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|matches
parameter_list|(
name|Object
name|argument
parameter_list|)
block|{
return|return
operator|(
operator|(
name|LoggingEvent
operator|)
name|argument
operator|)
operator|.
name|getRenderedMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Regionserver not serving any regions"
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRawScanConfig
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|testingUtility
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|)
decl_stmt|;
comment|// insert some test rows
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|iBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|iBytes
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN
argument_list|,
name|iBytes
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|ExecutorService
name|executor
init|=
operator|new
name|ScheduledThreadPoolExecutor
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Canary
operator|.
name|RegionStdOutSink
name|sink
init|=
name|spy
argument_list|(
operator|new
name|Canary
operator|.
name|RegionStdOutSink
argument_list|()
argument_list|)
decl_stmt|;
name|Canary
name|canary
init|=
operator|new
name|Canary
argument_list|(
name|executor
argument_list|,
name|sink
argument_list|)
decl_stmt|;
name|String
index|[]
name|args
init|=
block|{
literal|"-t"
block|,
literal|"10000"
block|,
name|name
operator|.
name|getMethodName
argument_list|()
block|}
decl_stmt|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
name|conf
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
argument_list|(
name|testingUtility
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|HBASE_CANARY_READ_RAW_SCAN_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
name|canary
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|sink
argument_list|,
name|atLeastOnce
argument_list|()
argument_list|)
operator|.
name|publishReadTiming
argument_list|(
name|isA
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
argument_list|,
name|isA
argument_list|(
name|HRegionInfo
operator|.
name|class
argument_list|)
argument_list|,
name|isA
argument_list|(
name|HColumnDescriptor
operator|.
name|class
argument_list|)
argument_list|,
name|anyLong
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"verify no read error count"
argument_list|,
literal|0
argument_list|,
name|canary
operator|.
name|getReadFailures
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runRegionserverCanary
parameter_list|()
throws|throws
name|Exception
block|{
name|ExecutorService
name|executor
init|=
operator|new
name|ScheduledThreadPoolExecutor
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Canary
name|canary
init|=
operator|new
name|Canary
argument_list|(
name|executor
argument_list|,
operator|new
name|Canary
operator|.
name|RegionServerStdOutSink
argument_list|()
argument_list|)
decl_stmt|;
name|String
index|[]
name|args
init|=
block|{
literal|"-t"
block|,
literal|"10000"
block|,
literal|"-regionserver"
block|}
decl_stmt|;
name|ToolRunner
operator|.
name|run
argument_list|(
name|testingUtility
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|canary
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"verify no read error count"
argument_list|,
literal|0
argument_list|,
name|canary
operator|.
name|getReadFailures
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

