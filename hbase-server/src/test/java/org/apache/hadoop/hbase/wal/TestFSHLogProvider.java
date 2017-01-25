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
name|wal
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
name|HashSet
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
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|FileStatus
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
name|regionserver
operator|.
name|MultiVersionConcurrencyControl
import|;
end_import

begin_comment
comment|// imports for things that haven't moved from regionserver.wal yet.
end_comment

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
name|WALEdit
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
name|util
operator|.
name|FSUtils
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestFSHLogProvider
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
name|TestFSHLogProvider
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|Configuration
name|conf
decl_stmt|;
specifier|protected
specifier|static
name|FileSystem
name|fs
decl_stmt|;
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
specifier|protected
name|MultiVersionConcurrencyControl
name|mvcc
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|currentTest
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
name|mvcc
operator|=
operator|new
name|MultiVersionConcurrencyControl
argument_list|()
expr_stmt|;
name|FileStatus
index|[]
name|entries
init|=
name|fs
operator|.
name|listStatus
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/"
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|dir
range|:
name|entries
control|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|dir
operator|.
name|getPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{   }
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
comment|// Make block sizes small.
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.blocksize"
argument_list|,
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
comment|// quicker heartbeat interval for faster DN death notification
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.namenode.heartbeat.recheck-interval"
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.heartbeat.interval"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.client.socket-timeout"
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
comment|// faster failover with cluster.shutdown();fs.close() idiom
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.ipc.client.connect.max.retries"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.client.block.recovery.retries"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.ipc.client.connection.maxidletime"
argument_list|,
literal|500
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
comment|// Set up a working space for our tests.
name|TEST_UTIL
operator|.
name|createRootDir
argument_list|()
expr_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|fs
operator|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
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
specifier|static
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"TestDefaultWALProvider"
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetServerNameFromWALDirectoryName
parameter_list|()
throws|throws
name|IOException
block|{
name|ServerName
name|sn
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"hn"
argument_list|,
literal|450
argument_list|,
literal|1398
argument_list|)
decl_stmt|;
name|String
name|hl
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
operator|+
literal|"/"
operator|+
name|AbstractFSWALProvider
operator|.
name|getWALDirectoryName
argument_list|(
name|sn
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
comment|// Must not throw exception
name|assertNull
argument_list|(
name|AbstractFSWALProvider
operator|.
name|getServerNameFromWALDirectoryName
argument_list|(
name|conf
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|AbstractFSWALProvider
operator|.
name|getServerNameFromWALDirectoryName
argument_list|(
name|conf
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|AbstractFSWALProvider
operator|.
name|getServerNameFromWALDirectoryName
argument_list|(
name|conf
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|AbstractFSWALProvider
operator|.
name|getServerNameFromWALDirectoryName
argument_list|(
name|conf
argument_list|,
literal|"                  "
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|AbstractFSWALProvider
operator|.
name|getServerNameFromWALDirectoryName
argument_list|(
name|conf
argument_list|,
name|hl
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|AbstractFSWALProvider
operator|.
name|getServerNameFromWALDirectoryName
argument_list|(
name|conf
argument_list|,
name|hl
operator|+
literal|"qdf"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|AbstractFSWALProvider
operator|.
name|getServerNameFromWALDirectoryName
argument_list|(
name|conf
argument_list|,
literal|"sfqf"
operator|+
name|hl
operator|+
literal|"qdf"
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|String
name|wals
init|=
literal|"/WALs/"
decl_stmt|;
name|ServerName
name|parsed
init|=
name|AbstractFSWALProvider
operator|.
name|getServerNameFromWALDirectoryName
argument_list|(
name|conf
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|wals
operator|+
name|sn
operator|+
literal|"/localhost%2C32984%2C1343316388997.1343316390417"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"standard"
argument_list|,
name|sn
argument_list|,
name|parsed
argument_list|)
expr_stmt|;
name|parsed
operator|=
name|AbstractFSWALProvider
operator|.
name|getServerNameFromWALDirectoryName
argument_list|(
name|conf
argument_list|,
name|hl
operator|+
literal|"/qdf"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"subdir"
argument_list|,
name|sn
argument_list|,
name|parsed
argument_list|)
expr_stmt|;
name|parsed
operator|=
name|AbstractFSWALProvider
operator|.
name|getServerNameFromWALDirectoryName
argument_list|(
name|conf
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|toString
argument_list|()
operator|+
name|wals
operator|+
name|sn
operator|+
literal|"-splitting/localhost%3A57020.1340474893931"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"split"
argument_list|,
name|sn
argument_list|,
name|parsed
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|addEdits
parameter_list|(
name|WAL
name|log
parameter_list|,
name|HRegionInfo
name|hri
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|,
name|int
name|times
parameter_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scopes
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
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
name|times
condition|;
name|i
operator|++
control|)
block|{
name|long
name|timestamp
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|WALEdit
name|cols
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|cols
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|row
argument_list|,
name|row
argument_list|,
name|timestamp
argument_list|,
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|log
operator|.
name|append
argument_list|(
name|hri
argument_list|,
name|getWalKey
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|timestamp
argument_list|,
name|scopes
argument_list|)
argument_list|,
name|cols
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|log
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
comment|/**    * used by TestDefaultWALProviderWithHLogKey    * @param scopes    */
name|WALKey
name|getWalKey
parameter_list|(
specifier|final
name|byte
index|[]
name|info
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|long
name|timestamp
parameter_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scopes
parameter_list|)
block|{
return|return
operator|new
name|WALKey
argument_list|(
name|info
argument_list|,
name|tableName
argument_list|,
name|timestamp
argument_list|,
name|mvcc
argument_list|,
name|scopes
argument_list|)
return|;
block|}
comment|/**    * helper method to simulate region flush for a WAL.    * @param wal    * @param regionEncodedName    */
specifier|protected
name|void
name|flushRegion
parameter_list|(
name|WAL
name|wal
parameter_list|,
name|byte
index|[]
name|regionEncodedName
parameter_list|,
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|flushedFamilyNames
parameter_list|)
block|{
name|wal
operator|.
name|startCacheFlush
argument_list|(
name|regionEncodedName
argument_list|,
name|flushedFamilyNames
argument_list|)
expr_stmt|;
name|wal
operator|.
name|completeCacheFlush
argument_list|(
name|regionEncodedName
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|UNSPECIFIED_REGION
init|=
operator|new
name|byte
index|[]
block|{}
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testLogCleaning
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
name|currentTest
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|currentTest
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"row"
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|HTableDescriptor
name|htd2
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|currentTest
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"2"
argument_list|)
argument_list|)
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"row"
argument_list|)
argument_list|)
decl_stmt|;
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scopes1
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|fam
range|:
name|htd
operator|.
name|getFamiliesKeys
argument_list|()
control|)
block|{
name|scopes1
operator|.
name|put
argument_list|(
name|fam
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scopes2
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|fam
range|:
name|htd2
operator|.
name|getFamiliesKeys
argument_list|()
control|)
block|{
name|scopes2
operator|.
name|put
argument_list|(
name|fam
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|final
name|Configuration
name|localConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|localConf
operator|.
name|set
argument_list|(
name|WALFactory
operator|.
name|WAL_PROVIDER
argument_list|,
name|FSHLogProvider
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|WALFactory
name|wals
init|=
operator|new
name|WALFactory
argument_list|(
name|localConf
argument_list|,
literal|null
argument_list|,
name|currentTest
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hri2
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd2
operator|.
name|getTableName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
decl_stmt|;
comment|// we want to mix edits from regions, so pick our own identifier.
specifier|final
name|WAL
name|log
init|=
name|wals
operator|.
name|getWAL
argument_list|(
name|UNSPECIFIED_REGION
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// Add a single edit and make sure that rolling won't remove the file
comment|// Before HBASE-3198 it used to delete it
name|addEdits
argument_list|(
name|log
argument_list|,
name|hri
argument_list|,
name|htd
argument_list|,
literal|1
argument_list|,
name|scopes1
argument_list|)
expr_stmt|;
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
comment|// See if there's anything wrong with more than 1 edit
name|addEdits
argument_list|(
name|log
argument_list|,
name|hri
argument_list|,
name|htd
argument_list|,
literal|2
argument_list|,
name|scopes1
argument_list|)
expr_stmt|;
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|FSHLogProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now mix edits from 2 regions, still no flushing
name|addEdits
argument_list|(
name|log
argument_list|,
name|hri
argument_list|,
name|htd
argument_list|,
literal|1
argument_list|,
name|scopes1
argument_list|)
expr_stmt|;
name|addEdits
argument_list|(
name|log
argument_list|,
name|hri2
argument_list|,
name|htd2
argument_list|,
literal|1
argument_list|,
name|scopes2
argument_list|)
expr_stmt|;
name|addEdits
argument_list|(
name|log
argument_list|,
name|hri
argument_list|,
name|htd
argument_list|,
literal|1
argument_list|,
name|scopes1
argument_list|)
expr_stmt|;
name|addEdits
argument_list|(
name|log
argument_list|,
name|hri2
argument_list|,
name|htd2
argument_list|,
literal|1
argument_list|,
name|scopes2
argument_list|)
expr_stmt|;
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
comment|// Flush the first region, we expect to see the first two files getting
comment|// archived. We need to append something or writer won't be rolled.
name|addEdits
argument_list|(
name|log
argument_list|,
name|hri2
argument_list|,
name|htd2
argument_list|,
literal|1
argument_list|,
name|scopes2
argument_list|)
expr_stmt|;
name|log
operator|.
name|startCacheFlush
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|htd
operator|.
name|getFamiliesKeys
argument_list|()
argument_list|)
expr_stmt|;
name|log
operator|.
name|completeCacheFlush
argument_list|(
name|hri
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
expr_stmt|;
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
comment|// Flush the second region, which removes all the remaining output files
comment|// since the oldest was completely flushed and the two others only contain
comment|// flush information
name|addEdits
argument_list|(
name|log
argument_list|,
name|hri2
argument_list|,
name|htd2
argument_list|,
literal|1
argument_list|,
name|scopes2
argument_list|)
expr_stmt|;
name|log
operator|.
name|startCacheFlush
argument_list|(
name|hri2
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|htd2
operator|.
name|getFamiliesKeys
argument_list|()
argument_list|)
expr_stmt|;
name|log
operator|.
name|completeCacheFlush
argument_list|(
name|hri2
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|)
expr_stmt|;
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|log
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|wals
operator|!=
literal|null
condition|)
block|{
name|wals
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Tests wal archiving by adding data, doing flushing/rolling and checking we archive old logs    * and also don't archive "live logs" (that is, a log with un-flushed entries).    *<p>    * This is what it does:    * It creates two regions, and does a series of inserts along with log rolling.    * Whenever a WAL is rolled, HLogBase checks previous wals for archiving. A wal is eligible for    * archiving if for all the regions which have entries in that wal file, have flushed - past    * their maximum sequence id in that wal file.    *<p>    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testWALArchiving
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|currentTest
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|table1
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|currentTest
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"1"
argument_list|)
argument_list|)
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"row"
argument_list|)
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|table2
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|currentTest
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"2"
argument_list|)
argument_list|)
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"row"
argument_list|)
argument_list|)
decl_stmt|;
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scopes1
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|fam
range|:
name|table1
operator|.
name|getFamiliesKeys
argument_list|()
control|)
block|{
name|scopes1
operator|.
name|put
argument_list|(
name|fam
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Integer
argument_list|>
name|scopes2
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|fam
range|:
name|table2
operator|.
name|getFamiliesKeys
argument_list|()
control|)
block|{
name|scopes2
operator|.
name|put
argument_list|(
name|fam
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
specifier|final
name|Configuration
name|localConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|localConf
operator|.
name|set
argument_list|(
name|WALFactory
operator|.
name|WAL_PROVIDER
argument_list|,
name|FSHLogProvider
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|WALFactory
name|wals
init|=
operator|new
name|WALFactory
argument_list|(
name|localConf
argument_list|,
literal|null
argument_list|,
name|currentTest
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
specifier|final
name|WAL
name|wal
init|=
name|wals
operator|.
name|getWAL
argument_list|(
name|UNSPECIFIED_REGION
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionInfo
name|hri1
init|=
operator|new
name|HRegionInfo
argument_list|(
name|table1
operator|.
name|getTableName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
decl_stmt|;
name|HRegionInfo
name|hri2
init|=
operator|new
name|HRegionInfo
argument_list|(
name|table2
operator|.
name|getTableName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
decl_stmt|;
comment|// ensure that we don't split the regions.
name|hri1
operator|.
name|setSplit
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|hri2
operator|.
name|setSplit
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// variables to mock region sequenceIds.
comment|// start with the testing logic: insert a waledit, and roll writer
name|addEdits
argument_list|(
name|wal
argument_list|,
name|hri1
argument_list|,
name|table1
argument_list|,
literal|1
argument_list|,
name|scopes1
argument_list|)
expr_stmt|;
name|wal
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
comment|// assert that the wal is rolled
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
comment|// add edits in the second wal file, and roll writer.
name|addEdits
argument_list|(
name|wal
argument_list|,
name|hri1
argument_list|,
name|table1
argument_list|,
literal|1
argument_list|,
name|scopes1
argument_list|)
expr_stmt|;
name|wal
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
comment|// assert that the wal is rolled
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
comment|// add a waledit to table1, and flush the region.
name|addEdits
argument_list|(
name|wal
argument_list|,
name|hri1
argument_list|,
name|table1
argument_list|,
literal|3
argument_list|,
name|scopes1
argument_list|)
expr_stmt|;
name|flushRegion
argument_list|(
name|wal
argument_list|,
name|hri1
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|table1
operator|.
name|getFamiliesKeys
argument_list|()
argument_list|)
expr_stmt|;
comment|// roll log; all old logs should be archived.
name|wal
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
comment|// add an edit to table2, and roll writer
name|addEdits
argument_list|(
name|wal
argument_list|,
name|hri2
argument_list|,
name|table2
argument_list|,
literal|1
argument_list|,
name|scopes2
argument_list|)
expr_stmt|;
name|wal
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
comment|// add edits for table1, and roll writer
name|addEdits
argument_list|(
name|wal
argument_list|,
name|hri1
argument_list|,
name|table1
argument_list|,
literal|2
argument_list|,
name|scopes1
argument_list|)
expr_stmt|;
name|wal
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
comment|// add edits for table2, and flush hri1.
name|addEdits
argument_list|(
name|wal
argument_list|,
name|hri2
argument_list|,
name|table2
argument_list|,
literal|2
argument_list|,
name|scopes2
argument_list|)
expr_stmt|;
name|flushRegion
argument_list|(
name|wal
argument_list|,
name|hri1
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|table2
operator|.
name|getFamiliesKeys
argument_list|()
argument_list|)
expr_stmt|;
comment|// the log : region-sequenceId map is
comment|// log1: region2 (unflushed)
comment|// log2: region1 (flushed)
comment|// log3: region2 (unflushed)
comment|// roll the writer; log2 should be archived.
name|wal
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
comment|// flush region2, and all logs should be archived.
name|addEdits
argument_list|(
name|wal
argument_list|,
name|hri2
argument_list|,
name|table2
argument_list|,
literal|2
argument_list|,
name|scopes2
argument_list|)
expr_stmt|;
name|flushRegion
argument_list|(
name|wal
argument_list|,
name|hri2
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|table2
operator|.
name|getFamiliesKeys
argument_list|()
argument_list|)
expr_stmt|;
name|wal
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|AbstractFSWALProvider
operator|.
name|getNumRolledLogFiles
argument_list|(
name|wal
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|wals
operator|!=
literal|null
condition|)
block|{
name|wals
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Write to a log file with three concurrent threads and verifying all data is written.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testConcurrentWrites
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Run the WPE tool with three threads writing 3000 edits each concurrently.
comment|// When done, verify that all edits were written.
name|int
name|errCode
init|=
name|WALPerformanceEvaluation
operator|.
name|innerMain
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-threads"
block|,
literal|"3"
block|,
literal|"-verify"
block|,
literal|"-noclosefs"
block|,
literal|"-iterations"
block|,
literal|"3000"
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|errCode
argument_list|)
expr_stmt|;
block|}
comment|/**    * Ensure that we can use Set.add to deduplicate WALs    */
annotation|@
name|Test
specifier|public
name|void
name|setMembershipDedups
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|Configuration
name|localConf
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|localConf
operator|.
name|set
argument_list|(
name|WALFactory
operator|.
name|WAL_PROVIDER
argument_list|,
name|FSHLogProvider
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|WALFactory
name|wals
init|=
operator|new
name|WALFactory
argument_list|(
name|localConf
argument_list|,
literal|null
argument_list|,
name|currentTest
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
specifier|final
name|Set
argument_list|<
name|WAL
argument_list|>
name|seen
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"first attempt to add WAL from default provider should work."
argument_list|,
name|seen
operator|.
name|add
argument_list|(
name|wals
operator|.
name|getWAL
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|random
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|)
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
literal|1000
condition|;
name|i
operator|++
control|)
block|{
name|assertFalse
argument_list|(
literal|"default wal provider is only supposed to return a single wal, which should "
operator|+
literal|"compare as .equals itself."
argument_list|,
name|seen
operator|.
name|add
argument_list|(
name|wals
operator|.
name|getWAL
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|random
operator|.
name|nextInt
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|wals
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

