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
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|ByteStringer
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
name|classification
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
name|ByteBufferInputStream
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
name|HBaseInterfaceAudience
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
name|ByteStringer
operator|.
name|wrap
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
name|ByteStringer
operator|.
name|wrap
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
name|ByteStringer
operator|.
name|wrap
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
name|ByteStringer
operator|.
name|wrap
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
specifier|protected
name|Cell
name|parseCell
parameter_list|()
throws|throws
name|IOException
block|{
name|CellProtos
operator|.
name|Cell
name|pbcell
init|=
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
decl_stmt|;
return|return
name|CellUtil
operator|.
name|createCell
argument_list|(
name|pbcell
operator|.
name|getRow
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|pbcell
operator|.
name|getFamily
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|pbcell
operator|.
name|getQualifier
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|pbcell
operator|.
name|getTimestamp
argument_list|()
argument_list|,
operator|(
name|byte
operator|)
name|pbcell
operator|.
name|getCellType
argument_list|()
operator|.
name|getNumber
argument_list|()
argument_list|,
name|pbcell
operator|.
name|getValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
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
name|ByteBuffer
name|buf
parameter_list|)
block|{
return|return
name|getDecoder
argument_list|(
operator|new
name|ByteBufferInputStream
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

