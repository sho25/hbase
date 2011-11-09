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
name|io
operator|.
name|hfile
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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|DateFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|SimpleDateFormat
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
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|FSDataOutputStream
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
name|io
operator|.
name|BytesWritable
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
name|io
operator|.
name|compress
operator|.
name|CompressionCodec
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
name|compress
operator|.
name|GzipCodec
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

begin_comment
comment|/**  *  Set of long-running tests to measure performance of HFile.  *<p>  * Copied from  *<a href="https://issues.apache.org/jira/browse/HADOOP-3315">hadoop-3315 tfile</a>.  * Remove after tfile is committed and use the tfile version of this class  * instead.</p>  */
end_comment

begin_class
specifier|public
class|class
name|TestHFilePerformance
extends|extends
name|TestCase
block|{
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|String
name|ROOT_DIR
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"TestHFilePerformance"
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|long
name|startTimeEpoch
decl_stmt|;
specifier|private
name|long
name|finishTimeEpoch
decl_stmt|;
specifier|private
name|DateFormat
name|formatter
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|formatter
operator|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd HH:mm:ss"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|startTime
parameter_list|()
block|{
name|startTimeEpoch
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|formatTime
argument_list|()
operator|+
literal|" Started timing."
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|stopTime
parameter_list|()
block|{
name|finishTimeEpoch
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|formatTime
argument_list|()
operator|+
literal|" Stopped timing."
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|getIntervalMillis
parameter_list|()
block|{
return|return
name|finishTimeEpoch
operator|-
name|startTimeEpoch
return|;
block|}
specifier|public
name|void
name|printlnWithTimestamp
parameter_list|(
name|String
name|message
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|formatTime
argument_list|()
operator|+
literal|"  "
operator|+
name|message
argument_list|)
expr_stmt|;
block|}
comment|/*    * Format millis into minutes and seconds.    */
specifier|public
name|String
name|formatTime
parameter_list|(
name|long
name|milis
parameter_list|)
block|{
return|return
name|formatter
operator|.
name|format
argument_list|(
name|milis
argument_list|)
return|;
block|}
specifier|public
name|String
name|formatTime
parameter_list|()
block|{
return|return
name|formatTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|FSDataOutputStream
name|createFSOutput
parameter_list|(
name|Path
name|name
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|name
argument_list|)
condition|)
name|fs
operator|.
name|delete
argument_list|(
name|name
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|FSDataOutputStream
name|fout
init|=
name|fs
operator|.
name|create
argument_list|(
name|name
argument_list|)
decl_stmt|;
return|return
name|fout
return|;
block|}
comment|//TODO have multiple ways of generating key/value e.g. dictionary words
comment|//TODO to have a sample compressable data, for now, made 1 out of 3 values random
comment|//     keys are all random.
specifier|private
specifier|static
class|class
name|KeyValueGenerator
block|{
name|Random
name|keyRandomizer
decl_stmt|;
name|Random
name|valueRandomizer
decl_stmt|;
name|long
name|randomValueRatio
init|=
literal|3
decl_stmt|;
comment|// 1 out of randomValueRatio generated values will be random.
name|long
name|valueSequence
init|=
literal|0
decl_stmt|;
name|KeyValueGenerator
parameter_list|()
block|{
name|keyRandomizer
operator|=
operator|new
name|Random
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
comment|//TODO with seed zero
name|valueRandomizer
operator|=
operator|new
name|Random
argument_list|(
literal|1L
argument_list|)
expr_stmt|;
comment|//TODO with seed one
block|}
comment|// Key is always random now.
name|void
name|getKey
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
block|{
name|keyRandomizer
operator|.
name|nextBytes
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
name|void
name|getValue
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
if|if
condition|(
name|valueSequence
operator|%
name|randomValueRatio
operator|==
literal|0
condition|)
name|valueRandomizer
operator|.
name|nextBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|valueSequence
operator|++
expr_stmt|;
block|}
block|}
comment|/**    *    * @param fileType "HFile" or "SequenceFile"    * @param keyLength    * @param valueLength    * @param codecName "none", "lzo", "gz", "snappy"    * @param rows number of rows to be written.    * @param writeMethod used for HFile only.    * @param minBlockSize used for HFile only.    * @throws IOException    */
comment|//TODO writeMethod: implement multiple ways of writing e.g. A) known length (no chunk) B) using a buffer and streaming (for many chunks).
specifier|public
name|void
name|timeWrite
parameter_list|(
name|String
name|fileType
parameter_list|,
name|int
name|keyLength
parameter_list|,
name|int
name|valueLength
parameter_list|,
name|String
name|codecName
parameter_list|,
name|long
name|rows
parameter_list|,
name|String
name|writeMethod
parameter_list|,
name|int
name|minBlockSize
parameter_list|)
throws|throws
name|IOException
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"File Type: "
operator|+
name|fileType
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Writing "
operator|+
name|fileType
operator|+
literal|" with codecName: "
operator|+
name|codecName
argument_list|)
expr_stmt|;
name|long
name|totalBytesWritten
init|=
literal|0
decl_stmt|;
comment|//Using separate randomizer for key/value with seeds matching Sequence File.
name|byte
index|[]
name|key
init|=
operator|new
name|byte
index|[
name|keyLength
index|]
decl_stmt|;
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
name|valueLength
index|]
decl_stmt|;
name|KeyValueGenerator
name|generator
init|=
operator|new
name|KeyValueGenerator
argument_list|()
decl_stmt|;
name|startTime
argument_list|()
expr_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|ROOT_DIR
argument_list|,
name|fileType
operator|+
literal|".Performance"
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|ROOT_DIR
operator|+
name|path
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|FSDataOutputStream
name|fout
init|=
name|createFSOutput
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"HFile"
operator|.
name|equals
argument_list|(
name|fileType
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"HFile write method: "
argument_list|)
expr_stmt|;
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
argument_list|)
operator|.
name|createWriter
argument_list|(
name|fout
argument_list|,
name|minBlockSize
argument_list|,
name|codecName
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// Writing value in one shot.
for|for
control|(
name|long
name|l
init|=
literal|0
init|;
name|l
operator|<
name|rows
condition|;
name|l
operator|++
control|)
block|{
name|generator
operator|.
name|getKey
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|generator
operator|.
name|getValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|totalBytesWritten
operator|+=
name|key
operator|.
name|length
expr_stmt|;
name|totalBytesWritten
operator|+=
name|value
operator|.
name|length
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"SequenceFile"
operator|.
name|equals
argument_list|(
name|fileType
argument_list|)
condition|)
block|{
name|CompressionCodec
name|codec
init|=
literal|null
decl_stmt|;
if|if
condition|(
literal|"gz"
operator|.
name|equals
argument_list|(
name|codecName
argument_list|)
condition|)
name|codec
operator|=
operator|new
name|GzipCodec
argument_list|()
expr_stmt|;
elseif|else
if|if
condition|(
operator|!
literal|"none"
operator|.
name|equals
argument_list|(
name|codecName
argument_list|)
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Codec not supported."
argument_list|)
throw|;
name|SequenceFile
operator|.
name|Writer
name|writer
decl_stmt|;
comment|//TODO
comment|//JobConf conf = new JobConf();
if|if
condition|(
operator|!
literal|"none"
operator|.
name|equals
argument_list|(
name|codecName
argument_list|)
condition|)
name|writer
operator|=
name|SequenceFile
operator|.
name|createWriter
argument_list|(
name|conf
argument_list|,
name|fout
argument_list|,
name|BytesWritable
operator|.
name|class
argument_list|,
name|BytesWritable
operator|.
name|class
argument_list|,
name|SequenceFile
operator|.
name|CompressionType
operator|.
name|BLOCK
argument_list|,
name|codec
argument_list|)
expr_stmt|;
else|else
name|writer
operator|=
name|SequenceFile
operator|.
name|createWriter
argument_list|(
name|conf
argument_list|,
name|fout
argument_list|,
name|BytesWritable
operator|.
name|class
argument_list|,
name|BytesWritable
operator|.
name|class
argument_list|,
name|SequenceFile
operator|.
name|CompressionType
operator|.
name|NONE
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|BytesWritable
name|keyBsw
decl_stmt|;
name|BytesWritable
name|valBsw
decl_stmt|;
for|for
control|(
name|long
name|l
init|=
literal|0
init|;
name|l
operator|<
name|rows
condition|;
name|l
operator|++
control|)
block|{
name|generator
operator|.
name|getKey
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|keyBsw
operator|=
operator|new
name|BytesWritable
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|totalBytesWritten
operator|+=
name|keyBsw
operator|.
name|getSize
argument_list|()
expr_stmt|;
name|generator
operator|.
name|getValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|valBsw
operator|=
operator|new
name|BytesWritable
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|keyBsw
argument_list|,
name|valBsw
argument_list|)
expr_stmt|;
name|totalBytesWritten
operator|+=
name|valBsw
operator|.
name|getSize
argument_list|()
expr_stmt|;
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
else|else
throw|throw
operator|new
name|IOException
argument_list|(
literal|"File Type is not supported"
argument_list|)
throw|;
name|fout
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopTime
argument_list|()
expr_stmt|;
name|printlnWithTimestamp
argument_list|(
literal|"Data written: "
argument_list|)
expr_stmt|;
name|printlnWithTimestamp
argument_list|(
literal|"  rate  = "
operator|+
name|totalBytesWritten
operator|/
name|getIntervalMillis
argument_list|()
operator|*
literal|1000
operator|/
literal|1024
operator|/
literal|1024
operator|+
literal|"MB/s"
argument_list|)
expr_stmt|;
name|printlnWithTimestamp
argument_list|(
literal|"  total = "
operator|+
name|totalBytesWritten
operator|+
literal|"B"
argument_list|)
expr_stmt|;
name|printlnWithTimestamp
argument_list|(
literal|"File written: "
argument_list|)
expr_stmt|;
name|printlnWithTimestamp
argument_list|(
literal|"  rate  = "
operator|+
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
operator|.
name|getLen
argument_list|()
operator|/
name|getIntervalMillis
argument_list|()
operator|*
literal|1000
operator|/
literal|1024
operator|/
literal|1024
operator|+
literal|"MB/s"
argument_list|)
expr_stmt|;
name|printlnWithTimestamp
argument_list|(
literal|"  total = "
operator|+
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
operator|.
name|getLen
argument_list|()
operator|+
literal|"B"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|timeReading
parameter_list|(
name|String
name|fileType
parameter_list|,
name|int
name|keyLength
parameter_list|,
name|int
name|valueLength
parameter_list|,
name|long
name|rows
parameter_list|,
name|int
name|method
parameter_list|)
throws|throws
name|IOException
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Reading file of type: "
operator|+
name|fileType
argument_list|)
expr_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|ROOT_DIR
argument_list|,
name|fileType
operator|+
literal|".Performance"
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Input file size: "
operator|+
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
operator|.
name|getLen
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|totalBytesRead
init|=
literal|0
decl_stmt|;
name|ByteBuffer
name|val
decl_stmt|;
name|ByteBuffer
name|key
decl_stmt|;
name|startTime
argument_list|()
expr_stmt|;
name|FSDataInputStream
name|fin
init|=
name|fs
operator|.
name|open
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"HFile"
operator|.
name|equals
argument_list|(
name|fileType
argument_list|)
condition|)
block|{
name|HFile
operator|.
name|Reader
name|reader
init|=
name|HFile
operator|.
name|createReader
argument_list|(
name|path
argument_list|,
name|fs
operator|.
name|open
argument_list|(
name|path
argument_list|)
argument_list|,
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
operator|.
name|getLen
argument_list|()
argument_list|,
operator|new
name|CacheConfig
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|method
condition|)
block|{
case|case
literal|0
case|:
case|case
literal|1
case|:
default|default:
block|{
name|HFileScanner
name|scanner
init|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|scanner
operator|.
name|seekTo
argument_list|()
expr_stmt|;
for|for
control|(
name|long
name|l
init|=
literal|0
init|;
name|l
operator|<
name|rows
condition|;
name|l
operator|++
control|)
block|{
name|key
operator|=
name|scanner
operator|.
name|getKey
argument_list|()
expr_stmt|;
name|val
operator|=
name|scanner
operator|.
name|getValue
argument_list|()
expr_stmt|;
name|totalBytesRead
operator|+=
name|key
operator|.
name|limit
argument_list|()
operator|+
name|val
operator|.
name|limit
argument_list|()
expr_stmt|;
name|scanner
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
block|}
break|break;
block|}
block|}
elseif|else
if|if
condition|(
literal|"SequenceFile"
operator|.
name|equals
argument_list|(
name|fileType
argument_list|)
condition|)
block|{
name|SequenceFile
operator|.
name|Reader
name|reader
decl_stmt|;
name|reader
operator|=
operator|new
name|SequenceFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|reader
operator|.
name|getCompressionCodec
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|printlnWithTimestamp
argument_list|(
literal|"Compression codec class: "
operator|+
name|reader
operator|.
name|getCompressionCodec
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
name|printlnWithTimestamp
argument_list|(
literal|"Compression codec class: "
operator|+
literal|"none"
argument_list|)
expr_stmt|;
name|BytesWritable
name|keyBsw
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
name|BytesWritable
name|valBsw
init|=
operator|new
name|BytesWritable
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|l
init|=
literal|0
init|;
name|l
operator|<
name|rows
condition|;
name|l
operator|++
control|)
block|{
name|reader
operator|.
name|next
argument_list|(
name|keyBsw
argument_list|,
name|valBsw
argument_list|)
expr_stmt|;
name|totalBytesRead
operator|+=
name|keyBsw
operator|.
name|getSize
argument_list|()
operator|+
name|valBsw
operator|.
name|getSize
argument_list|()
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
comment|//TODO make a tests for other types of SequenceFile reading scenarios
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"File Type not supported."
argument_list|)
throw|;
block|}
comment|//printlnWithTimestamp("Closing reader");
name|fin
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopTime
argument_list|()
expr_stmt|;
comment|//printlnWithTimestamp("Finished close");
name|printlnWithTimestamp
argument_list|(
literal|"Finished in "
operator|+
name|getIntervalMillis
argument_list|()
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
name|printlnWithTimestamp
argument_list|(
literal|"Data read: "
argument_list|)
expr_stmt|;
name|printlnWithTimestamp
argument_list|(
literal|"  rate  = "
operator|+
name|totalBytesRead
operator|/
name|getIntervalMillis
argument_list|()
operator|*
literal|1000
operator|/
literal|1024
operator|/
literal|1024
operator|+
literal|"MB/s"
argument_list|)
expr_stmt|;
name|printlnWithTimestamp
argument_list|(
literal|"  total = "
operator|+
name|totalBytesRead
operator|+
literal|"B"
argument_list|)
expr_stmt|;
name|printlnWithTimestamp
argument_list|(
literal|"File read: "
argument_list|)
expr_stmt|;
name|printlnWithTimestamp
argument_list|(
literal|"  rate  = "
operator|+
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
operator|.
name|getLen
argument_list|()
operator|/
name|getIntervalMillis
argument_list|()
operator|*
literal|1000
operator|/
literal|1024
operator|/
literal|1024
operator|+
literal|"MB/s"
argument_list|)
expr_stmt|;
name|printlnWithTimestamp
argument_list|(
literal|"  total = "
operator|+
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
operator|.
name|getLen
argument_list|()
operator|+
literal|"B"
argument_list|)
expr_stmt|;
comment|//TODO uncomment this for final committing so test files is removed.
comment|//fs.delete(path, true);
block|}
specifier|public
name|void
name|testRunComparisons
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|keyLength
init|=
literal|100
decl_stmt|;
comment|// 100B
name|int
name|valueLength
init|=
literal|5
operator|*
literal|1024
decl_stmt|;
comment|// 5KB
name|int
name|minBlockSize
init|=
literal|10
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
comment|// 10MB
name|int
name|rows
init|=
literal|10000
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"****************************** Sequence File *****************************"
argument_list|)
expr_stmt|;
name|timeWrite
argument_list|(
literal|"SequenceFile"
argument_list|,
name|keyLength
argument_list|,
name|valueLength
argument_list|,
literal|"none"
argument_list|,
name|rows
argument_list|,
literal|null
argument_list|,
name|minBlockSize
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\n+++++++\n"
argument_list|)
expr_stmt|;
name|timeReading
argument_list|(
literal|"SequenceFile"
argument_list|,
name|keyLength
argument_list|,
name|valueLength
argument_list|,
name|rows
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"----------------------"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|""
argument_list|)
expr_stmt|;
comment|/* DISABLED LZO     timeWrite("SequenceFile", keyLength, valueLength, "lzo", rows, null, minBlockSize);     System.out.println("\n+++++++\n");     timeReading("SequenceFile", keyLength, valueLength, rows, -1);      System.out.println("");     System.out.println("----------------------");     System.out.println("");      /* Sequence file can only use native hadoop libs gzipping so commenting out.      */
try|try
block|{
name|timeWrite
argument_list|(
literal|"SequenceFile"
argument_list|,
name|keyLength
argument_list|,
name|valueLength
argument_list|,
literal|"gz"
argument_list|,
name|rows
argument_list|,
literal|null
argument_list|,
name|minBlockSize
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\n+++++++\n"
argument_list|)
expr_stmt|;
name|timeReading
argument_list|(
literal|"SequenceFile"
argument_list|,
name|keyLength
argument_list|,
name|valueLength
argument_list|,
name|rows
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Skipping sequencefile gz: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\n\n\n"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"****************************** HFile *****************************"
argument_list|)
expr_stmt|;
name|timeWrite
argument_list|(
literal|"HFile"
argument_list|,
name|keyLength
argument_list|,
name|valueLength
argument_list|,
literal|"none"
argument_list|,
name|rows
argument_list|,
literal|null
argument_list|,
name|minBlockSize
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\n+++++++\n"
argument_list|)
expr_stmt|;
name|timeReading
argument_list|(
literal|"HFile"
argument_list|,
name|keyLength
argument_list|,
name|valueLength
argument_list|,
name|rows
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"----------------------"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|""
argument_list|)
expr_stmt|;
comment|/* DISABLED LZO     timeWrite("HFile", keyLength, valueLength, "lzo", rows, null, minBlockSize);     System.out.println("\n+++++++\n");     timeReading("HFile", keyLength, valueLength, rows, 0 );     System.out.println("\n+++++++\n");     timeReading("HFile", keyLength, valueLength, rows, 1 );     System.out.println("\n+++++++\n");     timeReading("HFile", keyLength, valueLength, rows, 2 );      System.out.println("");     System.out.println("----------------------");     System.out.println(""); */
name|timeWrite
argument_list|(
literal|"HFile"
argument_list|,
name|keyLength
argument_list|,
name|valueLength
argument_list|,
literal|"gz"
argument_list|,
name|rows
argument_list|,
literal|null
argument_list|,
name|minBlockSize
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\n+++++++\n"
argument_list|)
expr_stmt|;
name|timeReading
argument_list|(
literal|"HFile"
argument_list|,
name|keyLength
argument_list|,
name|valueLength
argument_list|,
name|rows
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"\n\n\n\nNotes: "
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" * Timing includes open/closing of files."
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" * Timing includes reading both Key and Value"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" * Data is generated as random bytes. Other methods e.g. using "
operator|+
literal|"dictionary with care for distributation of words is under development."
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" * Timing of write currently, includes random value/key generations. "
operator|+
literal|"Which is the same for Sequence File and HFile. Another possibility is to generate "
operator|+
literal|"test data beforehand"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" * We need to mitigate cache effect on benchmark. We can apply several "
operator|+
literal|"ideas, for next step we do a large dummy read between benchmark read to dismantle "
operator|+
literal|"caching of data. Renaming of file may be helpful. We can have a loop that reads with"
operator|+
literal|" the same method several times and flood cache every time and average it to get a"
operator|+
literal|" better number."
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

