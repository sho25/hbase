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
name|io
operator|.
name|UnsupportedEncodingException
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
name|net
operator|.
name|URLDecoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLEncoder
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
name|Collection
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
name|Partitioner
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

begin_comment
comment|/**  * Writes HFiles. Passed KeyValues must arrive in order.  * Currently, can only write files to a single column family at a  * time.  Multiple column families requires coordinating keys cross family.  * Writes current time as the sequence id for the file. Sets the major compacted  * attribute on created hfiles. Calling write(null,null) will forceably roll  * all HFiles being written.  * @see KeyValueSortReducer  */
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
specifier|static
specifier|final
name|String
name|COMPRESSION_CONF_KEY
init|=
literal|"hbase.hfileoutputformat.families.compression"
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
name|HConstants
operator|.
name|DEFAULT_MAX_FILE_SIZE
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
literal|"hbase.mapreduce.hfileoutputformat.blocksize"
argument_list|,
name|HFile
operator|.
name|DEFAULT_BLOCKSIZE
argument_list|)
decl_stmt|;
comment|// Invented config.  Add to hbase-*.xml if other than default compression.
specifier|final
name|String
name|defaultCompression
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
comment|// create a map from column family to the compression algorithm
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|compressionMap
init|=
name|createFamilyCompressionMap
argument_list|(
name|conf
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
specifier|private
specifier|final
name|byte
index|[]
name|now
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|rollRequested
init|=
literal|false
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
comment|// null input == user explicitly wants to flush
if|if
condition|(
name|row
operator|==
literal|null
operator|&&
name|kv
operator|==
literal|null
condition|)
block|{
name|rollWriters
argument_list|()
expr_stmt|;
return|return;
block|}
name|byte
index|[]
name|rowKey
init|=
name|kv
operator|.
name|getRow
argument_list|()
decl_stmt|;
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
comment|// If this is a new column family, verify that the directory exists
if|if
condition|(
name|wl
operator|==
literal|null
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
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
argument_list|)
expr_stmt|;
block|}
comment|// If any of the HFiles for the column families has reached
comment|// maxsize, we need to roll all the writers
if|if
condition|(
name|wl
operator|!=
literal|null
operator|&&
name|wl
operator|.
name|written
operator|+
name|length
operator|>=
name|maxsize
condition|)
block|{
name|this
operator|.
name|rollRequested
operator|=
literal|true
expr_stmt|;
block|}
comment|// This can only happen once a row is finished though
if|if
condition|(
name|rollRequested
operator|&&
name|Bytes
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|previousRow
argument_list|,
name|rowKey
argument_list|)
operator|!=
literal|0
condition|)
block|{
name|rollWriters
argument_list|()
expr_stmt|;
block|}
comment|// create a new HLog writer, if necessary
if|if
condition|(
name|wl
operator|==
literal|null
operator|||
name|wl
operator|.
name|writer
operator|==
literal|null
condition|)
block|{
name|wl
operator|=
name|getNewWriter
argument_list|(
name|family
argument_list|)
expr_stmt|;
block|}
comment|// we now have the proper HLog writer. full steam ahead
name|kv
operator|.
name|updateLatestStamp
argument_list|(
name|this
operator|.
name|now
argument_list|)
expr_stmt|;
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
name|rowKey
expr_stmt|;
block|}
specifier|private
name|void
name|rollWriters
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|WriterLength
name|wl
range|:
name|this
operator|.
name|writers
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|wl
operator|.
name|writer
operator|!=
literal|null
condition|)
block|{
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
name|close
argument_list|(
name|wl
operator|.
name|writer
argument_list|)
expr_stmt|;
block|}
name|wl
operator|.
name|writer
operator|=
literal|null
expr_stmt|;
name|wl
operator|.
name|written
operator|=
literal|0
expr_stmt|;
block|}
name|this
operator|.
name|rollRequested
operator|=
literal|false
expr_stmt|;
block|}
comment|/* Create a new HFile.Writer.        * @param family        * @return A WriterLength, containing a new HFile.Writer.        * @throws IOException        */
specifier|private
name|WriterLength
name|getNewWriter
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
block|{
name|WriterLength
name|wl
init|=
operator|new
name|WriterLength
argument_list|()
decl_stmt|;
name|Path
name|familydir
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
name|String
name|compression
init|=
name|compressionMap
operator|.
name|get
argument_list|(
name|family
argument_list|)
decl_stmt|;
name|compression
operator|=
name|compression
operator|==
literal|null
condition|?
name|defaultCompression
else|:
name|compression
expr_stmt|;
name|wl
operator|.
name|writer
operator|=
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
return|return
name|wl
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
name|WriterLength
name|wl
range|:
name|this
operator|.
name|writers
operator|.
name|values
argument_list|()
control|)
block|{
name|close
argument_list|(
name|wl
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
if|if
condition|(
name|startKeys
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No regions passed"
argument_list|)
throw|;
block|}
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
if|if
condition|(
operator|!
name|first
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"First region of table should have empty start key. Instead has: "
operator|+
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
throw|;
block|}
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
name|Class
argument_list|<
name|?
extends|extends
name|Partitioner
argument_list|>
name|topClass
decl_stmt|;
try|try
block|{
name|topClass
operator|=
name|getTotalOrderPartitionerClass
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed getting TotalOrderPartitioner"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|job
operator|.
name|setPartitionerClass
argument_list|(
name|topClass
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
comment|// Below we make explicit reference to the bundled TOP.  Its cheating.
comment|// We are assume the define in the hbase bundled TOP is as it is in
comment|// hadoop (whether 0.20 or 0.22, etc.)
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
comment|// Set compression algorithms based on column families
name|configureCompression
argument_list|(
name|table
argument_list|,
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
comment|/**    * If> hadoop 0.20, then we want to use the hadoop TotalOrderPartitioner.    * If 0.20, then we want to use the TOP that we have under hadoopbackport.    * This method is about hbase being able to run on different versions of    * hadoop.  In 0.20.x hadoops, we have to use the TOP that is bundled with    * hbase.  Otherwise, we use the one in Hadoop.    * @return Instance of the TotalOrderPartitioner class    * @throws ClassNotFoundException If can't find a TotalOrderPartitioner.    */
specifier|private
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|Partitioner
argument_list|>
name|getTotalOrderPartitionerClass
parameter_list|()
throws|throws
name|ClassNotFoundException
block|{
name|Class
argument_list|<
name|?
extends|extends
name|Partitioner
argument_list|>
name|clazz
init|=
literal|null
decl_stmt|;
try|try
block|{
name|clazz
operator|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|Partitioner
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|clazz
operator|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|Partitioner
argument_list|>
operator|)
name|Class
operator|.
name|forName
argument_list|(
literal|"org.apache.hadoop.hbase.mapreduce.hadoopbackport.TotalOrderPartitioner"
argument_list|)
expr_stmt|;
block|}
return|return
name|clazz
return|;
block|}
comment|/**    * Run inside the task to deserialize column family to compression algorithm    * map from the    * configuration.    *     * Package-private for unit tests only.    *     * @return a map from column family to the name of the configured compression    *         algorithm    */
specifier|static
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|createFamilyCompressionMap
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|compressionMap
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|String
name|compressionConf
init|=
name|conf
operator|.
name|get
argument_list|(
name|COMPRESSION_CONF_KEY
argument_list|,
literal|""
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|familyConf
range|:
name|compressionConf
operator|.
name|split
argument_list|(
literal|"&"
argument_list|)
control|)
block|{
name|String
index|[]
name|familySplit
init|=
name|familyConf
operator|.
name|split
argument_list|(
literal|"="
argument_list|)
decl_stmt|;
if|if
condition|(
name|familySplit
operator|.
name|length
operator|!=
literal|2
condition|)
block|{
continue|continue;
block|}
try|try
block|{
name|compressionMap
operator|.
name|put
argument_list|(
name|URLDecoder
operator|.
name|decode
argument_list|(
name|familySplit
index|[
literal|0
index|]
argument_list|,
literal|"UTF-8"
argument_list|)
operator|.
name|getBytes
argument_list|()
argument_list|,
name|URLDecoder
operator|.
name|decode
argument_list|(
name|familySplit
index|[
literal|1
index|]
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
comment|// will not happen with UTF-8 encoding
throw|throw
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|compressionMap
return|;
block|}
comment|/**    * Serialize column family to compression algorithm map to configuration.    * Invoked while configuring the MR job for incremental load.    *     * Package-private for unit tests only.    *     * @throws IOException    *           on failure to read column family descriptors    */
specifier|static
name|void
name|configureCompression
parameter_list|(
name|HTable
name|table
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|StringBuilder
name|compressionConfigValue
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|tableDescriptor
init|=
name|table
operator|.
name|getTableDescriptor
argument_list|()
decl_stmt|;
if|if
condition|(
name|tableDescriptor
operator|==
literal|null
condition|)
block|{
comment|// could happen with mock table instance
return|return;
block|}
name|Collection
argument_list|<
name|HColumnDescriptor
argument_list|>
name|families
init|=
name|tableDescriptor
operator|.
name|getFamilies
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|HColumnDescriptor
name|familyDescriptor
range|:
name|families
control|)
block|{
if|if
condition|(
name|i
operator|++
operator|>
literal|0
condition|)
block|{
name|compressionConfigValue
operator|.
name|append
argument_list|(
literal|'&'
argument_list|)
expr_stmt|;
block|}
name|compressionConfigValue
operator|.
name|append
argument_list|(
name|URLEncoder
operator|.
name|encode
argument_list|(
name|familyDescriptor
operator|.
name|getNameAsString
argument_list|()
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
name|compressionConfigValue
operator|.
name|append
argument_list|(
literal|'='
argument_list|)
expr_stmt|;
name|compressionConfigValue
operator|.
name|append
argument_list|(
name|URLEncoder
operator|.
name|encode
argument_list|(
name|familyDescriptor
operator|.
name|getCompression
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Get rid of the last ampersand
name|conf
operator|.
name|set
argument_list|(
name|COMPRESSION_CONF_KEY
argument_list|,
name|compressionConfigValue
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

