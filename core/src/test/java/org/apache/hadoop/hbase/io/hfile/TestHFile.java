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
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|HBaseTestCase
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
operator|.
name|KeyComparator
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
operator|.
name|BlockIndex
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
operator|.
name|Reader
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
operator|.
name|Writer
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
name|ClassSize
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
name|RawComparator
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

begin_comment
comment|/**  * test hfile features.  *<p>  * Copied from  *<a href="https://issues.apache.org/jira/browse/HADOOP-3315">hadoop-3315 tfile</a>.  * Remove after tfile is committed and use the tfile version of this class  * instead.</p>  */
end_comment

begin_class
specifier|public
class|class
name|TestHFile
extends|extends
name|HBaseTestCase
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestHFile
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|ROOT_DIR
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"test.build.data"
argument_list|,
literal|"/tmp/TestHFile"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|int
name|minBlockSize
init|=
literal|512
decl_stmt|;
specifier|private
specifier|static
name|String
name|localFormatter
init|=
literal|"%010d"
decl_stmt|;
comment|/**    * Test empty HFile.    * Test all features work reasonably when hfile is empty of entries.    * @throws IOException    */
specifier|public
name|void
name|testEmptyHFile
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|f
init|=
operator|new
name|Path
argument_list|(
name|ROOT_DIR
argument_list|,
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Writer
name|w
init|=
operator|new
name|Writer
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|f
argument_list|)
decl_stmt|;
name|w
operator|.
name|close
argument_list|()
expr_stmt|;
name|Reader
name|r
init|=
operator|new
name|Reader
argument_list|(
name|fs
argument_list|,
name|f
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|r
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|r
operator|.
name|getFirstKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|r
operator|.
name|getLastKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// write some records into the tfile
comment|// write them twice
specifier|private
name|int
name|writeSomeRecords
parameter_list|(
name|Writer
name|writer
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|n
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|value
init|=
literal|"value"
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|start
init|;
name|i
operator|<
operator|(
name|start
operator|+
name|n
operator|)
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
name|String
operator|.
name|format
argument_list|(
name|localFormatter
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
operator|+
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|start
operator|+
name|n
operator|)
return|;
block|}
specifier|private
name|void
name|readAllRecords
parameter_list|(
name|HFileScanner
name|scanner
parameter_list|)
throws|throws
name|IOException
block|{
name|readAndCheckbytes
argument_list|(
name|scanner
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|)
expr_stmt|;
block|}
comment|// read the records and check
specifier|private
name|int
name|readAndCheckbytes
parameter_list|(
name|HFileScanner
name|scanner
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|n
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|value
init|=
literal|"value"
decl_stmt|;
name|int
name|i
init|=
name|start
decl_stmt|;
for|for
control|(
init|;
name|i
operator|<
operator|(
name|start
operator|+
name|n
operator|)
condition|;
name|i
operator|++
control|)
block|{
name|ByteBuffer
name|key
init|=
name|scanner
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|ByteBuffer
name|val
init|=
name|scanner
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|String
name|keyStr
init|=
name|String
operator|.
name|format
argument_list|(
name|localFormatter
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|valStr
init|=
name|value
operator|+
name|keyStr
decl_stmt|;
name|byte
index|[]
name|keyBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"bytes for keys do not match "
operator|+
name|keyStr
operator|+
literal|" "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
argument_list|)
argument_list|,
name|Arrays
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|keyStr
argument_list|)
argument_list|,
name|keyBytes
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|valBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|val
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"bytes for vals do not match "
operator|+
name|valStr
operator|+
literal|" "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|valBytes
argument_list|)
argument_list|,
name|Arrays
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|valStr
argument_list|)
argument_list|,
name|valBytes
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|scanner
operator|.
name|next
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
name|assertEquals
argument_list|(
name|i
argument_list|,
name|start
operator|+
name|n
operator|-
literal|1
argument_list|)
expr_stmt|;
return|return
operator|(
name|start
operator|+
name|n
operator|)
return|;
block|}
specifier|private
name|byte
index|[]
name|getSomeKey
parameter_list|(
name|int
name|rowId
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
name|localFormatter
argument_list|,
name|Integer
operator|.
name|valueOf
argument_list|(
name|rowId
argument_list|)
argument_list|)
operator|.
name|getBytes
argument_list|()
return|;
block|}
specifier|private
name|void
name|writeRecords
parameter_list|(
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
name|writeSomeRecords
argument_list|(
name|writer
argument_list|,
literal|0
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
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
comment|/**    * test none codecs    */
name|void
name|basicWithSomeCodec
parameter_list|(
name|String
name|codec
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|ncTFile
init|=
operator|new
name|Path
argument_list|(
name|ROOT_DIR
argument_list|,
literal|"basic.hfile"
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|fout
init|=
name|createFSOutput
argument_list|(
name|ncTFile
argument_list|)
decl_stmt|;
name|Writer
name|writer
init|=
operator|new
name|Writer
argument_list|(
name|fout
argument_list|,
name|minBlockSize
argument_list|,
name|Compression
operator|.
name|getCompressionAlgorithmByName
argument_list|(
name|codec
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|writeRecords
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|fout
operator|.
name|close
argument_list|()
expr_stmt|;
name|FSDataInputStream
name|fin
init|=
name|fs
operator|.
name|open
argument_list|(
name|ncTFile
argument_list|)
decl_stmt|;
name|Reader
name|reader
init|=
operator|new
name|Reader
argument_list|(
name|fs
operator|.
name|open
argument_list|(
name|ncTFile
argument_list|)
argument_list|,
name|fs
operator|.
name|getFileStatus
argument_list|(
name|ncTFile
argument_list|)
operator|.
name|getLen
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// Load up the index.
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
comment|// Get a scanner that caches and that does not use pread.
name|HFileScanner
name|scanner
init|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// Align scanner at start of the file.
name|scanner
operator|.
name|seekTo
argument_list|()
expr_stmt|;
name|readAllRecords
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|seekTo
argument_list|(
name|getSomeKey
argument_list|(
literal|50
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"location lookup failed"
argument_list|,
name|scanner
operator|.
name|seekTo
argument_list|(
name|getSomeKey
argument_list|(
literal|50
argument_list|)
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
comment|// read the key and see if it matches
name|ByteBuffer
name|readKey
init|=
name|scanner
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"seeked key does not match"
argument_list|,
name|Arrays
operator|.
name|equals
argument_list|(
name|getSomeKey
argument_list|(
literal|50
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|readKey
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|seekTo
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|ByteBuffer
name|val1
init|=
name|scanner
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|scanner
operator|.
name|seekTo
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|ByteBuffer
name|val2
init|=
name|scanner
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|Arrays
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|val1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|val2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|fin
operator|.
name|close
argument_list|()
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|ncTFile
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testTFileFeatures
parameter_list|()
throws|throws
name|IOException
block|{
name|basicWithSomeCodec
argument_list|(
literal|"none"
argument_list|)
expr_stmt|;
name|basicWithSomeCodec
argument_list|(
literal|"gz"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|writeNumMetablocks
parameter_list|(
name|Writer
name|writer
parameter_list|,
name|int
name|n
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|writer
operator|.
name|appendMetaBlock
argument_list|(
literal|"HFileMeta"
operator|+
name|i
argument_list|,
operator|new
name|Writable
argument_list|()
block|{
specifier|private
name|int
name|val
decl_stmt|;
specifier|public
name|Writable
name|setVal
parameter_list|(
name|int
name|val
parameter_list|)
block|{
name|this
operator|.
name|val
operator|=
name|val
expr_stmt|;
return|return
name|this
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
name|out
operator|.
name|write
argument_list|(
operator|(
literal|"something to test"
operator|+
name|val
operator|)
operator|.
name|getBytes
argument_list|()
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
block|{ }
block|}
operator|.
name|setVal
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|someTestingWithMetaBlock
parameter_list|(
name|Writer
name|writer
parameter_list|)
block|{
name|writeNumMetablocks
argument_list|(
name|writer
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|readNumMetablocks
parameter_list|(
name|Reader
name|reader
parameter_list|,
name|int
name|n
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|ByteBuffer
name|actual
init|=
name|reader
operator|.
name|getMetaBlock
argument_list|(
literal|"HFileMeta"
operator|+
name|i
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|ByteBuffer
name|expected
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
operator|(
literal|"something to test"
operator|+
name|i
operator|)
operator|.
name|getBytes
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"failed to match metadata"
argument_list|,
name|actual
operator|.
name|compareTo
argument_list|(
name|expected
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|someReadingWithMetaBlock
parameter_list|(
name|Reader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|readNumMetablocks
argument_list|(
name|reader
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|metablocks
parameter_list|(
specifier|final
name|String
name|compress
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|mFile
init|=
operator|new
name|Path
argument_list|(
name|ROOT_DIR
argument_list|,
literal|"meta.hfile"
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|fout
init|=
name|createFSOutput
argument_list|(
name|mFile
argument_list|)
decl_stmt|;
name|Writer
name|writer
init|=
operator|new
name|Writer
argument_list|(
name|fout
argument_list|,
name|minBlockSize
argument_list|,
name|Compression
operator|.
name|getCompressionAlgorithmByName
argument_list|(
name|compress
argument_list|)
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|someTestingWithMetaBlock
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|fout
operator|.
name|close
argument_list|()
expr_stmt|;
name|FSDataInputStream
name|fin
init|=
name|fs
operator|.
name|open
argument_list|(
name|mFile
argument_list|)
decl_stmt|;
name|Reader
name|reader
init|=
operator|new
name|Reader
argument_list|(
name|fs
operator|.
name|open
argument_list|(
name|mFile
argument_list|)
argument_list|,
name|this
operator|.
name|fs
operator|.
name|getFileStatus
argument_list|(
name|mFile
argument_list|)
operator|.
name|getLen
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
comment|// No data -- this should return false.
name|assertFalse
argument_list|(
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
operator|.
name|seekTo
argument_list|()
argument_list|)
expr_stmt|;
name|someReadingWithMetaBlock
argument_list|(
name|reader
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|mFile
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
name|fin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// test meta blocks for tfiles
specifier|public
name|void
name|testMetaBlocks
parameter_list|()
throws|throws
name|Exception
block|{
name|metablocks
argument_list|(
literal|"none"
argument_list|)
expr_stmt|;
name|metablocks
argument_list|(
literal|"gz"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testNullMetaBlocks
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|mFile
init|=
operator|new
name|Path
argument_list|(
name|ROOT_DIR
argument_list|,
literal|"nometa.hfile"
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|fout
init|=
name|createFSOutput
argument_list|(
name|mFile
argument_list|)
decl_stmt|;
name|Writer
name|writer
init|=
operator|new
name|Writer
argument_list|(
name|fout
argument_list|,
name|minBlockSize
argument_list|,
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
literal|"foo"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"value"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|fout
operator|.
name|close
argument_list|()
expr_stmt|;
name|Reader
name|reader
init|=
operator|new
name|Reader
argument_list|(
name|fs
argument_list|,
name|mFile
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|reader
operator|.
name|getMetaBlock
argument_list|(
literal|"non-existant"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Make sure the orginals for our compression libs doesn't change on us.    */
specifier|public
name|void
name|testCompressionOrdinance
parameter_list|()
block|{
comment|//assertTrue(Compression.Algorithm.LZO.ordinal() == 0);
name|assertTrue
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|GZ
operator|.
name|ordinal
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
operator|.
name|ordinal
argument_list|()
operator|==
literal|2
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testComparator
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|mFile
init|=
operator|new
name|Path
argument_list|(
name|ROOT_DIR
argument_list|,
literal|"meta.tfile"
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|fout
init|=
name|createFSOutput
argument_list|(
name|mFile
argument_list|)
decl_stmt|;
name|Writer
name|writer
init|=
operator|new
name|Writer
argument_list|(
name|fout
argument_list|,
name|minBlockSize
argument_list|,
operator|(
name|Compression
operator|.
name|Algorithm
operator|)
literal|null
argument_list|,
operator|new
name|KeyComparator
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|byte
index|[]
name|b1
parameter_list|,
name|int
name|s1
parameter_list|,
name|int
name|l1
parameter_list|,
name|byte
index|[]
name|b2
parameter_list|,
name|int
name|s2
parameter_list|,
name|int
name|l2
parameter_list|)
block|{
return|return
operator|-
name|Bytes
operator|.
name|compareTo
argument_list|(
name|b1
argument_list|,
name|s1
argument_list|,
name|l1
argument_list|,
name|b2
argument_list|,
name|s2
argument_list|,
name|l2
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|byte
index|[]
name|o1
parameter_list|,
name|byte
index|[]
name|o2
parameter_list|)
block|{
return|return
name|compare
argument_list|(
name|o1
argument_list|,
literal|0
argument_list|,
name|o1
operator|.
name|length
argument_list|,
name|o2
argument_list|,
literal|0
argument_list|,
name|o2
operator|.
name|length
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
literal|"3"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"0"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
literal|"2"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"0"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
literal|"1"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"0"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Checks if the HeapSize calculator is within reason    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|testHeapSizeForBlockIndex
parameter_list|()
throws|throws
name|IOException
block|{
name|Class
name|cl
init|=
literal|null
decl_stmt|;
name|long
name|expected
init|=
literal|0L
decl_stmt|;
name|long
name|actual
init|=
literal|0L
decl_stmt|;
name|cl
operator|=
name|BlockIndex
operator|.
name|class
expr_stmt|;
name|expected
operator|=
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|BlockIndex
name|bi
init|=
operator|new
name|BlockIndex
argument_list|(
name|Bytes
operator|.
name|BYTES_RAWCOMPARATOR
argument_list|)
decl_stmt|;
name|actual
operator|=
name|bi
operator|.
name|heapSize
argument_list|()
expr_stmt|;
comment|//Since the arrays in BlockIndex(byte [][] blockKeys, long [] blockOffsets,
comment|//int [] blockDataSizes) are all null they are not going to show up in the
comment|//HeapSize calculation, so need to remove those array costs from ecpected.
name|expected
operator|-=
name|ClassSize
operator|.
name|align
argument_list|(
literal|3
operator|*
name|ClassSize
operator|.
name|ARRAY
argument_list|)
expr_stmt|;
if|if
condition|(
name|expected
operator|!=
name|actual
condition|)
block|{
name|ClassSize
operator|.
name|estimateBase
argument_list|(
name|cl
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|actual
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

