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
name|hbase
operator|.
name|filter
operator|.
name|StopRowFilter
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
name|filter
operator|.
name|WhileMatchRowFilter
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

begin_comment
comment|/**  * {@link TestGet} is a medley of tests of get all done up as a single test.  * This class   */
end_comment

begin_class
specifier|public
class|class
name|TestGet2
extends|extends
name|HBaseTestCase
block|{
specifier|private
name|MiniDFSCluster
name|miniHdfs
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
name|miniHdfs
operator|=
operator|new
name|MiniDFSCluster
argument_list|(
name|this
operator|.
name|conf
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests for HADOOP-2161.    * @throws Exception    */
specifier|public
name|void
name|testGetFull
parameter_list|()
throws|throws
name|Exception
block|{
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
name|HScannerInterface
name|scanner
init|=
literal|null
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
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|COLUMNS
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|addContent
argument_list|(
name|region
argument_list|,
name|COLUMNS
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Find two rows to use doing getFull.
specifier|final
name|Text
name|arbitraryStartRow
init|=
operator|new
name|Text
argument_list|(
literal|"b"
argument_list|)
decl_stmt|;
name|Text
name|actualStartRow
init|=
literal|null
decl_stmt|;
specifier|final
name|Text
name|arbitraryStopRow
init|=
operator|new
name|Text
argument_list|(
literal|"c"
argument_list|)
decl_stmt|;
name|Text
name|actualStopRow
init|=
literal|null
decl_stmt|;
name|Text
index|[]
name|columns
init|=
operator|new
name|Text
index|[]
block|{
operator|new
name|Text
argument_list|(
name|COLFAMILY_NAME1
argument_list|)
block|}
decl_stmt|;
name|scanner
operator|=
name|region
operator|.
name|getScanner
argument_list|(
name|columns
argument_list|,
name|arbitraryStartRow
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
operator|new
name|WhileMatchRowFilter
argument_list|(
operator|new
name|StopRowFilter
argument_list|(
name|arbitraryStopRow
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|value
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
while|while
condition|(
name|scanner
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
name|actualStartRow
operator|==
literal|null
condition|)
block|{
name|actualStartRow
operator|=
operator|new
name|Text
argument_list|(
name|key
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|actualStopRow
operator|=
name|key
operator|.
name|getRow
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Assert I got all out.
name|assertColumnsPresent
argument_list|(
name|region
argument_list|,
name|actualStartRow
argument_list|)
expr_stmt|;
name|assertColumnsPresent
argument_list|(
name|region
argument_list|,
name|actualStopRow
argument_list|)
expr_stmt|;
comment|// Force a flush so store files come into play.
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// Assert I got all out.
name|assertColumnsPresent
argument_list|(
name|region
argument_list|,
name|actualStartRow
argument_list|)
expr_stmt|;
name|assertColumnsPresent
argument_list|(
name|region
argument_list|,
name|actualStopRow
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|scanner
operator|!=
literal|null
condition|)
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
comment|/**    * @throws IOException    */
specifier|public
name|void
name|testGetAtTimestamp
parameter_list|()
throws|throws
name|IOException
block|{
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
name|HRegionIncommon
name|region_incommon
init|=
literal|null
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
name|region_incommon
operator|=
operator|new
name|HRegionIncommon
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|long
name|right_now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|one_second_ago
init|=
name|right_now
operator|-
literal|1000
decl_stmt|;
name|Text
name|t
init|=
operator|new
name|Text
argument_list|(
literal|"test_row"
argument_list|)
decl_stmt|;
name|long
name|lockid
init|=
name|region_incommon
operator|.
name|startBatchUpdate
argument_list|(
name|t
argument_list|)
decl_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|COLUMNS
index|[
literal|0
index|]
argument_list|,
literal|"old text"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|commit
argument_list|(
name|lockid
argument_list|,
name|one_second_ago
argument_list|)
expr_stmt|;
name|lockid
operator|=
name|region_incommon
operator|.
name|startBatchUpdate
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|COLUMNS
index|[
literal|0
index|]
argument_list|,
literal|"new text"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|commit
argument_list|(
name|lockid
argument_list|,
name|right_now
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|t
argument_list|,
name|COLUMNS
index|[
literal|0
index|]
argument_list|,
name|right_now
argument_list|,
literal|"new text"
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|t
argument_list|,
name|COLUMNS
index|[
literal|0
index|]
argument_list|,
name|one_second_ago
argument_list|,
literal|"old text"
argument_list|)
expr_stmt|;
comment|// Force a flush so store files come into play.
name|region_incommon
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|t
argument_list|,
name|COLUMNS
index|[
literal|0
index|]
argument_list|,
name|right_now
argument_list|,
literal|"new text"
argument_list|)
expr_stmt|;
name|assertCellValueEquals
argument_list|(
name|region
argument_list|,
name|t
argument_list|,
name|COLUMNS
index|[
literal|0
index|]
argument_list|,
name|one_second_ago
argument_list|,
literal|"old text"
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
comment|/** For HADOOP-2443 */
specifier|public
name|void
name|testGetClosestRowBefore
parameter_list|()
throws|throws
name|IOException
block|{
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
name|HRegionIncommon
name|region_incommon
init|=
literal|null
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
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
argument_list|,
literal|null
argument_list|,
literal|null
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
name|region_incommon
operator|=
operator|new
name|HRegionIncommon
argument_list|(
name|region
argument_list|)
expr_stmt|;
comment|// set up some test data
name|Text
name|t10
init|=
operator|new
name|Text
argument_list|(
literal|"010"
argument_list|)
decl_stmt|;
name|Text
name|t20
init|=
operator|new
name|Text
argument_list|(
literal|"020"
argument_list|)
decl_stmt|;
name|Text
name|t30
init|=
operator|new
name|Text
argument_list|(
literal|"030"
argument_list|)
decl_stmt|;
name|Text
name|t40
init|=
operator|new
name|Text
argument_list|(
literal|"040"
argument_list|)
decl_stmt|;
name|long
name|lockid
init|=
name|region_incommon
operator|.
name|startBatchUpdate
argument_list|(
name|t10
argument_list|)
decl_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|COLUMNS
index|[
literal|0
index|]
argument_list|,
literal|"t10 bytes"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|commit
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
name|lockid
operator|=
name|region_incommon
operator|.
name|startBatchUpdate
argument_list|(
name|t20
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|COLUMNS
index|[
literal|0
index|]
argument_list|,
literal|"t20 bytes"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|commit
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
name|lockid
operator|=
name|region_incommon
operator|.
name|startBatchUpdate
argument_list|(
name|t30
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|COLUMNS
index|[
literal|0
index|]
argument_list|,
literal|"t30 bytes"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|commit
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
name|lockid
operator|=
name|region_incommon
operator|.
name|startBatchUpdate
argument_list|(
name|t40
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|COLUMNS
index|[
literal|0
index|]
argument_list|,
literal|"t40 bytes"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|region_incommon
operator|.
name|commit
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
comment|// try finding "015"
name|Text
name|t15
init|=
operator|new
name|Text
argument_list|(
literal|"015"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|t15
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|String
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|COLUMNS
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|,
literal|"t10 bytes"
argument_list|)
expr_stmt|;
comment|// try "020", we should get that row exactly
name|results
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|t20
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|String
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|COLUMNS
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|,
literal|"t20 bytes"
argument_list|)
expr_stmt|;
comment|// try "050", should get stuff from "040"
name|Text
name|t50
init|=
operator|new
name|Text
argument_list|(
literal|"050"
argument_list|)
decl_stmt|;
name|results
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|t50
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|String
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|COLUMNS
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|,
literal|"t40 bytes"
argument_list|)
expr_stmt|;
comment|// force a flush
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// try finding "015"
name|results
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|t15
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|String
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|COLUMNS
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|,
literal|"t10 bytes"
argument_list|)
expr_stmt|;
comment|// try "020", we should get that row exactly
name|results
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|t20
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|String
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|COLUMNS
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|,
literal|"t20 bytes"
argument_list|)
expr_stmt|;
comment|// try "050", should get stuff from "040"
name|results
operator|=
name|region
operator|.
name|getClosestRowBefore
argument_list|(
name|t50
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|String
argument_list|(
name|results
operator|.
name|get
argument_list|(
name|COLUMNS
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|,
literal|"t40 bytes"
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
specifier|private
name|void
name|assertCellValueEquals
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|,
specifier|final
name|Text
name|row
parameter_list|,
specifier|final
name|Text
name|column
parameter_list|,
specifier|final
name|long
name|timestamp
parameter_list|,
specifier|final
name|String
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|result
init|=
name|region
operator|.
name|getFull
argument_list|(
name|row
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"cell value at a given timestamp"
argument_list|,
operator|new
name|String
argument_list|(
name|result
operator|.
name|get
argument_list|(
name|column
argument_list|)
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertColumnsPresent
parameter_list|(
specifier|final
name|HRegion
name|r
parameter_list|,
specifier|final
name|Text
name|row
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|result
init|=
name|r
operator|.
name|getFull
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|int
name|columnCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|e
range|:
name|result
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|columnCount
operator|++
expr_stmt|;
name|String
name|column
init|=
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|boolean
name|legitColumn
init|=
literal|false
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
name|COLUMNS
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// Assert value is same as row.  This is 'nature' of the data added.
name|assertTrue
argument_list|(
name|row
operator|.
name|equals
argument_list|(
operator|new
name|Text
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|COLUMNS
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
operator|new
name|Text
argument_list|(
name|column
argument_list|)
argument_list|)
condition|)
block|{
name|legitColumn
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
literal|"is legit column name"
argument_list|,
name|legitColumn
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|"count of columns"
argument_list|,
name|columnCount
argument_list|,
name|COLUMNS
operator|.
name|length
argument_list|)
expr_stmt|;
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
name|miniHdfs
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|miniHdfs
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
block|}
end_class

end_unit

