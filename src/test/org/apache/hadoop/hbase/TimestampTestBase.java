begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
operator|.
name|HColumnDescriptor
operator|.
name|CompressionType
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
name|HScannerInterface
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
name|HTableDescriptor
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
name|HColumnDescriptor
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

begin_comment
comment|/**  * Tests user specifiable time stamps putting, getting and scanning.  Also  * tests same in presence of deletes.  Test cores are written so can be  * run against an HRegion and against an HTable: i.e. both local and remote.  */
end_comment

begin_class
specifier|public
class|class
name|TimestampTestBase
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
name|TimestampTestBase
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|T0
init|=
literal|10L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|T1
init|=
literal|100L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|T2
init|=
literal|200L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COLUMN_NAME
init|=
literal|"contents:"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|COLUMN
init|=
operator|new
name|Text
argument_list|(
name|COLUMN_NAME
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|ROW
init|=
operator|new
name|Text
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
comment|// When creating column descriptor, how many versions of a cell to allow.
specifier|private
specifier|static
specifier|final
name|int
name|VERSIONS
init|=
literal|3
decl_stmt|;
comment|/*    * Run test that delete works according to description in<a    * href="https://issues.apache.org/jira/browse/HADOOP-1784">hadoop-1784</a>.    * @param incommon    * @param flusher    * @throws IOException    */
specifier|public
specifier|static
name|void
name|doTestDelete
parameter_list|(
specifier|final
name|Incommon
name|incommon
parameter_list|,
name|FlushCache
name|flusher
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Add values at various timestamps (Values are timestampes as bytes).
name|put
argument_list|(
name|incommon
argument_list|,
name|T0
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|incommon
argument_list|,
name|T1
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|incommon
argument_list|,
name|T2
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|incommon
argument_list|)
expr_stmt|;
comment|// Verify that returned versions match passed timestamps.
name|assertVersions
argument_list|(
name|incommon
argument_list|,
operator|new
name|long
index|[]
block|{
name|HConstants
operator|.
name|LATEST_TIMESTAMP
block|,
name|T2
block|,
name|T1
block|}
argument_list|)
expr_stmt|;
comment|// If I delete w/o specifying a timestamp, this means I'm deleting the
comment|// latest.
name|delete
argument_list|(
name|incommon
argument_list|)
expr_stmt|;
comment|// Verify that I get back T2 through T1 -- that the latest version has
comment|// been deleted.
name|assertVersions
argument_list|(
name|incommon
argument_list|,
operator|new
name|long
index|[]
block|{
name|T2
block|,
name|T1
block|,
name|T0
block|}
argument_list|)
expr_stmt|;
comment|// Flush everything out to disk and then retry
name|flusher
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|assertVersions
argument_list|(
name|incommon
argument_list|,
operator|new
name|long
index|[]
block|{
name|T2
block|,
name|T1
block|,
name|T0
block|}
argument_list|)
expr_stmt|;
comment|// Now add, back a latest so I can test remove other than the latest.
name|put
argument_list|(
name|incommon
argument_list|)
expr_stmt|;
name|assertVersions
argument_list|(
name|incommon
argument_list|,
operator|new
name|long
index|[]
block|{
name|HConstants
operator|.
name|LATEST_TIMESTAMP
block|,
name|T2
block|,
name|T1
block|}
argument_list|)
expr_stmt|;
name|delete
argument_list|(
name|incommon
argument_list|,
name|T2
argument_list|)
expr_stmt|;
name|assertVersions
argument_list|(
name|incommon
argument_list|,
operator|new
name|long
index|[]
block|{
name|HConstants
operator|.
name|LATEST_TIMESTAMP
block|,
name|T1
block|,
name|T0
block|}
argument_list|)
expr_stmt|;
comment|// Flush everything out to disk and then retry
name|flusher
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|assertVersions
argument_list|(
name|incommon
argument_list|,
operator|new
name|long
index|[]
block|{
name|HConstants
operator|.
name|LATEST_TIMESTAMP
block|,
name|T1
block|,
name|T0
block|}
argument_list|)
expr_stmt|;
comment|// Now try deleting all from T2 back inclusive (We first need to add T2
comment|// back into the mix and to make things a little interesting, delete and
comment|// then readd T1.
name|put
argument_list|(
name|incommon
argument_list|,
name|T2
argument_list|)
expr_stmt|;
name|delete
argument_list|(
name|incommon
argument_list|,
name|T1
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|incommon
argument_list|,
name|T1
argument_list|)
expr_stmt|;
name|incommon
operator|.
name|deleteAll
argument_list|(
name|ROW
argument_list|,
name|COLUMN
argument_list|,
name|T2
argument_list|)
expr_stmt|;
comment|// Should only be current value in set.  Assert this is so
name|assertOnlyLatest
argument_list|(
name|incommon
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
comment|// Flush everything out to disk and then redo above tests
name|flusher
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|assertOnlyLatest
argument_list|(
name|incommon
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|assertOnlyLatest
parameter_list|(
specifier|final
name|Incommon
name|incommon
parameter_list|,
specifier|final
name|long
name|currentTime
parameter_list|)
throws|throws
name|IOException
block|{
name|Cell
index|[]
name|cellValues
init|=
name|incommon
operator|.
name|get
argument_list|(
name|ROW
argument_list|,
name|COLUMN
argument_list|,
literal|3
comment|/*Ask for too much*/
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|cellValues
operator|.
name|length
argument_list|)
expr_stmt|;
name|long
name|time
init|=
name|Writables
operator|.
name|bytesToLong
argument_list|(
name|cellValues
index|[
literal|0
index|]
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|time
argument_list|,
name|currentTime
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|incommon
operator|.
name|get
argument_list|(
name|ROW
argument_list|,
name|COLUMN
argument_list|,
name|T1
argument_list|,
literal|3
comment|/*Too many*/
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|assertScanContentTimestamp
argument_list|(
name|incommon
argument_list|,
name|T1
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/*    * Assert that returned versions match passed in timestamps and that results    * are returned in the right order.  Assert that values when converted to    * longs match the corresponding passed timestamp.    * @param r    * @param tss    * @throws IOException    */
specifier|public
specifier|static
name|void
name|assertVersions
parameter_list|(
specifier|final
name|Incommon
name|incommon
parameter_list|,
specifier|final
name|long
index|[]
name|tss
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Assert that 'latest' is what we expect.
name|byte
index|[]
name|bytes
init|=
name|incommon
operator|.
name|get
argument_list|(
name|ROW
argument_list|,
name|COLUMN
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|Writables
operator|.
name|bytesToLong
argument_list|(
name|bytes
argument_list|)
argument_list|,
name|tss
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// Now assert that if we ask for multiple versions, that they come out in
comment|// order.
name|Cell
index|[]
name|cellValues
init|=
name|incommon
operator|.
name|get
argument_list|(
name|ROW
argument_list|,
name|COLUMN
argument_list|,
name|tss
operator|.
name|length
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|tss
operator|.
name|length
argument_list|,
name|cellValues
operator|.
name|length
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
name|cellValues
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|long
name|ts
init|=
name|Writables
operator|.
name|bytesToLong
argument_list|(
name|cellValues
index|[
name|i
index|]
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ts
argument_list|,
name|tss
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
comment|// Specify a timestamp get multiple versions.
name|cellValues
operator|=
name|incommon
operator|.
name|get
argument_list|(
name|ROW
argument_list|,
name|COLUMN
argument_list|,
name|tss
index|[
literal|0
index|]
argument_list|,
name|cellValues
operator|.
name|length
operator|-
literal|1
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|cellValues
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|long
name|ts
init|=
name|Writables
operator|.
name|bytesToLong
argument_list|(
name|cellValues
index|[
name|i
index|]
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ts
argument_list|,
name|tss
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
comment|// Test scanner returns expected version
name|assertScanContentTimestamp
argument_list|(
name|incommon
argument_list|,
name|tss
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
comment|/*    * Run test scanning different timestamps.    * @param incommon    * @param flusher    * @throws IOException    */
specifier|public
specifier|static
name|void
name|doTestTimestampScanning
parameter_list|(
specifier|final
name|Incommon
name|incommon
parameter_list|,
specifier|final
name|FlushCache
name|flusher
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Add a couple of values for three different timestamps.
name|put
argument_list|(
name|incommon
argument_list|,
name|T0
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|incommon
argument_list|,
name|T1
argument_list|)
expr_stmt|;
name|put
argument_list|(
name|incommon
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
comment|// Get count of latest items.
name|int
name|count
init|=
name|assertScanContentTimestamp
argument_list|(
name|incommon
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
decl_stmt|;
comment|// Assert I get same count when I scan at each timestamp.
name|assertEquals
argument_list|(
name|count
argument_list|,
name|assertScanContentTimestamp
argument_list|(
name|incommon
argument_list|,
name|T0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|count
argument_list|,
name|assertScanContentTimestamp
argument_list|(
name|incommon
argument_list|,
name|T1
argument_list|)
argument_list|)
expr_stmt|;
comment|// Flush everything out to disk and then retry
name|flusher
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|count
argument_list|,
name|assertScanContentTimestamp
argument_list|(
name|incommon
argument_list|,
name|T0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|count
argument_list|,
name|assertScanContentTimestamp
argument_list|(
name|incommon
argument_list|,
name|T1
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/*    * Assert that the scan returns only values< timestamp.     * @param r    * @param ts    * @return Count of items scanned.    * @throws IOException    */
specifier|public
specifier|static
name|int
name|assertScanContentTimestamp
parameter_list|(
specifier|final
name|Incommon
name|in
parameter_list|,
specifier|final
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
name|HScannerInterface
name|scanner
init|=
name|in
operator|.
name|getScanner
argument_list|(
name|COLUMNS
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|ts
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
try|try
block|{
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
name|assertTrue
argument_list|(
name|key
operator|.
name|getTimestamp
argument_list|()
operator|<=
name|ts
argument_list|)
expr_stmt|;
comment|// Content matches the key or HConstants.LATEST_TIMESTAMP.
comment|// (Key does not match content if we 'put' with LATEST_TIMESTAMP).
name|long
name|l
init|=
name|Writables
operator|.
name|bytesToLong
argument_list|(
name|value
operator|.
name|get
argument_list|(
name|COLUMN
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|key
operator|.
name|getTimestamp
argument_list|()
operator|==
name|l
operator|||
name|HConstants
operator|.
name|LATEST_TIMESTAMP
operator|==
name|l
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
name|value
operator|.
name|clear
argument_list|()
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
return|return
name|count
return|;
block|}
specifier|public
specifier|static
name|void
name|put
parameter_list|(
specifier|final
name|Incommon
name|loader
parameter_list|,
specifier|final
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
name|put
argument_list|(
name|loader
argument_list|,
name|Writables
operator|.
name|longToBytes
argument_list|(
name|ts
argument_list|)
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|put
parameter_list|(
specifier|final
name|Incommon
name|loader
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|ts
init|=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
decl_stmt|;
name|put
argument_list|(
name|loader
argument_list|,
name|Writables
operator|.
name|longToBytes
argument_list|(
name|ts
argument_list|)
argument_list|,
name|ts
argument_list|)
expr_stmt|;
block|}
comment|/*    * Put values.    * @param loader    * @param bytes    * @param ts    * @throws IOException    */
specifier|public
specifier|static
name|void
name|put
parameter_list|(
specifier|final
name|Incommon
name|loader
parameter_list|,
specifier|final
name|byte
index|[]
name|bytes
parameter_list|,
specifier|final
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
name|BatchUpdate
name|batchUpdate
init|=
name|ts
operator|==
name|HConstants
operator|.
name|LATEST_TIMESTAMP
condition|?
operator|new
name|BatchUpdate
argument_list|(
name|ROW
argument_list|)
else|:
operator|new
name|BatchUpdate
argument_list|(
name|ROW
argument_list|,
name|ts
argument_list|)
decl_stmt|;
name|batchUpdate
operator|.
name|put
argument_list|(
name|COLUMN
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
name|loader
operator|.
name|commit
argument_list|(
name|batchUpdate
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|delete
parameter_list|(
specifier|final
name|Incommon
name|loader
parameter_list|)
throws|throws
name|IOException
block|{
name|delete
argument_list|(
name|loader
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|delete
parameter_list|(
specifier|final
name|Incommon
name|loader
parameter_list|,
specifier|final
name|long
name|ts
parameter_list|)
throws|throws
name|IOException
block|{
name|BatchUpdate
name|batchUpdate
init|=
name|ts
operator|==
name|HConstants
operator|.
name|LATEST_TIMESTAMP
condition|?
operator|new
name|BatchUpdate
argument_list|(
name|ROW
argument_list|)
else|:
operator|new
name|BatchUpdate
argument_list|(
name|ROW
argument_list|,
name|ts
argument_list|)
decl_stmt|;
name|batchUpdate
operator|.
name|delete
argument_list|(
name|COLUMN
argument_list|)
expr_stmt|;
name|loader
operator|.
name|commit
argument_list|(
name|batchUpdate
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

