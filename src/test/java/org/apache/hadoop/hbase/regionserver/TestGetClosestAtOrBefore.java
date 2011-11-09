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
name|*
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
name|Delete
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
name|Writables
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
comment|/**  * {@link TestGet} is a medley of tests of get all done up as a single test.  * This class  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestGetClosestAtOrBefore
extends|extends
name|HBaseTestCase
block|{
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
name|TestGetClosestAtOrBefore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|T00
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"000"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|T10
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"010"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|T11
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"011"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|T12
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"012"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|T20
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"020"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|T30
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"030"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|T31
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"031"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|T35
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"035"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|T40
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"040"
argument_list|)
decl_stmt|;
specifier|public
name|void
name|testUsingMetaAndBinary
parameter_list|()
throws|throws
name|IOException
block|{
name|FileSystem
name|filesystem
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|rootdir
init|=
name|testDir
decl_stmt|;
comment|// Up flush size else we bind up when we use default catalog flush of 16k.
name|HTableDescriptor
operator|.
name|META_TABLEDESC
operator|.
name|setMemStoreFlushSize
argument_list|(
literal|64
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|HRegion
name|mr
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|rootdir
argument_list|,
name|this
operator|.
name|conf
argument_list|,
name|HTableDescriptor
operator|.
name|META_TABLEDESC
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Write rows for three tables 'A', 'B', and 'C'.
for|for
control|(
name|char
name|c
init|=
literal|'A'
init|;
name|c
operator|<
literal|'D'
condition|;
name|c
operator|++
control|)
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|""
operator|+
name|c
argument_list|)
decl_stmt|;
specifier|final
name|int
name|last
init|=
literal|128
decl_stmt|;
specifier|final
name|int
name|interval
init|=
literal|2
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|last
condition|;
name|i
operator|+=
name|interval
control|)
block|{
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
name|i
operator|==
literal|0
condition|?
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
else|:
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|)
argument_list|,
name|i
operator|==
name|last
condition|?
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
else|:
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|(
name|byte
operator|)
name|i
operator|+
name|interval
argument_list|)
argument_list|)
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|hri
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|put
operator|.
name|setWriteToWAL
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|hri
argument_list|)
argument_list|)
expr_stmt|;
name|mr
operator|.
name|put
argument_list|(
name|put
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
name|InternalScanner
name|s
init|=
name|mr
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|s
operator|.
name|next
argument_list|(
name|keys
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|keys
argument_list|)
expr_stmt|;
name|keys
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|44
argument_list|,
literal|44
argument_list|)
expr_stmt|;
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|45
argument_list|,
literal|44
argument_list|)
expr_stmt|;
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|46
argument_list|,
literal|46
argument_list|)
expr_stmt|;
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|43
argument_list|,
literal|42
argument_list|)
expr_stmt|;
name|mr
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|44
argument_list|,
literal|44
argument_list|)
expr_stmt|;
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|45
argument_list|,
literal|44
argument_list|)
expr_stmt|;
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|46
argument_list|,
literal|46
argument_list|)
expr_stmt|;
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|43
argument_list|,
literal|42
argument_list|)
expr_stmt|;
comment|// Now delete 'C' and make sure I don't get entries from 'B'.
name|byte
index|[]
name|firstRowInC
init|=
name|HRegionInfo
operator|.
name|createRegionName
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
operator|+
literal|'C'
argument_list|)
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|HConstants
operator|.
name|ZEROES
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|firstRowInC
argument_list|)
decl_stmt|;
name|s
operator|=
name|mr
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
try|try
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|s
operator|.
name|next
argument_list|(
name|keys
argument_list|)
condition|)
block|{
name|mr
operator|.
name|delete
argument_list|(
operator|new
name|Delete
argument_list|(
name|keys
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|keys
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Assert we get null back (pass -1).
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|44
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|45
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|46
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|43
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|mr
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|44
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|45
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|46
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|findRow
argument_list|(
name|mr
argument_list|,
literal|'C'
argument_list|,
literal|43
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|mr
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|mr
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|mr
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/*    * @param mr    * @param table    * @param rowToFind    * @param answer Pass -1 if we're not to find anything.    * @return Row found.    * @throws IOException    */
specifier|private
name|byte
index|[]
name|findRow
parameter_list|(
specifier|final
name|HRegion
name|mr
parameter_list|,
specifier|final
name|char
name|table
parameter_list|,
specifier|final
name|int
name|rowToFind
parameter_list|,
specifier|final
name|int
name|answer
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|tableb
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|""
operator|+
name|table
argument_list|)
decl_stmt|;
comment|// Find the row.
name|byte
index|[]
name|tofindBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|(
name|short
operator|)
name|rowToFind
argument_list|)
decl_stmt|;
name|byte
index|[]
name|metaKey
init|=
name|HRegionInfo
operator|.
name|createRegionName
argument_list|(
name|tableb
argument_list|,
name|tofindBytes
argument_list|,
name|HConstants
operator|.
name|NINES
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"find="
operator|+
operator|new
name|String
argument_list|(
name|metaKey
argument_list|)
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|mr
operator|.
name|getClosestRowBefore
argument_list|(
name|metaKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|answer
operator|==
operator|-
literal|1
condition|)
block|{
name|assertNull
argument_list|(
name|r
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|(
name|short
operator|)
name|answer
argument_list|)
argument_list|,
name|extractRowFromMetaRow
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
return|return
name|r
operator|.
name|getRow
argument_list|()
return|;
block|}
specifier|private
name|byte
index|[]
name|extractRowFromMetaRow
parameter_list|(
specifier|final
name|byte
index|[]
name|b
parameter_list|)
block|{
name|int
name|firstDelimiter
init|=
name|KeyValue
operator|.
name|getDelimiter
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
argument_list|,
name|HRegionInfo
operator|.
name|DELIMITER
argument_list|)
decl_stmt|;
name|int
name|lastDelimiter
init|=
name|KeyValue
operator|.
name|getDelimiterInReverse
argument_list|(
name|b
argument_list|,
literal|0
argument_list|,
name|b
operator|.
name|length
argument_list|,
name|HRegionInfo
operator|.
name|DELIMITER
argument_list|)
decl_stmt|;
name|int
name|length
init|=
name|lastDelimiter
operator|-
name|firstDelimiter
operator|-
literal|1
decl_stmt|;
name|byte
index|[]
name|row
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|b
argument_list|,
name|firstDelimiter
operator|+
literal|1
argument_list|,
name|row
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|row
return|;
block|}
comment|/**    * Test file of multiple deletes and with deletes as final key.    * @see<a href="https://issues.apache.org/jira/browse/HBASE-751">HBASE-751</a>    */
specifier|public
name|void
name|testGetClosestRowBefore3
parameter_list|()
throws|throws
name|IOException
block|{
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|c0
init|=
name|COLUMNS
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|c1
init|=
name|COLUMNS
index|[
literal|1
index|]
decl_stmt|;
try|try
block|{
name|HTableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|region
operator|=
name|createNewHRegion
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|T00
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T00
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T10
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T10
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T20
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T20
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T20
argument_list|,
name|c0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T20
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|T20
argument_list|)
decl_stmt|;
name|d
operator|.
name|deleteColumn
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|region
operator|.
name|delete
argument_list|(
name|d
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T20
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T10
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T30
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T30
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T30
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T30
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Delete
argument_list|(
name|T30
argument_list|)
expr_stmt|;
name|d
operator|.
name|deleteColumn
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|region
operator|.
name|delete
argument_list|(
name|d
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T30
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T10
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T31
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T10
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// try finding "010" after flush
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T30
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T10
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T31
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T10
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Put into a different column family.  Should make it so I still get t10
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T20
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c1
argument_list|,
name|c1
argument_list|,
name|T20
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T30
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T10
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T31
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T10
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T30
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T10
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T31
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T10
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now try combo of memcache and mapfiles.  Delete the t20 COLUMS[1]
comment|// in memory; make sure we get back t10 again.
name|d
operator|=
operator|new
name|Delete
argument_list|(
name|T20
argument_list|)
expr_stmt|;
name|d
operator|.
name|deleteColumn
argument_list|(
name|c1
argument_list|,
name|c1
argument_list|)
expr_stmt|;
name|region
operator|.
name|delete
argument_list|(
name|d
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T30
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T10
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Ask for a value off the end of the file.  Should return t10.
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T31
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T10
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T31
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T10
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Ok.  Let the candidate come out of hfile but have delete of
comment|// the candidate be in memory.
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T11
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T11
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Delete
argument_list|(
name|T10
argument_list|)
expr_stmt|;
name|d
operator|.
name|deleteColumn
argument_list|(
name|c1
argument_list|,
name|c1
argument_list|)
expr_stmt|;
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T12
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T11
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|region
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/** For HBASE-694 */
specifier|public
name|void
name|testGetClosestRowBefore2
parameter_list|()
throws|throws
name|IOException
block|{
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|c0
init|=
name|COLUMNS
index|[
literal|0
index|]
decl_stmt|;
try|try
block|{
name|HTableDescriptor
name|htd
init|=
name|createTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|region
operator|=
name|createNewHRegion
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|T10
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T10
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T30
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T30
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T40
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T40
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// try finding "035"
name|Result
name|r
init|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T35
argument_list|,
name|c0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T30
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// try finding "035"
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T35
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T30
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|T20
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|c0
argument_list|,
name|c0
argument_list|,
name|T20
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// try finding "035"
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T35
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T30
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// try finding "035"
name|r
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|T35
argument_list|,
name|c0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|T30
argument_list|,
name|r
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|region
operator|.
name|getLog
argument_list|()
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

