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
name|mockito
operator|.
name|Mockito
operator|.
name|mock
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
name|CategoryBasedTimeout
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
name|HDFSBlocksDistribution
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
name|ImmutableBytesWritable
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
name|mapreduce
operator|.
name|TableSnapshotInputFormat
operator|.
name|TableSnapshotRegionSplit
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
name|VerySlowMapReduceTests
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
name|io
operator|.
name|NullWritable
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
name|mapreduce
operator|.
name|InputSplit
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
name|mapreduce
operator|.
name|Job
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
name|mapreduce
operator|.
name|RecordReader
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
name|mapreduce
operator|.
name|Reducer
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
name|mapreduce
operator|.
name|TaskAttemptContext
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
name|mapreduce
operator|.
name|lib
operator|.
name|output
operator|.
name|NullOutputFormat
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
name|Assert
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
name|TestRule
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|VerySlowMapReduceTests
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
name|TestTableSnapshotInputFormat
extends|extends
name|TableSnapshotInputFormatTestBase
block|{
annotation|@
name|Rule
specifier|public
specifier|final
name|TestRule
name|timeout
init|=
name|CategoryBasedTimeout
operator|.
name|builder
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
operator|.
name|withLookingForStuckThread
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|bbb
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|yyy
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"yyy"
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|byte
index|[]
name|getStartRow
parameter_list|()
block|{
return|return
name|bbb
return|;
block|}
annotation|@
name|Override
specifier|protected
name|byte
index|[]
name|getEndRow
parameter_list|()
block|{
return|return
name|yyy
return|;
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
name|Test
specifier|public
name|void
name|testGetBestLocations
parameter_list|()
throws|throws
name|IOException
block|{
name|TableSnapshotInputFormatImpl
name|tsif
init|=
operator|new
name|TableSnapshotInputFormatImpl
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|HDFSBlocksDistribution
name|blockDistribution
init|=
operator|new
name|HDFSBlocksDistribution
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|()
argument_list|,
name|TableSnapshotInputFormatImpl
operator|.
name|getBestLocations
argument_list|(
name|conf
argument_list|,
name|blockDistribution
argument_list|)
argument_list|)
expr_stmt|;
name|blockDistribution
operator|.
name|addHostsAndBlockWeight
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"h1"
block|}
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"h1"
argument_list|)
argument_list|,
name|TableSnapshotInputFormatImpl
operator|.
name|getBestLocations
argument_list|(
name|conf
argument_list|,
name|blockDistribution
argument_list|)
argument_list|)
expr_stmt|;
name|blockDistribution
operator|.
name|addHostsAndBlockWeight
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"h1"
block|}
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"h1"
argument_list|)
argument_list|,
name|TableSnapshotInputFormatImpl
operator|.
name|getBestLocations
argument_list|(
name|conf
argument_list|,
name|blockDistribution
argument_list|)
argument_list|)
expr_stmt|;
name|blockDistribution
operator|.
name|addHostsAndBlockWeight
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"h2"
block|}
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"h1"
argument_list|)
argument_list|,
name|TableSnapshotInputFormatImpl
operator|.
name|getBestLocations
argument_list|(
name|conf
argument_list|,
name|blockDistribution
argument_list|)
argument_list|)
expr_stmt|;
name|blockDistribution
operator|=
operator|new
name|HDFSBlocksDistribution
argument_list|()
expr_stmt|;
name|blockDistribution
operator|.
name|addHostsAndBlockWeight
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"h1"
block|}
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|blockDistribution
operator|.
name|addHostsAndBlockWeight
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"h2"
block|}
argument_list|,
literal|7
argument_list|)
expr_stmt|;
name|blockDistribution
operator|.
name|addHostsAndBlockWeight
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"h3"
block|}
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|blockDistribution
operator|.
name|addHostsAndBlockWeight
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"h4"
block|}
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"h1"
argument_list|)
argument_list|,
name|TableSnapshotInputFormatImpl
operator|.
name|getBestLocations
argument_list|(
name|conf
argument_list|,
name|blockDistribution
argument_list|)
argument_list|)
expr_stmt|;
name|blockDistribution
operator|.
name|addHostsAndBlockWeight
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"h2"
block|}
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"h1"
argument_list|,
literal|"h2"
argument_list|)
argument_list|,
name|TableSnapshotInputFormatImpl
operator|.
name|getBestLocations
argument_list|(
name|conf
argument_list|,
name|blockDistribution
argument_list|)
argument_list|)
expr_stmt|;
name|blockDistribution
operator|.
name|addHostsAndBlockWeight
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"h2"
block|}
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"h2"
argument_list|,
literal|"h1"
argument_list|)
argument_list|,
name|TableSnapshotInputFormatImpl
operator|.
name|getBestLocations
argument_list|(
name|conf
argument_list|,
name|blockDistribution
argument_list|)
argument_list|)
expr_stmt|;
name|blockDistribution
operator|.
name|addHostsAndBlockWeight
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"h3"
block|}
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|blockDistribution
operator|.
name|addHostsAndBlockWeight
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"h4"
block|}
argument_list|,
literal|9
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
literal|"h2"
argument_list|,
literal|"h3"
argument_list|,
literal|"h4"
argument_list|,
literal|"h1"
argument_list|)
argument_list|,
name|TableSnapshotInputFormatImpl
operator|.
name|getBestLocations
argument_list|(
name|conf
argument_list|,
name|blockDistribution
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
enum|enum
name|TestTableSnapshotCounters
block|{
name|VALIDATION_ERROR
block|}
specifier|public
specifier|static
class|class
name|TestTableSnapshotMapper
extends|extends
name|TableMapper
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|NullWritable
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
name|void
name|map
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Result
name|value
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// Validate a single row coming from the snapshot, and emit the row key
name|verifyRowFromMap
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|context
operator|.
name|write
argument_list|(
name|key
argument_list|,
name|NullWritable
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|TestTableSnapshotReducer
extends|extends
name|Reducer
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|NullWritable
argument_list|,
name|NullWritable
argument_list|,
name|NullWritable
argument_list|>
block|{
name|HBaseTestingUtility
operator|.
name|SeenRowTracker
name|rowTracker
init|=
operator|new
name|HBaseTestingUtility
operator|.
name|SeenRowTracker
argument_list|(
name|bbb
argument_list|,
name|yyy
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|reduce
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Iterable
argument_list|<
name|NullWritable
argument_list|>
name|values
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|rowTracker
operator|.
name|addRow
argument_list|(
name|key
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|cleanup
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|rowTracker
operator|.
name|validate
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInitTableSnapshotMapperJobConfig
parameter_list|()
throws|throws
name|Exception
block|{
name|setupCluster
argument_list|()
expr_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testInitTableSnapshotMapperJobConfig"
argument_list|)
decl_stmt|;
name|String
name|snapshotName
init|=
literal|"foo"
decl_stmt|;
try|try
block|{
name|createTableAndSnapshot
argument_list|(
name|UTIL
argument_list|,
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|getStartRow
argument_list|()
argument_list|,
name|getEndRow
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|tmpTableDir
init|=
name|UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|snapshotName
argument_list|)
decl_stmt|;
name|TableMapReduceUtil
operator|.
name|initTableSnapshotMapperJob
argument_list|(
name|snapshotName
argument_list|,
operator|new
name|Scan
argument_list|()
argument_list|,
name|TestTableSnapshotMapper
operator|.
name|class
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|,
name|NullWritable
operator|.
name|class
argument_list|,
name|job
argument_list|,
literal|false
argument_list|,
name|tmpTableDir
argument_list|)
expr_stmt|;
comment|// TODO: would be better to examine directly the cache instance that results from this
comment|// config. Currently this is not possible because BlockCache initialization is static.
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Snapshot job should be configured for default LruBlockCache."
argument_list|,
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_DEFAULT
argument_list|,
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getFloat
argument_list|(
name|HConstants
operator|.
name|HFILE_BLOCK_CACHE_SIZE_KEY
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Snapshot job should not use BucketCache."
argument_list|,
literal|0
argument_list|,
name|job
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getFloat
argument_list|(
literal|"hbase.bucketcache.size"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
literal|0.01
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|deleteSnapshot
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|tearDownCluster
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|testRestoreSnapshotDoesNotCreateBackRefLinksInit
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|Path
name|tmpTableDir
parameter_list|)
throws|throws
name|Exception
block|{
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|TableMapReduceUtil
operator|.
name|initTableSnapshotMapperJob
argument_list|(
name|snapshotName
argument_list|,
operator|new
name|Scan
argument_list|()
argument_list|,
name|TestTableSnapshotMapper
operator|.
name|class
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|,
name|NullWritable
operator|.
name|class
argument_list|,
name|job
argument_list|,
literal|false
argument_list|,
name|tmpTableDir
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|testWithMockedMapReduce
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|int
name|numRegions
parameter_list|,
name|int
name|expectedNumSplits
parameter_list|)
throws|throws
name|Exception
block|{
name|setupCluster
argument_list|()
expr_stmt|;
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testWithMockedMapReduce"
argument_list|)
decl_stmt|;
try|try
block|{
name|createTableAndSnapshot
argument_list|(
name|util
argument_list|,
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|getStartRow
argument_list|()
argument_list|,
name|getEndRow
argument_list|()
argument_list|,
name|numRegions
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|tmpTableDir
init|=
name|util
operator|.
name|getDataTestDirOnTestFS
argument_list|(
name|snapshotName
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|getStartRow
argument_list|()
argument_list|,
name|getEndRow
argument_list|()
argument_list|)
decl_stmt|;
comment|// limit the scan
name|TableMapReduceUtil
operator|.
name|initTableSnapshotMapperJob
argument_list|(
name|snapshotName
argument_list|,
name|scan
argument_list|,
name|TestTableSnapshotMapper
operator|.
name|class
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|,
name|NullWritable
operator|.
name|class
argument_list|,
name|job
argument_list|,
literal|false
argument_list|,
name|tmpTableDir
argument_list|)
expr_stmt|;
name|verifyWithMockedMapReduce
argument_list|(
name|job
argument_list|,
name|numRegions
argument_list|,
name|expectedNumSplits
argument_list|,
name|getStartRow
argument_list|()
argument_list|,
name|getEndRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|deleteSnapshot
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|tearDownCluster
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|verifyWithMockedMapReduce
parameter_list|(
name|Job
name|job
parameter_list|,
name|int
name|numRegions
parameter_list|,
name|int
name|expectedNumSplits
parameter_list|,
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|stopRow
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|TableSnapshotInputFormat
name|tsif
init|=
operator|new
name|TableSnapshotInputFormat
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
init|=
name|tsif
operator|.
name|getSplits
argument_list|(
name|job
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectedNumSplits
argument_list|,
name|splits
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HBaseTestingUtility
operator|.
name|SeenRowTracker
name|rowTracker
init|=
operator|new
name|HBaseTestingUtility
operator|.
name|SeenRowTracker
argument_list|(
name|startRow
argument_list|,
name|stopRow
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
name|splits
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
comment|// validate input split
name|InputSplit
name|split
init|=
name|splits
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|split
operator|instanceof
name|TableSnapshotRegionSplit
argument_list|)
expr_stmt|;
comment|// validate record reader
name|TaskAttemptContext
name|taskAttemptContext
init|=
name|mock
argument_list|(
name|TaskAttemptContext
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|taskAttemptContext
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|RecordReader
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
name|rr
init|=
name|tsif
operator|.
name|createRecordReader
argument_list|(
name|split
argument_list|,
name|taskAttemptContext
argument_list|)
decl_stmt|;
name|rr
operator|.
name|initialize
argument_list|(
name|split
argument_list|,
name|taskAttemptContext
argument_list|)
expr_stmt|;
comment|// validate we can read all the data back
while|while
condition|(
name|rr
operator|.
name|nextKeyValue
argument_list|()
condition|)
block|{
name|byte
index|[]
name|row
init|=
name|rr
operator|.
name|getCurrentKey
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|verifyRowFromMap
argument_list|(
name|rr
operator|.
name|getCurrentKey
argument_list|()
argument_list|,
name|rr
operator|.
name|getCurrentValue
argument_list|()
argument_list|)
expr_stmt|;
name|rowTracker
operator|.
name|addRow
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
name|rr
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// validate all rows are seen
name|rowTracker
operator|.
name|validate
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|testWithMapReduceImpl
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|Path
name|tableDir
parameter_list|,
name|int
name|numRegions
parameter_list|,
name|int
name|expectedNumSplits
parameter_list|,
name|boolean
name|shutdownCluster
parameter_list|)
throws|throws
name|Exception
block|{
name|doTestWithMapReduce
argument_list|(
name|util
argument_list|,
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|getStartRow
argument_list|()
argument_list|,
name|getEndRow
argument_list|()
argument_list|,
name|tableDir
argument_list|,
name|numRegions
argument_list|,
name|expectedNumSplits
argument_list|,
name|shutdownCluster
argument_list|)
expr_stmt|;
block|}
comment|// this is also called by the IntegrationTestTableSnapshotInputFormat
specifier|public
specifier|static
name|void
name|doTestWithMapReduce
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|byte
index|[]
name|startRow
parameter_list|,
name|byte
index|[]
name|endRow
parameter_list|,
name|Path
name|tableDir
parameter_list|,
name|int
name|numRegions
parameter_list|,
name|int
name|expectedNumSplits
parameter_list|,
name|boolean
name|shutdownCluster
parameter_list|)
throws|throws
name|Exception
block|{
comment|//create the table and snapshot
name|createTableAndSnapshot
argument_list|(
name|util
argument_list|,
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|startRow
argument_list|,
name|endRow
argument_list|,
name|numRegions
argument_list|)
expr_stmt|;
if|if
condition|(
name|shutdownCluster
condition|)
block|{
name|util
operator|.
name|shutdownMiniHBaseCluster
argument_list|()
expr_stmt|;
block|}
try|try
block|{
comment|// create the job
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|startRow
argument_list|,
name|endRow
argument_list|)
decl_stmt|;
comment|// limit the scan
name|job
operator|.
name|setJarByClass
argument_list|(
name|util
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|addDependencyJarsForClasses
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|TestTableSnapshotInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|initTableSnapshotMapperJob
argument_list|(
name|snapshotName
argument_list|,
name|scan
argument_list|,
name|TestTableSnapshotMapper
operator|.
name|class
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|,
name|NullWritable
operator|.
name|class
argument_list|,
name|job
argument_list|,
literal|true
argument_list|,
name|tableDir
argument_list|)
expr_stmt|;
name|job
operator|.
name|setReducerClass
argument_list|(
name|TestTableSnapshotInputFormat
operator|.
name|TestTableSnapshotReducer
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|NullOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|shutdownCluster
condition|)
block|{
name|util
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|deleteSnapshot
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
name|util
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

