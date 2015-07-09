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
name|commons
operator|.
name|io
operator|.
name|IOUtils
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * Basic Cell codec that just writes out all the individual elements of a Cell.  Uses ints  * delimiting all lengths. Profligate. Needs tune up.  * Note: This will not write tags of a Cell.  */
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
name|CellCodec
implements|implements
name|Codec
block|{
specifier|static
class|class
name|CellEncoder
extends|extends
name|BaseEncoder
block|{
name|CellEncoder
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
comment|// Row
name|write
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
expr_stmt|;
comment|// Column family
name|write
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
expr_stmt|;
comment|// Qualifier
name|write
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
expr_stmt|;
comment|// Version
name|this
operator|.
name|out
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Type
name|this
operator|.
name|out
operator|.
name|write
argument_list|(
name|cell
operator|.
name|getTypeByte
argument_list|()
argument_list|)
expr_stmt|;
comment|// Value
name|write
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
expr_stmt|;
comment|// MvccVersion
name|this
operator|.
name|out
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|cell
operator|.
name|getMvccVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Write int length followed by array bytes.      * @param bytes      * @param offset      * @param length      * @throws IOException      */
specifier|private
name|void
name|write
parameter_list|(
specifier|final
name|byte
index|[]
name|bytes
parameter_list|,
specifier|final
name|int
name|offset
parameter_list|,
specifier|final
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO add BB backed os check and do for write. Pass Cell
name|this
operator|.
name|out
operator|.
name|write
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|out
operator|.
name|write
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
class|class
name|CellDecoder
extends|extends
name|BaseDecoder
block|{
specifier|public
name|CellDecoder
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
name|byte
index|[]
name|row
init|=
name|readByteArray
argument_list|(
name|this
operator|.
name|in
argument_list|)
decl_stmt|;
name|byte
index|[]
name|family
init|=
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|byte
index|[]
name|longArray
init|=
operator|new
name|byte
index|[
name|Bytes
operator|.
name|SIZEOF_LONG
index|]
decl_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|this
operator|.
name|in
argument_list|,
name|longArray
argument_list|)
expr_stmt|;
name|long
name|timestamp
init|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|longArray
argument_list|)
decl_stmt|;
name|byte
name|type
init|=
operator|(
name|byte
operator|)
name|this
operator|.
name|in
operator|.
name|read
argument_list|()
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
comment|// Read memstore version
name|byte
index|[]
name|memstoreTSArray
init|=
operator|new
name|byte
index|[
name|Bytes
operator|.
name|SIZEOF_LONG
index|]
decl_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|this
operator|.
name|in
argument_list|,
name|memstoreTSArray
argument_list|)
expr_stmt|;
name|long
name|memstoreTS
init|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|memstoreTSArray
argument_list|)
decl_stmt|;
return|return
name|CellUtil
operator|.
name|createCell
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|timestamp
argument_list|,
name|type
argument_list|,
name|value
argument_list|,
name|memstoreTS
argument_list|)
return|;
block|}
comment|/**      * @return Byte array read from the stream.      * @throws IOException      */
specifier|private
name|byte
index|[]
name|readByteArray
parameter_list|(
specifier|final
name|InputStream
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|intArray
init|=
operator|new
name|byte
index|[
name|Bytes
operator|.
name|SIZEOF_INT
index|]
decl_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|in
argument_list|,
name|intArray
argument_list|)
expr_stmt|;
name|int
name|length
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|intArray
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|IOUtils
operator|.
name|readFully
argument_list|(
name|in
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
return|return
name|bytes
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
name|CellDecoder
argument_list|(
name|is
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
name|CellEncoder
argument_list|(
name|os
argument_list|)
return|;
block|}
block|}
end_class

end_unit

