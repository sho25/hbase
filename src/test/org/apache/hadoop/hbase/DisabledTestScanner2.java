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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
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
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|filter
operator|.
name|RegExpRowFilter
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
name|RowFilterInterface
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
name|RowFilterSet
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
name|hbase
operator|.
name|io
operator|.
name|HbaseMapWritable
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
name|Writable
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
name|HTable
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
name|HBaseAdmin
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
name|HRegion
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
name|ipc
operator|.
name|HRegionInterface
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
name|RowResult
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
name|BatchUpdate
import|;
end_import

begin_comment
comment|/**  * Additional scanner tests.  * {@link TestScanner} does a custom setup/takedown not conducive  * to addition of extra scanning tests.  *  *<p>Temporarily disabled until hudson stabilizes again.  * @see TestScanner  */
end_comment

begin_class
specifier|public
class|class
name|DisabledTestScanner2
extends|extends
name|HBaseClusterTestCase
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|char
name|FIRST_ROWKEY
init|=
literal|'a'
decl_stmt|;
specifier|final
name|char
name|FIRST_BAD_RANGE_ROWKEY
init|=
literal|'j'
decl_stmt|;
specifier|final
name|char
name|LAST_BAD_RANGE_ROWKEY
init|=
literal|'q'
decl_stmt|;
specifier|final
name|char
name|LAST_ROWKEY
init|=
literal|'z'
decl_stmt|;
specifier|final
name|char
name|FIRST_COLKEY
init|=
literal|'0'
decl_stmt|;
specifier|final
name|char
name|LAST_COLKEY
init|=
literal|'3'
decl_stmt|;
specifier|static
name|byte
index|[]
name|GOOD_BYTES
init|=
literal|null
decl_stmt|;
specifier|static
name|byte
index|[]
name|BAD_BYTES
init|=
literal|null
decl_stmt|;
static|static
block|{
try|try
block|{
name|GOOD_BYTES
operator|=
literal|"goodstuff"
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
name|BAD_BYTES
operator|=
literal|"badstuff"
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Test for HADOOP-2467 fix.  If scanning more than one column family,    * filters such as the {@line WhileMatchRowFilter} could prematurely    * shutdown scanning if one of the stores ran started returned filter = true.    * @throws MasterNotRunningException    * @throws IOException    */
specifier|public
name|void
name|testScanningMultipleFamiliesOfDifferentVintage
parameter_list|()
throws|throws
name|MasterNotRunningException
throws|,
name|IOException
block|{
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
index|[]
name|families
init|=
name|createTable
argument_list|(
operator|new
name|HBaseAdmin
argument_list|(
name|this
operator|.
name|conf
argument_list|)
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|HScannerInterface
name|scanner
init|=
literal|null
decl_stmt|;
try|try
block|{
name|long
name|time
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Current time "
operator|+
name|time
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
name|families
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|byte
index|[]
name|lastKey
init|=
operator|new
name|byte
index|[]
block|{
literal|'a'
block|,
literal|'a'
block|,
call|(
name|byte
call|)
argument_list|(
literal|'b'
operator|+
name|i
argument_list|)
block|}
decl_stmt|;
name|Incommon
name|inc
init|=
operator|new
name|HTableIncommon
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|addContent
argument_list|(
name|inc
argument_list|,
name|families
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
argument_list|,
name|START_KEY_BYTES
argument_list|,
operator|new
name|Text
argument_list|(
name|lastKey
argument_list|)
argument_list|,
name|time
operator|+
operator|(
literal|1000
operator|*
name|i
operator|)
argument_list|)
expr_stmt|;
comment|// Add in to the first store a record that is in excess of the stop
comment|// row specified below setting up the scanner filter.  Add 'bbb'.
comment|// Use a stop filter of 'aad'.  The store scanner going to 'bbb' was
comment|// flipping the switch in StopRowFilter stopping us returning all
comment|// of the rest of the other store content.
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|BatchUpdate
name|batchUpdate
init|=
operator|new
name|BatchUpdate
argument_list|(
operator|new
name|Text
argument_list|(
literal|"bbb"
argument_list|)
argument_list|)
decl_stmt|;
name|batchUpdate
operator|.
name|put
argument_list|(
name|families
index|[
literal|0
index|]
argument_list|,
literal|"bbb"
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
name|inc
operator|.
name|commit
argument_list|(
name|batchUpdate
argument_list|)
expr_stmt|;
block|}
block|}
name|RowFilterInterface
name|f
init|=
operator|new
name|WhileMatchRowFilter
argument_list|(
operator|new
name|StopRowFilter
argument_list|(
operator|new
name|Text
argument_list|(
literal|"aad"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|scanner
operator|=
name|table
operator|.
name|obtainScanner
argument_list|(
name|families
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|f
argument_list|)
expr_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HStoreKey
argument_list|,
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|e
range|:
name|scanner
control|)
block|{
name|count
operator|++
expr_stmt|;
block|}
comment|// Should get back 3 rows: aaa, aab, and aac.
name|assertEquals
argument_list|(
name|count
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * @throws Exception    */
specifier|public
name|void
name|testStopRow
parameter_list|()
throws|throws
name|Exception
block|{
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
name|createTable
argument_list|(
operator|new
name|HBaseAdmin
argument_list|(
name|this
operator|.
name|conf
argument_list|)
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
specifier|final
name|String
name|lastKey
init|=
literal|"aac"
decl_stmt|;
name|addContent
argument_list|(
operator|new
name|HTableIncommon
argument_list|(
name|table
argument_list|)
argument_list|,
name|FIRST_COLKEY
operator|+
literal|":"
argument_list|)
expr_stmt|;
name|HScannerInterface
name|scanner
init|=
name|table
operator|.
name|obtainScanner
argument_list|(
operator|new
name|Text
index|[]
block|{
operator|new
name|Text
argument_list|(
name|FIRST_COLKEY
operator|+
literal|":"
argument_list|)
block|}
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
operator|new
name|Text
argument_list|(
name|lastKey
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HStoreKey
argument_list|,
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|e
range|:
name|scanner
control|)
block|{
if|if
condition|(
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|compareTo
argument_list|(
name|lastKey
argument_list|)
operator|>=
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @throws Exception    */
specifier|public
name|void
name|testIterator
parameter_list|()
throws|throws
name|Exception
block|{
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|HConstants
operator|.
name|ROOT_TABLE_NAME
argument_list|)
decl_stmt|;
name|HScannerInterface
name|scanner
init|=
name|table
operator|.
name|obtainScanner
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|HStoreKey
argument_list|,
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|e
range|:
name|scanner
control|)
block|{
name|assertNotNull
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test getting scanners with regexes for column names.    * @throws IOException     */
specifier|public
name|void
name|testRegexForColumnName
parameter_list|()
throws|throws
name|IOException
block|{
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Setup colkeys to be inserted
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
name|createTable
argument_list|(
name|admin
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
comment|// Add a row to columns without qualifiers and then two with.  Make one
comment|// numbers only so easy to find w/ a regex.
name|BatchUpdate
name|batchUpdate
init|=
operator|new
name|BatchUpdate
argument_list|(
operator|new
name|Text
argument_list|(
name|getName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|String
name|firstColkeyFamily
init|=
name|Character
operator|.
name|toString
argument_list|(
name|FIRST_COLKEY
argument_list|)
operator|+
literal|":"
decl_stmt|;
name|batchUpdate
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
name|firstColkeyFamily
operator|+
name|getName
argument_list|()
argument_list|)
argument_list|,
name|GOOD_BYTES
argument_list|)
expr_stmt|;
name|batchUpdate
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
name|firstColkeyFamily
operator|+
literal|"22222"
argument_list|)
argument_list|,
name|GOOD_BYTES
argument_list|)
expr_stmt|;
name|batchUpdate
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
name|firstColkeyFamily
argument_list|)
argument_list|,
name|GOOD_BYTES
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|batchUpdate
argument_list|)
expr_stmt|;
comment|// Now do a scan using a regex for a column name.
name|checkRegexingScanner
argument_list|(
name|table
argument_list|,
name|firstColkeyFamily
operator|+
literal|"\\d+"
argument_list|)
expr_stmt|;
comment|// Do a new scan that only matches on column family.
name|checkRegexingScanner
argument_list|(
name|table
argument_list|,
name|firstColkeyFamily
operator|+
literal|"$"
argument_list|)
expr_stmt|;
block|}
comment|/*    * Create a scanner w/ passed in column name regex.  Assert we only get    * back one column that matches.    * @param table    * @param regexColumnname    * @throws IOException    */
specifier|private
name|void
name|checkRegexingScanner
parameter_list|(
specifier|final
name|HTable
name|table
parameter_list|,
specifier|final
name|String
name|regexColumnname
parameter_list|)
throws|throws
name|IOException
block|{
name|Text
index|[]
name|regexCol
init|=
operator|new
name|Text
index|[]
block|{
operator|new
name|Text
argument_list|(
name|regexColumnname
argument_list|)
block|}
decl_stmt|;
name|HScannerInterface
name|scanner
init|=
name|table
operator|.
name|obtainScanner
argument_list|(
name|regexCol
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
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
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|results
argument_list|)
condition|)
block|{
for|for
control|(
name|Text
name|c
range|:
name|results
operator|.
name|keySet
argument_list|()
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|c
operator|.
name|toString
argument_list|()
operator|.
name|matches
argument_list|(
name|regexColumnname
argument_list|)
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|count
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test the scanner's handling of various filters.      *     * @throws Exception    */
specifier|public
name|void
name|testScannerFilter
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Setup HClient, ensure that it is running correctly
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// Setup colkeys to be inserted
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
name|Text
index|[]
name|colKeys
init|=
name|createTable
argument_list|(
name|admin
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Master is running."
argument_list|,
name|admin
operator|.
name|isMasterRunning
argument_list|()
argument_list|)
expr_stmt|;
comment|// Enter data
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|char
name|i
init|=
name|FIRST_ROWKEY
init|;
name|i
operator|<=
name|LAST_ROWKEY
condition|;
name|i
operator|++
control|)
block|{
name|Text
name|rowKey
init|=
operator|new
name|Text
argument_list|(
operator|new
name|String
argument_list|(
operator|new
name|char
index|[]
block|{
name|i
block|}
argument_list|)
argument_list|)
decl_stmt|;
name|BatchUpdate
name|batchUpdate
init|=
operator|new
name|BatchUpdate
argument_list|(
name|rowKey
argument_list|)
decl_stmt|;
for|for
control|(
name|char
name|j
init|=
literal|0
init|;
name|j
operator|<
name|colKeys
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|batchUpdate
operator|.
name|put
argument_list|(
name|colKeys
index|[
name|j
index|]
argument_list|,
operator|(
name|i
operator|>=
name|FIRST_BAD_RANGE_ROWKEY
operator|&&
name|i
operator|<=
name|LAST_BAD_RANGE_ROWKEY
operator|)
condition|?
name|BAD_BYTES
else|:
name|GOOD_BYTES
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|commit
argument_list|(
name|batchUpdate
argument_list|)
expr_stmt|;
block|}
name|regExpFilterTest
argument_list|(
name|table
argument_list|,
name|colKeys
argument_list|)
expr_stmt|;
name|rowFilterSetTest
argument_list|(
name|table
argument_list|,
name|colKeys
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param admin    * @param tableName    * @return Returns column keys used making table.    * @throws IOException    */
specifier|private
name|Text
index|[]
name|createTable
parameter_list|(
specifier|final
name|HBaseAdmin
name|admin
parameter_list|,
specifier|final
name|Text
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Setup colkeys to be inserted
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Text
index|[]
name|colKeys
init|=
operator|new
name|Text
index|[
operator|(
name|LAST_COLKEY
operator|-
name|FIRST_COLKEY
operator|)
operator|+
literal|1
index|]
decl_stmt|;
for|for
control|(
name|char
name|i
init|=
literal|0
init|;
name|i
operator|<
name|colKeys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|colKeys
index|[
name|i
index|]
operator|=
operator|new
name|Text
argument_list|(
operator|new
name|String
argument_list|(
operator|new
name|char
index|[]
block|{
call|(
name|char
call|)
argument_list|(
name|FIRST_COLKEY
operator|+
name|i
argument_list|)
block|,
literal|':'
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|colKeys
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Table with name "
operator|+
name|tableName
operator|+
literal|" created successfully."
argument_list|,
name|admin
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|colKeys
return|;
block|}
specifier|private
name|void
name|regExpFilterTest
parameter_list|(
name|HTable
name|table
parameter_list|,
name|Text
index|[]
name|colKeys
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Get the filter.  The RegExpRowFilter used should filter out vowels.
name|Map
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|colCriteria
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
name|i
operator|<
name|colKeys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|colCriteria
operator|.
name|put
argument_list|(
name|colKeys
index|[
name|i
index|]
argument_list|,
name|GOOD_BYTES
argument_list|)
expr_stmt|;
block|}
name|RowFilterInterface
name|filter
init|=
operator|new
name|RegExpRowFilter
argument_list|(
literal|"[^aeiou]"
argument_list|,
name|colCriteria
argument_list|)
decl_stmt|;
comment|// Create the scanner from the filter.
name|HScannerInterface
name|scanner
init|=
name|table
operator|.
name|obtainScanner
argument_list|(
name|colKeys
argument_list|,
operator|new
name|Text
argument_list|(
operator|new
name|String
argument_list|(
operator|new
name|char
index|[]
block|{
name|FIRST_ROWKEY
block|}
argument_list|)
argument_list|)
argument_list|,
name|filter
argument_list|)
decl_stmt|;
comment|// Iterate over the scanner, ensuring that results match the passed regex.
name|iterateOnScanner
argument_list|(
name|scanner
argument_list|,
literal|"[^aei-qu]"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|rowFilterSetTest
parameter_list|(
name|HTable
name|table
parameter_list|,
name|Text
index|[]
name|colKeys
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Get the filter.  The RegExpRowFilter used should filter out vowels and
comment|// the WhileMatchRowFilter(StopRowFilter) should filter out all rows
comment|// greater than or equal to 'r'.
name|Set
argument_list|<
name|RowFilterInterface
argument_list|>
name|filterSet
init|=
operator|new
name|HashSet
argument_list|<
name|RowFilterInterface
argument_list|>
argument_list|()
decl_stmt|;
name|filterSet
operator|.
name|add
argument_list|(
operator|new
name|RegExpRowFilter
argument_list|(
literal|"[^aeiou]"
argument_list|)
argument_list|)
expr_stmt|;
name|filterSet
operator|.
name|add
argument_list|(
operator|new
name|WhileMatchRowFilter
argument_list|(
operator|new
name|StopRowFilter
argument_list|(
operator|new
name|Text
argument_list|(
literal|"r"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|RowFilterInterface
name|filter
init|=
operator|new
name|RowFilterSet
argument_list|(
name|RowFilterSet
operator|.
name|Operator
operator|.
name|MUST_PASS_ALL
argument_list|,
name|filterSet
argument_list|)
decl_stmt|;
comment|// Create the scanner from the filter.
name|HScannerInterface
name|scanner
init|=
name|table
operator|.
name|obtainScanner
argument_list|(
name|colKeys
argument_list|,
operator|new
name|Text
argument_list|(
operator|new
name|String
argument_list|(
operator|new
name|char
index|[]
block|{
name|FIRST_ROWKEY
block|}
argument_list|)
argument_list|)
argument_list|,
name|filter
argument_list|)
decl_stmt|;
comment|// Iterate over the scanner, ensuring that results match the passed regex.
name|iterateOnScanner
argument_list|(
name|scanner
argument_list|,
literal|"[^aeior-z]"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|iterateOnScanner
parameter_list|(
name|HScannerInterface
name|scanner
parameter_list|,
name|String
name|regexToMatch
parameter_list|)
throws|throws
name|Exception
block|{
comment|// A pattern that will only match rows that should not have been filtered.
name|Pattern
name|p
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|regexToMatch
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Use the scanner to ensure all results match the above pattern.
name|HStoreKey
name|rowKey
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
name|columns
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
name|rowKey
argument_list|,
name|columns
argument_list|)
condition|)
block|{
name|String
name|key
init|=
name|rowKey
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Shouldn't have extracted '"
operator|+
name|key
operator|+
literal|"'"
argument_list|,
name|p
operator|.
name|matcher
argument_list|(
name|key
argument_list|)
operator|.
name|matches
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Test scanning of META table around split.    * There was a problem where only one of the splits showed in a scan.    * Split deletes a row and then adds two new ones.    * @throws IOException    */
specifier|public
name|void
name|testSplitDeleteOneAddTwoRegions
parameter_list|()
throws|throws
name|IOException
block|{
name|HTable
name|metaTable
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
comment|// First add a new table.  Its intial region will be added to META region.
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
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
name|admin
operator|.
name|createTable
argument_list|(
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|scan
argument_list|(
name|metaTable
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Expected one region"
argument_list|,
literal|1
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HRegionInfo
name|region
init|=
name|regions
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Expected region named for test"
argument_list|,
name|region
operator|.
name|getRegionName
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|startsWith
argument_list|(
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now do what happens at split time; remove old region and then add two
comment|// new ones in its place.
name|removeRegionFromMETA
argument_list|(
name|metaTable
argument_list|,
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|desc
init|=
name|region
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
name|Path
name|homedir
init|=
operator|new
name|Path
argument_list|(
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|HRegion
argument_list|>
name|newRegions
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegion
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|newRegions
operator|.
name|add
argument_list|(
name|HRegion
operator|.
name|createHRegion
argument_list|(
operator|new
name|HRegionInfo
argument_list|(
name|desc
argument_list|,
literal|null
argument_list|,
operator|new
name|Text
argument_list|(
literal|"midway"
argument_list|)
argument_list|)
argument_list|,
name|homedir
argument_list|,
name|this
operator|.
name|conf
argument_list|)
argument_list|)
expr_stmt|;
name|newRegions
operator|.
name|add
argument_list|(
name|HRegion
operator|.
name|createHRegion
argument_list|(
operator|new
name|HRegionInfo
argument_list|(
name|desc
argument_list|,
operator|new
name|Text
argument_list|(
literal|"midway"
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|,
name|homedir
argument_list|,
name|this
operator|.
name|conf
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
for|for
control|(
name|HRegion
name|r
range|:
name|newRegions
control|)
block|{
name|addRegionToMETA
argument_list|(
name|metaTable
argument_list|,
name|r
argument_list|,
name|this
operator|.
name|cluster
operator|.
name|getHMasterAddress
argument_list|()
argument_list|,
operator|-
literal|1L
argument_list|)
expr_stmt|;
block|}
name|regions
operator|=
name|scan
argument_list|(
name|metaTable
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Should be two regions only"
argument_list|,
literal|2
argument_list|,
name|regions
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
for|for
control|(
name|HRegion
name|r
range|:
name|newRegions
control|)
block|{
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|r
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
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|scan
parameter_list|(
specifier|final
name|HTable
name|t
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
decl_stmt|;
name|HRegionInterface
name|regionServer
init|=
literal|null
decl_stmt|;
name|long
name|scannerId
init|=
operator|-
literal|1L
decl_stmt|;
try|try
block|{
name|HRegionLocation
name|rl
init|=
name|t
operator|.
name|getRegionLocation
argument_list|(
name|t
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|regionServer
operator|=
name|t
operator|.
name|getConnection
argument_list|()
operator|.
name|getHRegionConnection
argument_list|(
name|rl
operator|.
name|getServerAddress
argument_list|()
argument_list|)
expr_stmt|;
name|scannerId
operator|=
name|regionServer
operator|.
name|openScanner
argument_list|(
name|rl
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|COLUMN_FAMILY_ARRAY
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|RowResult
name|values
init|=
name|regionServer
operator|.
name|next
argument_list|(
name|scannerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|values
operator|==
literal|null
operator|||
name|values
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
break|break;
block|}
name|HRegionInfo
name|info
init|=
operator|(
name|HRegionInfo
operator|)
name|Writables
operator|.
name|getWritable
argument_list|(
name|values
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|,
operator|new
name|HRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|serverName
init|=
name|Writables
operator|.
name|cellToString
argument_list|(
name|values
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|COL_SERVER
argument_list|)
argument_list|)
decl_stmt|;
name|long
name|startCode
init|=
name|Writables
operator|.
name|cellToLong
argument_list|(
name|values
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|COL_STARTCODE
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|" scanner: "
operator|+
name|Long
operator|.
name|valueOf
argument_list|(
name|scannerId
argument_list|)
operator|+
literal|": regioninfo: {"
operator|+
name|info
operator|.
name|toString
argument_list|()
operator|+
literal|"}, server: "
operator|+
name|serverName
operator|+
literal|", startCode: "
operator|+
name|startCode
argument_list|)
expr_stmt|;
name|regions
operator|.
name|add
argument_list|(
name|info
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
try|try
block|{
if|if
condition|(
name|scannerId
operator|!=
operator|-
literal|1L
condition|)
block|{
if|if
condition|(
name|regionServer
operator|!=
literal|null
condition|)
block|{
name|regionServer
operator|.
name|close
argument_list|(
name|scannerId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|regions
return|;
block|}
specifier|private
name|void
name|addRegionToMETA
parameter_list|(
specifier|final
name|HTable
name|t
parameter_list|,
specifier|final
name|HRegion
name|region
parameter_list|,
specifier|final
name|HServerAddress
name|serverAddress
parameter_list|,
specifier|final
name|long
name|startCode
parameter_list|)
throws|throws
name|IOException
block|{
name|BatchUpdate
name|batchUpdate
init|=
operator|new
name|BatchUpdate
argument_list|(
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|batchUpdate
operator|.
name|put
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|,
name|Writables
operator|.
name|getBytes
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|batchUpdate
operator|.
name|put
argument_list|(
name|HConstants
operator|.
name|COL_SERVER
argument_list|,
name|Writables
operator|.
name|stringToBytes
argument_list|(
name|serverAddress
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|batchUpdate
operator|.
name|put
argument_list|(
name|HConstants
operator|.
name|COL_STARTCODE
argument_list|,
name|Writables
operator|.
name|longToBytes
argument_list|(
name|startCode
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|commit
argument_list|(
name|batchUpdate
argument_list|)
expr_stmt|;
comment|// Assert added.
name|byte
index|[]
name|bytes
init|=
name|t
operator|.
name|get
argument_list|(
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|HRegionInfo
name|hri
init|=
name|Writables
operator|.
name|getHRegionInfo
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|region
operator|.
name|getRegionId
argument_list|()
argument_list|,
name|hri
operator|.
name|getRegionId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Added region "
operator|+
name|region
operator|.
name|getRegionName
argument_list|()
operator|+
literal|" to table "
operator|+
name|t
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*    * Delete<code>region</code> from META<code>table</code>.    * @param conf Configuration object    * @param table META table we are to delete region from.    * @param regionName Region to remove.    * @throws IOException    */
specifier|private
name|void
name|removeRegionFromMETA
parameter_list|(
specifier|final
name|HTable
name|t
parameter_list|,
specifier|final
name|Text
name|regionName
parameter_list|)
throws|throws
name|IOException
block|{
name|BatchUpdate
name|batchUpdate
init|=
operator|new
name|BatchUpdate
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
name|batchUpdate
operator|.
name|delete
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
expr_stmt|;
name|batchUpdate
operator|.
name|delete
argument_list|(
name|HConstants
operator|.
name|COL_SERVER
argument_list|)
expr_stmt|;
name|batchUpdate
operator|.
name|delete
argument_list|(
name|HConstants
operator|.
name|COL_STARTCODE
argument_list|)
expr_stmt|;
name|t
operator|.
name|commit
argument_list|(
name|batchUpdate
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removed "
operator|+
name|regionName
operator|+
literal|" from table "
operator|+
name|t
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

