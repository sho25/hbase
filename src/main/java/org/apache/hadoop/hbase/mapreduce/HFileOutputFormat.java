begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|Map
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
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|filecache
operator|.
name|DistributedCache
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
name|Put
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
name|io
operator|.
name|hfile
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
name|mapreduce
operator|.
name|hadoopbackport
operator|.
name|TotalOrderPartitioner
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
name|StoreFile
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
name|io
operator|.
name|SequenceFile
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
name|RecordWriter
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
name|FileOutputCommitter
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
name|FileOutputFormat
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Writes HFiles. Passed KeyValues must arrive in order.  * Currently, can only write files to a single column family at a  * time.  Multiple column families requires coordinating keys cross family.  * Writes current time as the sequence id for the file. Sets the major compacted  * attribute on created hfiles.  * @see KeyValueSortReducer  */
end_comment

begin_class
specifier|public
class|class
name|HFileOutputFormat
extends|extends
name|FileOutputFormat
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|KeyValue
argument_list|>
block|{
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HFileOutputFormat
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|KeyValue
argument_list|>
name|getRecordWriter
parameter_list|(
specifier|final
name|TaskAttemptContext
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// Get the path of the temporary output file
specifier|final
name|Path
name|outputPath
init|=
name|FileOutputFormat
operator|.
name|getOutputPath
argument_list|(
name|context
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|outputdir
init|=
operator|new
name|FileOutputCommitter
argument_list|(
name|outputPath
argument_list|,
name|context
argument_list|)
operator|.
name|getWorkPath
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|FileSystem
name|fs
init|=
name|outputdir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// These configs. are from hbase-*.xml
specifier|final
name|long
name|maxsize
init|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.hregion.max.filesize"
argument_list|,
literal|268435456
argument_list|)
decl_stmt|;
specifier|final
name|int
name|blocksize
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hfile.min.blocksize.size"
argument_list|,
literal|65536
argument_list|)
decl_stmt|;
comment|// Invented config.  Add to hbase-*.xml if other than default compression.
specifier|final
name|String
name|compression
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"hfile.compression"
argument_list|,
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|KeyValue
argument_list|>
argument_list|()
block|{
comment|// Map of families to writers and how much has been output on the writer.
specifier|private
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|WriterLength
argument_list|>
name|writers
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|WriterLength
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
specifier|private
name|byte
index|[]
name|previousRow
init|=
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
decl_stmt|;
specifier|public
name|void
name|write
parameter_list|(
name|ImmutableBytesWritable
name|row
parameter_list|,
name|KeyValue
name|kv
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|length
init|=
name|kv
operator|.
name|getLength
argument_list|()
decl_stmt|;
name|byte
index|[]
name|family
init|=
name|kv
operator|.
name|getFamily
argument_list|()
decl_stmt|;
name|WriterLength
name|wl
init|=
name|this
operator|.
name|writers
operator|.
name|get
argument_list|(
name|family
argument_list|)
decl_stmt|;
if|if
condition|(
name|wl
operator|==
literal|null
operator|||
operator|(
operator|(
name|length
operator|+
name|wl
operator|.
name|written
operator|)
operator|>=
name|maxsize
operator|)
operator|&&
name|Bytes
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|previousRow
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|previousRow
operator|.
name|length
argument_list|,
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getRowLength
argument_list|()
argument_list|)
operator|!=
literal|0
condition|)
block|{
comment|// Get a new writer.
name|Path
name|basedir
init|=
operator|new
name|Path
argument_list|(
name|outputdir
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|wl
operator|==
literal|null
condition|)
block|{
name|wl
operator|=
operator|new
name|WriterLength
argument_list|()
expr_stmt|;
name|this
operator|.
name|writers
operator|.
name|put
argument_list|(
name|family
argument_list|,
name|wl
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|writers
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"One family only"
argument_list|)
throw|;
comment|// If wl == null, first file in family.  Ensure family dir exits.
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|basedir
argument_list|)
condition|)
name|fs
operator|.
name|mkdirs
argument_list|(
name|basedir
argument_list|)
expr_stmt|;
block|}
name|wl
operator|.
name|writer
operator|=
name|getNewWriter
argument_list|(
name|wl
operator|.
name|writer
argument_list|,
name|basedir
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Writer="
operator|+
name|wl
operator|.
name|writer
operator|.
name|getPath
argument_list|()
operator|+
operator|(
operator|(
name|wl
operator|.
name|written
operator|==
literal|0
operator|)
condition|?
literal|""
else|:
literal|", wrote="
operator|+
name|wl
operator|.
name|written
operator|)
argument_list|)
expr_stmt|;
name|wl
operator|.
name|written
operator|=
literal|0
expr_stmt|;
block|}
name|wl
operator|.
name|writer
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|wl
operator|.
name|written
operator|+=
name|length
expr_stmt|;
comment|// Copy the row so we know when a row transition.
name|this
operator|.
name|previousRow
operator|=
name|kv
operator|.
name|getRow
argument_list|()
expr_stmt|;
block|}
comment|/* Create a new HFile.Writer. Close current if there is one.        * @param writer        * @param familydir        * @return A new HFile.Writer.        * @throws IOException        */
specifier|private
name|HFile
operator|.
name|Writer
name|getNewWriter
parameter_list|(
specifier|final
name|HFile
operator|.
name|Writer
name|writer
parameter_list|,
specifier|final
name|Path
name|familydir
parameter_list|)
throws|throws
name|IOException
block|{
name|close
argument_list|(
name|writer
argument_list|)
expr_stmt|;
return|return
operator|new
name|HFile
operator|.
name|Writer
argument_list|(
name|fs
argument_list|,
name|StoreFile
operator|.
name|getUniqueFile
argument_list|(
name|fs
argument_list|,
name|familydir
argument_list|)
argument_list|,
name|blocksize
argument_list|,
name|compression
argument_list|,
name|KeyValue
operator|.
name|KEY_COMPARATOR
argument_list|)
return|;
block|}
specifier|private
name|void
name|close
parameter_list|(
specifier|final
name|HFile
operator|.
name|Writer
name|w
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|w
operator|!=
literal|null
condition|)
block|{
name|w
operator|.
name|appendFileInfo
argument_list|(
name|StoreFile
operator|.
name|BULKLOAD_TIME_KEY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|w
operator|.
name|appendFileInfo
argument_list|(
name|StoreFile
operator|.
name|BULKLOAD_TASK_KEY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|context
operator|.
name|getTaskAttemptID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|w
operator|.
name|appendFileInfo
argument_list|(
name|StoreFile
operator|.
name|MAJOR_COMPACTION_KEY
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|w
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|close
parameter_list|(
name|TaskAttemptContext
name|c
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|WriterLength
argument_list|>
name|e
range|:
name|this
operator|.
name|writers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|close
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|writer
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
comment|/*    * Data structure to hold a Writer and amount of data written on it.    */
specifier|static
class|class
name|WriterLength
block|{
name|long
name|written
init|=
literal|0
decl_stmt|;
name|HFile
operator|.
name|Writer
name|writer
init|=
literal|null
decl_stmt|;
block|}
comment|/**    * Return the start keys of all of the regions in this table,    * as a list of ImmutableBytesWritable.    */
specifier|private
specifier|static
name|List
argument_list|<
name|ImmutableBytesWritable
argument_list|>
name|getRegionStartKeys
parameter_list|(
name|HTable
name|table
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
index|[]
name|byteKeys
init|=
name|table
operator|.
name|getStartKeys
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|ImmutableBytesWritable
argument_list|>
name|ret
init|=
operator|new
name|ArrayList
argument_list|<
name|ImmutableBytesWritable
argument_list|>
argument_list|(
name|byteKeys
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|byteKey
range|:
name|byteKeys
control|)
block|{
name|ret
operator|.
name|add
argument_list|(
operator|new
name|ImmutableBytesWritable
argument_list|(
name|byteKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
comment|/**    * Write out a SequenceFile that can be read by TotalOrderPartitioner    * that contains the split points in startKeys.    * @param partitionsPath output path for SequenceFile    * @param startKeys the region start keys    */
specifier|private
specifier|static
name|void
name|writePartitions
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Path
name|partitionsPath
parameter_list|,
name|List
argument_list|<
name|ImmutableBytesWritable
argument_list|>
name|startKeys
parameter_list|)
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
operator|!
name|startKeys
operator|.
name|isEmpty
argument_list|()
argument_list|,
literal|"No regions passed"
argument_list|)
expr_stmt|;
comment|// We're generating a list of split points, and we don't ever
comment|// have keys< the first region (which has an empty start key)
comment|// so we need to remove it. Otherwise we would end up with an
comment|// empty reducer with index 0
name|TreeSet
argument_list|<
name|ImmutableBytesWritable
argument_list|>
name|sorted
init|=
operator|new
name|TreeSet
argument_list|<
name|ImmutableBytesWritable
argument_list|>
argument_list|(
name|startKeys
argument_list|)
decl_stmt|;
name|ImmutableBytesWritable
name|first
init|=
name|sorted
operator|.
name|first
argument_list|()
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|first
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
argument_list|,
literal|"First region of table should have empty start key. Instead has: %s"
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|first
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sorted
operator|.
name|remove
argument_list|(
name|first
argument_list|)
expr_stmt|;
comment|// Write the actual file
name|FileSystem
name|fs
init|=
name|partitionsPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|SequenceFile
operator|.
name|Writer
name|writer
init|=
name|SequenceFile
operator|.
name|createWriter
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|partitionsPath
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|,
name|NullWritable
operator|.
name|class
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|ImmutableBytesWritable
name|startKey
range|:
name|sorted
control|)
block|{
name|writer
operator|.
name|append
argument_list|(
name|startKey
argument_list|,
name|NullWritable
operator|.
name|get
argument_list|()
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
comment|/**    * Configure a MapReduce Job to perform an incremental load into the given    * table. This    *<ul>    *<li>Inspects the table to configure a total order partitioner</li>    *<li>Uploads the partitions file to the cluster and adds it to the DistributedCache</li>    *<li>Sets the number of reduce tasks to match the current number of regions</li>    *<li>Sets the output key/value class to match HFileOutputFormat's requirements</li>    *<li>Sets the reducer up to perform the appropriate sorting (either KeyValueSortReducer or    *     PutSortReducer)</li>    *</ul>     * The user should be sure to set the map output value class to either KeyValue or Put before    * running this function.    */
specifier|public
specifier|static
name|void
name|configureIncrementalLoad
parameter_list|(
name|Job
name|job
parameter_list|,
name|HTable
name|table
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
name|job
operator|.
name|setPartitionerClass
argument_list|(
name|TotalOrderPartitioner
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputKeyClass
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputValueClass
argument_list|(
name|KeyValue
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|HFileOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// Based on the configured map output class, set the correct reducer to properly
comment|// sort the incoming values.
comment|// TODO it would be nice to pick one or the other of these formats.
if|if
condition|(
name|KeyValue
operator|.
name|class
operator|.
name|equals
argument_list|(
name|job
operator|.
name|getMapOutputValueClass
argument_list|()
argument_list|)
condition|)
block|{
name|job
operator|.
name|setReducerClass
argument_list|(
name|KeyValueSortReducer
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Put
operator|.
name|class
operator|.
name|equals
argument_list|(
name|job
operator|.
name|getMapOutputValueClass
argument_list|()
argument_list|)
condition|)
block|{
name|job
operator|.
name|setReducerClass
argument_list|(
name|PutSortReducer
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unknown map output value type:"
operator|+
name|job
operator|.
name|getMapOutputValueClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Looking up current regions for table "
operator|+
name|table
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ImmutableBytesWritable
argument_list|>
name|startKeys
init|=
name|getRegionStartKeys
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Configuring "
operator|+
name|startKeys
operator|.
name|size
argument_list|()
operator|+
literal|" reduce partitions "
operator|+
literal|"to match current region count"
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
name|startKeys
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|partitionsPath
init|=
operator|new
name|Path
argument_list|(
name|job
operator|.
name|getWorkingDirectory
argument_list|()
argument_list|,
literal|"partitions_"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Writing partition information to "
operator|+
name|partitionsPath
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|partitionsPath
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|writePartitions
argument_list|(
name|conf
argument_list|,
name|partitionsPath
argument_list|,
name|startKeys
argument_list|)
expr_stmt|;
name|partitionsPath
operator|.
name|makeQualified
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|URI
name|cacheUri
decl_stmt|;
try|try
block|{
name|cacheUri
operator|=
operator|new
name|URI
argument_list|(
name|partitionsPath
operator|.
name|toString
argument_list|()
operator|+
literal|"#"
operator|+
name|TotalOrderPartitioner
operator|.
name|DEFAULT_PATH
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|DistributedCache
operator|.
name|addCacheFile
argument_list|(
name|cacheUri
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|DistributedCache
operator|.
name|createSymlink
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Incremental table output configured."
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

