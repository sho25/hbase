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
name|mapreduce
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
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|nio
operator|.
name|ByteBuffer
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
name|Deque
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
name|NavigableMap
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
name|atomic
operator|.
name|AtomicInteger
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
name|fs
operator|.
name|FileSystem
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
name|TableExistsException
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
name|ClientProtocol
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
name|HConnection
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|BulkLoadHFileRequest
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
name|TestHRegionServerBulkLoad
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
name|mockito
operator|.
name|Mockito
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
name|collect
operator|.
name|Multimap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
import|;
end_import

begin_comment
comment|/**  * Test cases for the atomic load error handling of the bulk load functionality.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestLoadIncrementalHFilesSplitRecovery
block|{
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestHRegionServerBulkLoad
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
name|HBaseTestingUtility
name|util
decl_stmt|;
comment|//used by secure subclass
specifier|static
name|boolean
name|useSecure
init|=
literal|false
decl_stmt|;
specifier|final
specifier|static
name|int
name|NUM_CFS
init|=
literal|10
decl_stmt|;
specifier|final
specifier|static
name|byte
index|[]
name|QUAL
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
specifier|final
specifier|static
name|int
name|ROWCOUNT
init|=
literal|100
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|byte
index|[]
index|[]
name|families
init|=
operator|new
name|byte
index|[
name|NUM_CFS
index|]
index|[]
decl_stmt|;
static|static
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
name|NUM_CFS
condition|;
name|i
operator|++
control|)
block|{
name|families
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|byte
index|[]
name|rowkey
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"row_%08d"
argument_list|,
name|i
argument_list|)
argument_list|)
return|;
block|}
specifier|static
name|String
name|family
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"family_%04d"
argument_list|,
name|i
argument_list|)
return|;
block|}
specifier|static
name|byte
index|[]
name|value
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toBytes
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%010d"
argument_list|,
name|i
argument_list|)
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|buildHFiles
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|dir
parameter_list|,
name|int
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|val
init|=
name|value
argument_list|(
name|value
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_CFS
condition|;
name|i
operator|++
control|)
block|{
name|Path
name|testIn
init|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
name|family
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|TestHRegionServerBulkLoad
operator|.
name|createHFile
argument_list|(
name|fs
argument_list|,
operator|new
name|Path
argument_list|(
name|testIn
argument_list|,
literal|"hfile_"
operator|+
name|i
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|,
name|QUAL
argument_list|,
name|val
argument_list|,
name|ROWCOUNT
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Creates a table with given table name and specified number of column    * families if the table does not already exist.    */
specifier|private
name|void
name|setupTable
parameter_list|(
name|String
name|table
parameter_list|,
name|int
name|cfs
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Creating table "
operator|+
name|table
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|table
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TableExistsException
name|tee
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Table "
operator|+
name|table
operator|+
literal|" already exists"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Path
name|buildBulkFiles
parameter_list|(
name|String
name|table
parameter_list|,
name|int
name|value
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|dir
init|=
name|util
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|Path
name|bulk1
init|=
operator|new
name|Path
argument_list|(
name|dir
argument_list|,
name|table
operator|+
name|value
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|util
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|buildHFiles
argument_list|(
name|fs
argument_list|,
name|bulk1
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|bulk1
return|;
block|}
comment|/**    * Populate table with known values.    */
specifier|private
name|void
name|populateTable
parameter_list|(
name|String
name|table
parameter_list|,
name|int
name|value
parameter_list|)
throws|throws
name|Exception
block|{
comment|// create HFiles for different column families
name|LoadIncrementalHFiles
name|lih
init|=
operator|new
name|LoadIncrementalHFiles
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|useSecure
argument_list|)
decl_stmt|;
name|Path
name|bulk1
init|=
name|buildBulkFiles
argument_list|(
name|table
argument_list|,
name|value
argument_list|)
decl_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
name|lih
operator|.
name|doBulkLoad
argument_list|(
name|bulk1
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
comment|/**    * Split the known table in half.  (this is hard coded for this test suite)    */
specifier|private
name|void
name|forceSplit
parameter_list|(
name|String
name|table
parameter_list|)
block|{
try|try
block|{
comment|// need to call regions server to by synchronous but isn't visible.
name|HRegionServer
name|hrs
init|=
name|util
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|ProtobufUtil
operator|.
name|getOnlineRegions
argument_list|(
name|hrs
argument_list|)
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|hri
operator|.
name|getTableName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|table
argument_list|)
argument_list|)
condition|)
block|{
comment|// splitRegion doesn't work if startkey/endkey are null
name|ProtobufUtil
operator|.
name|split
argument_list|(
name|hrs
argument_list|,
name|hri
argument_list|,
name|rowkey
argument_list|(
name|ROWCOUNT
operator|/
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|// hard code split
block|}
block|}
comment|// verify that split completed.
name|int
name|regions
decl_stmt|;
do|do
block|{
name|regions
operator|=
literal|0
expr_stmt|;
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|ProtobufUtil
operator|.
name|getOnlineRegions
argument_list|(
name|hrs
argument_list|)
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|hri
operator|.
name|getTableName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|table
argument_list|)
argument_list|)
condition|)
block|{
name|regions
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|regions
operator|!=
literal|2
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Taking some time to complete split..."
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|250
argument_list|)
expr_stmt|;
block|}
block|}
do|while
condition|(
name|regions
operator|!=
literal|2
condition|)
do|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|util
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
name|teardownCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Checks that all columns have the expected value and that there is the    * expected number of rows.    */
name|void
name|assertExpectedTable
parameter_list|(
name|String
name|table
parameter_list|,
name|int
name|count
parameter_list|,
name|int
name|value
parameter_list|)
block|{
try|try
block|{
name|assertEquals
argument_list|(
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|listTables
argument_list|(
name|table
argument_list|)
operator|.
name|length
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|table
argument_list|)
decl_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|ResultScanner
name|sr
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|r
range|:
name|sr
control|)
block|{
name|i
operator|++
expr_stmt|;
for|for
control|(
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|nm
range|:
name|r
operator|.
name|getNoVersionMap
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|byte
index|[]
name|val
range|:
name|nm
operator|.
name|values
argument_list|()
control|)
block|{
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|val
argument_list|,
name|value
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|assertEquals
argument_list|(
name|count
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Failed due to exception"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test that shows that exception thrown from the RS side will result in an    * exception on the LIHFile client.    */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IOException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testBulkLoadPhaseFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|table
init|=
literal|"bulkLoadPhaseFailure"
decl_stmt|;
name|setupTable
argument_list|(
name|table
argument_list|,
literal|10
argument_list|)
expr_stmt|;
specifier|final
name|AtomicInteger
name|attmptedCalls
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|AtomicInteger
name|failedCalls
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|LoadIncrementalHFiles
name|lih
init|=
operator|new
name|LoadIncrementalHFiles
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|useSecure
argument_list|)
block|{
specifier|protected
name|List
argument_list|<
name|LoadQueueItem
argument_list|>
name|tryAtomicRegionLoad
parameter_list|(
specifier|final
name|HConnection
name|conn
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|first
parameter_list|,
name|Collection
argument_list|<
name|LoadQueueItem
argument_list|>
name|lqis
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|i
init|=
name|attmptedCalls
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|1
condition|)
block|{
name|HConnection
name|errConn
init|=
literal|null
decl_stmt|;
try|try
block|{
name|errConn
operator|=
name|getMockedConnection
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"mocking cruft, should never happen"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"mocking cruft, should never happen"
argument_list|)
throw|;
block|}
name|failedCalls
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return
name|super
operator|.
name|tryAtomicRegionLoad
argument_list|(
name|errConn
argument_list|,
name|tableName
argument_list|,
name|first
argument_list|,
name|lqis
argument_list|)
return|;
block|}
return|return
name|super
operator|.
name|tryAtomicRegionLoad
argument_list|(
name|conn
argument_list|,
name|tableName
argument_list|,
name|first
argument_list|,
name|lqis
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|// create HFiles for different column families
name|Path
name|dir
init|=
name|buildBulkFiles
argument_list|(
name|table
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
name|lih
operator|.
name|doBulkLoad
argument_list|(
name|dir
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"doBulkLoad should have thrown an exception"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HConnection
name|getMockedConnection
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|ServiceException
block|{
name|HConnection
name|c
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HConnection
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|c
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|doNothing
argument_list|()
operator|.
name|when
argument_list|(
name|c
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Make it so we return a particular location when asked.
specifier|final
name|HRegionLocation
name|loc
init|=
operator|new
name|HRegionLocation
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
operator|new
name|ServerName
argument_list|(
literal|"example.org"
argument_list|,
literal|1234
argument_list|,
literal|0
argument_list|)
argument_list|,
name|HConstants
operator|.
name|NO_SEQNUM
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|c
operator|.
name|getRegionLocation
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
name|Mockito
operator|.
name|anyBoolean
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|loc
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|c
operator|.
name|locateRegion
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|loc
argument_list|)
expr_stmt|;
name|ClientProtocol
name|hri
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ClientProtocol
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|hri
operator|.
name|bulkLoadHFile
argument_list|(
operator|(
name|RpcController
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
operator|(
name|BulkLoadHFileRequest
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenThrow
argument_list|(
operator|new
name|ServiceException
argument_list|(
operator|new
name|IOException
argument_list|(
literal|"injecting bulk load error"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|c
operator|.
name|getClient
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|hri
argument_list|)
expr_stmt|;
return|return
name|c
return|;
block|}
comment|/**    * This test exercises the path where there is a split after initial    * validation but before the atomic bulk load call. We cannot use presplitting    * to test this path, so we actually inject a split just before the atomic    * region load.    */
annotation|@
name|Test
specifier|public
name|void
name|testSplitWhileBulkLoadPhase
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|table
init|=
literal|"splitWhileBulkloadPhase"
decl_stmt|;
name|setupTable
argument_list|(
name|table
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|populateTable
argument_list|(
name|table
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertExpectedTable
argument_list|(
name|table
argument_list|,
name|ROWCOUNT
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// Now let's cause trouble.  This will occur after checks and cause bulk
comment|// files to fail when attempt to atomically import.  This is recoverable.
specifier|final
name|AtomicInteger
name|attemptedCalls
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|LoadIncrementalHFiles
name|lih2
init|=
operator|new
name|LoadIncrementalHFiles
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|useSecure
argument_list|)
block|{
specifier|protected
name|void
name|bulkLoadPhase
parameter_list|(
specifier|final
name|HTable
name|htable
parameter_list|,
specifier|final
name|HConnection
name|conn
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|Deque
argument_list|<
name|LoadQueueItem
argument_list|>
name|queue
parameter_list|,
specifier|final
name|Multimap
argument_list|<
name|ByteBuffer
argument_list|,
name|LoadQueueItem
argument_list|>
name|regionGroups
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|i
init|=
name|attemptedCalls
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|1
condition|)
block|{
comment|// On first attempt force a split.
name|forceSplit
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|bulkLoadPhase
argument_list|(
name|htable
argument_list|,
name|conn
argument_list|,
name|pool
argument_list|,
name|queue
argument_list|,
name|regionGroups
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
comment|// create HFiles for different column families
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|bulk
init|=
name|buildBulkFiles
argument_list|(
name|table
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|lih2
operator|.
name|doBulkLoad
argument_list|(
name|bulk
argument_list|,
name|t
argument_list|)
expr_stmt|;
comment|// check that data was loaded
comment|// The three expected attempts are 1) failure because need to split, 2)
comment|// load of split top 3) load of split bottom
name|assertEquals
argument_list|(
name|attemptedCalls
operator|.
name|get
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertExpectedTable
argument_list|(
name|table
argument_list|,
name|ROWCOUNT
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
comment|/**    * This test splits a table and attempts to bulk load.  The bulk import files    * should be split before atomically importing.    */
annotation|@
name|Test
specifier|public
name|void
name|testGroupOrSplitPresplit
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|table
init|=
literal|"groupOrSplitPresplit"
decl_stmt|;
name|setupTable
argument_list|(
name|table
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|populateTable
argument_list|(
name|table
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertExpectedTable
argument_list|(
name|table
argument_list|,
name|ROWCOUNT
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|forceSplit
argument_list|(
name|table
argument_list|)
expr_stmt|;
specifier|final
name|AtomicInteger
name|countedLqis
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|LoadIncrementalHFiles
name|lih
init|=
operator|new
name|LoadIncrementalHFiles
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|useSecure
argument_list|)
block|{
specifier|protected
name|List
argument_list|<
name|LoadQueueItem
argument_list|>
name|groupOrSplit
parameter_list|(
name|Multimap
argument_list|<
name|ByteBuffer
argument_list|,
name|LoadQueueItem
argument_list|>
name|regionGroups
parameter_list|,
specifier|final
name|LoadQueueItem
name|item
parameter_list|,
specifier|final
name|HTable
name|htable
parameter_list|,
specifier|final
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
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|LoadQueueItem
argument_list|>
name|lqis
init|=
name|super
operator|.
name|groupOrSplit
argument_list|(
name|regionGroups
argument_list|,
name|item
argument_list|,
name|htable
argument_list|,
name|startEndKeys
argument_list|)
decl_stmt|;
if|if
condition|(
name|lqis
operator|!=
literal|null
condition|)
block|{
name|countedLqis
operator|.
name|addAndGet
argument_list|(
name|lqis
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|lqis
return|;
block|}
block|}
decl_stmt|;
comment|// create HFiles for different column families
name|Path
name|bulk
init|=
name|buildBulkFiles
argument_list|(
name|table
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|HTable
name|ht
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
name|lih
operator|.
name|doBulkLoad
argument_list|(
name|bulk
argument_list|,
name|ht
argument_list|)
expr_stmt|;
name|assertExpectedTable
argument_list|(
name|table
argument_list|,
name|ROWCOUNT
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|20
argument_list|,
name|countedLqis
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * This simulates an remote exception which should cause LIHF to exit with an    * exception.    */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IOException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testGroupOrSplitFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|table
init|=
literal|"groupOrSplitFailure"
decl_stmt|;
name|setupTable
argument_list|(
name|table
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|LoadIncrementalHFiles
name|lih
init|=
operator|new
name|LoadIncrementalHFiles
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|useSecure
argument_list|)
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|LoadQueueItem
argument_list|>
name|groupOrSplit
parameter_list|(
name|Multimap
argument_list|<
name|ByteBuffer
argument_list|,
name|LoadQueueItem
argument_list|>
name|regionGroups
parameter_list|,
specifier|final
name|LoadQueueItem
name|item
parameter_list|,
specifier|final
name|HTable
name|table
parameter_list|,
specifier|final
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
parameter_list|)
throws|throws
name|IOException
block|{
name|i
operator|++
expr_stmt|;
if|if
condition|(
name|i
operator|==
literal|5
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"failure"
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|groupOrSplit
argument_list|(
name|regionGroups
argument_list|,
name|item
argument_list|,
name|table
argument_list|,
name|startEndKeys
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|// create HFiles for different column families
name|Path
name|dir
init|=
name|buildBulkFiles
argument_list|(
name|table
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|table
argument_list|)
argument_list|)
decl_stmt|;
name|lih
operator|.
name|doBulkLoad
argument_list|(
name|dir
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"doBulkLoad should have thrown an exception"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

