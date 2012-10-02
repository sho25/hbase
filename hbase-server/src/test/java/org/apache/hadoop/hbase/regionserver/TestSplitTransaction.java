begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|spy
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
name|when
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
name|Server
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
name|SmallTests
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
name|io
operator|.
name|hfile
operator|.
name|CacheConfig
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
name|io
operator|.
name|hfile
operator|.
name|HFile
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
name|io
operator|.
name|hfile
operator|.
name|LruBlockCache
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
name|BaseRegionObserver
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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
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
name|HLogFactory
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
name|PairOfSameType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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
name|ImmutableList
import|;
end_import

begin_comment
comment|/**  * Test the {@link SplitTransaction} class against an HRegion (as opposed to  * running cluster).  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestSplitTransaction
block|{
specifier|private
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Path
name|testdir
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|HRegion
name|parent
decl_stmt|;
specifier|private
name|HLog
name|wal
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|STARTROW
init|=
operator|new
name|byte
index|[]
block|{
literal|'a'
block|,
literal|'a'
block|,
literal|'a'
block|}
decl_stmt|;
comment|// '{' is next ascii after 'z'.
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ENDROW
init|=
operator|new
name|byte
index|[]
block|{
literal|'{'
block|,
literal|'{'
block|,
literal|'{'
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|GOOD_SPLIT_ROW
init|=
operator|new
name|byte
index|[]
block|{
literal|'d'
block|,
literal|'d'
block|,
literal|'d'
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CF
init|=
name|HConstants
operator|.
name|CATALOG_FAMILY
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|preRollBackCalled
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|postRollBackCalled
init|=
literal|false
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|CustomObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|this
operator|.
name|testdir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|wal
operator|=
name|HLogFactory
operator|.
name|createHLog
argument_list|(
name|fs
argument_list|,
name|this
operator|.
name|testdir
argument_list|,
literal|"logs"
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|createRegion
argument_list|(
name|this
operator|.
name|testdir
argument_list|,
name|this
operator|.
name|wal
argument_list|)
expr_stmt|;
name|RegionCoprocessorHost
name|host
init|=
operator|new
name|RegionCoprocessorHost
argument_list|(
name|this
operator|.
name|parent
argument_list|,
literal|null
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|parent
operator|.
name|setCoprocessorHost
argument_list|(
name|host
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"hbase.testing.nocluster"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|teardown
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|parent
operator|!=
literal|null
operator|&&
operator|!
name|this
operator|.
name|parent
operator|.
name|isClosed
argument_list|()
condition|)
name|this
operator|.
name|parent
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|this
operator|.
name|parent
operator|.
name|getRegionDir
argument_list|()
argument_list|)
operator|&&
operator|!
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|this
operator|.
name|parent
operator|.
name|getRegionDir
argument_list|()
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed delete of "
operator|+
name|this
operator|.
name|parent
operator|.
name|getRegionDir
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|this
operator|.
name|wal
operator|!=
literal|null
condition|)
name|this
operator|.
name|wal
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|this
operator|.
name|testdir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFailAfterPONR
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
specifier|final
name|int
name|rowcount
init|=
name|TEST_UTIL
operator|.
name|loadRegion
argument_list|(
name|this
operator|.
name|parent
argument_list|,
name|CF
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|rowcount
operator|>
literal|0
argument_list|)
expr_stmt|;
name|int
name|parentRowCount
init|=
name|countRows
argument_list|(
name|this
operator|.
name|parent
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|rowcount
argument_list|,
name|parentRowCount
argument_list|)
expr_stmt|;
comment|// Start transaction.
name|SplitTransaction
name|st
init|=
name|prepareGOOD_SPLIT_ROW
argument_list|()
decl_stmt|;
name|SplitTransaction
name|spiedUponSt
init|=
name|spy
argument_list|(
name|st
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doThrow
argument_list|(
operator|new
name|MockedFailedDaughterOpen
argument_list|()
argument_list|)
operator|.
name|when
argument_list|(
name|spiedUponSt
argument_list|)
operator|.
name|openDaughterRegion
argument_list|(
operator|(
name|Server
operator|)
name|Mockito
operator|.
name|anyObject
argument_list|()
argument_list|,
operator|(
name|HRegion
operator|)
name|Mockito
operator|.
name|anyObject
argument_list|()
argument_list|)
expr_stmt|;
comment|// Run the execute.  Look at what it returns.
name|boolean
name|expectedException
init|=
literal|false
decl_stmt|;
name|Server
name|mockServer
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Server
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockServer
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|spiedUponSt
operator|.
name|execute
argument_list|(
name|mockServer
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
operator|&&
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|MockedFailedDaughterOpen
condition|)
block|{
name|expectedException
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|expectedException
argument_list|)
expr_stmt|;
comment|// Run rollback returns that we should restart.
name|assertFalse
argument_list|(
name|spiedUponSt
operator|.
name|rollback
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure that region a and region b are still in the filesystem, that
comment|// they have not been removed; this is supposed to be the case if we go
comment|// past point of no return.
name|Path
name|tableDir
init|=
name|this
operator|.
name|parent
operator|.
name|getRegionDir
argument_list|()
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|Path
name|daughterADir
init|=
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
name|spiedUponSt
operator|.
name|getFirstDaughter
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|daughterBDir
init|=
operator|new
name|Path
argument_list|(
name|tableDir
argument_list|,
name|spiedUponSt
operator|.
name|getSecondDaughter
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|exists
argument_list|(
name|daughterADir
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|exists
argument_list|(
name|daughterBDir
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test straight prepare works.  Tries to split on {@link #GOOD_SPLIT_ROW}    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testPrepare
parameter_list|()
throws|throws
name|IOException
block|{
name|prepareGOOD_SPLIT_ROW
argument_list|()
expr_stmt|;
block|}
specifier|private
name|SplitTransaction
name|prepareGOOD_SPLIT_ROW
parameter_list|()
block|{
name|SplitTransaction
name|st
init|=
operator|new
name|SplitTransaction
argument_list|(
name|this
operator|.
name|parent
argument_list|,
name|GOOD_SPLIT_ROW
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|st
operator|.
name|prepare
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|st
return|;
block|}
comment|/**    * Pass a reference store    */
annotation|@
name|Test
specifier|public
name|void
name|testPrepareWithRegionsWithReference
parameter_list|()
throws|throws
name|IOException
block|{
comment|// create a mock that will act as a reference StoreFile
name|StoreFile
name|storeFileMock
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|StoreFile
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|storeFileMock
operator|.
name|isReference
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// add the mock to the parent stores
name|HStore
name|storeMock
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HStore
operator|.
name|class
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|StoreFile
argument_list|>
name|storeFileList
init|=
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|storeFileList
operator|.
name|add
argument_list|(
name|storeFileMock
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|storeMock
operator|.
name|getStorefiles
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|storeFileList
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|storeMock
operator|.
name|close
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|storeFileList
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|parent
operator|.
name|stores
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
argument_list|)
argument_list|,
name|storeMock
argument_list|)
expr_stmt|;
name|SplitTransaction
name|st
init|=
operator|new
name|SplitTransaction
argument_list|(
name|this
operator|.
name|parent
argument_list|,
name|GOOD_SPLIT_ROW
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"a region should not be splittable if it has instances of store file references"
argument_list|,
name|st
operator|.
name|prepare
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Pass an unreasonable split row.    */
annotation|@
name|Test
specifier|public
name|void
name|testPrepareWithBadSplitRow
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Pass start row as split key.
name|SplitTransaction
name|st
init|=
operator|new
name|SplitTransaction
argument_list|(
name|this
operator|.
name|parent
argument_list|,
name|STARTROW
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|st
operator|.
name|prepare
argument_list|()
argument_list|)
expr_stmt|;
name|st
operator|=
operator|new
name|SplitTransaction
argument_list|(
name|this
operator|.
name|parent
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|st
operator|.
name|prepare
argument_list|()
argument_list|)
expr_stmt|;
name|st
operator|=
operator|new
name|SplitTransaction
argument_list|(
name|this
operator|.
name|parent
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|'A'
block|,
literal|'A'
block|,
literal|'A'
block|}
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|st
operator|.
name|prepare
argument_list|()
argument_list|)
expr_stmt|;
name|st
operator|=
operator|new
name|SplitTransaction
argument_list|(
name|this
operator|.
name|parent
argument_list|,
name|ENDROW
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|st
operator|.
name|prepare
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPrepareWithClosedRegion
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|parent
operator|.
name|close
argument_list|()
expr_stmt|;
name|SplitTransaction
name|st
init|=
operator|new
name|SplitTransaction
argument_list|(
name|this
operator|.
name|parent
argument_list|,
name|GOOD_SPLIT_ROW
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|st
operator|.
name|prepare
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWholesomeSplitWithHFileV1
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|defaultVersion
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
name|HFile
operator|.
name|FORMAT_VERSION_KEY
argument_list|,
literal|2
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HFile
operator|.
name|FORMAT_VERSION_KEY
argument_list|,
literal|1
argument_list|)
expr_stmt|;
try|try
block|{
for|for
control|(
name|Store
name|store
range|:
name|this
operator|.
name|parent
operator|.
name|stores
operator|.
name|values
argument_list|()
control|)
block|{
name|store
operator|.
name|getFamily
argument_list|()
operator|.
name|setBloomFilterType
argument_list|(
name|StoreFile
operator|.
name|BloomType
operator|.
name|ROW
argument_list|)
expr_stmt|;
block|}
name|testWholesomeSplit
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HFile
operator|.
name|FORMAT_VERSION_KEY
argument_list|,
name|defaultVersion
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWholesomeSplit
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|rowcount
init|=
name|TEST_UTIL
operator|.
name|loadRegion
argument_list|(
name|this
operator|.
name|parent
argument_list|,
name|CF
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|rowcount
operator|>
literal|0
argument_list|)
expr_stmt|;
name|int
name|parentRowCount
init|=
name|countRows
argument_list|(
name|this
operator|.
name|parent
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|rowcount
argument_list|,
name|parentRowCount
argument_list|)
expr_stmt|;
comment|// Pretend region's blocks are not in the cache, used for
comment|// testWholesomeSplitWithHFileV1
name|CacheConfig
name|cacheConf
init|=
operator|new
name|CacheConfig
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
operator|(
operator|(
name|LruBlockCache
operator|)
name|cacheConf
operator|.
name|getBlockCache
argument_list|()
operator|)
operator|.
name|clearCache
argument_list|()
expr_stmt|;
comment|// Start transaction.
name|SplitTransaction
name|st
init|=
name|prepareGOOD_SPLIT_ROW
argument_list|()
decl_stmt|;
comment|// Run the execute.  Look at what it returns.
name|Server
name|mockServer
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Server
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockServer
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|PairOfSameType
argument_list|<
name|HRegion
argument_list|>
name|daughters
init|=
name|st
operator|.
name|execute
argument_list|(
name|mockServer
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// Do some assertions about execution.
name|assertTrue
argument_list|(
name|this
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|st
operator|.
name|getSplitDir
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Assert the parent region is closed.
name|assertTrue
argument_list|(
name|this
operator|.
name|parent
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
comment|// Assert splitdir is empty -- because its content will have been moved out
comment|// to be under the daughter region dirs.
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|this
operator|.
name|fs
operator|.
name|listStatus
argument_list|(
name|st
operator|.
name|getSplitDir
argument_list|()
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// Check daughters have correct key span.
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|parent
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|daughters
operator|.
name|getFirst
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|GOOD_SPLIT_ROW
argument_list|,
name|daughters
operator|.
name|getFirst
argument_list|()
operator|.
name|getEndKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|daughters
operator|.
name|getSecond
argument_list|()
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|GOOD_SPLIT_ROW
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|parent
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|daughters
operator|.
name|getSecond
argument_list|()
operator|.
name|getEndKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Count rows.
name|int
name|daughtersRowCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HRegion
name|r
range|:
name|daughters
control|)
block|{
comment|// Open so can count its content.
name|HRegion
name|openRegion
init|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|this
operator|.
name|testdir
argument_list|,
name|r
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|r
operator|.
name|getTableDesc
argument_list|()
argument_list|,
name|r
operator|.
name|getLog
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|int
name|count
init|=
name|countRows
argument_list|(
name|openRegion
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|count
operator|>
literal|0
operator|&&
name|count
operator|!=
name|rowcount
argument_list|)
expr_stmt|;
name|daughtersRowCount
operator|+=
name|count
expr_stmt|;
block|}
finally|finally
block|{
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|openRegion
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
name|rowcount
argument_list|,
name|daughtersRowCount
argument_list|)
expr_stmt|;
comment|// Assert the write lock is no longer held on parent
name|assertTrue
argument_list|(
operator|!
name|this
operator|.
name|parent
operator|.
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|isHeldByCurrentThread
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRollback
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|rowcount
init|=
name|TEST_UTIL
operator|.
name|loadRegion
argument_list|(
name|this
operator|.
name|parent
argument_list|,
name|CF
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|rowcount
operator|>
literal|0
argument_list|)
expr_stmt|;
name|int
name|parentRowCount
init|=
name|countRows
argument_list|(
name|this
operator|.
name|parent
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|rowcount
argument_list|,
name|parentRowCount
argument_list|)
expr_stmt|;
comment|// Start transaction.
name|SplitTransaction
name|st
init|=
name|prepareGOOD_SPLIT_ROW
argument_list|()
decl_stmt|;
name|SplitTransaction
name|spiedUponSt
init|=
name|spy
argument_list|(
name|st
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|spiedUponSt
operator|.
name|createDaughterRegion
argument_list|(
name|spiedUponSt
operator|.
name|getSecondDaughter
argument_list|()
argument_list|,
literal|null
argument_list|)
argument_list|)
operator|.
name|thenThrow
argument_list|(
operator|new
name|MockedFailedDaughterCreation
argument_list|()
argument_list|)
expr_stmt|;
comment|// Run the execute.  Look at what it returns.
name|boolean
name|expectedException
init|=
literal|false
decl_stmt|;
name|Server
name|mockServer
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Server
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|mockServer
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|spiedUponSt
operator|.
name|execute
argument_list|(
name|mockServer
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MockedFailedDaughterCreation
name|e
parameter_list|)
block|{
name|expectedException
operator|=
literal|true
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|expectedException
argument_list|)
expr_stmt|;
comment|// Run rollback
name|assertTrue
argument_list|(
name|spiedUponSt
operator|.
name|rollback
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// Assert I can scan parent.
name|int
name|parentRowCount2
init|=
name|countRows
argument_list|(
name|this
operator|.
name|parent
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|parentRowCount
argument_list|,
name|parentRowCount2
argument_list|)
expr_stmt|;
comment|// Assert rollback cleaned up stuff in fs
name|assertTrue
argument_list|(
operator|!
name|this
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|this
operator|.
name|testdir
argument_list|,
name|st
operator|.
name|getFirstDaughter
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|!
name|this
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|this
operator|.
name|testdir
argument_list|,
name|st
operator|.
name|getSecondDaughter
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|!
name|this
operator|.
name|parent
operator|.
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|isHeldByCurrentThread
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now retry the split but do not throw an exception this time.
name|assertTrue
argument_list|(
name|st
operator|.
name|prepare
argument_list|()
argument_list|)
expr_stmt|;
name|PairOfSameType
argument_list|<
name|HRegion
argument_list|>
name|daughters
init|=
name|st
operator|.
name|execute
argument_list|(
name|mockServer
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// Count rows.
name|int
name|daughtersRowCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HRegion
name|r
range|:
name|daughters
control|)
block|{
comment|// Open so can count its content.
name|HRegion
name|openRegion
init|=
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|this
operator|.
name|testdir
argument_list|,
name|r
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|r
operator|.
name|getTableDesc
argument_list|()
argument_list|,
name|r
operator|.
name|getLog
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|int
name|count
init|=
name|countRows
argument_list|(
name|openRegion
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|count
operator|>
literal|0
operator|&&
name|count
operator|!=
name|rowcount
argument_list|)
expr_stmt|;
name|daughtersRowCount
operator|+=
name|count
expr_stmt|;
block|}
finally|finally
block|{
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|openRegion
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
name|rowcount
argument_list|,
name|daughtersRowCount
argument_list|)
expr_stmt|;
comment|// Assert the write lock is no longer held on parent
name|assertTrue
argument_list|(
operator|!
name|this
operator|.
name|parent
operator|.
name|lock
operator|.
name|writeLock
argument_list|()
operator|.
name|isHeldByCurrentThread
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Rollback hooks should be called."
argument_list|,
name|wasRollBackHookCalled
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|wasRollBackHookCalled
parameter_list|()
block|{
return|return
operator|(
name|preRollBackCalled
operator|&&
name|postRollBackCalled
operator|)
return|;
block|}
comment|/**    * Exception used in this class only.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|private
class|class
name|MockedFailedDaughterCreation
extends|extends
name|IOException
block|{}
specifier|private
class|class
name|MockedFailedDaughterOpen
extends|extends
name|IOException
block|{}
specifier|private
name|int
name|countRows
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|rowcount
init|=
literal|0
decl_stmt|;
name|InternalScanner
name|scanner
init|=
name|r
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|hasNext
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|hasNext
condition|)
block|{
name|hasNext
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|kvs
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|kvs
operator|.
name|isEmpty
argument_list|()
condition|)
name|rowcount
operator|++
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|rowcount
return|;
block|}
name|HRegion
name|createRegion
parameter_list|(
specifier|final
name|Path
name|testdir
parameter_list|,
specifier|final
name|HLog
name|wal
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Make a region with start and end keys. Use 'aaa', to 'AAA'.  The load
comment|// region utility will add rows between 'aaa' and 'zzz'.
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|CF
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
name|STARTROW
argument_list|,
name|ENDROW
argument_list|)
decl_stmt|;
name|HRegion
name|r
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|hri
argument_list|,
name|testdir
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|htd
argument_list|)
decl_stmt|;
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|r
argument_list|)
expr_stmt|;
return|return
name|HRegion
operator|.
name|openHRegion
argument_list|(
name|testdir
argument_list|,
name|hri
argument_list|,
name|htd
argument_list|,
name|wal
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|CustomObserver
extends|extends
name|BaseRegionObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|preRollBackSplit
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|preRollBackCalled
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postRollBackSplit
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|postRollBackCalled
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

