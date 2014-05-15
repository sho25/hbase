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
name|mapred
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
name|TableSnapshotInputFormatTestBase
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
name|mapred
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
name|mapred
operator|.
name|JobClient
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|MapReduceBase
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
name|mapred
operator|.
name|OutputCollector
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
name|mapred
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
name|mapred
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
name|mapred
operator|.
name|Reporter
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
name|mapred
operator|.
name|RunningJob
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
name|mapred
operator|.
name|lib
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
name|Assert
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
name|Iterator
import|;
end_import

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
name|TestTableSnapshotInputFormat
extends|extends
name|TableSnapshotInputFormatTestBase
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|aaa
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|after_zzz
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zz{"
argument_list|)
decl_stmt|;
comment|// 'z' + 1 => '{'
specifier|private
specifier|static
specifier|final
name|String
name|COLUMNS
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|)
operator|+
literal|" "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|1
index|]
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
name|aaa
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
name|after_zzz
return|;
block|}
specifier|static
class|class
name|TestTableSnapshotMapper
extends|extends
name|MapReduceBase
implements|implements
name|TableMap
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|NullWritable
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Result
name|value
parameter_list|,
name|OutputCollector
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|NullWritable
argument_list|>
name|collector
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
block|{
name|verifyRowFromMap
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|collector
operator|.
name|collect
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
name|MapReduceBase
implements|implements
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
name|aaa
argument_list|,
name|after_zzz
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|reduce
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Iterator
argument_list|<
name|NullWritable
argument_list|>
name|values
parameter_list|,
name|OutputCollector
argument_list|<
name|NullWritable
argument_list|,
name|NullWritable
argument_list|>
name|collector
parameter_list|,
name|Reporter
name|reporter
parameter_list|)
throws|throws
name|IOException
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
specifier|public
name|void
name|close
parameter_list|()
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
name|JobConf
name|job
init|=
operator|new
name|JobConf
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
name|initTableSnapshotMapJob
argument_list|(
name|snapshotName
argument_list|,
name|COLUMNS
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
literal|"Snapshot job should not use SlabCache."
argument_list|,
literal|0
argument_list|,
name|job
operator|.
name|getFloat
argument_list|(
literal|"hbase.offheapcache.percentage"
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
comment|// TODO: mapred does not support limiting input range by startrow, endrow.
comment|// Thus the following tests must override parameterverification.
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testWithMockedMapReduceMultiRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|testWithMockedMapReduce
argument_list|(
name|UTIL
argument_list|,
literal|"testWithMockedMapReduceMultiRegion"
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
specifier|public
name|void
name|testWithMapReduceMultiRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|testWithMapReduce
argument_list|(
name|UTIL
argument_list|,
literal|"testWithMapReduceMultiRegion"
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|Override
comment|// run the MR job while HBase is offline
specifier|public
name|void
name|testWithMapReduceAndOfflineHBaseMultiRegion
parameter_list|()
throws|throws
name|Exception
block|{
name|testWithMapReduce
argument_list|(
name|UTIL
argument_list|,
literal|"testWithMapReduceAndOfflineHBaseMultiRegion"
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
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
name|JobConf
name|job
init|=
operator|new
name|JobConf
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
name|TableMapReduceUtil
operator|.
name|initTableSnapshotMapJob
argument_list|(
name|snapshotName
argument_list|,
name|COLUMNS
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
comment|// mapred doesn't support start and end keys? o.O
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
name|JobConf
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
name|InputSplit
index|[]
name|splits
init|=
name|tsif
operator|.
name|getSplits
argument_list|(
name|job
argument_list|,
literal|0
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
name|length
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
name|length
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
index|[
name|i
index|]
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|split
operator|instanceof
name|TableSnapshotInputFormat
operator|.
name|TableSnapshotRegionSplit
argument_list|)
expr_stmt|;
comment|// validate record reader
name|OutputCollector
name|collector
init|=
name|mock
argument_list|(
name|OutputCollector
operator|.
name|class
argument_list|)
decl_stmt|;
name|Reporter
name|reporter
init|=
name|mock
argument_list|(
name|Reporter
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|getRecordReader
argument_list|(
name|split
argument_list|,
name|job
argument_list|,
name|reporter
argument_list|)
decl_stmt|;
comment|// validate we can read all the data back
name|ImmutableBytesWritable
name|key
init|=
name|rr
operator|.
name|createKey
argument_list|()
decl_stmt|;
name|Result
name|value
init|=
name|rr
operator|.
name|createValue
argument_list|()
decl_stmt|;
while|while
condition|(
name|rr
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
name|verifyRowFromMap
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|rowTracker
operator|.
name|addRow
argument_list|(
name|key
operator|.
name|copyBytes
argument_list|()
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
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|util
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|jobConf
operator|.
name|setJarByClass
argument_list|(
name|util
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
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
name|TableMapReduceUtil
operator|.
name|addDependencyJars
argument_list|(
name|jobConf
argument_list|,
name|TestTableSnapshotInputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|initTableSnapshotMapJob
argument_list|(
name|snapshotName
argument_list|,
name|COLUMNS
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
name|jobConf
argument_list|,
literal|true
argument_list|,
name|tableDir
argument_list|)
expr_stmt|;
name|jobConf
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
name|jobConf
operator|.
name|setNumReduceTasks
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setOutputFormat
argument_list|(
name|NullOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|RunningJob
name|job
init|=
name|JobClient
operator|.
name|runJob
argument_list|(
name|jobConf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|job
operator|.
name|isSuccessful
argument_list|()
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

