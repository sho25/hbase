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
operator|.
name|regionserver
package|;
end_package

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
name|java
operator|.
name|util
operator|.
name|Map
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
name|KeyValueUtil
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
name|io
operator|.
name|compress
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
name|junit
operator|.
name|Assert
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
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestBlocksScanned
extends|extends
name|HBaseTestCase
block|{
specifier|private
specifier|static
name|byte
index|[]
name|TABLE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"TestBlocksScanned"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|COL
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"col"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|START_KEY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|END_KEY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzz"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|int
name|BLOCK_SIZE
init|=
literal|70
decl_stmt|;
specifier|private
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
literal|null
decl_stmt|;
specifier|private
specifier|static
name|HTableDescriptor
name|TESTTABLEDESC
init|=
literal|null
decl_stmt|;
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
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|TESTTABLEDESC
operator|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE
argument_list|)
argument_list|)
expr_stmt|;
name|TESTTABLEDESC
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
operator|.
name|setMaxVersions
argument_list|(
literal|10
argument_list|)
operator|.
name|setBlockCacheEnabled
argument_list|(
literal|true
argument_list|)
operator|.
name|setBlocksize
argument_list|(
name|BLOCK_SIZE
argument_list|)
operator|.
name|setCompressionType
argument_list|(
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBlocksScanned
parameter_list|()
throws|throws
name|Exception
block|{
name|HRegion
name|r
init|=
name|createNewHRegion
argument_list|(
name|TESTTABLEDESC
argument_list|,
name|START_KEY
argument_list|,
name|END_KEY
argument_list|,
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|addContent
argument_list|(
name|r
argument_list|,
name|FAMILY
argument_list|,
name|COL
argument_list|)
expr_stmt|;
name|r
operator|.
name|flushcache
argument_list|()
expr_stmt|;
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
literal|"aaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaz"
argument_list|)
argument_list|)
decl_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|COL
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
literal|1
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
name|List
argument_list|<
name|Cell
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
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
empty_stmt|;
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
name|int
name|expectResultSize
init|=
literal|'z'
operator|-
literal|'a'
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|expectResultSize
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|kvPerBlock
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
name|BLOCK_SIZE
operator|/
operator|(
name|double
operator|)
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|kvPerBlock
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

