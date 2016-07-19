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
name|testclassification
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
name|Pair
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
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
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
comment|/**  * Tests bulk loading of HFiles with old non-secure client for backward compatibility. Will be  * removed when old non-secure client for backward compatibility is not supported.  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestHRegionServerBulkLoadWithOldClient
extends|extends
name|TestHRegionServerBulkLoad
block|{
specifier|public
name|TestHRegionServerBulkLoadWithOldClient
parameter_list|(
name|int
name|duration
parameter_list|)
block|{
name|super
argument_list|(
name|duration
argument_list|)
expr_stmt|;
block|}
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
name|TestHRegionServerBulkLoadWithOldClient
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TableName
name|tableName
decl_stmt|;
specifier|public
name|AtomicHFileLoader
parameter_list|(
name|TableName
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
argument_list|<>
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
name|ClusterConnection
name|conn
init|=
operator|(
name|ClusterConnection
operator|)
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getConnection
argument_list|()
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
name|tableName
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
parameter_list|(
name|int
name|callTimeout
parameter_list|)
throws|throws
name|Exception
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Non-secure old client"
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
argument_list|,
literal|null
argument_list|,
literal|null
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
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
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
literal|5
operator|==
literal|0
condition|)
block|{
comment|// 5 * 50 = 250 open file handles!
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
name|tableName
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
parameter_list|(
name|int
name|callTimeout
parameter_list|)
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
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|void
name|runAtomicBulkloadTest
parameter_list|(
name|TableName
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
block|}
end_class

end_unit

