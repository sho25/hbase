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
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseTestCase
operator|.
name|assertByteEquals
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
name|nio
operator|.
name|ByteBuffer
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|CellComparator
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
name|CellScanner
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
name|testclassification
operator|.
name|ClientTests
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
name|ClassRule
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
name|SmallTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestResult
extends|extends
name|TestCase
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
name|TestResult
operator|.
name|class
argument_list|)
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
name|TestResult
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|static
name|KeyValue
index|[]
name|genKVs
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|,
specifier|final
name|long
name|timestamp
parameter_list|,
specifier|final
name|int
name|cols
parameter_list|)
block|{
name|KeyValue
index|[]
name|kvs
init|=
operator|new
name|KeyValue
index|[
name|cols
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
name|cols
condition|;
name|i
operator|++
control|)
block|{
name|kvs
index|[
name|i
index|]
operator|=
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
name|timestamp
argument_list|,
name|Bytes
operator|.
name|add
argument_list|(
name|value
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|kvs
return|;
block|}
specifier|static
specifier|final
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
comment|/**    * Run some tests to ensure Result acts like a proper CellScanner.    * @throws IOException    */
specifier|public
name|void
name|testResultAsCellScanner
parameter_list|()
throws|throws
name|IOException
block|{
name|Cell
index|[]
name|cells
init|=
name|genKVs
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|value
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|cells
argument_list|,
name|CellComparator
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|Result
operator|.
name|create
argument_list|(
name|cells
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|r
argument_list|,
name|cells
argument_list|)
expr_stmt|;
comment|// Assert I run over same result multiple times.
name|assertSame
argument_list|(
name|r
operator|.
name|cellScanner
argument_list|()
argument_list|,
name|cells
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|r
operator|.
name|cellScanner
argument_list|()
argument_list|,
name|cells
argument_list|)
expr_stmt|;
comment|// Assert we are not creating new object when doing cellscanner
name|assertTrue
argument_list|(
name|r
operator|==
name|r
operator|.
name|cellScanner
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|assertSame
parameter_list|(
specifier|final
name|CellScanner
name|cellScanner
parameter_list|,
specifier|final
name|Cell
index|[]
name|cells
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|cellScanner
operator|.
name|advance
argument_list|()
condition|)
block|{
name|assertTrue
argument_list|(
name|cells
index|[
name|count
index|]
operator|.
name|equals
argument_list|(
name|cellScanner
operator|.
name|current
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|count
operator|++
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|cells
operator|.
name|length
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testBasicGetColumn
parameter_list|()
throws|throws
name|Exception
block|{
name|KeyValue
index|[]
name|kvs
init|=
name|genKVs
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|value
argument_list|,
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|kvs
argument_list|,
name|CellComparator
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|Result
operator|.
name|create
argument_list|(
name|kvs
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
literal|100
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|byte
index|[]
name|qf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|ks
init|=
name|r
operator|.
name|getColumnCells
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|ks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|ks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|r
operator|.
name|getColumnLatestCell
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testCurrentOnEmptyCell
parameter_list|()
throws|throws
name|IOException
block|{
name|Result
name|r
init|=
name|Result
operator|.
name|create
argument_list|(
operator|new
name|Cell
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|r
operator|.
name|advance
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|r
operator|.
name|current
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testAdvanceTwiceOnEmptyCell
parameter_list|()
throws|throws
name|IOException
block|{
name|Result
name|r
init|=
name|Result
operator|.
name|create
argument_list|(
operator|new
name|Cell
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|r
operator|.
name|advance
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|r
operator|.
name|advance
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"NoSuchElementException should have been thrown!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchElementException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"As expected: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testMultiVersionGetColumn
parameter_list|()
throws|throws
name|Exception
block|{
name|KeyValue
index|[]
name|kvs1
init|=
name|genKVs
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|value
argument_list|,
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|KeyValue
index|[]
name|kvs2
init|=
name|genKVs
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|value
argument_list|,
literal|200
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|KeyValue
index|[]
name|kvs
init|=
operator|new
name|KeyValue
index|[
name|kvs1
operator|.
name|length
operator|+
name|kvs2
operator|.
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|kvs1
argument_list|,
literal|0
argument_list|,
name|kvs
argument_list|,
literal|0
argument_list|,
name|kvs1
operator|.
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|kvs2
argument_list|,
literal|0
argument_list|,
name|kvs
argument_list|,
name|kvs1
operator|.
name|length
argument_list|,
name|kvs2
operator|.
name|length
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|kvs
argument_list|,
name|CellComparator
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|Result
operator|.
name|create
argument_list|(
name|kvs
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
literal|100
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|byte
index|[]
name|qf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|ks
init|=
name|r
operator|.
name|getColumnCells
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|ks
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|ks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|200
argument_list|,
name|ks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ks
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|r
operator|.
name|getColumnLatestCell
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testBasicGetValue
parameter_list|()
throws|throws
name|Exception
block|{
name|KeyValue
index|[]
name|kvs
init|=
name|genKVs
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|value
argument_list|,
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|kvs
argument_list|,
name|CellComparator
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|Result
operator|.
name|create
argument_list|(
name|kvs
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
literal|100
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|byte
index|[]
name|qf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertByteEquals
argument_list|(
name|Bytes
operator|.
name|add
argument_list|(
name|value
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|,
name|r
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|containsColumn
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testMultiVersionGetValue
parameter_list|()
throws|throws
name|Exception
block|{
name|KeyValue
index|[]
name|kvs1
init|=
name|genKVs
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|value
argument_list|,
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|KeyValue
index|[]
name|kvs2
init|=
name|genKVs
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|value
argument_list|,
literal|200
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|KeyValue
index|[]
name|kvs
init|=
operator|new
name|KeyValue
index|[
name|kvs1
operator|.
name|length
operator|+
name|kvs2
operator|.
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|kvs1
argument_list|,
literal|0
argument_list|,
name|kvs
argument_list|,
literal|0
argument_list|,
name|kvs1
operator|.
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|kvs2
argument_list|,
literal|0
argument_list|,
name|kvs
argument_list|,
name|kvs1
operator|.
name|length
argument_list|,
name|kvs2
operator|.
name|length
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|kvs
argument_list|,
name|CellComparator
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|Result
operator|.
name|create
argument_list|(
name|kvs
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
literal|100
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|byte
index|[]
name|qf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertByteEquals
argument_list|(
name|Bytes
operator|.
name|add
argument_list|(
name|value
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|,
name|r
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|r
operator|.
name|containsColumn
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testBasicLoadValue
parameter_list|()
throws|throws
name|Exception
block|{
name|KeyValue
index|[]
name|kvs
init|=
name|genKVs
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|value
argument_list|,
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|kvs
argument_list|,
name|CellComparator
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
name|Result
name|r
init|=
name|Result
operator|.
name|create
argument_list|(
name|kvs
argument_list|)
decl_stmt|;
name|ByteBuffer
name|loadValueBuffer
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|1024
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
literal|100
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|byte
index|[]
name|qf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|loadValueBuffer
operator|.
name|clear
argument_list|()
expr_stmt|;
name|r
operator|.
name|loadValue
argument_list|(
name|family
argument_list|,
name|qf
argument_list|,
name|loadValueBuffer
argument_list|)
expr_stmt|;
name|loadValueBuffer
operator|.
name|flip
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|loadValueBuffer
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|add
argument_list|(
name|value
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|add
argument_list|(
name|value
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|r
operator|.
name|getValueAsByteBuffer
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testMultiVersionLoadValue
parameter_list|()
throws|throws
name|Exception
block|{
name|KeyValue
index|[]
name|kvs1
init|=
name|genKVs
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|value
argument_list|,
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|KeyValue
index|[]
name|kvs2
init|=
name|genKVs
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|value
argument_list|,
literal|200
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|KeyValue
index|[]
name|kvs
init|=
operator|new
name|KeyValue
index|[
name|kvs1
operator|.
name|length
operator|+
name|kvs2
operator|.
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|kvs1
argument_list|,
literal|0
argument_list|,
name|kvs
argument_list|,
literal|0
argument_list|,
name|kvs1
operator|.
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|kvs2
argument_list|,
literal|0
argument_list|,
name|kvs
argument_list|,
name|kvs1
operator|.
name|length
argument_list|,
name|kvs2
operator|.
name|length
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|kvs
argument_list|,
name|CellComparator
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
name|ByteBuffer
name|loadValueBuffer
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|1024
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|Result
operator|.
name|create
argument_list|(
name|kvs
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
literal|100
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|byte
index|[]
name|qf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|loadValueBuffer
operator|.
name|clear
argument_list|()
expr_stmt|;
name|r
operator|.
name|loadValue
argument_list|(
name|family
argument_list|,
name|qf
argument_list|,
name|loadValueBuffer
argument_list|)
expr_stmt|;
name|loadValueBuffer
operator|.
name|flip
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|loadValueBuffer
argument_list|,
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|add
argument_list|(
name|value
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|add
argument_list|(
name|value
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|r
operator|.
name|getValueAsByteBuffer
argument_list|(
name|family
argument_list|,
name|qf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Verify that Result.compareResults(...) behaves correctly.    */
specifier|public
name|void
name|testCompareResults
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|value1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"value1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qual
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
name|KeyValue
name|kv1
init|=
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qual
argument_list|,
name|value
argument_list|)
decl_stmt|;
name|KeyValue
name|kv2
init|=
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qual
argument_list|,
name|value1
argument_list|)
decl_stmt|;
name|Result
name|r1
init|=
name|Result
operator|.
name|create
argument_list|(
operator|new
name|KeyValue
index|[]
block|{
name|kv1
block|}
argument_list|)
decl_stmt|;
name|Result
name|r2
init|=
name|Result
operator|.
name|create
argument_list|(
operator|new
name|KeyValue
index|[]
block|{
name|kv2
block|}
argument_list|)
decl_stmt|;
comment|// no exception thrown
name|Result
operator|.
name|compareResults
argument_list|(
name|r1
argument_list|,
name|r1
argument_list|)
expr_stmt|;
try|try
block|{
comment|// these are different (HBASE-4800)
name|Result
operator|.
name|compareResults
argument_list|(
name|r1
argument_list|,
name|r2
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|x
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|x
operator|.
name|getMessage
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"This result was different:"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Verifies that one can't modify instance of EMPTY_RESULT.    */
specifier|public
name|void
name|testEmptyResultIsReadonly
parameter_list|()
block|{
name|Result
name|emptyResult
init|=
name|Result
operator|.
name|EMPTY_RESULT
decl_stmt|;
name|Result
name|otherResult
init|=
operator|new
name|Result
argument_list|()
decl_stmt|;
try|try
block|{
name|emptyResult
operator|.
name|copyFrom
argument_list|(
name|otherResult
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"UnsupportedOperationException should have been thrown!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"As expected: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|emptyResult
operator|.
name|setExists
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"UnsupportedOperationException should have been thrown!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"As expected: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Microbenchmark that compares {@link Result#getValue} and {@link Result#loadValue} performance.    *    * @throws Exception    */
specifier|public
name|void
name|doReadBenchmark
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|n
init|=
literal|5
decl_stmt|;
specifier|final
name|int
name|m
init|=
literal|100000000
decl_stmt|;
name|StringBuilder
name|valueSB
init|=
operator|new
name|StringBuilder
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|valueSB
operator|.
name|append
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|Math
operator|.
name|random
argument_list|()
operator|*
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|StringBuilder
name|rowSB
init|=
operator|new
name|StringBuilder
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
literal|50
condition|;
name|i
operator|++
control|)
block|{
name|rowSB
operator|.
name|append
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|Math
operator|.
name|random
argument_list|()
operator|*
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|KeyValue
index|[]
name|kvs
init|=
name|genKVs
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|rowSB
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|family
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|valueSB
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
literal|1
argument_list|,
name|n
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|kvs
argument_list|,
name|CellComparator
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
name|ByteBuffer
name|loadValueBuffer
init|=
name|ByteBuffer
operator|.
name|allocate
argument_list|(
literal|1024
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|Result
operator|.
name|create
argument_list|(
name|kvs
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|qfs
init|=
operator|new
name|byte
index|[
name|n
index|]
index|[
name|Bytes
operator|.
name|SIZEOF_INT
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
name|n
condition|;
operator|++
name|i
control|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|qfs
index|[
name|i
index|]
argument_list|,
literal|0
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
literal|0
argument_list|,
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|)
expr_stmt|;
block|}
comment|// warm up
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
literal|100000
condition|;
name|k
operator|++
control|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
operator|++
name|i
control|)
block|{
name|r
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qfs
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|loadValueBuffer
operator|.
name|clear
argument_list|()
expr_stmt|;
name|r
operator|.
name|loadValue
argument_list|(
name|family
argument_list|,
name|qfs
index|[
name|i
index|]
argument_list|,
name|loadValueBuffer
argument_list|)
expr_stmt|;
name|loadValueBuffer
operator|.
name|flip
argument_list|()
expr_stmt|;
block|}
block|}
name|System
operator|.
name|gc
argument_list|()
expr_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|m
condition|;
name|k
operator|++
control|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
operator|++
name|i
control|)
block|{
name|loadValueBuffer
operator|.
name|clear
argument_list|()
expr_stmt|;
name|r
operator|.
name|loadValue
argument_list|(
name|family
argument_list|,
name|qfs
index|[
name|i
index|]
argument_list|,
name|loadValueBuffer
argument_list|)
expr_stmt|;
name|loadValueBuffer
operator|.
name|flip
argument_list|()
expr_stmt|;
block|}
block|}
name|long
name|stop
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"loadValue(): "
operator|+
operator|(
name|stop
operator|-
name|start
operator|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|gc
argument_list|()
expr_stmt|;
name|start
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|m
condition|;
name|k
operator|++
control|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
name|i
operator|++
control|)
block|{
name|r
operator|.
name|getValue
argument_list|(
name|family
argument_list|,
name|qfs
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|stop
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"getValue():  "
operator|+
operator|(
name|stop
operator|-
name|start
operator|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Calls non-functional test methods.    *    * @param args    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|TestResult
name|testResult
init|=
operator|new
name|TestResult
argument_list|()
decl_stmt|;
try|try
block|{
name|testResult
operator|.
name|doReadBenchmark
argument_list|()
expr_stmt|;
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
literal|"Unexpected exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

