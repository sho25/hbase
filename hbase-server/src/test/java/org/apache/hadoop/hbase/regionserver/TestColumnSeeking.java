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
name|regionserver
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
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|Collection
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
name|HBaseClassTestRule
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
name|HBaseTestingUtility
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
name|KeyValueTestUtil
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
name|TableName
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
name|ColumnFamilyDescriptor
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
name|ColumnFamilyDescriptorBuilder
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
name|Durability
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
name|client
operator|.
name|TableDescriptorBuilder
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
name|testclassification
operator|.
name|MediumTests
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
name|testclassification
operator|.
name|RegionServerTests
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
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestColumnSeeking
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestColumnSeeking
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
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
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestColumnSeeking
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testDuplicateVersions
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|family
init|=
literal|"Family"
decl_stmt|;
name|byte
index|[]
name|familyBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Family"
argument_list|)
decl_stmt|;
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
name|familyDescriptor
init|=
operator|new
name|ColumnFamilyDescriptorBuilder
operator|.
name|ModifyableColumnFamilyDescriptor
argument_list|(
name|familyBytes
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
literal|1000
argument_list|)
decl_stmt|;
name|familyDescriptor
operator|.
name|setMaxVersions
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
name|tableDescriptor
init|=
operator|new
name|TableDescriptorBuilder
operator|.
name|ModifyableTableDescriptor
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|tableDescriptor
operator|.
name|setColumnFamily
argument_list|(
name|familyDescriptor
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|table
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
comment|// Set this so that the archiver writes to the temp dir as well.
name|HRegion
name|region
init|=
name|TEST_UTIL
operator|.
name|createLocalHRegion
argument_list|(
name|info
argument_list|,
name|tableDescriptor
argument_list|)
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|String
argument_list|>
name|rows
init|=
name|generateRandomWords
argument_list|(
literal|10
argument_list|,
literal|"row"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|allColumns
init|=
name|generateRandomWords
argument_list|(
literal|10
argument_list|,
literal|"column"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|values
init|=
name|generateRandomWords
argument_list|(
literal|100
argument_list|,
literal|"value"
argument_list|)
decl_stmt|;
name|long
name|maxTimestamp
init|=
literal|2
decl_stmt|;
name|double
name|selectPercent
init|=
literal|0.5
decl_stmt|;
name|int
name|numberOfTests
init|=
literal|5
decl_stmt|;
name|double
name|flushPercentage
init|=
literal|0.2
decl_stmt|;
name|double
name|minorPercentage
init|=
literal|0.2
decl_stmt|;
name|double
name|majorPercentage
init|=
literal|0.2
decl_stmt|;
name|double
name|putPercentage
init|=
literal|0.2
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|KeyValue
argument_list|>
name|allKVMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|KeyValue
argument_list|>
index|[]
name|kvMaps
init|=
operator|new
name|HashMap
index|[
name|numberOfTests
index|]
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
index|[]
name|columnLists
init|=
operator|new
name|ArrayList
index|[
name|numberOfTests
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
name|numberOfTests
condition|;
name|i
operator|++
control|)
block|{
name|kvMaps
index|[
name|i
index|]
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|columnLists
index|[
name|i
index|]
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|column
range|:
name|allColumns
control|)
block|{
if|if
condition|(
name|Math
operator|.
name|random
argument_list|()
operator|<
name|selectPercent
condition|)
block|{
name|columnLists
index|[
name|i
index|]
operator|.
name|add
argument_list|(
name|column
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|String
name|value
range|:
name|values
control|)
block|{
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
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|column
range|:
name|allColumns
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
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|Math
operator|.
name|random
argument_list|()
operator|<
name|putPercentage
condition|)
block|{
name|p
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|allKVMap
operator|.
name|put
argument_list|(
name|kv
operator|.
name|getKeyString
argument_list|()
argument_list|,
name|kv
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
name|numberOfTests
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|columnLists
index|[
name|i
index|]
operator|.
name|contains
argument_list|(
name|column
argument_list|)
condition|)
block|{
name|kvMaps
index|[
name|i
index|]
operator|.
name|put
argument_list|(
name|kv
operator|.
name|getKeyString
argument_list|()
argument_list|,
name|kv
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|Math
operator|.
name|random
argument_list|()
operator|<
name|flushPercentage
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Flushing... "
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|Math
operator|.
name|random
argument_list|()
operator|<
name|minorPercentage
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Minor compacting... "
argument_list|)
expr_stmt|;
name|region
operator|.
name|compact
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|Math
operator|.
name|random
argument_list|()
operator|<
name|majorPercentage
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Major compacting... "
argument_list|)
expr_stmt|;
name|region
operator|.
name|compact
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numberOfTests
operator|+
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|Collection
argument_list|<
name|KeyValue
argument_list|>
name|kvSet
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
if|if
condition|(
name|i
operator|<
name|numberOfTests
condition|)
block|{
if|if
condition|(
name|columnLists
index|[
name|i
index|]
operator|.
name|isEmpty
argument_list|()
condition|)
continue|continue;
comment|// HBASE-7700
name|kvSet
operator|=
name|kvMaps
index|[
name|i
index|]
operator|.
name|values
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|column
range|:
name|columnLists
index|[
name|i
index|]
control|)
block|{
name|scan
operator|.
name|addColumn
argument_list|(
name|familyBytes
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"ExplicitColumns scanner"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Columns: "
operator|+
name|columnLists
index|[
name|i
index|]
operator|.
name|size
argument_list|()
operator|+
literal|"  Keys: "
operator|+
name|kvSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|kvSet
operator|=
name|allKVMap
operator|.
name|values
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Wildcard scanner"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Columns: "
operator|+
name|allColumns
operator|.
name|size
argument_list|()
operator|+
literal|"  Keys: "
operator|+
name|kvSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|List
argument_list|<
name|Cell
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|kvSet
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
name|assertTrue
argument_list|(
name|KeyValueTestUtil
operator|.
name|containsIgnoreMvccVersion
argument_list|(
name|results
argument_list|,
name|kvSet
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
specifier|public
name|void
name|testReseeking
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|family
init|=
literal|"Family"
decl_stmt|;
name|byte
index|[]
name|familyBytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Family"
argument_list|)
decl_stmt|;
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|TableDescriptorBuilder
name|tableDescriptorBuilder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|table
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
name|columnFamilyDescriptor
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|family
argument_list|)
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
literal|3
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|tableDescriptorBuilder
operator|.
name|setColumnFamily
argument_list|(
name|columnFamilyDescriptor
argument_list|)
expr_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|table
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
name|TEST_UTIL
operator|.
name|createLocalHRegion
argument_list|(
name|info
argument_list|,
name|tableDescriptorBuilder
operator|.
name|build
argument_list|()
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
literal|10
argument_list|,
literal|"row"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|allColumns
init|=
name|generateRandomWords
argument_list|(
literal|100
argument_list|,
literal|"column"
argument_list|)
decl_stmt|;
name|long
name|maxTimestamp
init|=
literal|2
decl_stmt|;
name|double
name|selectPercent
init|=
literal|0.5
decl_stmt|;
name|int
name|numberOfTests
init|=
literal|5
decl_stmt|;
name|double
name|flushPercentage
init|=
literal|0.2
decl_stmt|;
name|double
name|minorPercentage
init|=
literal|0.2
decl_stmt|;
name|double
name|majorPercentage
init|=
literal|0.2
decl_stmt|;
name|double
name|putPercentage
init|=
literal|0.2
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|KeyValue
argument_list|>
name|allKVMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|KeyValue
argument_list|>
index|[]
name|kvMaps
init|=
operator|new
name|HashMap
index|[
name|numberOfTests
index|]
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
index|[]
name|columnLists
init|=
operator|new
name|ArrayList
index|[
name|numberOfTests
index|]
decl_stmt|;
name|String
name|valueString
init|=
literal|"Value"
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
name|numberOfTests
condition|;
name|i
operator|++
control|)
block|{
name|kvMaps
index|[
name|i
index|]
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|columnLists
index|[
name|i
index|]
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|column
range|:
name|allColumns
control|)
block|{
if|if
condition|(
name|Math
operator|.
name|random
argument_list|()
operator|<
name|selectPercent
condition|)
block|{
name|columnLists
index|[
name|i
index|]
operator|.
name|add
argument_list|(
name|column
argument_list|)
expr_stmt|;
block|}
block|}
block|}
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
name|setDurability
argument_list|(
name|Durability
operator|.
name|SKIP_WAL
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|column
range|:
name|allColumns
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
if|if
condition|(
name|Math
operator|.
name|random
argument_list|()
operator|<
name|putPercentage
condition|)
block|{
name|p
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|allKVMap
operator|.
name|put
argument_list|(
name|kv
operator|.
name|getKeyString
argument_list|()
argument_list|,
name|kv
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
name|numberOfTests
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|columnLists
index|[
name|i
index|]
operator|.
name|contains
argument_list|(
name|column
argument_list|)
condition|)
block|{
name|kvMaps
index|[
name|i
index|]
operator|.
name|put
argument_list|(
name|kv
operator|.
name|getKeyString
argument_list|()
argument_list|,
name|kv
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|Math
operator|.
name|random
argument_list|()
operator|<
name|flushPercentage
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Flushing... "
argument_list|)
expr_stmt|;
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|Math
operator|.
name|random
argument_list|()
operator|<
name|minorPercentage
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Minor compacting... "
argument_list|)
expr_stmt|;
name|region
operator|.
name|compact
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|Math
operator|.
name|random
argument_list|()
operator|<
name|majorPercentage
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Major compacting... "
argument_list|)
expr_stmt|;
name|region
operator|.
name|compact
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numberOfTests
operator|+
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|Collection
argument_list|<
name|KeyValue
argument_list|>
name|kvSet
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
if|if
condition|(
name|i
operator|<
name|numberOfTests
condition|)
block|{
if|if
condition|(
name|columnLists
index|[
name|i
index|]
operator|.
name|isEmpty
argument_list|()
condition|)
continue|continue;
comment|// HBASE-7700
name|kvSet
operator|=
name|kvMaps
index|[
name|i
index|]
operator|.
name|values
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|column
range|:
name|columnLists
index|[
name|i
index|]
control|)
block|{
name|scan
operator|.
name|addColumn
argument_list|(
name|familyBytes
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"ExplicitColumns scanner"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Columns: "
operator|+
name|columnLists
index|[
name|i
index|]
operator|.
name|size
argument_list|()
operator|+
literal|"  Keys: "
operator|+
name|kvSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|kvSet
operator|=
name|allKVMap
operator|.
name|values
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Wildcard scanner"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Columns: "
operator|+
name|allColumns
operator|.
name|size
argument_list|()
operator|+
literal|"  Keys: "
operator|+
name|kvSet
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|List
argument_list|<
name|Cell
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|kvSet
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
name|assertTrue
argument_list|(
name|KeyValueTestUtil
operator|.
name|containsIgnoreMvccVersion
argument_list|(
name|results
argument_list|,
name|kvSet
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HBaseTestingUtility
operator|.
name|closeRegionAndWAL
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
argument_list|<>
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
literal|5
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
argument_list|<>
argument_list|(
name|wordSet
argument_list|)
decl_stmt|;
return|return
name|wordList
return|;
block|}
block|}
end_class

end_unit

