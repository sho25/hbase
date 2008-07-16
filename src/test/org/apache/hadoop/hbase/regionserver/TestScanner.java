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
name|HServerAddress
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
name|io
operator|.
name|Cell
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
name|COLUMN_FAMILY
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
name|COL_REGIONINFO
block|,
name|HConstants
operator|.
name|COL_SERVER
block|,
name|HConstants
operator|.
name|COL_STARTCODE
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW_KEY
init|=
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HRegionInfo
name|REGION_INFO
init|=
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
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
name|MiniDFSCluster
name|cluster
init|=
literal|null
decl_stmt|;
specifier|private
name|HRegion
name|r
decl_stmt|;
specifier|private
name|HRegionIncommon
name|region
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
name|cluster
operator|=
operator|new
name|MiniDFSCluster
argument_list|(
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
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
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
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|info
operator|.
name|getTableDesc
argument_list|()
operator|.
name|compareTo
argument_list|(
name|REGION_INFO
operator|.
name|getTableDesc
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|results
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
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
name|scanner
operator|=
name|r
operator|.
name|getScanner
argument_list|(
name|scanColumns
index|[
name|i
index|]
argument_list|,
name|FIRST_ROW
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
name|assertTrue
argument_list|(
name|results
operator|.
name|containsKey
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|val
init|=
name|results
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
name|assertTrue
argument_list|(
name|results
operator|.
name|containsKey
argument_list|(
name|HConstants
operator|.
name|COL_STARTCODE
argument_list|)
argument_list|)
expr_stmt|;
name|val
operator|=
name|results
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|COL_STARTCODE
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
name|results
operator|.
name|containsKey
argument_list|(
name|HConstants
operator|.
name|COL_SERVER
argument_list|)
argument_list|)
expr_stmt|;
name|val
operator|=
name|results
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|COL_SERVER
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
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
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
comment|/** Use get to retrieve the HRegionInfo and validate it */
specifier|private
name|void
name|getRegionInfo
parameter_list|()
throws|throws
name|IOException
block|{
name|byte
index|[]
name|bytes
init|=
name|region
operator|.
name|get
argument_list|(
name|ROW_KEY
argument_list|,
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|validateRegionInfo
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
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
name|REGION_INFO
operator|.
name|getTableDesc
argument_list|()
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
name|BatchUpdate
name|batchUpdate
init|=
operator|new
name|BatchUpdate
argument_list|(
name|ROW_KEY
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
operator|.
name|write
argument_list|(
name|s
argument_list|)
expr_stmt|;
name|batchUpdate
operator|.
name|put
argument_list|(
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|,
name|byteStream
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|region
operator|.
name|commit
argument_list|(
name|batchUpdate
argument_list|)
expr_stmt|;
comment|// What we just committed is in the memcache. Verify that we can get
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
name|HServerAddress
name|address
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"foo.bar.com:1234"
argument_list|)
decl_stmt|;
name|batchUpdate
operator|=
operator|new
name|BatchUpdate
argument_list|(
name|ROW_KEY
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
name|Bytes
operator|.
name|toBytes
argument_list|(
name|address
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
name|Bytes
operator|.
name|toBytes
argument_list|(
name|START_CODE
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|commit
argument_list|(
name|batchUpdate
argument_list|)
expr_stmt|;
comment|// Validate that we can still get the HRegionInfo, even though it is in
comment|// an older row on disk and there is a newer row in the memcache
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
operator|new
name|HServerAddress
argument_list|(
literal|"bar.foo.com:4321"
argument_list|)
expr_stmt|;
name|batchUpdate
operator|=
operator|new
name|BatchUpdate
argument_list|(
name|ROW_KEY
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
name|Bytes
operator|.
name|toBytes
argument_list|(
name|address
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|region
operator|.
name|commit
argument_list|(
name|batchUpdate
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
finally|finally
block|{
name|shutdownDfs
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

