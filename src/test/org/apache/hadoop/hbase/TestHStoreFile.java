begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|dfs
operator|.
name|MiniDFSCluster
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
name|io
operator|.
name|MapFile
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
name|Text
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
name|WritableComparable
import|;
end_import

begin_comment
comment|/**  * Test HStoreFile  */
end_comment

begin_class
specifier|public
class|class
name|TestHStoreFile
extends|extends
name|TestCase
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
name|TestHStoreFile
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|String
name|DIR
init|=
literal|"/"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|char
name|FIRST_CHAR
init|=
literal|'a'
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|char
name|LAST_CHAR
init|=
literal|'z'
decl_stmt|;
specifier|private
name|MiniDFSCluster
name|cluster
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
name|Path
name|dir
init|=
literal|null
decl_stmt|;
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|new
name|HBaseConfiguration
argument_list|()
expr_stmt|;
name|this
operator|.
name|cluster
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|cluster
operator|=
operator|new
name|MiniDFSCluster
argument_list|(
name|this
operator|.
name|conf
argument_list|,
literal|2
argument_list|,
literal|true
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|cluster
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
name|this
operator|.
name|dir
operator|=
operator|new
name|Path
argument_list|(
name|DIR
argument_list|,
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|this
operator|.
name|cluster
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Path
name|writeMapFile
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|DIR
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|MapFile
operator|.
name|Writer
name|writer
init|=
operator|new
name|MapFile
operator|.
name|Writer
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|fs
argument_list|,
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|HStoreKey
operator|.
name|class
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
decl_stmt|;
name|writeStoreFile
argument_list|(
name|writer
argument_list|)
expr_stmt|;
return|return
name|path
return|;
block|}
specifier|private
name|Path
name|writeSmallMapFile
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|DIR
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|MapFile
operator|.
name|Writer
name|writer
init|=
operator|new
name|MapFile
operator|.
name|Writer
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|fs
argument_list|,
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|HStoreKey
operator|.
name|class
argument_list|,
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
decl_stmt|;
try|try
block|{
for|for
control|(
name|char
name|d
init|=
name|FIRST_CHAR
init|;
name|d
operator|<=
name|LAST_CHAR
condition|;
name|d
operator|++
control|)
block|{
name|byte
index|[]
name|b
init|=
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
name|d
block|}
decl_stmt|;
name|Text
name|t
init|=
operator|new
name|Text
argument_list|(
operator|new
name|String
argument_list|(
name|b
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
operator|new
name|HStoreKey
argument_list|(
name|t
argument_list|,
name|t
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|,
operator|new
name|ImmutableBytesWritable
argument_list|(
name|t
operator|.
name|getBytes
argument_list|()
argument_list|)
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
return|return
name|path
return|;
block|}
comment|/*    * Writes HStoreKey and ImmutableBytes data to passed writer and    * then closes it.    * @param writer    * @throws IOException    */
specifier|private
name|void
name|writeStoreFile
parameter_list|(
specifier|final
name|MapFile
operator|.
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
for|for
control|(
name|char
name|d
init|=
name|FIRST_CHAR
init|;
name|d
operator|<=
name|LAST_CHAR
condition|;
name|d
operator|++
control|)
block|{
for|for
control|(
name|char
name|e
init|=
name|FIRST_CHAR
init|;
name|e
operator|<=
name|LAST_CHAR
condition|;
name|e
operator|++
control|)
block|{
name|byte
index|[]
name|b
init|=
operator|new
name|byte
index|[]
block|{
operator|(
name|byte
operator|)
name|d
block|,
operator|(
name|byte
operator|)
name|e
block|}
decl_stmt|;
name|Text
name|t
init|=
operator|new
name|Text
argument_list|(
operator|new
name|String
argument_list|(
name|b
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
decl_stmt|;
name|writer
operator|.
name|append
argument_list|(
operator|new
name|HStoreKey
argument_list|(
name|t
argument_list|,
name|t
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|,
operator|new
name|ImmutableBytesWritable
argument_list|(
name|t
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
comment|/**    * Test that our mechanism of writing store files in one region to reference    * store files in other regions works.    * @throws IOException    */
specifier|public
name|void
name|testReference
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Make a store file and write data to it.
name|HStoreFile
name|hsf
init|=
operator|new
name|HStoreFile
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|this
operator|.
name|dir
argument_list|,
operator|new
name|Text
argument_list|(
name|getName
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"colfamily"
argument_list|)
argument_list|,
literal|1234567890L
argument_list|)
decl_stmt|;
name|MapFile
operator|.
name|Writer
name|writer
init|=
name|hsf
operator|.
name|getWriter
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|SequenceFile
operator|.
name|CompressionType
operator|.
name|NONE
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|writeStoreFile
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|MapFile
operator|.
name|Reader
name|reader
init|=
name|hsf
operator|.
name|getReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// Split on a row, not in middle of row.  Midkey returned by reader
comment|// may be in middle of row.  Create new one with empty column and
comment|// timestamp.
name|HStoreKey
name|midkey
init|=
operator|new
name|HStoreKey
argument_list|(
operator|(
operator|(
name|HStoreKey
operator|)
name|reader
operator|.
name|midKey
argument_list|()
operator|)
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|HStoreKey
name|hsk
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|reader
operator|.
name|finalKey
argument_list|(
name|hsk
argument_list|)
expr_stmt|;
name|Text
name|finalKey
init|=
name|hsk
operator|.
name|getRow
argument_list|()
decl_stmt|;
comment|// Make a reference for the bottom half of the just written file.
name|HStoreFile
operator|.
name|Reference
name|reference
init|=
operator|new
name|HStoreFile
operator|.
name|Reference
argument_list|(
name|hsf
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|hsf
operator|.
name|getFileId
argument_list|()
argument_list|,
name|midkey
argument_list|,
name|HStoreFile
operator|.
name|Range
operator|.
name|top
argument_list|)
decl_stmt|;
name|HStoreFile
name|refHsf
init|=
operator|new
name|HStoreFile
argument_list|(
name|this
operator|.
name|conf
argument_list|,
operator|new
name|Path
argument_list|(
name|DIR
argument_list|,
name|getName
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
name|getName
argument_list|()
operator|+
literal|"_reference"
argument_list|)
argument_list|,
name|hsf
operator|.
name|getColFamily
argument_list|()
argument_list|,
literal|456
argument_list|,
name|reference
argument_list|)
decl_stmt|;
comment|// Assert that reference files are written and that we can write and
comment|// read the info reference file at least.
name|refHsf
operator|.
name|writeReferenceFiles
argument_list|(
name|this
operator|.
name|fs
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|this
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|refHsf
operator|.
name|getMapFilePath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|this
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|refHsf
operator|.
name|getInfoFilePath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HStoreFile
operator|.
name|Reference
name|otherReference
init|=
name|HStoreFile
operator|.
name|readSplitInfo
argument_list|(
name|refHsf
operator|.
name|getInfoFilePath
argument_list|()
argument_list|,
name|this
operator|.
name|fs
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|reference
operator|.
name|getRegionName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|otherReference
operator|.
name|getRegionName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|reference
operator|.
name|getFileId
argument_list|()
argument_list|,
name|otherReference
operator|.
name|getFileId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|reference
operator|.
name|getMidkey
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|otherReference
operator|.
name|getMidkey
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now confirm that I can read from the reference and that it only gets
comment|// keys from top half of the file.
name|MapFile
operator|.
name|Reader
name|halfReader
init|=
name|refHsf
operator|.
name|getReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|ImmutableBytesWritable
name|value
init|=
operator|new
name|ImmutableBytesWritable
argument_list|()
decl_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|halfReader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
if|if
condition|(
name|first
condition|)
block|{
name|assertEquals
argument_list|(
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|midkey
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|first
operator|=
literal|false
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|finalKey
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Write a file and then assert that we can read from top and bottom halves    * using two HalfMapFiles.    * @throws Exception    */
specifier|public
name|void
name|testBasicHalfMapFile
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|p
init|=
name|writeMapFile
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|WritableComparable
name|midkey
init|=
name|getMidkey
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|checkHalfMapFile
argument_list|(
name|p
argument_list|,
name|midkey
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check HalfMapFile works even if file we're to go against is smaller than    * the default MapFile interval of 128: i.e. index gets entry every 128     * keys.    * @throws Exception    */
specifier|public
name|void
name|testSmallHalfMapFile
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|p
init|=
name|writeSmallMapFile
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
comment|// I know keys are a-z.  Let the midkey we want to use be 'd'.  See if
comment|// HalfMapFiles work even if size of file is< than default MapFile
comment|// interval.
name|checkHalfMapFile
argument_list|(
name|p
argument_list|,
operator|new
name|HStoreKey
argument_list|(
operator|new
name|Text
argument_list|(
literal|"d"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|WritableComparable
name|getMidkey
parameter_list|(
specifier|final
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
name|MapFile
operator|.
name|Reader
name|reader
init|=
operator|new
name|MapFile
operator|.
name|Reader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|ImmutableBytesWritable
name|value
init|=
operator|new
name|ImmutableBytesWritable
argument_list|()
decl_stmt|;
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|String
name|firstKey
init|=
name|key
operator|.
name|toString
argument_list|()
decl_stmt|;
name|WritableComparable
name|midkey
init|=
name|reader
operator|.
name|midKey
argument_list|()
decl_stmt|;
name|reader
operator|.
name|finalKey
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"First key "
operator|+
name|firstKey
operator|+
literal|", midkey "
operator|+
name|midkey
operator|.
name|toString
argument_list|()
operator|+
literal|", last key "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|midkey
return|;
block|}
specifier|private
name|void
name|checkHalfMapFile
parameter_list|(
specifier|final
name|Path
name|p
parameter_list|,
name|WritableComparable
name|midkey
parameter_list|)
throws|throws
name|IOException
block|{
name|MapFile
operator|.
name|Reader
name|top
init|=
literal|null
decl_stmt|;
name|MapFile
operator|.
name|Reader
name|bottom
init|=
literal|null
decl_stmt|;
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|ImmutableBytesWritable
name|value
init|=
operator|new
name|ImmutableBytesWritable
argument_list|()
decl_stmt|;
name|String
name|previous
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Now make two HalfMapFiles and assert they can read the full backing
comment|// file, one from the top and the other from the bottom.
comment|// Test bottom half first.
name|bottom
operator|=
operator|new
name|HStoreFile
operator|.
name|HalfMapFileReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|HStoreFile
operator|.
name|Range
operator|.
name|bottom
argument_list|,
name|midkey
argument_list|)
expr_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|bottom
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
name|previous
operator|=
name|key
operator|.
name|toString
argument_list|()
expr_stmt|;
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"First in bottom: "
operator|+
name|previous
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|key
operator|.
name|compareTo
argument_list|(
name|midkey
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|previous
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Last in bottom: "
operator|+
name|previous
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Now test reading from the top.
name|top
operator|=
operator|new
name|HStoreFile
operator|.
name|HalfMapFileReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|HStoreFile
operator|.
name|Range
operator|.
name|top
argument_list|,
name|midkey
argument_list|)
expr_stmt|;
name|first
operator|=
literal|true
expr_stmt|;
while|while
condition|(
name|top
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
name|assertTrue
argument_list|(
name|key
operator|.
name|compareTo
argument_list|(
name|midkey
argument_list|)
operator|>=
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|HStoreKey
operator|)
name|midkey
operator|)
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"First in top: "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Last in top: "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|top
operator|.
name|getClosest
argument_list|(
name|midkey
argument_list|,
name|value
argument_list|)
expr_stmt|;
comment|// Assert value is same as key.
name|assertEquals
argument_list|(
operator|new
name|String
argument_list|(
name|value
operator|.
name|get
argument_list|()
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|,
operator|(
operator|(
name|HStoreKey
operator|)
name|midkey
operator|)
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Next test using a midkey that does not exist in the file.
comment|// First, do a key that is< than first key. Ensure splits behave
comment|// properly.
name|WritableComparable
name|badkey
init|=
operator|new
name|HStoreKey
argument_list|(
operator|new
name|Text
argument_list|(
literal|"   "
argument_list|)
argument_list|)
decl_stmt|;
name|bottom
operator|=
operator|new
name|HStoreFile
operator|.
name|HalfMapFileReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|HStoreFile
operator|.
name|Range
operator|.
name|bottom
argument_list|,
name|badkey
argument_list|)
expr_stmt|;
comment|// When badkey is< than the bottom, should return no values.
name|assertFalse
argument_list|(
name|bottom
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now read from the top.
name|top
operator|=
operator|new
name|HStoreFile
operator|.
name|HalfMapFileReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|HStoreFile
operator|.
name|Range
operator|.
name|top
argument_list|,
name|badkey
argument_list|)
expr_stmt|;
name|first
operator|=
literal|true
expr_stmt|;
while|while
condition|(
name|top
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
name|assertTrue
argument_list|(
name|key
operator|.
name|compareTo
argument_list|(
name|badkey
argument_list|)
operator|>=
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"First top when key< bottom: "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|tmp
init|=
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
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
name|tmp
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|tmp
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
literal|'a'
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Last top when key< bottom: "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|tmp
init|=
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
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
name|tmp
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|tmp
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
literal|'z'
argument_list|)
expr_stmt|;
block|}
comment|// Test when badkey is> than last key in file ('||'> 'zz').
name|badkey
operator|=
operator|new
name|HStoreKey
argument_list|(
operator|new
name|Text
argument_list|(
literal|"|||"
argument_list|)
argument_list|)
expr_stmt|;
name|bottom
operator|=
operator|new
name|HStoreFile
operator|.
name|HalfMapFileReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|HStoreFile
operator|.
name|Range
operator|.
name|bottom
argument_list|,
name|badkey
argument_list|)
expr_stmt|;
name|first
operator|=
literal|true
expr_stmt|;
while|while
condition|(
name|bottom
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"First bottom when key> top: "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|tmp
operator|=
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|tmp
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|tmp
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
literal|'a'
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Last bottom when key> top: "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|tmp
operator|=
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|tmp
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|tmp
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|==
literal|'z'
argument_list|)
expr_stmt|;
block|}
comment|// Now look at top. Should not return any values.
name|top
operator|=
operator|new
name|HStoreFile
operator|.
name|HalfMapFileReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|HStoreFile
operator|.
name|Range
operator|.
name|top
argument_list|,
name|badkey
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|top
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|top
operator|!=
literal|null
condition|)
block|{
name|top
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|bottom
operator|!=
literal|null
condition|)
block|{
name|bottom
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|fs
operator|.
name|delete
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Assert HalFMapFile does right thing when midkey does not exist in the    * backing file (its larger or smaller than any of the backing mapfiles keys).    *     * @throws Exception    */
specifier|public
name|void
name|testOutOfRangeMidkeyHalfMapFile
parameter_list|()
throws|throws
name|Exception
block|{
name|MapFile
operator|.
name|Reader
name|top
init|=
literal|null
decl_stmt|;
name|MapFile
operator|.
name|Reader
name|bottom
init|=
literal|null
decl_stmt|;
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|ImmutableBytesWritable
name|value
init|=
operator|new
name|ImmutableBytesWritable
argument_list|()
decl_stmt|;
name|Path
name|p
init|=
name|writeMapFile
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
try|try
block|{
comment|// Test using a midkey that does not exist in the file.
comment|// First, do a key that is< than first key.  Ensure splits behave
comment|// properly.
name|HStoreKey
name|midkey
init|=
operator|new
name|HStoreKey
argument_list|(
operator|new
name|Text
argument_list|(
literal|"   "
argument_list|)
argument_list|)
decl_stmt|;
name|bottom
operator|=
operator|new
name|HStoreFile
operator|.
name|HalfMapFileReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|HStoreFile
operator|.
name|Range
operator|.
name|bottom
argument_list|,
name|midkey
argument_list|)
expr_stmt|;
comment|// When midkey is< than the bottom, should return no values.
name|assertFalse
argument_list|(
name|bottom
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now read from the top.
name|top
operator|=
operator|new
name|HStoreFile
operator|.
name|HalfMapFileReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|HStoreFile
operator|.
name|Range
operator|.
name|top
argument_list|,
name|midkey
argument_list|)
expr_stmt|;
name|boolean
name|first
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|top
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
name|assertTrue
argument_list|(
name|key
operator|.
name|compareTo
argument_list|(
name|midkey
argument_list|)
operator|>=
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"First top when key< bottom: "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"aa"
argument_list|,
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Last top when key< bottom: "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"zz"
argument_list|,
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Test when midkey is> than last key in file ('||'> 'zz').
name|midkey
operator|=
operator|new
name|HStoreKey
argument_list|(
operator|new
name|Text
argument_list|(
literal|"|||"
argument_list|)
argument_list|)
expr_stmt|;
name|bottom
operator|=
operator|new
name|HStoreFile
operator|.
name|HalfMapFileReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|HStoreFile
operator|.
name|Range
operator|.
name|bottom
argument_list|,
name|midkey
argument_list|)
expr_stmt|;
name|first
operator|=
literal|true
expr_stmt|;
while|while
condition|(
name|bottom
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
condition|)
block|{
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"First bottom when key> top: "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"aa"
argument_list|,
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Last bottom when key> top: "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"zz"
argument_list|,
name|key
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now look at top.  Should not return any values.
name|top
operator|=
operator|new
name|HStoreFile
operator|.
name|HalfMapFileReader
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|HStoreFile
operator|.
name|Range
operator|.
name|top
argument_list|,
name|midkey
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|top
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|top
operator|!=
literal|null
condition|)
block|{
name|top
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|bottom
operator|!=
literal|null
condition|)
block|{
name|bottom
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|fs
operator|.
name|delete
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

