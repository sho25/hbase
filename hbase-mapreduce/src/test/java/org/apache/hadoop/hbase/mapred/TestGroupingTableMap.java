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
name|mapred
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
name|assertArrayEquals
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
name|assertNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|times
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verify
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verifyNoMoreInteractions
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|verifyZeroInteractions
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|io
operator|.
name|ImmutableBytesWritable
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
name|MapReduceTests
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
name|apache
operator|.
name|hadoop
operator|.
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|OutputCollector
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
name|mapred
operator|.
name|Reporter
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
name|ClassRule
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
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MapReduceTests
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
name|TestGroupingTableMap
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
name|TestGroupingTableMap
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"deprecation"
block|,
literal|"unchecked"
block|}
argument_list|)
specifier|public
name|void
name|shouldNotCallCollectonSinceFindUniqueKeyValueMoreThanOnes
parameter_list|()
throws|throws
name|Exception
block|{
name|GroupingTableMap
name|gTableMap
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Result
name|result
init|=
name|mock
argument_list|(
name|Result
operator|.
name|class
argument_list|)
decl_stmt|;
name|Reporter
name|reporter
init|=
name|mock
argument_list|(
name|Reporter
operator|.
name|class
argument_list|)
decl_stmt|;
name|gTableMap
operator|=
operator|new
name|GroupingTableMap
argument_list|()
expr_stmt|;
name|Configuration
name|cfg
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|cfg
operator|.
name|set
argument_list|(
name|GroupingTableMap
operator|.
name|GROUP_COLUMNS
argument_list|,
literal|"familyA:qualifierA familyB:qualifierB"
argument_list|)
expr_stmt|;
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|cfg
argument_list|)
decl_stmt|;
name|gTableMap
operator|.
name|configure
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
name|byte
index|[]
name|row
init|=
block|{}
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|keyValues
init|=
name|ImmutableList
operator|.
expr|<
name|Cell
operator|>
name|of
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
literal|"familyA"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"qualifierA"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1111"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
literal|"familyA"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"qualifierA"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2222"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
literal|"familyB"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"qualifierB"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3333"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|result
operator|.
name|listCells
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|keyValues
argument_list|)
expr_stmt|;
name|OutputCollector
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
name|outputCollectorMock
init|=
name|mock
argument_list|(
name|OutputCollector
operator|.
name|class
argument_list|)
decl_stmt|;
name|gTableMap
operator|.
name|map
argument_list|(
literal|null
argument_list|,
name|result
argument_list|,
name|outputCollectorMock
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|result
argument_list|)
operator|.
name|listCells
argument_list|()
expr_stmt|;
name|verifyZeroInteractions
argument_list|(
name|outputCollectorMock
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|gTableMap
operator|!=
literal|null
condition|)
name|gTableMap
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"deprecation"
block|,
literal|"unchecked"
block|}
argument_list|)
specifier|public
name|void
name|shouldCreateNewKeyAlthoughExtraKey
parameter_list|()
throws|throws
name|Exception
block|{
name|GroupingTableMap
name|gTableMap
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Result
name|result
init|=
name|mock
argument_list|(
name|Result
operator|.
name|class
argument_list|)
decl_stmt|;
name|Reporter
name|reporter
init|=
name|mock
argument_list|(
name|Reporter
operator|.
name|class
argument_list|)
decl_stmt|;
name|gTableMap
operator|=
operator|new
name|GroupingTableMap
argument_list|()
expr_stmt|;
name|Configuration
name|cfg
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|cfg
operator|.
name|set
argument_list|(
name|GroupingTableMap
operator|.
name|GROUP_COLUMNS
argument_list|,
literal|"familyA:qualifierA familyB:qualifierB"
argument_list|)
expr_stmt|;
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|cfg
argument_list|)
decl_stmt|;
name|gTableMap
operator|.
name|configure
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
name|byte
index|[]
name|row
init|=
block|{}
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|keyValues
init|=
name|ImmutableList
operator|.
expr|<
name|Cell
operator|>
name|of
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
literal|"familyA"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"qualifierA"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"1111"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
literal|"familyB"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"qualifierB"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"2222"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
literal|"familyC"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"qualifierC"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"3333"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|result
operator|.
name|listCells
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|keyValues
argument_list|)
expr_stmt|;
name|OutputCollector
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
name|outputCollectorMock
init|=
name|mock
argument_list|(
name|OutputCollector
operator|.
name|class
argument_list|)
decl_stmt|;
name|gTableMap
operator|.
name|map
argument_list|(
literal|null
argument_list|,
name|result
argument_list|,
name|outputCollectorMock
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|result
argument_list|)
operator|.
name|listCells
argument_list|()
expr_stmt|;
name|verify
argument_list|(
name|outputCollectorMock
argument_list|,
name|times
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|any
argument_list|()
argument_list|,
name|any
argument_list|()
argument_list|)
expr_stmt|;
name|verifyNoMoreInteractions
argument_list|(
name|outputCollectorMock
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|gTableMap
operator|!=
literal|null
condition|)
name|gTableMap
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"deprecation"
block|}
argument_list|)
specifier|public
name|void
name|shouldCreateNewKey
parameter_list|()
throws|throws
name|Exception
block|{
name|GroupingTableMap
name|gTableMap
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Result
name|result
init|=
name|mock
argument_list|(
name|Result
operator|.
name|class
argument_list|)
decl_stmt|;
name|Reporter
name|reporter
init|=
name|mock
argument_list|(
name|Reporter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|bSeparator
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|" "
argument_list|)
decl_stmt|;
name|gTableMap
operator|=
operator|new
name|GroupingTableMap
argument_list|()
expr_stmt|;
name|Configuration
name|cfg
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|cfg
operator|.
name|set
argument_list|(
name|GroupingTableMap
operator|.
name|GROUP_COLUMNS
argument_list|,
literal|"familyA:qualifierA familyB:qualifierB"
argument_list|)
expr_stmt|;
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|cfg
argument_list|)
decl_stmt|;
name|gTableMap
operator|.
name|configure
argument_list|(
name|jobConf
argument_list|)
expr_stmt|;
specifier|final
name|byte
index|[]
name|firstPartKeyValue
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"34879512738945"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|secondPartKeyValue
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"35245142671437"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
block|{}
decl_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|ImmutableList
operator|.
expr|<
name|Cell
operator|>
name|of
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
literal|"familyA"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"qualifierA"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|firstPartKeyValue
argument_list|)
argument_list|,
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
literal|"familyB"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"qualifierB"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|secondPartKeyValue
argument_list|)
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|result
operator|.
name|listCells
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|cells
argument_list|)
expr_stmt|;
specifier|final
name|AtomicBoolean
name|outputCollected
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
name|OutputCollector
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
name|outputCollector
init|=
operator|new
name|OutputCollector
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Result
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|collect
parameter_list|(
name|ImmutableBytesWritable
name|arg
parameter_list|,
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
block|{
name|assertArrayEquals
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|primitives
operator|.
name|Bytes
operator|.
name|concat
argument_list|(
name|firstPartKeyValue
argument_list|,
name|bSeparator
argument_list|,
name|secondPartKeyValue
argument_list|)
argument_list|,
name|arg
operator|.
name|copyBytes
argument_list|()
argument_list|)
expr_stmt|;
name|outputCollected
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|gTableMap
operator|.
name|map
argument_list|(
literal|null
argument_list|,
name|result
argument_list|,
name|outputCollector
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|result
argument_list|)
operator|.
name|listCells
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Output not received"
argument_list|,
name|outputCollected
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|byte
index|[]
name|firstPartValue
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"238947928"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|secondPartValue
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"4678456942345"
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|data
init|=
block|{
name|firstPartValue
block|,
name|secondPartValue
block|}
decl_stmt|;
name|ImmutableBytesWritable
name|byteWritable
init|=
name|gTableMap
operator|.
name|createGroupKey
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|primitives
operator|.
name|Bytes
operator|.
name|concat
argument_list|(
name|firstPartValue
argument_list|,
name|bSeparator
argument_list|,
name|secondPartValue
argument_list|)
argument_list|,
name|byteWritable
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|gTableMap
operator|!=
literal|null
condition|)
name|gTableMap
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"deprecation"
block|}
argument_list|)
specifier|public
name|void
name|shouldReturnNullFromCreateGroupKey
parameter_list|()
throws|throws
name|Exception
block|{
name|GroupingTableMap
name|gTableMap
init|=
literal|null
decl_stmt|;
try|try
block|{
name|gTableMap
operator|=
operator|new
name|GroupingTableMap
argument_list|()
expr_stmt|;
name|assertNull
argument_list|(
name|gTableMap
operator|.
name|createGroupKey
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|gTableMap
operator|!=
literal|null
condition|)
name|gTableMap
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

