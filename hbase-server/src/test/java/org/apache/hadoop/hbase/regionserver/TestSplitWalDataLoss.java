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
name|assertTrue
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
name|doAnswer
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
name|spy
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
name|Map
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
name|lang
operator|.
name|mutable
operator|.
name|MutableBoolean
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
name|DroppedSnapshotException
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
name|NamespaceDescriptor
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
name|Connection
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
name|monitoring
operator|.
name|MonitoredTask
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
operator|.
name|PrepareFlushResult
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
operator|.
name|FlushResult
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
name|EnvironmentEdgeManager
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
name|mockito
operator|.
name|Matchers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
import|;
end_import

begin_comment
comment|/**  * Testcase for https://issues.apache.org/jira/browse/HBASE-13811  */
end_comment

begin_class
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
name|TestSplitWalDataLoss
block|{
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
name|TestSplitWalDataLoss
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HBaseTestingUtility
name|testUtil
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|NamespaceDescriptor
name|namespace
init|=
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|namespace
operator|.
name|getName
argument_list|()
argument_list|,
literal|"dataloss"
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|qualifier
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
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
name|testUtil
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.msginterval"
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
name|testUtil
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|DISTRIBUTED_LOG_REPLAY_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testUtil
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|Admin
name|admin
init|=
name|testUtil
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createNamespace
argument_list|(
name|namespace
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|testUtil
operator|.
name|waitTableAvailable
argument_list|(
name|tableName
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
name|testUtil
operator|.
name|shutdownMiniCluster
argument_list|()
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
specifier|final
name|HRegionServer
name|rs
init|=
name|testUtil
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
specifier|final
name|HRegion
name|region
init|=
operator|(
name|HRegion
operator|)
name|rs
operator|.
name|getOnlineRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|HRegion
name|spiedRegion
init|=
name|spy
argument_list|(
name|region
argument_list|)
decl_stmt|;
specifier|final
name|MutableBoolean
name|flushed
init|=
operator|new
name|MutableBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|MutableBoolean
name|reported
init|=
operator|new
name|MutableBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|FlushResult
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|FlushResult
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
synchronized|synchronized
init|(
name|flushed
init|)
block|{
name|flushed
operator|.
name|setValue
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|flushed
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|reported
init|)
block|{
while|while
condition|(
operator|!
name|reported
operator|.
name|booleanValue
argument_list|()
condition|)
block|{
name|reported
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
name|rs
operator|.
name|getWAL
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
operator|.
name|abortCacheFlush
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|DroppedSnapshotException
argument_list|(
literal|"testcase"
argument_list|)
throw|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|spiedRegion
argument_list|)
operator|.
name|internalFlushCacheAndCommit
argument_list|(
name|Matchers
operator|.
expr|<
name|WAL
operator|>
name|any
argument_list|()
argument_list|,
name|Matchers
operator|.
expr|<
name|MonitoredTask
operator|>
name|any
argument_list|()
argument_list|,
name|Matchers
operator|.
expr|<
name|PrepareFlushResult
operator|>
name|any
argument_list|()
argument_list|,
name|Matchers
operator|.
expr|<
name|Collection
argument_list|<
name|Store
argument_list|>
operator|>
name|any
argument_list|()
argument_list|)
expr_stmt|;
comment|// Find region key; don't pick up key for hbase:meta by mistake.
name|String
name|key
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Region
argument_list|>
name|entry
range|:
name|rs
operator|.
name|onlineRegions
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
condition|)
block|{
name|key
operator|=
name|entry
operator|.
name|getKey
argument_list|()
expr_stmt|;
break|break;
block|}
block|}
name|rs
operator|.
name|onlineRegions
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|spiedRegion
argument_list|)
expr_stmt|;
name|Connection
name|conn
init|=
name|testUtil
operator|.
name|getConnection
argument_list|()
decl_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|tableName
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
literal|"row0"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val0"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|long
name|oldestSeqIdOfStore
init|=
name|region
operator|.
name|getOldestSeqIdOfStore
argument_list|(
name|family
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"CHANGE OLDEST "
operator|+
name|oldestSeqIdOfStore
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|oldestSeqIdOfStore
operator|>
name|HConstants
operator|.
name|NO_SEQNUM
argument_list|)
expr_stmt|;
name|rs
operator|.
name|cacheFlusher
operator|.
name|requestFlush
argument_list|(
name|spiedRegion
argument_list|,
literal|false
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|flushed
init|)
block|{
while|while
condition|(
operator|!
name|flushed
operator|.
name|booleanValue
argument_list|()
condition|)
block|{
name|flushed
operator|.
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
try|try
init|(
name|Table
name|table
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|tableName
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
literal|"row1"
argument_list|)
argument_list|)
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|rs
operator|.
name|tryRegionServerReport
argument_list|(
name|now
operator|-
literal|500
argument_list|,
name|now
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|reported
init|)
block|{
name|reported
operator|.
name|setValue
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|reported
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
while|while
condition|(
name|testUtil
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|tableName
argument_list|)
operator|==
name|rs
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|Table
name|table
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|Result
name|result
init|=
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row0"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val0"
argument_list|)
argument_list|,
name|result
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

