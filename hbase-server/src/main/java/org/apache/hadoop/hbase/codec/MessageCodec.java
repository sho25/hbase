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
name|codec
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|ExtendedCellBuilderFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|ByteBuffInputStream
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
name|nio
operator|.
name|ByteBuff
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
name|ExtendedCellBuilder
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
name|HBaseInterfaceAudience
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
name|protobuf
operator|.
name|UnsafeByteOperations
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
name|shaded
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|CellProtos
import|;
end_import

begin_comment
comment|/**  * Codec that just writes out Cell as a protobuf Cell Message.  Does not write the mvcc stamp.  * Use a different codec if you want that in the stream.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|MessageCodec
implements|implements
name|Codec
block|{
specifier|static
class|class
name|MessageEncoder
extends|extends
name|BaseEncoder
block|{
name|MessageEncoder
parameter_list|(
specifier|final
name|OutputStream
name|out
parameter_list|)
block|{
name|super
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
name|checkFlushed
argument_list|()
expr_stmt|;
name|CellProtos
operator|.
name|Cell
operator|.
name|Builder
name|builder
init|=
name|CellProtos
operator|.
name|Cell
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
comment|// This copies bytes from Cell to ByteString.  I don't see anyway around the copy.
comment|// ByteString is final.
name|builder
operator|.
name|setRow
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|cell
operator|.
name|getRowArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getRowLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setFamily
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|cell
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setQualifier
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|cell
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setTimestamp
argument_list|(
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setCellType
argument_list|(
name|CellProtos
operator|.
name|CellType
operator|.
name|valueOf
argument_list|(
name|cell
operator|.
name|getTypeByte
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setValue
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|CellProtos
operator|.
name|Cell
name|pbcell
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|pbcell
operator|.
name|writeDelimitedTo
argument_list|(
name|this
operator|.
name|out
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|MessageDecoder
extends|extends
name|BaseDecoder
block|{
specifier|private
specifier|final
name|ExtendedCellBuilder
name|cellBuilder
init|=
name|ExtendedCellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|SHALLOW_COPY
argument_list|)
decl_stmt|;
name|MessageDecoder
parameter_list|(
specifier|final
name|InputStream
name|in
parameter_list|)
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|Cell
name|parseCell
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|ProtobufUtil
operator|.
name|toCell
argument_list|(
name|cellBuilder
argument_list|,
name|CellProtos
operator|.
name|Cell
operator|.
name|parseDelimitedFrom
argument_list|(
name|this
operator|.
name|in
argument_list|)
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Decoder
name|getDecoder
parameter_list|(
name|InputStream
name|is
parameter_list|)
block|{
return|return
operator|new
name|MessageDecoder
argument_list|(
name|is
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Decoder
name|getDecoder
parameter_list|(
name|ByteBuff
name|buf
parameter_list|)
block|{
return|return
name|getDecoder
argument_list|(
operator|new
name|ByteBuffInputStream
argument_list|(
name|buf
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Encoder
name|getEncoder
parameter_list|(
name|OutputStream
name|os
parameter_list|)
block|{
return|return
operator|new
name|MessageEncoder
argument_list|(
name|os
argument_list|)
return|;
block|}
block|}
end_class

end_unit

