begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|ref
operator|.
name|SoftReference
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
name|Collections
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
name|FSDataInputStream
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
name|FilterFileSystem
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
name|fs
operator|.
name|PositionedReadable
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
name|MiniHBaseCluster
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
name|fs
operator|.
name|HFileSystem
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
name|HFileContext
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
name|HFileContextBuilder
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
name|HFileScanner
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
name|junit
operator|.
name|Assume
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

begin_comment
comment|/**  * Test cases that ensure that file system level errors are bubbled up  * appropriately to clients, rather than swallowed.  */
end_comment

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
name|TestFSErrorsExposed
block|{
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
name|TestFSErrorsExposed
operator|.
name|class
argument_list|)
decl_stmt|;
name|HBaseTestingUtility
name|util
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
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
comment|/**    * Injects errors into the pread calls of an on-disk file, and makes    * sure those bubble up to the HFile scanner    */
annotation|@
name|Test
specifier|public
name|void
name|testHFileScannerThrowsErrors
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|hfilePath
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|util
operator|.
name|getDataTestDir
argument_list|(
literal|"internalScannerExposesErrors"
argument_list|)
argument_list|,
literal|"regionname"
argument_list|)
argument_list|,
literal|"familyname"
argument_list|)
decl_stmt|;
name|HFileSystem
name|hfs
init|=
operator|(
name|HFileSystem
operator|)
name|util
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|FaultyFileSystem
name|faultyfs
init|=
operator|new
name|FaultyFileSystem
argument_list|(
name|hfs
operator|.
name|getBackingFs
argument_list|()
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
operator|new
name|HFileSystem
argument_list|(
name|faultyfs
argument_list|)
decl_stmt|;
name|CacheConfig
name|cacheConf
init|=
operator|new
name|CacheConfig
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withBlockSize
argument_list|(
literal|2
operator|*
literal|1024
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|StoreFileWriter
name|writer
init|=
operator|new
name|StoreFileWriter
operator|.
name|Builder
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|cacheConf
argument_list|,
name|hfs
argument_list|)
operator|.
name|withOutputDir
argument_list|(
name|hfilePath
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|meta
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TestHStoreFile
operator|.
name|writeStoreFile
argument_list|(
name|writer
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|)
expr_stmt|;
name|HStoreFile
name|sf
init|=
operator|new
name|HStoreFile
argument_list|(
name|fs
argument_list|,
name|writer
operator|.
name|getPath
argument_list|()
argument_list|,
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|cacheConf
argument_list|,
name|BloomType
operator|.
name|NONE
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|sf
operator|.
name|initReader
argument_list|()
expr_stmt|;
name|StoreFileReader
name|reader
init|=
name|sf
operator|.
name|getReader
argument_list|()
decl_stmt|;
name|HFileScanner
name|scanner
init|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|FaultyInputStream
name|inStream
init|=
name|faultyfs
operator|.
name|inStreams
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|inStream
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|seekTo
argument_list|()
expr_stmt|;
comment|// Do at least one successful read
name|assertTrue
argument_list|(
name|scanner
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
name|faultyfs
operator|.
name|startFaults
argument_list|()
expr_stmt|;
try|try
block|{
name|int
name|scanned
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|()
condition|)
block|{
name|scanned
operator|++
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"Scanner didn't throw after faults injected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got expected exception"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ioe
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Fault"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// end of test so evictOnClose
block|}
comment|/**    * Injects errors into the pread calls of an on-disk file, and makes    * sure those bubble up to the StoreFileScanner    */
annotation|@
name|Test
specifier|public
name|void
name|testStoreFileScannerThrowsErrors
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|hfilePath
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|util
operator|.
name|getDataTestDir
argument_list|(
literal|"internalScannerExposesErrors"
argument_list|)
argument_list|,
literal|"regionname"
argument_list|)
argument_list|,
literal|"familyname"
argument_list|)
decl_stmt|;
name|HFileSystem
name|hfs
init|=
operator|(
name|HFileSystem
operator|)
name|util
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|FaultyFileSystem
name|faultyfs
init|=
operator|new
name|FaultyFileSystem
argument_list|(
name|hfs
operator|.
name|getBackingFs
argument_list|()
argument_list|)
decl_stmt|;
name|HFileSystem
name|fs
init|=
operator|new
name|HFileSystem
argument_list|(
name|faultyfs
argument_list|)
decl_stmt|;
name|CacheConfig
name|cacheConf
init|=
operator|new
name|CacheConfig
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HFileContext
name|meta
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withBlockSize
argument_list|(
literal|2
operator|*
literal|1024
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|StoreFileWriter
name|writer
init|=
operator|new
name|StoreFileWriter
operator|.
name|Builder
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|cacheConf
argument_list|,
name|hfs
argument_list|)
operator|.
name|withOutputDir
argument_list|(
name|hfilePath
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|meta
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TestHStoreFile
operator|.
name|writeStoreFile
argument_list|(
name|writer
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|)
expr_stmt|;
name|HStoreFile
name|sf
init|=
operator|new
name|HStoreFile
argument_list|(
name|fs
argument_list|,
name|writer
operator|.
name|getPath
argument_list|()
argument_list|,
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|cacheConf
argument_list|,
name|BloomType
operator|.
name|NONE
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|StoreFileScanner
argument_list|>
name|scanners
init|=
name|StoreFileScanner
operator|.
name|getScannersForStoreFiles
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|sf
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
comment|// 0 is passed as readpoint because this test operates on HStoreFile directly
literal|0
argument_list|)
decl_stmt|;
name|KeyValueScanner
name|scanner
init|=
name|scanners
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|FaultyInputStream
name|inStream
init|=
name|faultyfs
operator|.
name|inStreams
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|inStream
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|seek
argument_list|(
name|KeyValue
operator|.
name|LOWESTKEY
argument_list|)
expr_stmt|;
comment|// Do at least one successful read
name|assertNotNull
argument_list|(
name|scanner
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
name|faultyfs
operator|.
name|startFaults
argument_list|()
expr_stmt|;
try|try
block|{
name|int
name|scanned
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|scanned
operator|++
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"Scanner didn't throw after faults injected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Got expected exception"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ioe
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Could not iterate"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Cluster test which starts a region server with a region, then    * removes the data from HDFS underneath it, and ensures that    * errors are bubbled to the client.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|5
operator|*
literal|60
operator|*
literal|1000
argument_list|)
specifier|public
name|void
name|testFullSystemBubblesFSErrors
parameter_list|()
throws|throws
name|Exception
block|{
comment|// We won't have an error if the datanode is not there if we use short circuit
comment|//  it's a known 'feature'.
name|Assume
operator|.
name|assumeTrue
argument_list|(
operator|!
name|util
operator|.
name|isReadShortCircuitOn
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Make it fail faster.
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
argument_list|,
literal|90000
argument_list|)
expr_stmt|;
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.lease.recovery.timeout"
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
name|util
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.lease.recovery.dfs.timeout"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|util
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
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
name|byte
index|[]
name|fam
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
name|Admin
name|admin
init|=
name|util
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|fam
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
literal|1
argument_list|)
operator|.
name|setBlockCacheEnabled
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
comment|// Make a new Configuration so it makes a new connection that has the
comment|// above configuration on it; else we use the old one w/ 10 as default.
try|try
init|(
name|Table
name|table
init|=
name|util
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
comment|// Load some data
name|util
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|fam
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|util
operator|.
name|flush
argument_list|()
expr_stmt|;
name|util
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// Kill the DFS cluster
name|util
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|shutdownDataNodes
argument_list|()
expr_stmt|;
try|try
block|{
name|util
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Did not fail to count after removing data"
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
name|info
argument_list|(
literal|"Got expected error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Could not seek"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Restart data nodes so that HBase can shut down cleanly.
name|util
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|restartDataNodes
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|MiniHBaseCluster
name|cluster
init|=
name|util
operator|.
name|getMiniHBaseCluster
argument_list|()
decl_stmt|;
if|if
condition|(
name|cluster
operator|!=
literal|null
condition|)
name|cluster
operator|.
name|killAll
argument_list|()
expr_stmt|;
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|FaultyFileSystem
extends|extends
name|FilterFileSystem
block|{
name|List
argument_list|<
name|SoftReference
argument_list|<
name|FaultyInputStream
argument_list|>
argument_list|>
name|inStreams
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|FaultyFileSystem
parameter_list|(
name|FileSystem
name|testFileSystem
parameter_list|)
block|{
name|super
argument_list|(
name|testFileSystem
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|FSDataInputStream
name|open
parameter_list|(
name|Path
name|p
parameter_list|,
name|int
name|bufferSize
parameter_list|)
throws|throws
name|IOException
block|{
name|FSDataInputStream
name|orig
init|=
name|fs
operator|.
name|open
argument_list|(
name|p
argument_list|,
name|bufferSize
argument_list|)
decl_stmt|;
name|FaultyInputStream
name|faulty
init|=
operator|new
name|FaultyInputStream
argument_list|(
name|orig
argument_list|)
decl_stmt|;
name|inStreams
operator|.
name|add
argument_list|(
operator|new
name|SoftReference
argument_list|<>
argument_list|(
name|faulty
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|faulty
return|;
block|}
comment|/**      * Starts to simulate faults on all streams opened so far      */
specifier|public
name|void
name|startFaults
parameter_list|()
block|{
for|for
control|(
name|SoftReference
argument_list|<
name|FaultyInputStream
argument_list|>
name|is
range|:
name|inStreams
control|)
block|{
name|is
operator|.
name|get
argument_list|()
operator|.
name|startFaults
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|static
class|class
name|FaultyInputStream
extends|extends
name|FSDataInputStream
block|{
name|boolean
name|faultsStarted
init|=
literal|false
decl_stmt|;
specifier|public
name|FaultyInputStream
parameter_list|(
name|InputStream
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|startFaults
parameter_list|()
block|{
name|faultsStarted
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|long
name|position
parameter_list|,
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|injectFault
argument_list|()
expr_stmt|;
return|return
operator|(
operator|(
name|PositionedReadable
operator|)
name|in
operator|)
operator|.
name|read
argument_list|(
name|position
argument_list|,
name|buffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|private
name|void
name|injectFault
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|faultsStarted
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Fault injected"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

