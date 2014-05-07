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
name|ByteArrayOutputStream
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|UUID
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
name|classification
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
name|classification
operator|.
name|InterfaceStability
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
name|CellUtil
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
name|HDFSBlocksDistribution
operator|.
name|HostAndWeight
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
name|ClientSideRegionScanner
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
name|IsolationLevel
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
name|TableSnapshotScanner
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|SnapshotDescription
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
name|SnapshotProtos
operator|.
name|SnapshotRegionManifest
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
name|MapReduceProtos
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
name|HRegion
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
name|snapshot
operator|.
name|ExportSnapshot
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
name|snapshot
operator|.
name|RestoreSnapshotHelper
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
name|snapshot
operator|.
name|SnapshotDescriptionUtils
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
name|snapshot
operator|.
name|SnapshotManifest
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
comment|/**  * TableSnapshotInputFormat allows a MapReduce job to run over a table snapshot. The job  * bypasses HBase servers, and directly accesses the underlying files (hfile, recovered edits,  * hlogs, etc) directly to provide maximum performance. The snapshot is not required to be  * restored to the live cluster or cloned. This also allows to run the mapreduce job from an  * online or offline hbase cluster. The snapshot files can be exported by using the  * {@link ExportSnapshot} tool, to a pure-hdfs cluster, and this InputFormat can be used to  * run the mapreduce job directly over the snapshot files. The snapshot should not be deleted  * while there are jobs reading from snapshot files.  *<p>  * Usage is similar to TableInputFormat, and  * {@link TableMapReduceUtil#initTableSnapshotMapperJob(String, Scan, Class, Class, Class, Job,  *   boolean, Path)}  * can be used to configure the job.  *<pre>{@code  * Job job = new Job(conf);  * Scan scan = new Scan();  * TableMapReduceUtil.initTableSnapshotMapperJob(snapshotName,  *      scan, MyTableMapper.class, MyMapKeyOutput.class,  *      MyMapOutputValueWritable.class, job, true);  * }  *</pre>  *<p>  * Internally, this input format restores the snapshot into the given tmp directory. Similar to  * {@link TableInputFormat} an InputSplit is created per region. The region is opened for reading  * from each RecordReader. An internal RegionScanner is used to execute the {@link Scan} obtained  * from the user.  *<p>  * HBase owns all the data and snapshot files on the filesystem. Only the HBase user can read from  * snapshot files and data files. HBase also enforces security because all the requests are handled  * by the server layer, and the user cannot read from the data files directly.  * To read from snapshot files directly from the file system, the user who is running the MR job  * must have sufficient permissions to access snapshot and reference files.  * This means that to run mapreduce over snapshot files, the MR job has to be run as the HBase  * user or the user must have group or other priviledges in the filesystem (See HBASE-8369).  * Note that, given other users access to read from snapshot/data files will completely circumvent  * the access control enforced by HBase.  * @see TableSnapshotScanner  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
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
comment|// TODO: Snapshots files are owned in fs by the hbase user. There is no
comment|// easy way to delegate access.
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
name|TableSnapshotInputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** See {@link #getBestLocations(Configuration, HDFSBlocksDistribution)} */
specifier|private
specifier|static
specifier|final
name|String
name|LOCALITY_CUTOFF_MULTIPLIER
init|=
literal|"hbase.tablesnapshotinputformat.locality.cutoff.multiplier"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|float
name|DEFAULT_LOCALITY_CUTOFF_MULTIPLIER
init|=
literal|0.8f
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|SNAPSHOT_NAME_KEY
init|=
literal|"hbase.TableSnapshotInputFormat.snapshot.name"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_DIR_KEY
init|=
literal|"hbase.TableSnapshotInputFormat.table.dir"
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|static
class|class
name|TableSnapshotRegionSplit
extends|extends
name|InputSplit
implements|implements
name|Writable
block|{
specifier|private
name|HTableDescriptor
name|htd
decl_stmt|;
specifier|private
name|HRegionInfo
name|regionInfo
decl_stmt|;
specifier|private
name|String
index|[]
name|locations
decl_stmt|;
comment|// constructor for mapreduce framework / Writable
specifier|public
name|TableSnapshotRegionSplit
parameter_list|()
block|{ }
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
parameter_list|)
block|{
name|this
operator|.
name|htd
operator|=
name|htd
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
if|if
condition|(
name|locations
operator|==
literal|null
operator|||
name|locations
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|locations
operator|=
operator|new
name|String
index|[
literal|0
index|]
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|locations
operator|=
name|locations
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|locations
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
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
comment|//TODO: We can obtain the file sizes of the snapshot here.
return|return
literal|0
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
name|locations
return|;
block|}
comment|// TODO: We should have ProtobufSerialization in Hadoop, and directly use PB objects instead of
comment|// doing this wrapping with Writables.
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
name|MapReduceProtos
operator|.
name|TableSnapshotRegionSplit
operator|.
name|Builder
name|builder
init|=
name|MapReduceProtos
operator|.
name|TableSnapshotRegionSplit
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTable
argument_list|(
name|htd
operator|.
name|convert
argument_list|()
argument_list|)
operator|.
name|setRegion
argument_list|(
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|regionInfo
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|location
range|:
name|locations
control|)
block|{
name|builder
operator|.
name|addLocations
argument_list|(
name|location
argument_list|)
expr_stmt|;
block|}
name|MapReduceProtos
operator|.
name|TableSnapshotRegionSplit
name|split
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|split
operator|.
name|writeTo
argument_list|(
name|baos
argument_list|)
expr_stmt|;
name|baos
operator|.
name|close
argument_list|()
expr_stmt|;
name|byte
index|[]
name|buf
init|=
name|baos
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|buf
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|buf
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
name|int
name|len
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|buf
argument_list|)
expr_stmt|;
name|MapReduceProtos
operator|.
name|TableSnapshotRegionSplit
name|split
init|=
name|MapReduceProtos
operator|.
name|TableSnapshotRegionSplit
operator|.
name|PARSER
operator|.
name|parseFrom
argument_list|(
name|buf
argument_list|)
decl_stmt|;
name|this
operator|.
name|htd
operator|=
name|HTableDescriptor
operator|.
name|convert
argument_list|(
name|split
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|split
operator|.
name|getRegion
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|locationsList
init|=
name|split
operator|.
name|getLocationsList
argument_list|()
decl_stmt|;
name|this
operator|.
name|locations
operator|=
name|locationsList
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|locationsList
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
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
name|TableSnapshotRegionSplit
name|split
decl_stmt|;
specifier|private
name|Scan
name|scan
decl_stmt|;
specifier|private
name|Result
name|result
init|=
literal|null
decl_stmt|;
specifier|private
name|ImmutableBytesWritable
name|row
init|=
literal|null
decl_stmt|;
specifier|private
name|ClientSideRegionScanner
name|scanner
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
name|Configuration
name|conf
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|this
operator|.
name|split
operator|=
operator|(
name|TableSnapshotRegionSplit
operator|)
name|split
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
name|this
operator|.
name|split
operator|.
name|htd
decl_stmt|;
name|HRegionInfo
name|hri
init|=
name|this
operator|.
name|split
operator|.
name|regionInfo
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FSUtils
operator|.
name|getCurrentFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|tmpRootDir
init|=
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|TABLE_DIR_KEY
argument_list|)
argument_list|)
decl_stmt|;
comment|// This is the user specified root
comment|// directory where snapshot was restored
comment|// create scan
name|String
name|scanStr
init|=
name|conf
operator|.
name|get
argument_list|(
name|TableInputFormat
operator|.
name|SCAN
argument_list|)
decl_stmt|;
if|if
condition|(
name|scanStr
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"A Scan is not configured for this job"
argument_list|)
throw|;
block|}
name|scan
operator|=
name|TableMapReduceUtil
operator|.
name|convertStringToScan
argument_list|(
name|scanStr
argument_list|)
expr_stmt|;
comment|// region is immutable, this should be fine,
comment|// otherwise we have to set the thread read point
name|scan
operator|.
name|setIsolationLevel
argument_list|(
name|IsolationLevel
operator|.
name|READ_UNCOMMITTED
argument_list|)
expr_stmt|;
comment|// disable caching of data blocks
name|scan
operator|.
name|setCacheBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|scanner
operator|=
operator|new
name|ClientSideRegionScanner
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|tmpRootDir
argument_list|,
name|htd
argument_list|,
name|hri
argument_list|,
name|scan
argument_list|,
literal|null
argument_list|)
expr_stmt|;
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
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
comment|//we are done
return|return
literal|false
return|;
block|}
if|if
condition|(
name|this
operator|.
name|row
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|row
operator|=
operator|new
name|ImmutableBytesWritable
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|row
operator|.
name|set
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|ScanMetrics
name|scanMetrics
init|=
name|scanner
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
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
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
name|row
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
name|result
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
literal|0
return|;
comment|// TODO: use total bytes to estimate
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
if|if
condition|(
name|this
operator|.
name|scanner
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
name|Configuration
name|conf
init|=
name|job
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|String
name|snapshotName
init|=
name|getSnapshotName
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|rootDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|snapshotDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
name|snapshotName
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
name|SnapshotDescription
name|snapshotDesc
init|=
name|SnapshotDescriptionUtils
operator|.
name|readSnapshotInfo
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|)
decl_stmt|;
name|SnapshotManifest
name|manifest
init|=
name|SnapshotManifest
operator|.
name|open
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|,
name|snapshotDesc
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SnapshotRegionManifest
argument_list|>
name|regionManifests
init|=
name|manifest
operator|.
name|getRegionManifests
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionManifests
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Snapshot seems empty"
argument_list|)
throw|;
block|}
comment|// load table descriptor
name|HTableDescriptor
name|htd
init|=
name|manifest
operator|.
name|getTableDescriptor
argument_list|()
decl_stmt|;
name|Scan
name|scan
init|=
name|TableMapReduceUtil
operator|.
name|convertStringToScan
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|TableInputFormat
operator|.
name|SCAN
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|tableDir
init|=
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|TABLE_DIR_KEY
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|InputSplit
argument_list|>
name|splits
init|=
operator|new
name|ArrayList
argument_list|<
name|InputSplit
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|SnapshotRegionManifest
name|regionManifest
range|:
name|regionManifests
control|)
block|{
comment|// load region descriptor
name|HRegionInfo
name|hri
init|=
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|regionManifest
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|CellUtil
operator|.
name|overlappingKeys
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|,
name|scan
operator|.
name|getStopRow
argument_list|()
argument_list|,
name|hri
operator|.
name|getStartKey
argument_list|()
argument_list|,
name|hri
operator|.
name|getEndKey
argument_list|()
argument_list|)
condition|)
block|{
comment|// compute HDFS locations from snapshot files (which will get the locations for
comment|// referred hfiles)
name|List
argument_list|<
name|String
argument_list|>
name|hosts
init|=
name|getBestLocations
argument_list|(
name|conf
argument_list|,
name|HRegion
operator|.
name|computeHDFSBlocksDistribution
argument_list|(
name|conf
argument_list|,
name|htd
argument_list|,
name|hri
argument_list|,
name|tableDir
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|len
init|=
name|Math
operator|.
name|min
argument_list|(
literal|3
argument_list|,
name|hosts
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|hosts
operator|=
name|hosts
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|splits
operator|.
name|add
argument_list|(
operator|new
name|TableSnapshotRegionSplit
argument_list|(
name|htd
argument_list|,
name|hri
argument_list|,
name|hosts
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|splits
return|;
block|}
comment|/**    * This computes the locations to be passed from the InputSplit. MR/Yarn schedulers does not take    * weights into account, thus will treat every location passed from the input split as equal. We    * do not want to blindly pass all the locations, since we are creating one split per region, and    * the region's blocks are all distributed throughout the cluster unless favorite node assignment    * is used. On the expected stable case, only one location will contain most of the blocks as    * local.    * On the other hand, in favored node assignment, 3 nodes will contain highly local blocks. Here    * we are doing a simple heuristic, where we will pass all hosts which have at least 80%    * (hbase.tablesnapshotinputformat.locality.cutoff.multiplier) as much block locality as the top    * host with the best locality.    */
annotation|@
name|VisibleForTesting
name|List
argument_list|<
name|String
argument_list|>
name|getBestLocations
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HDFSBlocksDistribution
name|blockDistribution
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|locations
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|HostAndWeight
index|[]
name|hostAndWeights
init|=
name|blockDistribution
operator|.
name|getTopHostsWithWeights
argument_list|()
decl_stmt|;
if|if
condition|(
name|hostAndWeights
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|locations
return|;
block|}
name|HostAndWeight
name|topHost
init|=
name|hostAndWeights
index|[
literal|0
index|]
decl_stmt|;
name|locations
operator|.
name|add
argument_list|(
name|topHost
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
comment|// Heuristic: filter all hosts which have at least cutoffMultiplier % of block locality
name|double
name|cutoffMultiplier
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|LOCALITY_CUTOFF_MULTIPLIER
argument_list|,
name|DEFAULT_LOCALITY_CUTOFF_MULTIPLIER
argument_list|)
decl_stmt|;
name|double
name|filterWeight
init|=
name|topHost
operator|.
name|getWeight
argument_list|()
operator|*
name|cutoffMultiplier
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|hostAndWeights
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|hostAndWeights
index|[
name|i
index|]
operator|.
name|getWeight
argument_list|()
operator|>=
name|filterWeight
condition|)
block|{
name|locations
operator|.
name|add
argument_list|(
name|hostAndWeights
index|[
name|i
index|]
operator|.
name|getHost
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
return|return
name|locations
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
name|Configuration
name|conf
init|=
name|job
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|SNAPSHOT_NAME_KEY
argument_list|,
name|snapshotName
argument_list|)
expr_stmt|;
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|rootDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|restoreDir
operator|=
operator|new
name|Path
argument_list|(
name|restoreDir
argument_list|,
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO: restore from record readers to parallelize.
name|RestoreSnapshotHelper
operator|.
name|copySnapshotForScanner
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|rootDir
argument_list|,
name|restoreDir
argument_list|,
name|snapshotName
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|TABLE_DIR_KEY
argument_list|,
name|restoreDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
name|getSnapshotName
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|snapshotName
init|=
name|conf
operator|.
name|get
argument_list|(
name|SNAPSHOT_NAME_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|snapshotName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Snapshot name must be provided"
argument_list|)
throw|;
block|}
return|return
name|snapshotName
return|;
block|}
block|}
end_class

end_unit

