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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|HBaseConfiguration
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
name|MultithreadedTestUtil
operator|.
name|RepeatingTestThread
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
name|MultithreadedTestUtil
operator|.
name|TestContext
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
name|RegionServerCallable
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
name|RpcRetryingCaller
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
name|RpcRetryingCallerFactory
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
name|compress
operator|.
name|Compression
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
name|compress
operator|.
name|Compression
operator|.
name|Algorithm
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
name|protobuf
operator|.
name|RequestConverter
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
name|AdminProtos
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
name|AdminProtos
operator|.
name|CompactRegionRequest
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Tests bulk loading of HFiles and shows the atomicity or lack of atomicity of  * the region server's bullkLoad functionality.  */
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
name|TestHRegionServerBulkLoad
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
specifier|private
specifier|static
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|private
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
specifier|private
specifier|final
specifier|static
name|int
name|NUM_CFS
init|=
literal|10
decl_stmt|;
specifier|public
specifier|static
name|int
name|BLOCKSIZE
init|=
literal|64
operator|*
literal|1024
decl_stmt|;
specifier|public
specifier|static
name|Algorithm
name|COMPRESSION
init|=
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
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
comment|/**    * Create an HFile with the given number of rows with a specified value.    */
specifier|public
specifier|static
name|void
name|createHFile
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
name|int
name|numRows
parameter_list|)
throws|throws
name|IOException
block|{
name|HFileContext
name|context
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withBlockSize
argument_list|(
name|BLOCKSIZE
argument_list|)
operator|.
name|withCompressionAlgo
argument_list|(
name|COMPRESSION
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HFile
operator|.
name|Writer
name|writer
init|=
name|HFile
operator|.
name|getWriterFactory
argument_list|(
name|conf
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|)
operator|.
name|withPath
argument_list|(
name|fs
argument_list|,
name|path
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|context
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
comment|// subtract 2 since iterateOnSplits doesn't include boundary keys
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numRows
condition|;
name|i
operator|++
control|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|rowkey
argument_list|(
name|i
argument_list|)
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|now
argument_list|,
name|value
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Thread that does full scans of the table looking for any partially    * completed rows.    *    * Each iteration of this loads 10 hdfs files, which occupies 5 file open file    * handles. So every 10 iterations (500 file handles) it does a region    * compaction to reduce the number of open file handles.    */
specifier|public
specifier|static
class|class
name|AtomicHFileLoader
extends|extends
name|RepeatingTestThread
block|{
specifier|final
name|AtomicLong
name|numBulkLoads
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|final
name|AtomicLong
name|numCompactions
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|public
name|AtomicHFileLoader
parameter_list|(
name|String
name|tableName
parameter_list|,
name|TestContext
name|ctx
parameter_list|,
name|byte
name|targetFamilies
index|[]
index|[]
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
specifier|public
name|void
name|doAnAction
parameter_list|()
throws|throws
name|Exception
block|{
name|long
name|iteration
init|=
name|numBulkLoads
operator|.
name|getAndIncrement
argument_list|()
decl_stmt|;
name|Path
name|dir
init|=
name|UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"bulkLoad_%08d"
argument_list|,
name|iteration
argument_list|)
argument_list|)
decl_stmt|;
comment|// create HFiles for different column families
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|byte
index|[]
name|val
init|=
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
name|iteration
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
name|famPaths
init|=
operator|new
name|ArrayList
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|(
name|NUM_CFS
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
name|hfile
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
name|byte
index|[]
name|fam
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|createHFile
argument_list|(
name|fs
argument_list|,
name|hfile
argument_list|,
name|fam
argument_list|,
name|QUAL
argument_list|,
name|val
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|famPaths
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|(
name|fam
argument_list|,
name|hfile
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// bulk load HFiles
specifier|final
name|HConnection
name|conn
init|=
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|getConnection
argument_list|()
decl_stmt|;
name|TableName
name|tbl
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|RegionServerCallable
argument_list|<
name|Void
argument_list|>
name|callable
init|=
operator|new
name|RegionServerCallable
argument_list|<
name|Void
argument_list|>
argument_list|(
name|conn
argument_list|,
name|tbl
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Going to connect to server "
operator|+
name|getLocation
argument_list|()
operator|+
literal|" for row "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|regionName
init|=
name|getLocation
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|BulkLoadHFileRequest
name|request
init|=
name|RequestConverter
operator|.
name|buildBulkLoadHFileRequest
argument_list|(
name|famPaths
argument_list|,
name|regionName
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|getStub
argument_list|()
operator|.
name|bulkLoadHFile
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|RpcRetryingCallerFactory
name|factory
init|=
operator|new
name|RpcRetryingCallerFactory
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|RpcRetryingCaller
argument_list|<
name|Void
argument_list|>
name|caller
init|=
name|factory
operator|.
expr|<
name|Void
operator|>
name|newCaller
argument_list|()
decl_stmt|;
name|caller
operator|.
name|callWithRetries
argument_list|(
name|callable
argument_list|)
expr_stmt|;
comment|// Periodically do compaction to reduce the number of open file handles.
if|if
condition|(
name|numBulkLoads
operator|.
name|get
argument_list|()
operator|%
literal|10
operator|==
literal|0
condition|)
block|{
comment|// 10 * 50 = 500 open file handles!
name|callable
operator|=
operator|new
name|RegionServerCallable
argument_list|<
name|Void
argument_list|>
argument_list|(
name|conn
argument_list|,
name|tbl
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"compacting "
operator|+
name|getLocation
argument_list|()
operator|+
literal|" for row "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|AdminProtos
operator|.
name|AdminService
operator|.
name|BlockingInterface
name|server
init|=
name|conn
operator|.
name|getAdmin
argument_list|(
name|getLocation
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
decl_stmt|;
name|CompactRegionRequest
name|request
init|=
name|RequestConverter
operator|.
name|buildCompactRegionRequest
argument_list|(
name|getLocation
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|server
operator|.
name|compactRegion
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|numCompactions
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
expr_stmt|;
name|caller
operator|.
name|callWithRetries
argument_list|(
name|callable
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Thread that does full scans of the table looking for any partially    * completed rows.    */
specifier|public
specifier|static
class|class
name|AtomicScanReader
extends|extends
name|RepeatingTestThread
block|{
name|byte
name|targetFamilies
index|[]
index|[]
decl_stmt|;
name|HTable
name|table
decl_stmt|;
name|AtomicLong
name|numScans
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
name|AtomicLong
name|numRowsScanned
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
name|String
name|TABLE_NAME
decl_stmt|;
specifier|public
name|AtomicScanReader
parameter_list|(
name|String
name|TABLE_NAME
parameter_list|,
name|TestContext
name|ctx
parameter_list|,
name|byte
name|targetFamilies
index|[]
index|[]
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|this
operator|.
name|TABLE_NAME
operator|=
name|TABLE_NAME
expr_stmt|;
name|this
operator|.
name|targetFamilies
operator|=
name|targetFamilies
expr_stmt|;
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|doAnAction
parameter_list|()
throws|throws
name|Exception
block|{
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|targetFamilies
control|)
block|{
name|s
operator|.
name|addFamily
argument_list|(
name|family
argument_list|)
expr_stmt|;
block|}
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
for|for
control|(
name|Result
name|res
range|:
name|scanner
control|)
block|{
name|byte
index|[]
name|lastRow
init|=
literal|null
decl_stmt|,
name|lastFam
init|=
literal|null
decl_stmt|,
name|lastQual
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|gotValue
init|=
literal|null
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|targetFamilies
control|)
block|{
name|byte
name|qualifier
index|[]
init|=
name|QUAL
decl_stmt|;
name|byte
name|thisValue
index|[]
init|=
name|res
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
decl_stmt|;
if|if
condition|(
name|gotValue
operator|!=
literal|null
operator|&&
name|thisValue
operator|!=
literal|null
operator|&&
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|gotValue
argument_list|,
name|thisValue
argument_list|)
condition|)
block|{
name|StringBuilder
name|msg
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"Failed on scan "
argument_list|)
operator|.
name|append
argument_list|(
name|numScans
argument_list|)
operator|.
name|append
argument_list|(
literal|" after scanning "
argument_list|)
operator|.
name|append
argument_list|(
name|numRowsScanned
argument_list|)
operator|.
name|append
argument_list|(
literal|" rows!\n"
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"Current  was "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|res
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|"/"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
operator|+
literal|":"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|qualifier
argument_list|)
operator|+
literal|" = "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|thisValue
argument_list|)
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|msg
operator|.
name|append
argument_list|(
literal|"Previous  was "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|lastRow
argument_list|)
operator|+
literal|"/"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|lastFam
argument_list|)
operator|+
literal|":"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|lastQual
argument_list|)
operator|+
literal|" = "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|gotValue
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|msg
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
name|lastFam
operator|=
name|family
expr_stmt|;
name|lastQual
operator|=
name|qualifier
expr_stmt|;
name|lastRow
operator|=
name|res
operator|.
name|getRow
argument_list|()
expr_stmt|;
name|gotValue
operator|=
name|thisValue
expr_stmt|;
block|}
name|numRowsScanned
operator|.
name|getAndIncrement
argument_list|()
expr_stmt|;
block|}
name|numScans
operator|.
name|getAndIncrement
argument_list|()
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
name|TableName
operator|.
name|valueOf
argument_list|(
name|table
argument_list|)
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
name|UTIL
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
comment|/**    * Atomic bulk load.    */
annotation|@
name|Test
specifier|public
name|void
name|testAtomicBulkLoad
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|TABLE_NAME
init|=
literal|"atomicBulkLoad"
decl_stmt|;
name|int
name|millisToRun
init|=
literal|30000
decl_stmt|;
name|int
name|numScanners
init|=
literal|50
decl_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
try|try
block|{
name|runAtomicBulkloadTest
argument_list|(
name|TABLE_NAME
argument_list|,
name|millisToRun
argument_list|,
name|numScanners
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
name|void
name|runAtomicBulkloadTest
parameter_list|(
name|String
name|tableName
parameter_list|,
name|int
name|millisToRun
parameter_list|,
name|int
name|numScanners
parameter_list|)
throws|throws
name|Exception
block|{
name|setupTable
argument_list|(
name|tableName
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|TestContext
name|ctx
init|=
operator|new
name|TestContext
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|AtomicHFileLoader
name|loader
init|=
operator|new
name|AtomicHFileLoader
argument_list|(
name|tableName
argument_list|,
name|ctx
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|addThread
argument_list|(
name|loader
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|AtomicScanReader
argument_list|>
name|scanners
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
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
name|numScanners
condition|;
name|i
operator|++
control|)
block|{
name|AtomicScanReader
name|scanner
init|=
operator|new
name|AtomicScanReader
argument_list|(
name|tableName
argument_list|,
name|ctx
argument_list|,
name|families
argument_list|)
decl_stmt|;
name|scanners
operator|.
name|add
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|addThread
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|startThreads
argument_list|()
expr_stmt|;
name|ctx
operator|.
name|waitFor
argument_list|(
name|millisToRun
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|stop
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Loaders:"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"  loaded "
operator|+
name|loader
operator|.
name|numBulkLoads
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"  compations "
operator|+
name|loader
operator|.
name|numCompactions
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Scanners:"
argument_list|)
expr_stmt|;
for|for
control|(
name|AtomicScanReader
name|scanner
range|:
name|scanners
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"  scanned "
operator|+
name|scanner
operator|.
name|numScans
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"  verified "
operator|+
name|scanner
operator|.
name|numRowsScanned
operator|.
name|get
argument_list|()
operator|+
literal|" rows"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Run test on an HBase instance for 5 minutes. This assumes that the table    * under test only has a single region.    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
name|args
index|[]
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|Configuration
name|c
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|TestHRegionServerBulkLoad
name|test
init|=
operator|new
name|TestHRegionServerBulkLoad
argument_list|()
decl_stmt|;
name|test
operator|.
name|setConf
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|test
operator|.
name|runAtomicBulkloadTest
argument_list|(
literal|"atomicTableTest"
argument_list|,
literal|5
operator|*
literal|60
operator|*
literal|1000
argument_list|,
literal|50
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|System
operator|.
name|exit
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// something hangs (believe it is lru threadpool)
block|}
block|}
specifier|private
name|void
name|setConf
parameter_list|(
name|Configuration
name|c
parameter_list|)
block|{
name|UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

