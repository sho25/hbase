begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License  */
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
operator|.
name|wal
package|;
end_package

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
name|io
operator|.
name|util
operator|.
name|Dictionary
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
name|ProtobufUtil
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
name|wal
operator|.
name|HLog
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
name|WritableUtils
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
import|;
end_import

begin_comment
comment|/**  * A set of static functions for running our custom WAL compression/decompression.  * Also contains a command line tool to compress and uncompress HLogs.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Compressor
block|{
comment|/**    * Command line tool to compress and uncompress WALs.    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|!=
literal|2
operator|||
name|args
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
literal|"--help"
argument_list|)
operator|||
name|args
index|[
literal|0
index|]
operator|.
name|equals
argument_list|(
literal|"-h"
argument_list|)
condition|)
block|{
name|printHelp
argument_list|()
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|Path
name|inputPath
init|=
operator|new
name|Path
argument_list|(
name|args
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|Path
name|outputPath
init|=
operator|new
name|Path
argument_list|(
name|args
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|transformFile
argument_list|(
name|inputPath
argument_list|,
name|outputPath
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|printHelp
parameter_list|()
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"usage: Compressor<input><output>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"If<input> HLog is compressed,<output> will be decompressed."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"If<input> HLog is uncompressed,<output> will be compressed."
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|private
specifier|static
name|void
name|transformFile
parameter_list|(
name|Path
name|input
parameter_list|,
name|Path
name|output
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|FileSystem
name|inFS
init|=
name|input
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|outFS
init|=
name|output
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HLog
operator|.
name|Reader
name|in
init|=
name|HLogFactory
operator|.
name|createReader
argument_list|(
name|inFS
argument_list|,
name|input
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HLog
operator|.
name|Writer
name|out
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
operator|(
name|in
operator|instanceof
name|ReaderBase
operator|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Cannot proceed, invalid reader type: "
operator|+
name|in
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|boolean
name|compress
init|=
operator|(
operator|(
name|ReaderBase
operator|)
name|in
operator|)
operator|.
name|hasCompression
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|ENABLE_WAL_COMPRESSION
argument_list|,
operator|!
name|compress
argument_list|)
expr_stmt|;
name|out
operator|=
name|HLogFactory
operator|.
name|createWriter
argument_list|(
name|outFS
argument_list|,
name|output
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|HLog
operator|.
name|Entry
name|e
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|e
operator|=
name|in
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
name|out
operator|.
name|append
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|out
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|out
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Reads the next compressed entry and returns it as a byte array    *     * @param in the DataInput to read from    * @param dict the dictionary we use for our read.    * @return the uncompressed array.    */
annotation|@
name|Deprecated
specifier|static
name|byte
index|[]
name|readCompressed
parameter_list|(
name|DataInput
name|in
parameter_list|,
name|Dictionary
name|dict
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
name|status
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|status
operator|==
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
condition|)
block|{
name|int
name|length
init|=
name|WritableUtils
operator|.
name|readVInt
argument_list|(
name|in
argument_list|)
decl_stmt|;
comment|// if this isn't in the dictionary, we need to add to the dictionary.
name|byte
index|[]
name|arr
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|arr
argument_list|)
expr_stmt|;
if|if
condition|(
name|dict
operator|!=
literal|null
condition|)
name|dict
operator|.
name|addEntry
argument_list|(
name|arr
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|arr
return|;
block|}
else|else
block|{
comment|// Status here is the higher-order byte of index of the dictionary entry
comment|// (when its not Dictionary.NOT_IN_DICTIONARY -- dictionary indices are
comment|// shorts).
name|short
name|dictIdx
init|=
name|toShort
argument_list|(
name|status
argument_list|,
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|entry
init|=
name|dict
operator|.
name|getEntry
argument_list|(
name|dictIdx
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Missing dictionary entry for index "
operator|+
name|dictIdx
argument_list|)
throw|;
block|}
return|return
name|entry
return|;
block|}
block|}
comment|/**    * Reads a compressed entry into an array.    * The output into the array ends up length-prefixed.    *     * @param to the array to write into    * @param offset array offset to start writing to    * @param in the DataInput to read from    * @param dict the dictionary to use for compression    *     * @return the length of the uncompressed data    */
annotation|@
name|Deprecated
specifier|static
name|int
name|uncompressIntoArray
parameter_list|(
name|byte
index|[]
name|to
parameter_list|,
name|int
name|offset
parameter_list|,
name|DataInput
name|in
parameter_list|,
name|Dictionary
name|dict
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
name|status
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|status
operator|==
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
condition|)
block|{
comment|// status byte indicating that data to be read is not in dictionary.
comment|// if this isn't in the dictionary, we need to add to the dictionary.
name|int
name|length
init|=
name|WritableUtils
operator|.
name|readVInt
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|to
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|dict
operator|.
name|addEntry
argument_list|(
name|to
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|length
return|;
block|}
else|else
block|{
comment|// the status byte also acts as the higher order byte of the dictionary
comment|// entry
name|short
name|dictIdx
init|=
name|toShort
argument_list|(
name|status
argument_list|,
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|entry
decl_stmt|;
try|try
block|{
name|entry
operator|=
name|dict
operator|.
name|getEntry
argument_list|(
name|dictIdx
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to uncompress the log entry"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Missing dictionary entry for index "
operator|+
name|dictIdx
argument_list|)
throw|;
block|}
comment|// now we write the uncompressed value.
name|Bytes
operator|.
name|putBytes
argument_list|(
name|to
argument_list|,
name|offset
argument_list|,
name|entry
argument_list|,
literal|0
argument_list|,
name|entry
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|entry
operator|.
name|length
return|;
block|}
block|}
comment|/**    * Compresses and writes an array to a DataOutput    *     * @param data the array to write.    * @param out the DataOutput to write into    * @param dict the dictionary to use for compression    */
annotation|@
name|Deprecated
specifier|static
name|void
name|writeCompressed
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|DataOutput
name|out
parameter_list|,
name|Dictionary
name|dict
parameter_list|)
throws|throws
name|IOException
block|{
name|short
name|dictIdx
init|=
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
decl_stmt|;
if|if
condition|(
name|dict
operator|!=
literal|null
condition|)
block|{
name|dictIdx
operator|=
name|dict
operator|.
name|findEntry
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|dictIdx
operator|==
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
condition|)
block|{
comment|// not in dict
name|out
operator|.
name|writeByte
argument_list|(
name|Dictionary
operator|.
name|NOT_IN_DICTIONARY
argument_list|)
expr_stmt|;
name|WritableUtils
operator|.
name|writeVInt
argument_list|(
name|out
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeShort
argument_list|(
name|dictIdx
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|short
name|toShort
parameter_list|(
name|byte
name|hi
parameter_list|,
name|byte
name|lo
parameter_list|)
block|{
name|short
name|s
init|=
call|(
name|short
call|)
argument_list|(
operator|(
operator|(
name|hi
operator|&
literal|0xFF
operator|)
operator|<<
literal|8
operator|)
operator||
operator|(
name|lo
operator|&
literal|0xFF
operator|)
argument_list|)
decl_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|s
operator|>=
literal|0
argument_list|)
expr_stmt|;
return|return
name|s
return|;
block|}
block|}
end_class

end_unit

