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
name|SequenceFile
operator|.
name|Reader
import|;
end_import

begin_comment
comment|/** JUnit test case for HLog */
end_comment

begin_class
specifier|public
class|class
name|TestHLog
extends|extends
name|HBaseTestCase
implements|implements
name|HConstants
block|{
specifier|private
name|Path
name|dir
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
annotation|@
name|Override
specifier|protected
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
name|dir
operator|=
name|getUnitTestdir
argument_list|(
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|dir
argument_list|)
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|dir
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
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
name|fs
operator|.
name|exists
argument_list|(
name|this
operator|.
name|dir
argument_list|)
condition|)
block|{
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|this
operator|.
name|dir
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
block|}
comment|/**    * Just write multiple logs then split.  Before fix for HADOOP-2283, this    * would fail.    * @throws IOException    */
specifier|public
name|void
name|testSplit
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|Text
name|tableName
init|=
operator|new
name|Text
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Text
name|rowName
init|=
name|tableName
decl_stmt|;
name|HLog
name|log
init|=
operator|new
name|HLog
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|this
operator|.
name|dir
argument_list|,
name|this
operator|.
name|conf
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// Add edits for three regions.
try|try
block|{
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
literal|3
condition|;
name|ii
operator|++
control|)
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|3
condition|;
name|j
operator|++
control|)
block|{
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|edit
init|=
operator|new
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|Text
name|column
init|=
operator|new
name|Text
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|j
argument_list|)
argument_list|)
decl_stmt|;
name|edit
operator|.
name|put
argument_list|(
operator|new
name|HStoreKey
argument_list|(
name|rowName
argument_list|,
name|column
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
argument_list|,
name|column
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|log
operator|.
name|append
argument_list|(
operator|new
name|Text
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|,
name|tableName
argument_list|,
name|edit
argument_list|)
expr_stmt|;
block|}
block|}
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
block|}
name|HLog
operator|.
name|splitLog
argument_list|(
name|this
operator|.
name|testDir
argument_list|,
name|this
operator|.
name|dir
argument_list|,
name|this
operator|.
name|fs
argument_list|,
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|log
operator|!=
literal|null
condition|)
block|{
name|log
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @throws IOException    */
specifier|public
name|void
name|testAppend
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|COL_COUNT
init|=
literal|10
decl_stmt|;
specifier|final
name|Text
name|regionName
init|=
operator|new
name|Text
argument_list|(
literal|"regionname"
argument_list|)
decl_stmt|;
specifier|final
name|Text
name|tableName
init|=
operator|new
name|Text
argument_list|(
literal|"tablename"
argument_list|)
decl_stmt|;
specifier|final
name|Text
name|row
init|=
operator|new
name|Text
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
name|Reader
name|reader
init|=
literal|null
decl_stmt|;
name|HLog
name|log
init|=
operator|new
name|HLog
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|,
name|this
operator|.
name|conf
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Write columns named 1, 2, 3, etc. and then values of single byte
comment|// 1, 2, 3...
name|long
name|timestamp
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
name|cols
init|=
operator|new
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|byte
index|[]
argument_list|>
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
name|COL_COUNT
condition|;
name|i
operator|++
control|)
block|{
name|cols
operator|.
name|put
argument_list|(
operator|new
name|HStoreKey
argument_list|(
name|row
argument_list|,
operator|new
name|Text
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|,
name|timestamp
argument_list|)
argument_list|,
operator|new
name|byte
index|[]
block|{
call|(
name|byte
call|)
argument_list|(
name|i
operator|+
literal|'0'
argument_list|)
block|}
argument_list|)
expr_stmt|;
block|}
name|log
operator|.
name|append
argument_list|(
name|regionName
argument_list|,
name|tableName
argument_list|,
name|cols
argument_list|)
expr_stmt|;
name|long
name|logSeqId
init|=
name|log
operator|.
name|startCacheFlush
argument_list|()
decl_stmt|;
name|log
operator|.
name|completeCacheFlush
argument_list|(
name|regionName
argument_list|,
name|tableName
argument_list|,
name|logSeqId
argument_list|)
expr_stmt|;
name|log
operator|.
name|close
argument_list|()
expr_stmt|;
name|Path
name|filename
init|=
name|log
operator|.
name|computeFilename
argument_list|(
name|log
operator|.
name|filenum
operator|-
literal|1
argument_list|)
decl_stmt|;
name|log
operator|=
literal|null
expr_stmt|;
comment|// Now open a reader on the log and assert append worked.
name|reader
operator|=
operator|new
name|SequenceFile
operator|.
name|Reader
argument_list|(
name|fs
argument_list|,
name|filename
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|HLogKey
name|key
init|=
operator|new
name|HLogKey
argument_list|()
decl_stmt|;
name|HLogEdit
name|val
init|=
operator|new
name|HLogEdit
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
name|COL_COUNT
condition|;
name|i
operator|++
control|)
block|{
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|regionName
argument_list|,
name|key
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|key
operator|.
name|getTablename
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|row
argument_list|,
name|key
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|i
operator|+
literal|'0'
argument_list|)
argument_list|,
name|val
operator|.
name|getVal
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|key
operator|+
literal|" "
operator|+
name|val
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
name|reader
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|val
argument_list|)
condition|)
block|{
comment|// Assert only one more row... the meta flushed row.
name|assertEquals
argument_list|(
name|regionName
argument_list|,
name|key
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|tableName
argument_list|,
name|key
operator|.
name|getTablename
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HLog
operator|.
name|METAROW
argument_list|,
name|key
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HLog
operator|.
name|METACOLUMN
argument_list|,
name|val
operator|.
name|getColumn
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|HLogEdit
operator|.
name|completeCacheFlush
operator|.
name|compareTo
argument_list|(
name|val
operator|.
name|getVal
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|key
operator|+
literal|" "
operator|+
name|val
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|log
operator|!=
literal|null
condition|)
block|{
name|log
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|reader
operator|!=
literal|null
condition|)
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

