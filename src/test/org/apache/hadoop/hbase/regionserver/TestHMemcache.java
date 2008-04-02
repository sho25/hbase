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
name|io
operator|.
name|UnsupportedEncodingException
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
name|SortedMap
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
name|io
operator|.
name|Cell
import|;
end_import

begin_comment
comment|/** memcache test case */
end_comment

begin_class
specifier|public
class|class
name|TestHMemcache
extends|extends
name|TestCase
block|{
specifier|private
name|Memcache
name|hmemcache
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ROW_COUNT
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|COLUMNS_COUNT
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_FAMILY
init|=
literal|"column"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|FIRST_ROW
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_VALS
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|CONTENTS_BASIC
init|=
operator|new
name|Text
argument_list|(
literal|"contents:basic"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CONTENTSTR
init|=
literal|"contentstr"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ANCHORNUM
init|=
literal|"anchor:anchornum-"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ANCHORSTR
init|=
literal|"anchorstr"
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
name|hmemcache
operator|=
operator|new
name|Memcache
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws UnsupportedEncodingException    */
specifier|public
name|void
name|testMemcache
parameter_list|()
throws|throws
name|UnsupportedEncodingException
block|{
for|for
control|(
name|int
name|k
init|=
name|FIRST_ROW
init|;
name|k
operator|<=
name|NUM_VALS
condition|;
name|k
operator|++
control|)
block|{
name|Text
name|row
init|=
operator|new
name|Text
argument_list|(
literal|"row_"
operator|+
name|k
argument_list|)
decl_stmt|;
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|(
name|row
argument_list|,
name|CONTENTS_BASIC
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|hmemcache
operator|.
name|add
argument_list|(
name|key
argument_list|,
operator|(
name|CONTENTSTR
operator|+
name|k
operator|)
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|key
operator|=
operator|new
name|HStoreKey
argument_list|(
name|row
argument_list|,
operator|new
name|Text
argument_list|(
name|ANCHORNUM
operator|+
name|k
argument_list|)
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|hmemcache
operator|.
name|add
argument_list|(
name|key
argument_list|,
operator|(
name|ANCHORSTR
operator|+
name|k
operator|)
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Read them back
for|for
control|(
name|int
name|k
init|=
name|FIRST_ROW
init|;
name|k
operator|<=
name|NUM_VALS
condition|;
name|k
operator|++
control|)
block|{
name|List
argument_list|<
name|Cell
argument_list|>
name|results
decl_stmt|;
name|Text
name|row
init|=
operator|new
name|Text
argument_list|(
literal|"row_"
operator|+
name|k
argument_list|)
decl_stmt|;
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|(
name|row
argument_list|,
name|CONTENTS_BASIC
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|results
operator|=
name|hmemcache
operator|.
name|get
argument_list|(
name|key
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"no data for "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|bodystr
init|=
operator|new
name|String
argument_list|(
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
decl_stmt|;
name|String
name|teststr
init|=
name|CONTENTSTR
operator|+
name|k
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Incorrect value for key: ("
operator|+
name|key
operator|.
name|toString
argument_list|()
operator|+
literal|"), expected: '"
operator|+
name|teststr
operator|+
literal|"' got: '"
operator|+
name|bodystr
operator|+
literal|"'"
argument_list|,
name|teststr
operator|.
name|compareTo
argument_list|(
name|bodystr
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|key
operator|=
operator|new
name|HStoreKey
argument_list|(
name|row
argument_list|,
operator|new
name|Text
argument_list|(
name|ANCHORNUM
operator|+
name|k
argument_list|)
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|results
operator|=
name|hmemcache
operator|.
name|get
argument_list|(
name|key
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"no data for "
operator|+
name|key
operator|.
name|toString
argument_list|()
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|bodystr
operator|=
operator|new
name|String
argument_list|(
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
name|teststr
operator|=
name|ANCHORSTR
operator|+
name|k
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Incorrect value for key: ("
operator|+
name|key
operator|.
name|toString
argument_list|()
operator|+
literal|"), expected: '"
operator|+
name|teststr
operator|+
literal|"' got: '"
operator|+
name|bodystr
operator|+
literal|"'"
argument_list|,
name|teststr
operator|.
name|compareTo
argument_list|(
name|bodystr
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Text
name|getRowName
parameter_list|(
specifier|final
name|int
name|index
parameter_list|)
block|{
return|return
operator|new
name|Text
argument_list|(
literal|"row"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|index
argument_list|)
argument_list|)
return|;
block|}
specifier|private
name|Text
name|getColumnName
parameter_list|(
specifier|final
name|int
name|rowIndex
parameter_list|,
specifier|final
name|int
name|colIndex
parameter_list|)
block|{
return|return
operator|new
name|Text
argument_list|(
name|COLUMN_FAMILY
operator|+
literal|":"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|rowIndex
argument_list|)
operator|+
literal|";"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|colIndex
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Adds {@link #ROW_COUNT} rows and {@link #COLUMNS_COUNT}    * @param hmc Instance to add rows to.    */
specifier|private
name|void
name|addRows
parameter_list|(
specifier|final
name|Memcache
name|hmc
parameter_list|)
throws|throws
name|UnsupportedEncodingException
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
name|ROW_COUNT
condition|;
name|i
operator|++
control|)
block|{
name|long
name|timestamp
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
name|COLUMNS_COUNT
condition|;
name|ii
operator|++
control|)
block|{
name|Text
name|k
init|=
name|getColumnName
argument_list|(
name|i
argument_list|,
name|ii
argument_list|)
decl_stmt|;
name|hmc
operator|.
name|add
argument_list|(
operator|new
name|HStoreKey
argument_list|(
name|getRowName
argument_list|(
name|i
argument_list|)
argument_list|,
name|k
argument_list|,
name|timestamp
argument_list|)
argument_list|,
name|k
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|runSnapshot
parameter_list|(
specifier|final
name|Memcache
name|hmc
parameter_list|)
block|{
comment|// Save off old state.
name|int
name|oldHistorySize
init|=
name|hmc
operator|.
name|snapshot
operator|.
name|size
argument_list|()
decl_stmt|;
name|hmc
operator|.
name|snapshot
argument_list|()
expr_stmt|;
comment|// Make some assertions about what just happened.
name|assertTrue
argument_list|(
literal|"History size has not increased"
argument_list|,
name|oldHistorySize
operator|<
name|hmc
operator|.
name|snapshot
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**     * Test memcache snapshots    * @throws IOException    */
specifier|public
name|void
name|testSnapshotting
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|snapshotCount
init|=
literal|5
decl_stmt|;
comment|// Add some rows, run a snapshot. Do it a few times.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|snapshotCount
condition|;
name|i
operator|++
control|)
block|{
name|addRows
argument_list|(
name|this
operator|.
name|hmemcache
argument_list|)
expr_stmt|;
name|runSnapshot
argument_list|(
name|this
operator|.
name|hmemcache
argument_list|)
expr_stmt|;
name|this
operator|.
name|hmemcache
operator|.
name|getSnapshot
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|"History not being cleared"
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|hmemcache
operator|.
name|snapshot
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|isExpectedRowWithoutTimestamps
parameter_list|(
specifier|final
name|int
name|rowIndex
parameter_list|,
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|row
parameter_list|)
throws|throws
name|UnsupportedEncodingException
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Text
name|colname
range|:
name|row
operator|.
name|keySet
argument_list|()
control|)
block|{
name|String
name|expectedColname
init|=
name|getColumnName
argument_list|(
name|rowIndex
argument_list|,
name|i
operator|++
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|colnameStr
init|=
name|colname
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Column name"
argument_list|,
name|colnameStr
argument_list|,
name|expectedColname
argument_list|)
expr_stmt|;
comment|// Value is column name as bytes.  Usually result is
comment|// 100 bytes in size at least. This is the default size
comment|// for BytesWriteable.  For comparison, comvert bytes to
comment|// String and trim to remove trailing null bytes.
name|byte
index|[]
name|value
init|=
name|row
operator|.
name|get
argument_list|(
name|colname
argument_list|)
decl_stmt|;
name|String
name|colvalueStr
init|=
operator|new
name|String
argument_list|(
name|value
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Content"
argument_list|,
name|colnameStr
argument_list|,
name|colvalueStr
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|isExpectedRow
parameter_list|(
specifier|final
name|int
name|rowIndex
parameter_list|,
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|Cell
argument_list|>
name|row
parameter_list|)
throws|throws
name|UnsupportedEncodingException
block|{
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|converted
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
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Text
argument_list|,
name|Cell
argument_list|>
name|entry
range|:
name|row
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|converted
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|isExpectedRowWithoutTimestamps
argument_list|(
name|rowIndex
argument_list|,
name|converted
argument_list|)
expr_stmt|;
block|}
comment|/** Test getFull from memcache    * @throws UnsupportedEncodingException    */
specifier|public
name|void
name|testGetFull
parameter_list|()
throws|throws
name|UnsupportedEncodingException
block|{
name|addRows
argument_list|(
name|this
operator|.
name|hmemcache
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
name|ROW_COUNT
condition|;
name|i
operator|++
control|)
block|{
name|HStoreKey
name|hsk
init|=
operator|new
name|HStoreKey
argument_list|(
name|getRowName
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|Cell
argument_list|>
name|all
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|Long
argument_list|>
name|deletes
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|Long
argument_list|>
argument_list|()
decl_stmt|;
name|this
operator|.
name|hmemcache
operator|.
name|getFull
argument_list|(
name|hsk
argument_list|,
literal|null
argument_list|,
name|deletes
argument_list|,
name|all
argument_list|)
expr_stmt|;
name|isExpectedRow
argument_list|(
name|i
argument_list|,
name|all
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test memcache scanner    * @throws IOException    */
specifier|public
name|void
name|testScanner
parameter_list|()
throws|throws
name|IOException
block|{
name|addRows
argument_list|(
name|this
operator|.
name|hmemcache
argument_list|)
expr_stmt|;
name|long
name|timestamp
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Text
index|[]
name|cols
init|=
operator|new
name|Text
index|[
name|COLUMNS_COUNT
operator|*
name|ROW_COUNT
index|]
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
name|ROW_COUNT
condition|;
name|i
operator|++
control|)
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
name|COLUMNS_COUNT
condition|;
name|ii
operator|++
control|)
block|{
name|cols
index|[
operator|(
name|ii
operator|+
operator|(
name|i
operator|*
name|COLUMNS_COUNT
operator|)
operator|)
index|]
operator|=
name|getColumnName
argument_list|(
name|i
argument_list|,
name|ii
argument_list|)
expr_stmt|;
block|}
block|}
name|HInternalScannerInterface
name|scanner
init|=
name|this
operator|.
name|hmemcache
operator|.
name|getScanner
argument_list|(
name|timestamp
argument_list|,
name|cols
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|)
decl_stmt|;
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
name|results
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
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|scanner
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|results
argument_list|)
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
literal|"Row name"
argument_list|,
name|key
operator|.
name|toString
argument_list|()
operator|.
name|startsWith
argument_list|(
name|getRowName
argument_list|(
name|i
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Count of columns"
argument_list|,
name|COLUMNS_COUNT
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|row
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
name|results
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|row
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|isExpectedRowWithoutTimestamps
argument_list|(
name|i
argument_list|,
name|row
argument_list|)
expr_stmt|;
comment|// Clear out set.  Otherwise row results accumulate.
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
comment|/** For HBASE-528 */
specifier|public
name|void
name|testGetRowKeyAtOrBefore
parameter_list|()
block|{
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
name|t35
init|=
operator|new
name|Text
argument_list|(
literal|"035"
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
name|hmemcache
operator|.
name|add
argument_list|(
name|getHSKForRow
argument_list|(
name|t10
argument_list|)
argument_list|,
literal|"t10 bytes"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|hmemcache
operator|.
name|add
argument_list|(
name|getHSKForRow
argument_list|(
name|t20
argument_list|)
argument_list|,
literal|"t20 bytes"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|hmemcache
operator|.
name|add
argument_list|(
name|getHSKForRow
argument_list|(
name|t30
argument_list|)
argument_list|,
literal|"t30 bytes"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
comment|// write a delete in there to see if things still work ok
name|hmemcache
operator|.
name|add
argument_list|(
name|getHSKForRow
argument_list|(
name|t35
argument_list|)
argument_list|,
name|HLogEdit
operator|.
name|deleteBytes
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|hmemcache
operator|.
name|add
argument_list|(
name|getHSKForRow
argument_list|(
name|t40
argument_list|)
argument_list|,
literal|"t40 bytes"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|SortedMap
argument_list|<
name|HStoreKey
argument_list|,
name|Long
argument_list|>
name|results
init|=
literal|null
decl_stmt|;
comment|// try finding "015"
name|results
operator|=
operator|new
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|Long
argument_list|>
argument_list|()
expr_stmt|;
name|Text
name|t15
init|=
operator|new
name|Text
argument_list|(
literal|"015"
argument_list|)
decl_stmt|;
name|hmemcache
operator|.
name|getRowKeyAtOrBefore
argument_list|(
name|t15
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t10
argument_list|,
name|results
operator|.
name|lastKey
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
comment|// try "020", we should get that row exactly
name|results
operator|=
operator|new
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|Long
argument_list|>
argument_list|()
expr_stmt|;
name|hmemcache
operator|.
name|getRowKeyAtOrBefore
argument_list|(
name|t20
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t20
argument_list|,
name|results
operator|.
name|lastKey
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
comment|// try "038", should skip the deleted "035" and give "030"
name|results
operator|=
operator|new
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|Long
argument_list|>
argument_list|()
expr_stmt|;
name|Text
name|t38
init|=
operator|new
name|Text
argument_list|(
literal|"038"
argument_list|)
decl_stmt|;
name|hmemcache
operator|.
name|getRowKeyAtOrBefore
argument_list|(
name|t38
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t30
argument_list|,
name|results
operator|.
name|lastKey
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
comment|// try "050", should get stuff from "040"
name|results
operator|=
operator|new
name|TreeMap
argument_list|<
name|HStoreKey
argument_list|,
name|Long
argument_list|>
argument_list|()
expr_stmt|;
name|Text
name|t50
init|=
operator|new
name|Text
argument_list|(
literal|"050"
argument_list|)
decl_stmt|;
name|hmemcache
operator|.
name|getRowKeyAtOrBefore
argument_list|(
name|t50
argument_list|,
name|results
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|t40
argument_list|,
name|results
operator|.
name|lastKey
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HStoreKey
name|getHSKForRow
parameter_list|(
name|Text
name|row
parameter_list|)
block|{
return|return
operator|new
name|HStoreKey
argument_list|(
name|row
argument_list|,
operator|new
name|Text
argument_list|(
literal|"test_col:"
argument_list|)
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
return|;
block|}
block|}
end_class

end_unit

