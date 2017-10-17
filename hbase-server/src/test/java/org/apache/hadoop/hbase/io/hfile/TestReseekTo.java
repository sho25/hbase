begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|hfile
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
name|hadoop
operator|.
name|fs
operator|.
name|FSDataOutputStream
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
name|CellComparatorImpl
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
name|Tag
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
name|ArrayBackedTag
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
name|IOTests
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
name|SmallTests
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

begin_comment
comment|/**  * Test {@link HFileScanner#reseekTo(org.apache.hadoop.hbase.Cell)}  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|IOTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestReseekTo
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
name|testReseekTo
parameter_list|()
throws|throws
name|Exception
block|{
name|testReseekToInternals
argument_list|(
name|TagUsage
operator|.
name|NO_TAG
argument_list|)
expr_stmt|;
name|testReseekToInternals
argument_list|(
name|TagUsage
operator|.
name|ONLY_TAG
argument_list|)
expr_stmt|;
name|testReseekToInternals
argument_list|(
name|TagUsage
operator|.
name|PARTIAL_TAG
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testReseekToInternals
parameter_list|(
name|TagUsage
name|tagUsage
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|ncTFile
init|=
operator|new
name|Path
argument_list|(
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|"basic.hfile"
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|fout
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
operator|.
name|create
argument_list|(
name|ncTFile
argument_list|)
decl_stmt|;
if|if
condition|(
name|tagUsage
operator|!=
name|TagUsage
operator|.
name|NO_TAG
condition|)
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hfile.format.version"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
name|CacheConfig
name|cacheConf
init|=
operator|new
name|CacheConfig
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|HFileContext
name|context
init|=
operator|new
name|HFileContextBuilder
argument_list|()
operator|.
name|withBlockSize
argument_list|(
literal|4000
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HFile
operator|.
name|Writer
name|writer
init|=
name|HFile
operator|.
name|getWriterFactory
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|cacheConf
argument_list|)
operator|.
name|withOutputStream
argument_list|(
name|fout
argument_list|)
operator|.
name|withFileContext
argument_list|(
name|context
argument_list|)
comment|// NOTE: This test is dependent on this deprecated nonstandard comparator
operator|.
name|withComparator
argument_list|(
name|CellComparatorImpl
operator|.
name|COMPARATOR
argument_list|)
operator|.
name|create
argument_list|()
decl_stmt|;
name|int
name|numberOfKeys
init|=
literal|1000
decl_stmt|;
name|String
name|valueString
init|=
literal|"Value"
decl_stmt|;
name|List
argument_list|<
name|Integer
argument_list|>
name|keyList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|valueList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|key
init|=
literal|0
init|;
name|key
operator|<
name|numberOfKeys
condition|;
name|key
operator|++
control|)
block|{
name|String
name|value
init|=
name|valueString
operator|+
name|key
decl_stmt|;
name|KeyValue
name|kv
decl_stmt|;
name|keyList
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|valueList
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|tagUsage
operator|==
name|TagUsage
operator|.
name|NO_TAG
condition|)
block|{
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|tagUsage
operator|==
name|TagUsage
operator|.
name|ONLY_TAG
condition|)
block|{
name|Tag
name|t
init|=
operator|new
name|ArrayBackedTag
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|"myTag1"
argument_list|)
decl_stmt|;
name|Tag
index|[]
name|tags
init|=
operator|new
name|Tag
index|[
literal|1
index|]
decl_stmt|;
name|tags
index|[
literal|0
index|]
operator|=
name|t
expr_stmt|;
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|,
name|tags
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|key
operator|%
literal|4
operator|==
literal|0
condition|)
block|{
name|Tag
name|t
init|=
operator|new
name|ArrayBackedTag
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|"myTag1"
argument_list|)
decl_stmt|;
name|Tag
index|[]
name|tags
init|=
operator|new
name|Tag
index|[
literal|1
index|]
decl_stmt|;
name|tags
index|[
literal|0
index|]
operator|=
name|t
expr_stmt|;
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|,
name|tags
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|fout
operator|.
name|close
argument_list|()
expr_stmt|;
name|HFile
operator|.
name|Reader
name|reader
init|=
name|HFile
operator|.
name|createReader
argument_list|(
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
argument_list|,
name|ncTFile
argument_list|,
name|cacheConf
argument_list|,
literal|true
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|reader
operator|.
name|loadFileInfo
argument_list|()
expr_stmt|;
name|HFileScanner
name|scanner
init|=
name|reader
operator|.
name|getScanner
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|scanner
operator|.
name|seekTo
argument_list|()
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
name|keyList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Integer
name|key
init|=
name|keyList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
name|value
init|=
name|valueList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|scanner
operator|.
name|seekTo
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|value
argument_list|,
name|scanner
operator|.
name|getValueString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|scanner
operator|.
name|seekTo
argument_list|()
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
name|keyList
operator|.
name|size
argument_list|()
condition|;
name|i
operator|+=
literal|10
control|)
block|{
name|Integer
name|key
init|=
name|keyList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
name|value
init|=
name|valueList
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|scanner
operator|.
name|reseekTo
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"i is "
operator|+
name|i
argument_list|,
name|value
argument_list|,
name|scanner
operator|.
name|getValueString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

