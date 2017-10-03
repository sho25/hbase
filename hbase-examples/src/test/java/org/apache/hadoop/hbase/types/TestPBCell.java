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
name|types
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
name|CellBuilderType
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
name|ExtendedCellBuilderFactory
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
name|MiscTests
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
name|CellProtos
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
name|PositionedByteRange
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
name|SimplePositionedByteRange
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
block|{
name|SmallTests
operator|.
name|class
block|,
name|MiscTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestPBCell
block|{
specifier|private
specifier|static
specifier|final
name|PBCell
name|CODEC
init|=
operator|new
name|PBCell
argument_list|()
decl_stmt|;
comment|/**    * Basic test to verify utility methods in {@link PBType} and delegation to protobuf works.    */
annotation|@
name|Test
specifier|public
name|void
name|testRoundTrip
parameter_list|()
block|{
specifier|final
name|Cell
name|cell
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
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
literal|"val"
argument_list|)
argument_list|)
decl_stmt|;
name|CellProtos
operator|.
name|Cell
name|c
init|=
name|ProtobufUtil
operator|.
name|toCell
argument_list|(
name|cell
argument_list|)
decl_stmt|,
name|decoded
decl_stmt|;
name|PositionedByteRange
name|pbr
init|=
operator|new
name|SimplePositionedByteRange
argument_list|(
name|c
operator|.
name|getSerializedSize
argument_list|()
argument_list|)
decl_stmt|;
name|pbr
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|int
name|encodedLength
init|=
name|CODEC
operator|.
name|encode
argument_list|(
name|pbr
argument_list|,
name|c
argument_list|)
decl_stmt|;
name|pbr
operator|.
name|setPosition
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|decoded
operator|=
name|CODEC
operator|.
name|decode
argument_list|(
name|pbr
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|encodedLength
argument_list|,
name|pbr
operator|.
name|getPosition
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|equals
argument_list|(
name|cell
argument_list|,
name|ProtobufUtil
operator|.
name|toCell
argument_list|(
name|ExtendedCellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|SHALLOW_COPY
argument_list|)
argument_list|,
name|decoded
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

