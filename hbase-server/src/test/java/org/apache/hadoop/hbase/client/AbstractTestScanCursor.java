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
name|client
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
name|conf
operator|.
name|Configuration
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
name|CellUtil
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
name|HTestConst
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
name|FilterBase
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
name|StoreScanner
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
name|Threads
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractTestScanCursor
block|{
specifier|protected
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|/**    * Table configuration    */
specifier|protected
specifier|static
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestScanCursor"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|int
name|NUM_ROWS
init|=
literal|5
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testRow"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
index|[]
name|ROWS
init|=
name|HTestConst
operator|.
name|makeNAscii
argument_list|(
name|ROW
argument_list|,
name|NUM_ROWS
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|int
name|NUM_FAMILIES
init|=
literal|2
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
index|[]
name|FAMILIES
init|=
name|HTestConst
operator|.
name|makeNAscii
argument_list|(
name|FAMILY
argument_list|,
name|NUM_FAMILIES
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|int
name|NUM_QUALIFIERS
init|=
literal|2
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier"
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
index|[]
name|QUALIFIERS
init|=
name|HTestConst
operator|.
name|makeNAscii
argument_list|(
name|QUALIFIER
argument_list|,
name|NUM_QUALIFIERS
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
name|int
name|VALUE_SIZE
init|=
literal|10
decl_stmt|;
specifier|protected
specifier|static
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|createMaxByteArray
argument_list|(
name|VALUE_SIZE
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|int
name|TIMEOUT
init|=
literal|4000
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD
argument_list|,
name|TIMEOUT
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_TIMEOUT_KEY
argument_list|,
name|TIMEOUT
argument_list|)
expr_stmt|;
comment|// Check the timeout condition after every cell
name|conf
operator|.
name|setLong
argument_list|(
name|StoreScanner
operator|.
name|HBASE_CELLS_SCANNED_PER_HEARTBEAT_CHECK
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|createTestTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|ROWS
argument_list|,
name|FAMILIES
argument_list|,
name|QUALIFIERS
argument_list|,
name|VALUE
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|createTestTable
parameter_list|(
name|TableName
name|name
parameter_list|,
name|byte
index|[]
index|[]
name|rows
parameter_list|,
name|byte
index|[]
index|[]
name|families
parameter_list|,
name|byte
index|[]
index|[]
name|qualifiers
parameter_list|,
name|byte
index|[]
name|cellValue
parameter_list|)
throws|throws
name|IOException
block|{
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|name
argument_list|,
name|families
argument_list|)
operator|.
name|put
argument_list|(
name|createPuts
argument_list|(
name|rows
argument_list|,
name|families
argument_list|,
name|qualifiers
argument_list|,
name|cellValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|List
argument_list|<
name|Put
argument_list|>
name|createPuts
parameter_list|(
name|byte
index|[]
index|[]
name|rows
parameter_list|,
name|byte
index|[]
index|[]
name|families
parameter_list|,
name|byte
index|[]
index|[]
name|qualifiers
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|row
init|=
literal|0
init|;
name|row
operator|<
name|rows
operator|.
name|length
condition|;
name|row
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|rows
index|[
name|row
index|]
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|fam
init|=
literal|0
init|;
name|fam
operator|<
name|families
operator|.
name|length
condition|;
name|fam
operator|++
control|)
block|{
for|for
control|(
name|int
name|qual
init|=
literal|0
init|;
name|qual
operator|<
name|qualifiers
operator|.
name|length
condition|;
name|qual
operator|++
control|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|rows
index|[
name|row
index|]
argument_list|,
name|families
index|[
name|fam
index|]
argument_list|,
name|qualifiers
index|[
name|qual
index|]
argument_list|,
name|qual
argument_list|,
name|value
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
name|puts
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
return|return
name|puts
return|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
specifier|final
class|class
name|SparseFilter
extends|extends
name|FilterBase
block|{
specifier|private
specifier|final
name|boolean
name|reversed
decl_stmt|;
specifier|public
name|SparseFilter
parameter_list|(
name|boolean
name|reversed
parameter_list|)
block|{
name|this
operator|.
name|reversed
operator|=
name|reversed
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterCell
parameter_list|(
specifier|final
name|Cell
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|Threads
operator|.
name|sleep
argument_list|(
name|TIMEOUT
operator|/
literal|2
operator|+
literal|100
argument_list|)
expr_stmt|;
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|c
argument_list|)
argument_list|,
name|ROWS
index|[
name|reversed
condition|?
literal|0
else|:
name|NUM_ROWS
operator|-
literal|1
index|]
argument_list|)
condition|?
name|ReturnCode
operator|.
name|INCLUDE
else|:
name|ReturnCode
operator|.
name|SKIP
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|reversed
condition|?
operator|new
name|byte
index|[]
block|{
literal|1
block|}
else|:
operator|new
name|byte
index|[]
block|{
literal|0
block|}
return|;
block|}
specifier|public
specifier|static
name|Filter
name|parseFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|pbBytes
parameter_list|)
block|{
return|return
operator|new
name|SparseFilter
argument_list|(
name|pbBytes
index|[
literal|0
index|]
operator|!=
literal|0
argument_list|)
return|;
block|}
block|}
specifier|protected
name|Scan
name|createScanWithSparseFilter
parameter_list|()
block|{
return|return
operator|new
name|Scan
argument_list|()
operator|.
name|setMaxResultSize
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|setCaching
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|setNeedCursorResult
argument_list|(
literal|true
argument_list|)
operator|.
name|setAllowPartialResults
argument_list|(
literal|true
argument_list|)
operator|.
name|setFilter
argument_list|(
operator|new
name|SparseFilter
argument_list|(
literal|false
argument_list|)
argument_list|)
return|;
block|}
specifier|protected
name|Scan
name|createReversedScanWithSparseFilter
parameter_list|()
block|{
return|return
operator|new
name|Scan
argument_list|()
operator|.
name|setMaxResultSize
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|setCaching
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|setReversed
argument_list|(
literal|true
argument_list|)
operator|.
name|setNeedCursorResult
argument_list|(
literal|true
argument_list|)
operator|.
name|setAllowPartialResults
argument_list|(
literal|true
argument_list|)
operator|.
name|setFilter
argument_list|(
operator|new
name|SparseFilter
argument_list|(
literal|true
argument_list|)
argument_list|)
return|;
block|}
specifier|protected
name|Scan
name|createScanWithSizeLimit
parameter_list|()
block|{
return|return
operator|new
name|Scan
argument_list|()
operator|.
name|setMaxResultSize
argument_list|(
literal|1
argument_list|)
operator|.
name|setCaching
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|setNeedCursorResult
argument_list|(
literal|true
argument_list|)
return|;
block|}
block|}
end_class

end_unit

