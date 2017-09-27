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
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|client
operator|.
name|metrics
operator|.
name|ScanMetrics
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
name|util
operator|.
name|RegionSplitter
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
name|Writable
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
name|InputFormat
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
name|JobContext
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
name|TaskAttemptContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * TableSnapshotInputFormat allows a MapReduce job to run over a table snapshot. The job  * bypasses HBase servers, and directly accesses the underlying files (hfile, recovered edits,  * wals, etc) directly to provide maximum performance. The snapshot is not required to be  * restored to the live cluster or cloned. This also allows to run the mapreduce job from an  * online or offline hbase cluster. The snapshot files can be exported by using the  * {@link org.apache.hadoop.hbase.snapshot.ExportSnapshot} tool, to a pure-hdfs cluster,  * and this InputFormat can be used to run the mapreduce job directly over the snapshot files.  * The snapshot should not be deleted while there are jobs reading from snapshot files.  *<p>  * Usage is similar to TableInputFormat, and  * {@link TableMapReduceUtil#initTableSnapshotMapperJob(String, Scan, Class, Class, Class, Job, boolean, Path)}  * can be used to configure the job.  *<pre>{@code  * Job job = new Job(conf);  * Scan scan = new Scan();  * TableMapReduceUtil.initTableSnapshotMapperJob(snapshotName,  *      scan, MyTableMapper.class, MyMapKeyOutput.class,  *      MyMapOutputValueWritable.class, job, true);  * }  *</pre>  *<p>  * Internally, this input format restores the snapshot into the given tmp directory. By default,  * and similar to {@link TableInputFormat} an InputSplit is created per region, but optionally you  * can run N mapper tasks per every region, in which case the region key range will be split to  * N sub-ranges and an InputSplit will be created per sub-range. The region is opened for reading  * from each RecordReader. An internal RegionScanner is used to execute the  * {@link org.apache.hadoop.hbase.CellScanner} obtained from the user.  *<p>  * HBase owns all the data and snapshot files on the filesystem. Only the 'hbase' user can read from  * snapshot files and data files.  * To read from snapshot files directly from the file system, the user who is running the MR job  * must have sufficient permissions to access snapshot and reference files.  * This means that to run mapreduce over snapshot files, the MR job has to be run as the HBase  * user or the user must have group or other privileges in the filesystem (See HBASE-8369).  * Note that, given other users access to read from snapshot/data files will completely circumvent  * the access control enforced by HBase.  * @see org.apache.hadoop.hbase.client.TableSnapshotScanner  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|TableSnapshotInputFormat
extends|extends
name|InputFormat
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
block|{
specifier|public
specifier|static
class|class
name|TableSnapshotRegionSplit
extends|extends
name|InputSplit
implements|implements
name|Writable
block|{
specifier|private
name|TableSnapshotInputFormatImpl
operator|.
name|InputSplit
name|delegate
decl_stmt|;
comment|// constructor for mapreduce framework / Writable
specifier|public
name|TableSnapshotRegionSplit
parameter_list|()
block|{
name|this
operator|.
name|delegate
operator|=
operator|new
name|TableSnapshotInputFormatImpl
operator|.
name|InputSplit
argument_list|()
expr_stmt|;
block|}
specifier|public
name|TableSnapshotRegionSplit
parameter_list|(
name|TableSnapshotInputFormatImpl
operator|.
name|InputSplit
name|delegate
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
block|}
specifier|public
name|TableSnapshotRegionSplit
parameter_list|(
name|HTableDescriptor
name|htd
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|locations
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|Path
name|restoreDir
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
operator|new
name|TableSnapshotInputFormatImpl
operator|.
name|InputSplit
argument_list|(
name|htd
argument_list|,
name|regionInfo
argument_list|,
name|locations
argument_list|,
name|scan
argument_list|,
name|restoreDir
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|delegate
operator|.
name|getLength
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getLocations
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|delegate
operator|.
name|getLocations
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|delegate
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|delegate
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
comment|/**      * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0      *             Use {@link #getRegion()}      */
annotation|@
name|Deprecated
specifier|public
name|HRegionInfo
name|getRegionInfo
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getRegionInfo
argument_list|()
return|;
block|}
specifier|public
name|RegionInfo
name|getRegion
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getRegionInfo
argument_list|()
return|;
block|}
block|}
annotation|@
name|VisibleForTesting
specifier|static
class|class
name|TableSnapshotRegionRecordReader
extends|extends
name|RecordReader
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
block|{
specifier|private
name|TableSnapshotInputFormatImpl
operator|.
name|RecordReader
name|delegate
init|=
operator|new
name|TableSnapshotInputFormatImpl
operator|.
name|RecordReader
argument_list|()
decl_stmt|;
specifier|private
name|TaskAttemptContext
name|context
decl_stmt|;
specifier|private
name|Method
name|getCounter
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|getCounter
operator|=
name|TableRecordReaderImpl
operator|.
name|retrieveGetCounterWithStringsParams
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|delegate
operator|.
name|initialize
argument_list|(
operator|(
operator|(
name|TableSnapshotRegionSplit
operator|)
name|split
operator|)
operator|.
name|delegate
argument_list|,
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|nextKeyValue
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|boolean
name|result
init|=
name|delegate
operator|.
name|nextKeyValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
condition|)
block|{
name|ScanMetrics
name|scanMetrics
init|=
name|delegate
operator|.
name|getScanner
argument_list|()
operator|.
name|getScanMetrics
argument_list|()
decl_stmt|;
if|if
condition|(
name|scanMetrics
operator|!=
literal|null
operator|&&
name|context
operator|!=
literal|null
condition|)
block|{
name|TableRecordReaderImpl
operator|.
name|updateCounters
argument_list|(
name|scanMetrics
argument_list|,
literal|0
argument_list|,
name|getCounter
argument_list|,
name|context
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|ImmutableBytesWritable
name|getCurrentKey
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|delegate
operator|.
name|getCurrentKey
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Result
name|getCurrentValue
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|delegate
operator|.
name|getCurrentValue
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|getProgress
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|delegate
operator|.
name|getProgress
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|delegate
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|RecordReader
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
name|createRecordReader
parameter_list|(
name|InputSplit
name|split
parameter_list|,
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|TableSnapshotRegionRecordReader
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|InputSplit
argument_list|>
name|getSplits
parameter_list|(
name|JobContext
name|job
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|List
argument_list|<
name|InputSplit
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|TableSnapshotInputFormatImpl
operator|.
name|InputSplit
name|split
range|:
name|TableSnapshotInputFormatImpl
operator|.
name|getSplits
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|)
control|)
block|{
name|results
operator|.
name|add
argument_list|(
operator|new
name|TableSnapshotRegionSplit
argument_list|(
name|split
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|results
return|;
block|}
comment|/**    * Configures the job to use TableSnapshotInputFormat to read from a snapshot.    * @param job the job to configure    * @param snapshotName the name of the snapshot to read from    * @param restoreDir a temporary directory to restore the snapshot into. Current user should    * have write permissions to this directory, and this should not be a subdirectory of rootdir.    * After the job is finished, restoreDir can be deleted.    * @throws IOException if an error occurs    */
specifier|public
specifier|static
name|void
name|setInput
parameter_list|(
name|Job
name|job
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|Path
name|restoreDir
parameter_list|)
throws|throws
name|IOException
block|{
name|TableSnapshotInputFormatImpl
operator|.
name|setInput
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|snapshotName
argument_list|,
name|restoreDir
argument_list|)
expr_stmt|;
block|}
comment|/**    * Configures the job to use TableSnapshotInputFormat to read from a snapshot.    * @param job the job to configure    * @param snapshotName the name of the snapshot to read from    * @param restoreDir a temporary directory to restore the snapshot into. Current user should    * have write permissions to this directory, and this should not be a subdirectory of rootdir.    * After the job is finished, restoreDir can be deleted.    * @param splitAlgo split algorithm to generate splits from region    * @param numSplitsPerRegion how many input splits to generate per one region    * @throws IOException if an error occurs    */
specifier|public
specifier|static
name|void
name|setInput
parameter_list|(
name|Job
name|job
parameter_list|,
name|String
name|snapshotName
parameter_list|,
name|Path
name|restoreDir
parameter_list|,
name|RegionSplitter
operator|.
name|SplitAlgorithm
name|splitAlgo
parameter_list|,
name|int
name|numSplitsPerRegion
parameter_list|)
throws|throws
name|IOException
block|{
name|TableSnapshotInputFormatImpl
operator|.
name|setInput
argument_list|(
name|job
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|snapshotName
argument_list|,
name|restoreDir
argument_list|,
name|splitAlgo
argument_list|,
name|numSplitsPerRegion
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

