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
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutputStream
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
name|HRegionInfo
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
name|UnknownScannerException
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
name|Get
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
name|filter
operator|.
name|Filter
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
name|InclusiveStopFilter
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
name|PrefixFilter
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
name|WhileMatchFilter
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
name|Compression
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

begin_comment
comment|/**  * Test of a long-lived scanner validating as we go.  */
end_comment

begin_class
specifier|public
class|class
name|TestScanner
extends|extends
name|HBaseTestCase
block|{
specifier|private
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
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FIRST_ROW
init|=
name|HConstants
operator|.
name|EMPTY_START_ROW
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|COLS
init|=
block|{
name|HConstants
operator|.
name|CATALOG_FAMILY
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|EXPLICIT_COLS
init|=
block|{
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
block|,
name|HConstants
operator|.
name|SERVER_QUALIFIER
block|,
comment|// TODO ryan
comment|//HConstants.STARTCODE_QUALIFIER
block|}
decl_stmt|;
specifier|static
specifier|final
name|HTableDescriptor
name|TESTTABLEDESC
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"testscanner"
argument_list|)
decl_stmt|;
static|static
block|{
name|TESTTABLEDESC
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|10
argument_list|,
comment|// Ten is arbitrary number.  Keep versions to help debuggging.
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
operator|.
name|getName
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|8
operator|*
literal|1024
argument_list|,
name|HConstants
operator|.
name|FOREVER
argument_list|,
name|StoreFile
operator|.
name|BloomType
operator|.
name|NONE
operator|.
name|toString
argument_list|()
argument_list|,
name|HConstants
operator|.
name|REPLICATION_SCOPE_LOCAL
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/** HRegionInfo for root region */
specifier|public
specifier|static
specifier|final
name|HRegionInfo
name|REGION_INFO
init|=
operator|new
name|HRegionInfo
argument_list|(
name|TESTTABLEDESC
operator|.
name|getName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW_KEY
init|=
name|REGION_INFO
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|START_CODE
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|private
name|HRegion
name|r
decl_stmt|;
specifier|private
name|HRegionIncommon
name|region
decl_stmt|;
comment|/**    * Test basic stop row filter works.    * @throws Exception    */
specifier|public
name|void
name|testStopRow
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|startrow
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbb"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|stoprow
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ccc"
argument_list|)
decl_stmt|;
try|try
block|{
name|this
operator|.
name|r
operator|=
name|createNewHRegion
argument_list|(
name|TESTTABLEDESC
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|addContent
argument_list|(
name|this
operator|.
name|r
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
comment|// Do simple test of getting one row only first.
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"abd"
argument_list|)
argument_list|)
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|InternalScanner
name|s
init|=
name|r
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|s
operator|.
name|next
argument_list|(
name|results
argument_list|)
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|count
argument_list|)
expr_stmt|;
comment|// Now do something a bit more imvolved.
name|scan
operator|=
operator|new
name|Scan
argument_list|(
name|startrow
argument_list|,
name|stoprow
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|s
operator|=
name|r
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|count
operator|=
literal|0
expr_stmt|;
name|KeyValue
name|kv
init|=
literal|null
decl_stmt|;
name|results
operator|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|boolean
name|first
init|=
literal|true
init|;
name|s
operator|.
name|next
argument_list|(
name|results
argument_list|)
condition|;
control|)
block|{
name|kv
operator|=
name|results
operator|.
name|get
argument_list|(
literal|0
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
name|BYTES_COMPARATOR
operator|.
name|compare
argument_list|(
name|startrow
argument_list|,
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|first
operator|=
literal|false
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
operator|.
name|compare
argument_list|(
name|stoprow
argument_list|,
name|kv
operator|.
name|getRow
argument_list|()
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
comment|// We got something back.
name|assertTrue
argument_list|(
name|count
operator|>
literal|10
argument_list|)
expr_stmt|;
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
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
name|void
name|rowPrefixFilter
parameter_list|(
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|InternalScanner
name|s
init|=
name|r
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|boolean
name|hasMore
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|hasMore
condition|)
block|{
name|hasMore
operator|=
name|s
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|results
control|)
block|{
name|assertEquals
argument_list|(
operator|(
name|byte
operator|)
literal|'a'
argument_list|,
name|kv
operator|.
name|getRow
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
name|byte
operator|)
literal|'b'
argument_list|,
name|kv
operator|.
name|getRow
argument_list|()
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|void
name|rowInclusiveStopFilter
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|byte
index|[]
name|stopRow
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|scan
operator|.
name|addFamily
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|InternalScanner
name|s
init|=
name|r
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|boolean
name|hasMore
init|=
literal|true
decl_stmt|;
while|while
condition|(
name|hasMore
condition|)
block|{
name|hasMore
operator|=
name|s
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|results
control|)
block|{
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|compareTo
argument_list|(
name|kv
operator|.
name|getRow
argument_list|()
argument_list|,
name|stopRow
argument_list|)
operator|<=
literal|0
argument_list|)
expr_stmt|;
block|}
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|testFilters
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|this
operator|.
name|r
operator|=
name|createNewHRegion
argument_list|(
name|TESTTABLEDESC
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|addContent
argument_list|(
name|this
operator|.
name|r
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|byte
index|[]
name|prefix
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"ab"
argument_list|)
decl_stmt|;
name|Filter
name|newFilter
init|=
operator|new
name|PrefixFilter
argument_list|(
name|prefix
argument_list|)
decl_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|newFilter
argument_list|)
expr_stmt|;
name|rowPrefixFilter
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|byte
index|[]
name|stopRow
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"bbc"
argument_list|)
decl_stmt|;
name|newFilter
operator|=
operator|new
name|WhileMatchFilter
argument_list|(
operator|new
name|InclusiveStopFilter
argument_list|(
name|stopRow
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|newFilter
argument_list|)
expr_stmt|;
name|rowInclusiveStopFilter
argument_list|(
name|scan
argument_list|,
name|stopRow
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|this
operator|.
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
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
comment|/**    * Test that closing a scanner while a client is using it doesn't throw    * NPEs but instead a UnknownScannerException. HBASE-2503    * @throws Exception    */
specifier|public
name|void
name|testRaceBetweenClientAndTimeout
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|this
operator|.
name|r
operator|=
name|createNewHRegion
argument_list|(
name|TESTTABLEDESC
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|addContent
argument_list|(
name|this
operator|.
name|r
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|InternalScanner
name|s
init|=
name|r
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|s
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
name|s
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"We don't want anything more, we should be failing"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownScannerException
name|ex
parameter_list|)
block|{
comment|// ok!
return|return;
block|}
block|}
finally|finally
block|{
name|this
operator|.
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
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
comment|/** The test!    * @throws IOException    */
specifier|public
name|void
name|testScanner
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|r
operator|=
name|createNewHRegion
argument_list|(
name|TESTTABLEDESC
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|region
operator|=
operator|new
name|HRegionIncommon
argument_list|(
name|r
argument_list|)
expr_stmt|;
comment|// Write information to the meta table
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|ROW_KEY
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|byteStream
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|s
init|=
operator|new
name|DataOutputStream
argument_list|(
name|byteStream
argument_list|)
decl_stmt|;
name|REGION_INFO
operator|.
name|write
argument_list|(
name|s
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
name|byteStream
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// What we just committed is in the memstore. Verify that we can get
comment|// it back both with scanning and get
name|scan
argument_list|(
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|getRegionInfo
argument_list|()
expr_stmt|;
comment|// Close and re-open
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|r
operator|=
name|openClosedRegion
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|region
operator|=
operator|new
name|HRegionIncommon
argument_list|(
name|r
argument_list|)
expr_stmt|;
comment|// Verify we can get the data back now that it is on disk.
name|scan
argument_list|(
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|getRegionInfo
argument_list|()
expr_stmt|;
comment|// Store some new information
name|String
name|address
init|=
literal|"foo.bar.com:1234"
decl_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW_KEY
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|null
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
name|SERVER_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|address
argument_list|)
argument_list|)
expr_stmt|;
comment|//      put.add(HConstants.COL_STARTCODE, Bytes.toBytes(START_CODE));
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// Validate that we can still get the HRegionInfo, even though it is in
comment|// an older row on disk and there is a newer row in the memstore
name|scan
argument_list|(
literal|true
argument_list|,
name|address
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|getRegionInfo
argument_list|()
expr_stmt|;
comment|// flush cache
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// Validate again
name|scan
argument_list|(
literal|true
argument_list|,
name|address
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|getRegionInfo
argument_list|()
expr_stmt|;
comment|// Close and reopen
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|r
operator|=
name|openClosedRegion
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|region
operator|=
operator|new
name|HRegionIncommon
argument_list|(
name|r
argument_list|)
expr_stmt|;
comment|// Validate again
name|scan
argument_list|(
literal|true
argument_list|,
name|address
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|getRegionInfo
argument_list|()
expr_stmt|;
comment|// Now update the information again
name|address
operator|=
literal|"bar.foo.com:4321"
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|ROW_KEY
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|null
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
name|SERVER_QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|address
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// Validate again
name|scan
argument_list|(
literal|true
argument_list|,
name|address
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|getRegionInfo
argument_list|()
expr_stmt|;
comment|// flush cache
name|region
operator|.
name|flushcache
argument_list|()
expr_stmt|;
comment|// Validate again
name|scan
argument_list|(
literal|true
argument_list|,
name|address
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|getRegionInfo
argument_list|()
expr_stmt|;
comment|// Close and reopen
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|r
operator|=
name|openClosedRegion
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|region
operator|=
operator|new
name|HRegionIncommon
argument_list|(
name|r
argument_list|)
expr_stmt|;
comment|// Validate again
name|scan
argument_list|(
literal|true
argument_list|,
name|address
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|getRegionInfo
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
comment|// clean up
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
comment|/** Compare the HRegionInfo we read from HBase to what we stored */
specifier|private
name|void
name|validateRegionInfo
parameter_list|(
name|byte
index|[]
name|regionBytes
parameter_list|)
throws|throws
name|IOException
block|{
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
name|regionBytes
argument_list|,
operator|new
name|HRegionInfo
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|REGION_INFO
operator|.
name|getRegionId
argument_list|()
argument_list|,
name|info
operator|.
name|getRegionId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|info
operator|.
name|getStartKey
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|info
operator|.
name|getEndKey
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|Bytes
operator|.
name|compareTo
argument_list|(
name|info
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|REGION_INFO
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|//assertEquals(0, info.getTableDesc().compareTo(REGION_INFO.getTableDesc()));
block|}
comment|/** Use a scanner to get the region info and then validate the results */
specifier|private
name|void
name|scan
parameter_list|(
name|boolean
name|validateStartcode
parameter_list|,
name|String
name|serverName
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalScanner
name|scanner
init|=
literal|null
decl_stmt|;
name|Scan
name|scan
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|byte
index|[]
index|[]
index|[]
name|scanColumns
init|=
block|{
name|COLS
block|,
name|EXPLICIT_COLS
block|}
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
name|scanColumns
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
name|scan
operator|=
operator|new
name|Scan
argument_list|(
name|FIRST_ROW
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|ii
init|=
literal|0
init|;
name|ii
operator|<
name|EXPLICIT_COLS
operator|.
name|length
condition|;
name|ii
operator|++
control|)
block|{
name|scan
operator|.
name|addColumn
argument_list|(
name|COLS
index|[
literal|0
index|]
argument_list|,
name|EXPLICIT_COLS
index|[
name|ii
index|]
argument_list|)
expr_stmt|;
block|}
name|scanner
operator|=
name|r
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
condition|)
block|{
name|assertTrue
argument_list|(
name|hasColumn
argument_list|(
name|results
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|val
init|=
name|getColumn
argument_list|(
name|results
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|validateRegionInfo
argument_list|(
name|val
argument_list|)
expr_stmt|;
if|if
condition|(
name|validateStartcode
condition|)
block|{
comment|//            assertTrue(hasColumn(results, HConstants.CATALOG_FAMILY,
comment|//                HConstants.STARTCODE_QUALIFIER));
comment|//            val = getColumn(results, HConstants.CATALOG_FAMILY,
comment|//                HConstants.STARTCODE_QUALIFIER).getValue();
name|assertNotNull
argument_list|(
name|val
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|val
operator|.
name|length
operator|==
literal|0
argument_list|)
expr_stmt|;
name|long
name|startCode
init|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|val
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|START_CODE
argument_list|,
name|startCode
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|serverName
operator|!=
literal|null
condition|)
block|{
name|assertTrue
argument_list|(
name|hasColumn
argument_list|(
name|results
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|SERVER_QUALIFIER
argument_list|)
argument_list|)
expr_stmt|;
name|val
operator|=
name|getColumn
argument_list|(
name|results
argument_list|,
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|SERVER_QUALIFIER
argument_list|)
operator|.
name|getValue
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|val
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|val
operator|.
name|length
operator|==
literal|0
argument_list|)
expr_stmt|;
name|String
name|server
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|val
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|server
operator|.
name|compareTo
argument_list|(
name|serverName
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|InternalScanner
name|s
init|=
name|scanner
decl_stmt|;
name|scanner
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|s
operator|!=
literal|null
condition|)
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
specifier|private
name|boolean
name|hasColumn
parameter_list|(
specifier|final
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
if|if
condition|(
name|kv
operator|.
name|matchingFamily
argument_list|(
name|family
argument_list|)
operator|&&
name|kv
operator|.
name|matchingQualifier
argument_list|(
name|qualifier
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|KeyValue
name|getColumn
parameter_list|(
specifier|final
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
if|if
condition|(
name|kv
operator|.
name|matchingFamily
argument_list|(
name|family
argument_list|)
operator|&&
name|kv
operator|.
name|matchingQualifier
argument_list|(
name|qualifier
argument_list|)
condition|)
block|{
return|return
name|kv
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/** Use get to retrieve the HRegionInfo and validate it */
specifier|private
name|void
name|getRegionInfo
parameter_list|()
throws|throws
name|IOException
block|{
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|ROW_KEY
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|region
operator|.
name|get
argument_list|(
name|get
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|result
operator|.
name|value
argument_list|()
decl_stmt|;
name|validateRegionInfo
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests to do a sync flush during the middle of a scan. This is testing the StoreScanner    * update readers code essentially.  This is not highly concurrent, since its all 1 thread.    * HBase-910.    * @throws Exception    */
specifier|public
name|void
name|testScanAndSyncFlush
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|r
operator|=
name|createNewHRegion
argument_list|(
name|TESTTABLEDESC
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|HRegionIncommon
name|hri
init|=
operator|new
name|HRegionIncommon
argument_list|(
name|r
argument_list|)
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Added: "
operator|+
name|addContent
argument_list|(
name|hri
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|count
init|=
name|count
argument_list|(
name|hri
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|count
argument_list|,
name|count
argument_list|(
name|hri
argument_list|,
literal|100
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// do a sync flush.
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
name|this
operator|.
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
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
comment|/**    * Tests to do a concurrent flush (using a 2nd thread) while scanning.  This tests both    * the StoreScanner update readers and the transition from memstore -> snapshot -> store file.    *    * @throws Exception    */
specifier|public
name|void
name|testScanAndRealConcurrentFlush
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|r
operator|=
name|createNewHRegion
argument_list|(
name|TESTTABLEDESC
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|HRegionIncommon
name|hri
init|=
operator|new
name|HRegionIncommon
argument_list|(
name|r
argument_list|)
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Added: "
operator|+
name|addContent
argument_list|(
name|hri
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|HConstants
operator|.
name|REGIONINFO_QUALIFIER
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|count
init|=
name|count
argument_list|(
name|hri
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|count
argument_list|,
name|count
argument_list|(
name|hri
argument_list|,
literal|100
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|// do a true concurrent background thread flush
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
name|this
operator|.
name|r
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
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
comment|/*    * @param hri Region    * @param flushIndex At what row we start the flush.    * @param concurrent if the flush should be concurrent or sync.    * @return Count of rows found.    * @throws IOException    */
specifier|private
name|int
name|count
parameter_list|(
specifier|final
name|HRegionIncommon
name|hri
parameter_list|,
specifier|final
name|int
name|flushIndex
parameter_list|,
name|boolean
name|concurrent
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Taking out counting scan"
argument_list|)
expr_stmt|;
name|ScannerIncommon
name|s
init|=
name|hri
operator|.
name|getScanner
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
name|EXPLICIT_COLS
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
name|boolean
name|justFlushed
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|s
operator|.
name|next
argument_list|(
name|values
argument_list|)
condition|)
block|{
if|if
condition|(
name|justFlushed
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"after next() just after next flush"
argument_list|)
expr_stmt|;
name|justFlushed
operator|=
literal|false
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
if|if
condition|(
name|flushIndex
operator|==
name|count
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting flush at flush index "
operator|+
name|flushIndex
argument_list|)
expr_stmt|;
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|hri
operator|.
name|flushcache
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Finishing flush"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed flush cache"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
if|if
condition|(
name|concurrent
condition|)
block|{
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// concurrently flush.
block|}
else|else
block|{
name|t
operator|.
name|run
argument_list|()
expr_stmt|;
comment|// sync flush
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Continuing on after kicking off background flush"
argument_list|)
expr_stmt|;
name|justFlushed
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Found "
operator|+
name|count
operator|+
literal|" items"
argument_list|)
expr_stmt|;
return|return
name|count
return|;
block|}
block|}
end_class

end_unit

