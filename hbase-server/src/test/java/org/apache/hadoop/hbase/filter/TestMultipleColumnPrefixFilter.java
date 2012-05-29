begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|filter
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
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
name|HashMap
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
name|regionserver
operator|.
name|InternalScanner
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
name|junit
operator|.
name|Test
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
name|TestMultipleColumnPrefixFilter
block|{
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testMultipleColumnPrefixFilter
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|family
init|=
literal|"Family"
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"TestMultipleColumnPrefixFilter"
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
comment|// HRegionInfo info = new HRegionInfo(htd, null, null, false);
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|info
argument_list|,
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|htd
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rows
init|=
name|generateRandomWords
argument_list|(
literal|100
argument_list|,
literal|"row"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columns
init|=
name|generateRandomWords
argument_list|(
literal|10000
argument_list|,
literal|"column"
argument_list|)
decl_stmt|;
name|long
name|maxTimestamp
init|=
literal|2
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvList
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|prefixMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|prefixMap
operator|.
name|put
argument_list|(
literal|"p"
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|prefixMap
operator|.
name|put
argument_list|(
literal|"q"
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|prefixMap
operator|.
name|put
argument_list|(
literal|"s"
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|valueString
init|=
literal|"ValueString"
decl_stmt|;
for|for
control|(
name|String
name|row
range|:
name|rows
control|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|setWriteToWAL
argument_list|(
literal|false
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|column
range|:
name|columns
control|)
block|{
for|for
control|(
name|long
name|timestamp
init|=
literal|1
init|;
name|timestamp
operator|<=
name|maxTimestamp
condition|;
name|timestamp
operator|++
control|)
block|{
name|KeyValue
name|kv
init|=
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|column
argument_list|,
name|timestamp
argument_list|,
name|valueString
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|kvList
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|prefixMap
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|column
operator|.
name|startsWith
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|prefixMap
operator|.
name|get
argument_list|(
name|s
argument_list|)
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|MultipleColumnPrefixFilter
name|filter
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
name|setMaxVersions
argument_list|()
expr_stmt|;
name|byte
index|[]
index|[]
name|filter_prefix
init|=
operator|new
name|byte
index|[
literal|2
index|]
index|[]
decl_stmt|;
name|filter_prefix
index|[
literal|0
index|]
operator|=
operator|new
name|byte
index|[]
block|{
literal|'p'
block|}
expr_stmt|;
name|filter_prefix
index|[
literal|1
index|]
operator|=
operator|new
name|byte
index|[]
block|{
literal|'q'
block|}
expr_stmt|;
name|filter
operator|=
operator|new
name|MultipleColumnPrefixFilter
argument_list|(
name|filter_prefix
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
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
name|InternalScanner
name|scanner
init|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
condition|)
empty_stmt|;
name|assertEquals
argument_list|(
name|prefixMap
operator|.
name|get
argument_list|(
literal|"p"
argument_list|)
operator|.
name|size
argument_list|()
operator|+
name|prefixMap
operator|.
name|get
argument_list|(
literal|"q"
argument_list|)
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
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultipleColumnPrefixFilterWithManyFamilies
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|family1
init|=
literal|"Family1"
decl_stmt|;
name|String
name|family2
init|=
literal|"Family2"
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"TestMultipleColumnPrefixFilter"
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family1
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
name|family2
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|info
argument_list|,
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|htd
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rows
init|=
name|generateRandomWords
argument_list|(
literal|100
argument_list|,
literal|"row"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columns
init|=
name|generateRandomWords
argument_list|(
literal|10000
argument_list|,
literal|"column"
argument_list|)
decl_stmt|;
name|long
name|maxTimestamp
init|=
literal|3
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvList
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|prefixMap
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|prefixMap
operator|.
name|put
argument_list|(
literal|"p"
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|prefixMap
operator|.
name|put
argument_list|(
literal|"q"
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|prefixMap
operator|.
name|put
argument_list|(
literal|"s"
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|valueString
init|=
literal|"ValueString"
decl_stmt|;
for|for
control|(
name|String
name|row
range|:
name|rows
control|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|setWriteToWAL
argument_list|(
literal|false
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|column
range|:
name|columns
control|)
block|{
for|for
control|(
name|long
name|timestamp
init|=
literal|1
init|;
name|timestamp
operator|<=
name|maxTimestamp
condition|;
name|timestamp
operator|++
control|)
block|{
name|double
name|rand
init|=
name|Math
operator|.
name|random
argument_list|()
decl_stmt|;
name|KeyValue
name|kv
decl_stmt|;
if|if
condition|(
name|rand
operator|<
literal|0.5
condition|)
name|kv
operator|=
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
name|row
argument_list|,
name|family1
argument_list|,
name|column
argument_list|,
name|timestamp
argument_list|,
name|valueString
argument_list|)
expr_stmt|;
else|else
name|kv
operator|=
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
name|row
argument_list|,
name|family2
argument_list|,
name|column
argument_list|,
name|timestamp
argument_list|,
name|valueString
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|kvList
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|s
range|:
name|prefixMap
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|column
operator|.
name|startsWith
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|prefixMap
operator|.
name|get
argument_list|(
name|s
argument_list|)
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|MultipleColumnPrefixFilter
name|filter
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
name|setMaxVersions
argument_list|()
expr_stmt|;
name|byte
index|[]
index|[]
name|filter_prefix
init|=
operator|new
name|byte
index|[
literal|2
index|]
index|[]
decl_stmt|;
name|filter_prefix
index|[
literal|0
index|]
operator|=
operator|new
name|byte
index|[]
block|{
literal|'p'
block|}
expr_stmt|;
name|filter_prefix
index|[
literal|1
index|]
operator|=
operator|new
name|byte
index|[]
block|{
literal|'q'
block|}
expr_stmt|;
name|filter
operator|=
operator|new
name|MultipleColumnPrefixFilter
argument_list|(
name|filter_prefix
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setFilter
argument_list|(
name|filter
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
name|InternalScanner
name|scanner
init|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
condition|)
empty_stmt|;
name|assertEquals
argument_list|(
name|prefixMap
operator|.
name|get
argument_list|(
literal|"p"
argument_list|)
operator|.
name|size
argument_list|()
operator|+
name|prefixMap
operator|.
name|get
argument_list|(
literal|"q"
argument_list|)
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
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultipleColumnPrefixFilterWithColumnPrefixFilter
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|family
init|=
literal|"Family"
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"TestMultipleColumnPrefixFilter"
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|info
argument_list|,
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|htd
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rows
init|=
name|generateRandomWords
argument_list|(
literal|100
argument_list|,
literal|"row"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|columns
init|=
name|generateRandomWords
argument_list|(
literal|10000
argument_list|,
literal|"column"
argument_list|)
decl_stmt|;
name|long
name|maxTimestamp
init|=
literal|2
decl_stmt|;
name|String
name|valueString
init|=
literal|"ValueString"
decl_stmt|;
for|for
control|(
name|String
name|row
range|:
name|rows
control|)
block|{
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|setWriteToWAL
argument_list|(
literal|false
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|column
range|:
name|columns
control|)
block|{
for|for
control|(
name|long
name|timestamp
init|=
literal|1
init|;
name|timestamp
operator|<=
name|maxTimestamp
condition|;
name|timestamp
operator|++
control|)
block|{
name|KeyValue
name|kv
init|=
name|KeyValueTestUtil
operator|.
name|create
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|column
argument_list|,
name|timestamp
argument_list|,
name|valueString
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
name|region
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|MultipleColumnPrefixFilter
name|multiplePrefixFilter
decl_stmt|;
name|Scan
name|scan1
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan1
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|byte
index|[]
index|[]
name|filter_prefix
init|=
operator|new
name|byte
index|[
literal|1
index|]
index|[]
decl_stmt|;
name|filter_prefix
index|[
literal|0
index|]
operator|=
operator|new
name|byte
index|[]
block|{
literal|'p'
block|}
expr_stmt|;
name|multiplePrefixFilter
operator|=
operator|new
name|MultipleColumnPrefixFilter
argument_list|(
name|filter_prefix
argument_list|)
expr_stmt|;
name|scan1
operator|.
name|setFilter
argument_list|(
name|multiplePrefixFilter
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results1
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|InternalScanner
name|scanner1
init|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan1
argument_list|)
decl_stmt|;
while|while
condition|(
name|scanner1
operator|.
name|next
argument_list|(
name|results1
argument_list|)
condition|)
empty_stmt|;
name|ColumnPrefixFilter
name|singlePrefixFilter
decl_stmt|;
name|Scan
name|scan2
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan2
operator|.
name|setMaxVersions
argument_list|()
expr_stmt|;
name|singlePrefixFilter
operator|=
operator|new
name|ColumnPrefixFilter
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"p"
argument_list|)
argument_list|)
expr_stmt|;
name|scan2
operator|.
name|setFilter
argument_list|(
name|singlePrefixFilter
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results2
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|InternalScanner
name|scanner2
init|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan1
argument_list|)
decl_stmt|;
while|while
condition|(
name|scanner2
operator|.
name|next
argument_list|(
name|results2
argument_list|)
condition|)
empty_stmt|;
name|assertEquals
argument_list|(
name|results1
operator|.
name|size
argument_list|()
argument_list|,
name|results2
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HRegion
operator|.
name|closeHRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|generateRandomWords
parameter_list|(
name|int
name|numberOfWords
parameter_list|,
name|String
name|suffix
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|wordSet
init|=
operator|new
name|HashSet
argument_list|<
name|String
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
name|numberOfWords
condition|;
name|i
operator|++
control|)
block|{
name|int
name|lengthOfWords
init|=
call|(
name|int
call|)
argument_list|(
name|Math
operator|.
name|random
argument_list|()
operator|*
literal|2
argument_list|)
operator|+
literal|1
decl_stmt|;
name|char
index|[]
name|wordChar
init|=
operator|new
name|char
index|[
name|lengthOfWords
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|wordChar
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|wordChar
index|[
name|j
index|]
operator|=
call|(
name|char
call|)
argument_list|(
name|Math
operator|.
name|random
argument_list|()
operator|*
literal|26
operator|+
literal|97
argument_list|)
expr_stmt|;
block|}
name|String
name|word
decl_stmt|;
if|if
condition|(
name|suffix
operator|==
literal|null
condition|)
block|{
name|word
operator|=
operator|new
name|String
argument_list|(
name|wordChar
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|word
operator|=
operator|new
name|String
argument_list|(
name|wordChar
argument_list|)
operator|+
name|suffix
expr_stmt|;
block|}
name|wordSet
operator|.
name|add
argument_list|(
name|word
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|wordList
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|wordSet
argument_list|)
decl_stmt|;
return|return
name|wordList
return|;
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

