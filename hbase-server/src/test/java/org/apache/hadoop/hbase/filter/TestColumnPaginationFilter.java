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
name|filter
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

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
name|DataInputStream
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|Before
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

begin_comment
comment|/**  * Test for the ColumnPaginationFilter, used mainly to test the successful serialization of the filter.  * More test functionality can be found within {@link org.apache.hadoop.hbase.filter.TestFilter#testColumnPaginationFilter()}  */
end_comment

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
name|TestColumnPaginationFilter
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row_1_test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VAL_1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COLUMN_QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
specifier|private
name|Filter
name|columnPaginationFilter
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|columnPaginationFilter
operator|=
name|getColumnPaginationFilter
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Filter
name|getColumnPaginationFilter
parameter_list|()
block|{
return|return
operator|new
name|ColumnPaginationFilter
argument_list|(
literal|1
argument_list|,
literal|0
argument_list|)
return|;
block|}
specifier|private
name|Filter
name|serializationTest
parameter_list|(
name|Filter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
name|HBaseProtos
operator|.
name|Filter
name|filterProto
init|=
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|filter
argument_list|)
decl_stmt|;
name|Filter
name|newFilter
init|=
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|filterProto
argument_list|)
decl_stmt|;
return|return
name|newFilter
return|;
block|}
comment|/**      * The more specific functionality tests are contained within the TestFilters class.  This class is mainly for testing      * serialization      *      * @param filter      * @throws Exception      */
specifier|private
name|void
name|basicFilterTests
parameter_list|(
name|ColumnPaginationFilter
name|filter
parameter_list|)
throws|throws
name|Exception
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|COLUMN_FAMILY
argument_list|,
name|COLUMN_QUALIFIER
argument_list|,
name|VAL_1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"basicFilter1"
argument_list|,
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|kv
argument_list|)
operator|==
name|Filter
operator|.
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
block|}
comment|/**      * Tests serialization      * @throws Exception      */
annotation|@
name|Test
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|Filter
name|newFilter
init|=
name|serializationTest
argument_list|(
name|columnPaginationFilter
argument_list|)
decl_stmt|;
name|basicFilterTests
argument_list|(
operator|(
name|ColumnPaginationFilter
operator|)
name|newFilter
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

