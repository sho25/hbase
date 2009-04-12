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
operator|.
name|regionserver
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
name|HStoreKey
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
name|Reference
operator|.
name|Range
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
name|io
operator|.
name|hfile
operator|.
name|HFileScanner
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
name|hdfs
operator|.
name|MiniDFSCluster
import|;
end_import

begin_comment
comment|/**  * Test HStoreFile  */
end_comment

begin_class
specifier|public
class|class
name|TestStoreFile
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
name|TestStoreFile
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|MiniDFSCluster
name|cluster
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
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
comment|// Set the hbase.rootdir to be the home directory in mini dfs.
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|,
name|this
operator|.
name|cluster
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getHomeDirectory
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|shutdownDfs
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|shutdownDfs
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
comment|// ReflectionUtils.printThreadInfo(new PrintWriter(System.out),
comment|//  "Temporary end-of-test thread dump debugging HADOOP-2040: " + getName());
block|}
comment|/**    * Write a file and then assert that we can read from top and bottom halves    * using two HalfMapFiles.    * @throws Exception    */
specifier|public
name|void
name|testBasicHalfMapFile
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Make up a directory hierarchy that has a regiondir and familyname.
name|HFile
operator|.
name|Writer
name|writer
init|=
name|StoreFile
operator|.
name|getWriter
argument_list|(
name|this
operator|.
name|fs
argument_list|,
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|this
operator|.
name|testDir
argument_list|,
literal|"regionname"
argument_list|)
argument_list|,
literal|"familyname"
argument_list|)
argument_list|,
literal|2
operator|*
literal|1024
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|writeStoreFile
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|checkHalfHFile
argument_list|(
operator|new
name|StoreFile
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|writer
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/*    * Writes HStoreKey and ImmutableBytes data to passed writer and    * then closes it.    * @param writer    * @throws IOException    */
specifier|private
name|void
name|writeStoreFile
parameter_list|(
specifier|final
name|HFile
operator|.
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|byte
index|[]
name|column
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|getName
argument_list|()
operator|+
name|KeyValue
operator|.
name|COLUMN_FAMILY_DELIMITER
operator|+
name|getName
argument_list|()
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
name|writer
operator|.
name|append
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|b
argument_list|,
name|column
argument_list|,
name|now
argument_list|,
name|b
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
name|Path
name|storedir
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|this
operator|.
name|testDir
argument_list|,
literal|"regionname"
argument_list|)
argument_list|,
literal|"familyname"
argument_list|)
decl_stmt|;
name|Path
name|dir
init|=
operator|new
name|Path
argument_list|(
name|storedir
argument_list|,
literal|"1234567890"
argument_list|)
decl_stmt|;
comment|// Make a store file and write data to it.
name|HFile
operator|.
name|Writer
name|writer
init|=
name|StoreFile
operator|.
name|getWriter
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|dir
argument_list|,
literal|8
operator|*
literal|1024
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|writeStoreFile
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|StoreFile
name|hsf
init|=
operator|new
name|StoreFile
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|writer
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
name|HFile
operator|.
name|Reader
name|reader
init|=
name|hsf
operator|.
name|getReader
argument_list|()
decl_stmt|;
comment|// Split on a row, not in middle of row.  Midkey returned by reader
comment|// may be in middle of row.  Create new one with empty column and
comment|// timestamp.
name|HStoreKey
name|hsk
init|=
name|HStoreKey
operator|.
name|create
argument_list|(
name|reader
operator|.
name|midkey
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|midkey
init|=
name|hsk
operator|.
name|getRow
argument_list|()
decl_stmt|;
name|hsk
operator|=
name|HStoreKey
operator|.
name|create
argument_list|(
name|reader
operator|.
name|getLastKey
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|finalKey
init|=
name|hsk
operator|.
name|getRow
argument_list|()
decl_stmt|;
comment|// Make a reference
name|Path
name|refPath
init|=
name|StoreFile
operator|.
name|split
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|,
name|hsf
argument_list|,
name|reader
operator|.
name|midkey
argument_list|()
argument_list|,
name|Range
operator|.
name|top
argument_list|)
decl_stmt|;
name|StoreFile
name|refHsf
init|=
operator|new
name|StoreFile
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|refPath
argument_list|)
decl_stmt|;
comment|// Now confirm that I can read from the reference and that it only gets
comment|// keys from top half of the file.
name|HFileScanner
name|s
init|=
name|refHsf
operator|.
name|getReader
argument_list|()
operator|.
name|getScanner
argument_list|()
decl_stmt|;
for|for
control|(
name|boolean
name|first
init|=
literal|true
init|;
operator|(
operator|!
name|s
operator|.
name|isSeeked
argument_list|()
operator|&&
name|s
operator|.
name|seekTo
argument_list|()
operator|)
operator|||
name|s
operator|.
name|next
argument_list|()
condition|;
control|)
block|{
name|ByteBuffer
name|bb
init|=
name|s
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|hsk
operator|=
name|HStoreKey
operator|.
name|create
argument_list|(
name|bb
operator|.
name|array
argument_list|()
argument_list|,
name|bb
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|bb
operator|.
name|limit
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|first
condition|)
block|{
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|hsk
operator|.
name|getRow
argument_list|()
argument_list|,
name|midkey
argument_list|)
argument_list|)
expr_stmt|;
name|first
operator|=
literal|false
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|hsk
operator|.
name|getRow
argument_list|()
argument_list|,
name|finalKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|checkHalfHFile
parameter_list|(
specifier|final
name|StoreFile
name|f
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|midkey
init|=
name|f
operator|.
name|getReader
argument_list|()
operator|.
name|midkey
argument_list|()
decl_stmt|;
comment|// Create top split.
name|Path
name|topDir
init|=
name|Store
operator|.
name|getStoreHomedir
argument_list|(
name|this
operator|.
name|testDir
argument_list|,
literal|1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|f
operator|.
name|getPath
argument_list|()
operator|.
name|getParent
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|topDir
argument_list|)
condition|)
block|{
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|topDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|Path
name|topPath
init|=
name|StoreFile
operator|.
name|split
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|topDir
argument_list|,
name|f
argument_list|,
name|midkey
argument_list|,
name|Range
operator|.
name|top
argument_list|)
decl_stmt|;
comment|// Create bottom split.
name|Path
name|bottomDir
init|=
name|Store
operator|.
name|getStoreHomedir
argument_list|(
name|this
operator|.
name|testDir
argument_list|,
literal|2
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|f
operator|.
name|getPath
argument_list|()
operator|.
name|getParent
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|fs
operator|.
name|exists
argument_list|(
name|bottomDir
argument_list|)
condition|)
block|{
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|bottomDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|Path
name|bottomPath
init|=
name|StoreFile
operator|.
name|split
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|bottomDir
argument_list|,
name|f
argument_list|,
name|midkey
argument_list|,
name|Range
operator|.
name|bottom
argument_list|)
decl_stmt|;
comment|// Make readers on top and bottom.
name|HFile
operator|.
name|Reader
name|top
init|=
operator|new
name|StoreFile
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|topPath
argument_list|)
operator|.
name|getReader
argument_list|()
decl_stmt|;
name|HFile
operator|.
name|Reader
name|bottom
init|=
operator|new
name|StoreFile
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|bottomPath
argument_list|)
operator|.
name|getReader
argument_list|()
decl_stmt|;
name|ByteBuffer
name|previous
init|=
literal|null
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Midkey: "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|midkey
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|midkeyBytes
init|=
operator|new
name|HStoreKey
argument_list|(
name|midkey
argument_list|)
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|ByteBuffer
name|bbMidkeyBytes
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|midkeyBytes
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Now make two HalfMapFiles and assert they can read the full backing
comment|// file, one from the top and the other from the bottom.
comment|// Test bottom half first.
comment|// Now test reading from the top.
name|boolean
name|first
init|=
literal|true
decl_stmt|;
name|ByteBuffer
name|key
init|=
literal|null
decl_stmt|;
name|HFileScanner
name|topScanner
init|=
name|top
operator|.
name|getScanner
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
operator|!
name|topScanner
operator|.
name|isSeeked
argument_list|()
operator|&&
name|topScanner
operator|.
name|seekTo
argument_list|()
operator|)
operator|||
operator|(
name|topScanner
operator|.
name|isSeeked
argument_list|()
operator|&&
name|topScanner
operator|.
name|next
argument_list|()
operator|)
condition|)
block|{
name|key
operator|=
name|topScanner
operator|.
name|getKey
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|topScanner
operator|.
name|getReader
argument_list|()
operator|.
name|getComparator
argument_list|()
operator|.
name|compare
argument_list|(
name|key
operator|.
name|array
argument_list|()
argument_list|,
name|key
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|key
operator|.
name|limit
argument_list|()
argument_list|,
name|midkeyBytes
argument_list|,
literal|0
argument_list|,
name|midkeyBytes
operator|.
name|length
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
literal|"First in top: "
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
argument_list|)
expr_stmt|;
name|first
operator|=
literal|true
expr_stmt|;
name|HFileScanner
name|bottomScanner
init|=
name|bottom
operator|.
name|getScanner
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
operator|!
name|bottomScanner
operator|.
name|isSeeked
argument_list|()
operator|&&
name|bottomScanner
operator|.
name|seekTo
argument_list|()
operator|)
operator|||
name|bottomScanner
operator|.
name|next
argument_list|()
condition|)
block|{
name|previous
operator|=
name|bottomScanner
operator|.
name|getKey
argument_list|()
expr_stmt|;
name|key
operator|=
name|bottomScanner
operator|.
name|getKey
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
name|Bytes
operator|.
name|toString
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|previous
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|key
operator|.
name|compareTo
argument_list|(
name|bbMidkeyBytes
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
name|Bytes
operator|.
name|toString
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|previous
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Remove references.
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|topPath
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|bottomPath
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Next test using a midkey that does not exist in the file.
comment|// First, do a key that is< than first key. Ensure splits behave
comment|// properly.
name|byte
index|[]
name|badmidkey
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"  ."
argument_list|)
decl_stmt|;
name|topPath
operator|=
name|StoreFile
operator|.
name|split
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|topDir
argument_list|,
name|f
argument_list|,
name|badmidkey
argument_list|,
name|Range
operator|.
name|top
argument_list|)
expr_stmt|;
name|bottomPath
operator|=
name|StoreFile
operator|.
name|split
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|bottomDir
argument_list|,
name|f
argument_list|,
name|badmidkey
argument_list|,
name|Range
operator|.
name|bottom
argument_list|)
expr_stmt|;
name|top
operator|=
operator|new
name|StoreFile
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|topPath
argument_list|)
operator|.
name|getReader
argument_list|()
expr_stmt|;
name|bottom
operator|=
operator|new
name|StoreFile
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|bottomPath
argument_list|)
operator|.
name|getReader
argument_list|()
expr_stmt|;
name|bottomScanner
operator|=
name|bottom
operator|.
name|getScanner
argument_list|()
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|(
operator|!
name|bottomScanner
operator|.
name|isSeeked
argument_list|()
operator|&&
name|bottomScanner
operator|.
name|seekTo
argument_list|()
operator|)
operator|||
name|bottomScanner
operator|.
name|next
argument_list|()
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
comment|// When badkey is< than the bottom, should return no values.
name|assertTrue
argument_list|(
name|count
operator|==
literal|0
argument_list|)
expr_stmt|;
comment|// Now read from the top.
name|first
operator|=
literal|true
expr_stmt|;
name|topScanner
operator|=
name|top
operator|.
name|getScanner
argument_list|()
expr_stmt|;
while|while
condition|(
operator|(
operator|!
name|topScanner
operator|.
name|isSeeked
argument_list|()
operator|&&
name|topScanner
operator|.
name|seekTo
argument_list|()
operator|)
operator|||
name|topScanner
operator|.
name|next
argument_list|()
condition|)
block|{
name|key
operator|=
name|topScanner
operator|.
name|getKey
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|topScanner
operator|.
name|getReader
argument_list|()
operator|.
name|getComparator
argument_list|()
operator|.
name|compare
argument_list|(
name|key
operator|.
name|array
argument_list|()
argument_list|,
name|key
operator|.
name|arrayOffset
argument_list|()
argument_list|,
name|key
operator|.
name|limit
argument_list|()
argument_list|,
name|badmidkey
argument_list|,
literal|0
argument_list|,
name|badmidkey
operator|.
name|length
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
name|first
operator|=
literal|false
expr_stmt|;
name|HStoreKey
name|keyhsk
init|=
name|HStoreKey
operator|.
name|create
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"First top when key< bottom: "
operator|+
name|keyhsk
argument_list|)
expr_stmt|;
name|String
name|tmp
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|keyhsk
operator|.
name|getRow
argument_list|()
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
name|HStoreKey
name|keyhsk
init|=
name|HStoreKey
operator|.
name|create
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Last top when key< bottom: "
operator|+
name|keyhsk
argument_list|)
expr_stmt|;
name|String
name|tmp
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|keyhsk
operator|.
name|getRow
argument_list|()
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
comment|// Remove references.
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|topPath
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|bottomPath
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Test when badkey is> than last key in file ('||'> 'zz').
name|badmidkey
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"|||"
argument_list|)
expr_stmt|;
name|topPath
operator|=
name|StoreFile
operator|.
name|split
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|topDir
argument_list|,
name|f
argument_list|,
name|badmidkey
argument_list|,
name|Range
operator|.
name|top
argument_list|)
expr_stmt|;
name|bottomPath
operator|=
name|StoreFile
operator|.
name|split
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|bottomDir
argument_list|,
name|f
argument_list|,
name|badmidkey
argument_list|,
name|Range
operator|.
name|bottom
argument_list|)
expr_stmt|;
name|top
operator|=
operator|new
name|StoreFile
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|topPath
argument_list|)
operator|.
name|getReader
argument_list|()
expr_stmt|;
name|bottom
operator|=
operator|new
name|StoreFile
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|bottomPath
argument_list|)
operator|.
name|getReader
argument_list|()
expr_stmt|;
name|first
operator|=
literal|true
expr_stmt|;
name|bottomScanner
operator|=
name|bottom
operator|.
name|getScanner
argument_list|()
expr_stmt|;
while|while
condition|(
operator|(
operator|!
name|bottomScanner
operator|.
name|isSeeked
argument_list|()
operator|&&
name|bottomScanner
operator|.
name|seekTo
argument_list|()
operator|)
operator|||
name|bottomScanner
operator|.
name|next
argument_list|()
condition|)
block|{
name|key
operator|=
name|bottomScanner
operator|.
name|getKey
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
name|keyhsk
operator|=
name|HStoreKey
operator|.
name|create
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"First bottom when key> top: "
operator|+
name|keyhsk
argument_list|)
expr_stmt|;
name|tmp
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|keyhsk
operator|.
name|getRow
argument_list|()
argument_list|)
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
name|keyhsk
operator|=
name|HStoreKey
operator|.
name|create
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Last bottom when key> top: "
operator|+
name|keyhsk
argument_list|)
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
name|Bytes
operator|.
name|toString
argument_list|(
name|keyhsk
operator|.
name|getRow
argument_list|()
argument_list|)
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
name|count
operator|=
literal|0
expr_stmt|;
name|topScanner
operator|=
name|top
operator|.
name|getScanner
argument_list|()
expr_stmt|;
while|while
condition|(
operator|(
operator|!
name|topScanner
operator|.
name|isSeeked
argument_list|()
operator|&&
name|topScanner
operator|.
name|seekTo
argument_list|()
operator|)
operator|||
operator|(
name|topScanner
operator|.
name|isSeeked
argument_list|()
operator|&&
name|topScanner
operator|.
name|next
argument_list|()
operator|)
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
comment|// When badkey is< than the bottom, should return no values.
name|assertTrue
argument_list|(
name|count
operator|==
literal|0
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
name|f
operator|.
name|getPath
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

