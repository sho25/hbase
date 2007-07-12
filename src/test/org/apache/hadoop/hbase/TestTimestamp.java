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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/** Tests user specifyable time stamps */
end_comment

begin_class
specifier|public
class|class
name|TestTimestamp
extends|extends
name|HBaseClusterTestCase
block|{
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
name|String
name|COLUMN_NAME
init|=
literal|"contents:"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"test"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VERSION1
init|=
literal|"version1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|LATEST
init|=
literal|"latest"
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
index|[]
name|COLUMNS
init|=
block|{
name|COLUMN
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Text
name|TABLE
init|=
operator|new
name|Text
argument_list|(
name|TABLE_NAME
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
specifier|private
name|HClient
name|client
decl_stmt|;
comment|/** constructor */
specifier|public
name|TestTimestamp
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
name|client
operator|=
operator|new
name|HClient
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
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
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|COLUMN_NAME
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|client
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
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
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
comment|/** the test */
specifier|public
name|void
name|testTimestamp
parameter_list|()
block|{
try|try
block|{
name|client
operator|.
name|openTable
argument_list|(
name|TABLE
argument_list|)
expr_stmt|;
comment|// store a value specifying an update time
name|long
name|lockid
init|=
name|client
operator|.
name|startUpdate
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|client
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|COLUMN
argument_list|,
name|VERSION1
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|commit
argument_list|(
name|lockid
argument_list|,
name|T0
argument_list|)
expr_stmt|;
comment|// store a value specifying 'now' as the update time
name|lockid
operator|=
name|client
operator|.
name|startUpdate
argument_list|(
name|ROW
argument_list|)
expr_stmt|;
name|client
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
name|COLUMN
argument_list|,
name|LATEST
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|commit
argument_list|(
name|lockid
argument_list|)
expr_stmt|;
comment|// delete values older than T1
name|lockid
operator|=
name|client
operator|.
name|startUpdate
argument_list|(
name|ROW
argument_list|)
expr_stmt|;
name|client
operator|.
name|delete
argument_list|(
name|lockid
argument_list|,
name|COLUMN
argument_list|)
expr_stmt|;
name|client
operator|.
name|commit
argument_list|(
name|lockid
argument_list|,
name|T1
argument_list|)
expr_stmt|;
comment|// now retrieve...
comment|// the most recent version:
name|byte
index|[]
name|bytes
init|=
name|client
operator|.
name|get
argument_list|(
name|ROW
argument_list|,
name|COLUMN
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|bytes
operator|!=
literal|null
operator|&&
name|bytes
operator|.
name|length
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|LATEST
operator|.
name|equals
argument_list|(
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// any version<= time T1
name|byte
index|[]
index|[]
name|values
init|=
name|client
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
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|values
argument_list|)
expr_stmt|;
comment|// the version from T0
name|values
operator|=
name|client
operator|.
name|get
argument_list|(
name|ROW
argument_list|,
name|COLUMN
argument_list|,
name|T0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|values
operator|.
name|length
operator|==
literal|1
operator|&&
name|VERSION1
operator|.
name|equals
argument_list|(
operator|new
name|String
argument_list|(
name|values
index|[
literal|0
index|]
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// flush everything out to disk
name|HRegionServer
name|s
init|=
name|cluster
operator|.
name|regionServers
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|r
range|:
name|s
operator|.
name|onlineRegions
operator|.
name|values
argument_list|()
control|)
block|{
name|r
operator|.
name|flushcache
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|// now retrieve...
comment|// the most recent version:
name|bytes
operator|=
name|client
operator|.
name|get
argument_list|(
name|ROW
argument_list|,
name|COLUMN
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|bytes
operator|!=
literal|null
operator|&&
name|bytes
operator|.
name|length
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|LATEST
operator|.
name|equals
argument_list|(
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// any version<= time T1
name|values
operator|=
name|client
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
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|values
argument_list|)
expr_stmt|;
comment|// the version from T0
name|values
operator|=
name|client
operator|.
name|get
argument_list|(
name|ROW
argument_list|,
name|COLUMN
argument_list|,
name|T0
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|values
operator|.
name|length
operator|==
literal|1
operator|&&
name|VERSION1
operator|.
name|equals
argument_list|(
operator|new
name|String
argument_list|(
name|values
index|[
literal|0
index|]
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// three versions older than now
name|values
operator|=
name|client
operator|.
name|get
argument_list|(
name|ROW
argument_list|,
name|COLUMN
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|values
operator|.
name|length
operator|==
literal|1
operator|&&
name|LATEST
operator|.
name|equals
argument_list|(
operator|new
name|String
argument_list|(
name|values
index|[
literal|0
index|]
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test scanners
name|HScannerInterface
name|scanner
init|=
name|client
operator|.
name|obtainScanner
argument_list|(
name|COLUMNS
argument_list|,
name|HClient
operator|.
name|EMPTY_START_ROW
argument_list|)
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
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|count
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
operator|.
name|size
argument_list|()
argument_list|,
literal|1
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
name|scanner
operator|=
name|client
operator|.
name|obtainScanner
argument_list|(
name|COLUMNS
argument_list|,
name|HClient
operator|.
name|EMPTY_START_ROW
argument_list|,
name|T1
argument_list|)
expr_stmt|;
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
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|count
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
operator|.
name|size
argument_list|()
argument_list|,
literal|0
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
name|scanner
operator|=
name|client
operator|.
name|obtainScanner
argument_list|(
name|COLUMNS
argument_list|,
name|HClient
operator|.
name|EMPTY_START_ROW
argument_list|,
name|T0
argument_list|)
expr_stmt|;
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
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|count
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|results
operator|.
name|size
argument_list|()
argument_list|,
literal|0
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
name|fail
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

