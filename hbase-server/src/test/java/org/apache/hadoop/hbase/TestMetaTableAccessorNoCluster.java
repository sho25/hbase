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
name|NavigableMap
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
name|ClusterConnection
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
name|HConnectionTestingUtility
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
name|RegionInfoBuilder
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
name|ipc
operator|.
name|HBaseRpcController
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
name|zookeeper
operator|.
name|ZKWatcher
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ScanRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ScanResponse
import|;
end_import

begin_comment
comment|/**  * Test MetaTableAccessor but without spinning up a cluster.  * We mock regionserver back and forth (we do spin up a zk cluster).  */
end_comment

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
specifier|public
class|class
name|TestMetaTableAccessorNoCluster
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
name|TestMetaTableAccessorNoCluster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestMetaTableAccessorNoCluster
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
name|Abortable
name|ABORTABLE
init|=
operator|new
name|Abortable
argument_list|()
block|{
name|boolean
name|aborted
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|why
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|this
operator|.
name|aborted
operator|=
literal|true
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|this
operator|.
name|aborted
return|;
block|}
block|}
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|after
parameter_list|()
throws|throws
name|IOException
block|{
name|UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetHRegionInfo
parameter_list|()
throws|throws
name|IOException
block|{
name|assertNull
argument_list|(
name|MetaTableAccessor
operator|.
name|getRegionInfo
argument_list|(
operator|new
name|Result
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Result
name|r
init|=
name|Result
operator|.
name|create
argument_list|(
name|kvs
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|MetaTableAccessor
operator|.
name|getRegionInfo
argument_list|(
name|r
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|f
init|=
name|HConstants
operator|.
name|CATALOG_FAMILY
decl_stmt|;
comment|// Make a key value that doesn't have the expected qualifier.
name|kvs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|f
argument_list|,
name|HConstants
operator|.
name|SERVER_QUALIFIER
argument_list|,
name|f
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|=
name|Result
operator|.
name|create
argument_list|(
name|kvs
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|MetaTableAccessor
operator|.
name|getRegionInfo
argument_list|(
name|r
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make a key that does not have a regioninfo value.
name|kvs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|f
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|,
name|f
argument_list|)
argument_list|)
expr_stmt|;
name|RegionInfo
name|hri
init|=
name|MetaTableAccessor
operator|.
name|getRegionInfo
argument_list|(
name|Result
operator|.
name|create
argument_list|(
name|kvs
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|hri
operator|==
literal|null
argument_list|)
expr_stmt|;
comment|// OK, give it what it expects
name|kvs
operator|.
name|clear
argument_list|()
expr_stmt|;
name|kvs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|f
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|,
name|RegionInfo
operator|.
name|toByteArray
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|hri
operator|=
name|MetaTableAccessor
operator|.
name|getRegionInfo
argument_list|(
name|Result
operator|.
name|create
argument_list|(
name|kvs
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|hri
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|RegionInfo
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|hri
argument_list|,
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that MetaTableAccessor will ride over server throwing    * "Server not running" IOEs.    * @see @link {https://issues.apache.org/jira/browse/HBASE-3446}    * @throws IOException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testRideOverServerNotRunning
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ServiceException
block|{
comment|// Need a zk watcher.
name|ZKWatcher
name|zkw
init|=
operator|new
name|ZKWatcher
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|ABORTABLE
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// This is a servername we use in a few places below.
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"example.com"
argument_list|,
literal|1234
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|ClusterConnection
name|connection
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Mock an ClientProtocol. Our mock implementation will fail a few
comment|// times when we go to open a scanner.
specifier|final
name|ClientProtos
operator|.
name|ClientService
operator|.
name|BlockingInterface
name|implementation
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ClientProtos
operator|.
name|ClientService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// When scan called throw IOE 'Server not running' a few times
comment|// before we return a scanner id.  Whats WEIRD is that these
comment|// exceptions do not show in the log because they are caught and only
comment|// printed if we FAIL.  We eventually succeed after retry so these don't
comment|// show.  We will know if they happened or not because we will ask
comment|// mockito at the end of this test to verify that scan was indeed
comment|// called the wanted number of times.
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|byte
index|[]
name|rowToVerify
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"rowToVerify"
argument_list|)
decl_stmt|;
name|kvs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowToVerify
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|,
name|RegionInfo
operator|.
name|toByteArray
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|kvs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowToVerify
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|SERVER_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|sn
operator|.
name|getHostAndPort
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|kvs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|rowToVerify
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|STARTCODE_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|sn
operator|.
name|getStartcode
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|CellScannable
argument_list|>
name|cellScannables
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|cellScannables
operator|.
name|add
argument_list|(
name|Result
operator|.
name|create
argument_list|(
name|kvs
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|ScanResponse
operator|.
name|Builder
name|builder
init|=
name|ScanResponse
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|CellScannable
name|result
range|:
name|cellScannables
control|)
block|{
name|builder
operator|.
name|addCellsPerResult
argument_list|(
operator|(
operator|(
name|Result
operator|)
name|result
operator|)
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Mockito
operator|.
name|when
argument_list|(
name|implementation
operator|.
name|scan
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
name|ScanRequest
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
literal|"Server not running (1 of 3)"
argument_list|)
argument_list|)
operator|.
name|thenThrow
argument_list|(
operator|new
name|ServiceException
argument_list|(
literal|"Server not running (2 of 3)"
argument_list|)
argument_list|)
operator|.
name|thenThrow
argument_list|(
operator|new
name|ServiceException
argument_list|(
literal|"Server not running (3 of 3)"
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|ScanResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ScanResponse
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
operator|(
operator|(
name|HBaseRpcController
operator|)
name|invocation
operator|.
name|getArgument
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|setCellScanner
argument_list|(
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cellScannables
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|setScannerId
argument_list|(
literal|1234567890L
argument_list|)
operator|.
name|setMoreResults
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Associate a spied-upon Connection with UTIL.getConfiguration.  Need
comment|// to shove this in here first so it gets picked up all over; e.g. by
comment|// HTable.
name|connection
operator|=
name|HConnectionTestingUtility
operator|.
name|getSpiedConnection
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
comment|// Fix the location lookup so it 'works' though no network.  First
comment|// make an 'any location' object.
specifier|final
name|HRegionLocation
name|anyLocation
init|=
operator|new
name|HRegionLocation
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|sn
argument_list|)
decl_stmt|;
specifier|final
name|RegionLocations
name|rl
init|=
operator|new
name|RegionLocations
argument_list|(
name|anyLocation
argument_list|)
decl_stmt|;
comment|// Return the RegionLocations object when locateRegion
comment|// The ugly format below comes of 'Important gotcha on spying real objects!' from
comment|// http://mockito.googlecode.com/svn/branches/1.6/javadoc/org/mockito/Mockito.html
name|Mockito
operator|.
name|doReturn
argument_list|(
name|rl
argument_list|)
operator|.
name|when
argument_list|(
name|connection
argument_list|)
operator|.
name|locateRegion
argument_list|(
operator|(
name|TableName
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
argument_list|,
name|Mockito
operator|.
name|anyBoolean
argument_list|()
argument_list|,
name|Mockito
operator|.
name|anyInt
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now shove our HRI implementation into the spied-upon connection.
name|Mockito
operator|.
name|doReturn
argument_list|(
name|implementation
argument_list|)
operator|.
name|when
argument_list|(
name|connection
argument_list|)
operator|.
name|getClient
argument_list|(
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
expr_stmt|;
comment|// Scan meta for user tables and verify we got back expected answer.
name|NavigableMap
argument_list|<
name|RegionInfo
argument_list|,
name|Result
argument_list|>
name|hris
init|=
name|MetaTableAccessor
operator|.
name|getServerUserRegions
argument_list|(
name|connection
argument_list|,
name|sn
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|hris
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|RegionInfo
operator|.
name|COMPARATOR
operator|.
name|compare
argument_list|(
name|hris
operator|.
name|firstEntry
argument_list|()
operator|.
name|getKey
argument_list|()
argument_list|,
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|rowToVerify
argument_list|,
name|hris
operator|.
name|firstEntry
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Finally verify that scan was called four times -- three times
comment|// with exception and then on 4th attempt we succeed
name|Mockito
operator|.
name|verify
argument_list|(
name|implementation
argument_list|,
name|Mockito
operator|.
name|times
argument_list|(
literal|4
argument_list|)
argument_list|)
operator|.
name|scan
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
name|ScanRequest
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|connection
operator|!=
literal|null
operator|&&
operator|!
name|connection
operator|.
name|isClosed
argument_list|()
condition|)
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

