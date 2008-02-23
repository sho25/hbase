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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|io
operator|.
name|BatchUpdate
import|;
end_import

begin_comment
comment|/** test the scanner API at all levels */
end_comment

begin_class
specifier|public
class|class
name|TestScannerAPI
extends|extends
name|HBaseClusterTestCase
block|{
specifier|private
specifier|final
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
literal|"a:"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"b:"
argument_list|)
block|}
decl_stmt|;
specifier|private
specifier|final
name|Text
name|startRow
init|=
operator|new
name|Text
argument_list|(
literal|"0"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|values
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * @throws Exception    */
specifier|public
name|TestScannerAPI
parameter_list|()
throws|throws
name|Exception
block|{
name|super
argument_list|()
expr_stmt|;
try|try
block|{
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
name|columns
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"a:1"
argument_list|)
argument_list|,
literal|"1"
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|values
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"1"
argument_list|)
argument_list|,
name|columns
argument_list|)
expr_stmt|;
name|columns
operator|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
expr_stmt|;
name|columns
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"a:2"
argument_list|)
argument_list|,
literal|"2"
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|columns
operator|.
name|put
argument_list|(
operator|new
name|Text
argument_list|(
literal|"b:2"
argument_list|)
argument_list|,
literal|"2"
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
throw|throw
name|e
throw|;
block|}
block|}
comment|/**    * @throws IOException    */
specifier|public
name|void
name|testApi
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|String
name|tableName
init|=
name|getName
argument_list|()
decl_stmt|;
comment|// Create table
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|tableDesc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
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
name|columns
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|tableDesc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|columns
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
name|tableDesc
argument_list|)
expr_stmt|;
comment|// Insert values
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
name|getName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Text
argument_list|,
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|>
name|row
range|:
name|values
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
name|row
operator|.
name|getKey
argument_list|()
argument_list|)
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
name|val
range|:
name|row
operator|.
name|getValue
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|b
operator|.
name|put
argument_list|(
name|val
operator|.
name|getKey
argument_list|()
argument_list|,
name|val
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|commit
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Map
argument_list|<
name|Text
argument_list|,
name|HRegion
argument_list|>
name|regions
init|=
name|cluster
operator|.
name|getRegionThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getOnlineRegions
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
name|HRegion
argument_list|>
name|e
range|:
name|regions
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isMetaRegion
argument_list|()
condition|)
block|{
name|region
operator|=
name|e
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
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
name|IOException
name|iox
init|=
operator|new
name|IOException
argument_list|(
literal|"error finding region"
argument_list|)
decl_stmt|;
name|iox
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|iox
throw|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"null"
argument_list|)
name|HScannerInterface
name|scanner
init|=
name|region
operator|.
name|getScanner
argument_list|(
name|columns
argument_list|,
name|startRow
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
block|{
name|verify
argument_list|(
name|scanner
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
name|table
operator|.
name|obtainScanner
argument_list|(
name|columns
argument_list|,
name|startRow
argument_list|)
expr_stmt|;
try|try
block|{
name|verify
argument_list|(
name|scanner
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
name|table
operator|.
name|obtainScanner
argument_list|(
name|columns
argument_list|,
name|startRow
argument_list|)
expr_stmt|;
try|try
block|{
for|for
control|(
name|Iterator
argument_list|<
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
argument_list|>
name|iterator
init|=
name|scanner
operator|.
name|iterator
argument_list|()
init|;
name|iterator
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
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
name|row
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|HStoreKey
name|key
init|=
name|row
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"row key"
argument_list|,
name|values
operator|.
name|containsKey
argument_list|(
name|key
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
name|row
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|columnValues
init|=
name|values
operator|.
name|get
argument_list|(
name|key
operator|.
name|getRow
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|columnValues
operator|.
name|size
argument_list|()
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
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
name|columnValues
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Text
name|column
init|=
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"column"
argument_list|,
name|results
operator|.
name|containsKey
argument_list|(
name|column
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"value"
argument_list|,
name|Arrays
operator|.
name|equals
argument_list|(
name|columnValues
operator|.
name|get
argument_list|(
name|column
argument_list|)
argument_list|,
name|results
operator|.
name|get
argument_list|(
name|column
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
specifier|private
name|void
name|verify
parameter_list|(
name|HScannerInterface
name|scanner
parameter_list|)
throws|throws
name|IOException
block|{
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|SortedMap
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
name|Text
name|row
init|=
name|key
operator|.
name|getRow
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"row key"
argument_list|,
name|values
operator|.
name|containsKey
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|SortedMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|columnValues
init|=
name|values
operator|.
name|get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|columnValues
operator|.
name|size
argument_list|()
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
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
name|columnValues
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Text
name|column
init|=
name|e
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"column"
argument_list|,
name|results
operator|.
name|containsKey
argument_list|(
name|column
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"value"
argument_list|,
name|Arrays
operator|.
name|equals
argument_list|(
name|columnValues
operator|.
name|get
argument_list|(
name|column
argument_list|)
argument_list|,
name|results
operator|.
name|get
argument_list|(
name|column
argument_list|)
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
block|}
end_class

end_unit

