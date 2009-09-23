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
name|FileOutputFormat
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mortbay
operator|.
name|log
operator|.
name|Log
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
specifier|public
name|RecordWriter
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|KeyValue
argument_list|>
name|getRecordWriter
parameter_list|(
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
name|outputdir
init|=
name|FileOutputFormat
operator|.
name|getOutputPath
argument_list|(
name|context
argument_list|)
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
name|Log
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
name|StoreFile
operator|.
name|appendMetadata
argument_list|(
name|w
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|true
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
comment|/*    * Data structure to hold a Writer and amount of data written on it.     */
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
block|}
end_class

end_unit

