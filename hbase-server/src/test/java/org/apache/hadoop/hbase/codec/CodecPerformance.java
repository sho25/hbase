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
name|IOException
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
name|codec
operator|.
name|CellCodec
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
name|codec
operator|.
name|Codec
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
name|codec
operator|.
name|KeyValueCodec
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
name|codec
operator|.
name|MessageCodec
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
name|CellOutputStream
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
comment|/**  * Do basic codec performance eval.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|CodecPerformance
block|{
comment|/** @deprecated LOG variable would be made private. since 1.2, remove in 3.0 */
annotation|@
name|Deprecated
specifier|public
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|CodecPerformance
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
name|Cell
index|[]
name|getCells
parameter_list|(
specifier|final
name|int
name|howMany
parameter_list|)
block|{
name|Cell
index|[]
name|cells
init|=
operator|new
name|Cell
index|[
name|howMany
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
name|howMany
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|index
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|index
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
argument_list|,
name|index
argument_list|,
name|index
argument_list|)
decl_stmt|;
name|cells
index|[
name|i
index|]
operator|=
name|kv
expr_stmt|;
block|}
return|return
name|cells
return|;
block|}
specifier|static
name|int
name|getRoughSize
parameter_list|(
specifier|final
name|Cell
index|[]
name|cells
parameter_list|)
block|{
name|int
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Cell
name|c
range|:
name|cells
control|)
block|{
name|size
operator|+=
name|c
operator|.
name|getRowLength
argument_list|()
operator|+
name|c
operator|.
name|getFamilyLength
argument_list|()
operator|+
name|c
operator|.
name|getQualifierLength
argument_list|()
operator|+
name|c
operator|.
name|getValueLength
argument_list|()
expr_stmt|;
name|size
operator|+=
name|Bytes
operator|.
name|SIZEOF_LONG
operator|+
name|Bytes
operator|.
name|SIZEOF_BYTE
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
specifier|static
name|byte
index|[]
name|runEncoderTest
parameter_list|(
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|int
name|initialBufferSize
parameter_list|,
specifier|final
name|ByteArrayOutputStream
name|baos
parameter_list|,
specifier|final
name|CellOutputStream
name|encoder
parameter_list|,
specifier|final
name|Cell
index|[]
name|cells
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
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
name|cells
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|encoder
operator|.
name|write
argument_list|(
name|cells
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|encoder
operator|.
name|flush
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|""
operator|+
name|index
operator|+
literal|" encoded count="
operator|+
name|cells
operator|.
name|length
operator|+
literal|" in "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|+
literal|"ms for encoder "
operator|+
name|encoder
argument_list|)
expr_stmt|;
comment|// Ensure we did not have to grow the backing buffer.
name|assertTrue
argument_list|(
name|baos
operator|.
name|size
argument_list|()
operator|<
name|initialBufferSize
argument_list|)
expr_stmt|;
return|return
name|baos
operator|.
name|toByteArray
argument_list|()
return|;
block|}
specifier|static
name|Cell
index|[]
name|runDecoderTest
parameter_list|(
specifier|final
name|int
name|index
parameter_list|,
specifier|final
name|int
name|count
parameter_list|,
specifier|final
name|CellScanner
name|decoder
parameter_list|)
throws|throws
name|IOException
block|{
name|Cell
index|[]
name|cells
init|=
operator|new
name|Cell
index|[
name|count
index|]
decl_stmt|;
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|decoder
operator|.
name|advance
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|cells
index|[
name|i
index|]
operator|=
name|decoder
operator|.
name|current
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|""
operator|+
name|index
operator|+
literal|" decoded count="
operator|+
name|cells
operator|.
name|length
operator|+
literal|" in "
operator|+
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
operator|+
literal|"ms for decoder "
operator|+
name|decoder
argument_list|)
expr_stmt|;
comment|// Ensure we did not have to grow the backing buffer.
name|assertTrue
argument_list|(
name|cells
operator|.
name|length
operator|==
name|count
argument_list|)
expr_stmt|;
return|return
name|cells
return|;
block|}
specifier|static
name|void
name|verifyCells
parameter_list|(
specifier|final
name|Cell
index|[]
name|input
parameter_list|,
specifier|final
name|Cell
index|[]
name|output
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|input
operator|.
name|length
argument_list|,
name|output
operator|.
name|length
argument_list|)
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
name|input
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|input
index|[
name|i
index|]
operator|.
name|equals
argument_list|(
name|output
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
specifier|static
name|void
name|doCodec
parameter_list|(
specifier|final
name|Codec
name|codec
parameter_list|,
specifier|final
name|Cell
index|[]
name|cells
parameter_list|,
specifier|final
name|int
name|cycles
parameter_list|,
specifier|final
name|int
name|count
parameter_list|,
specifier|final
name|int
name|initialBufferSize
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|bytes
init|=
literal|null
decl_stmt|;
name|Cell
index|[]
name|cellsDecoded
init|=
literal|null
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
name|cycles
condition|;
name|i
operator|++
control|)
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|(
name|initialBufferSize
argument_list|)
decl_stmt|;
name|Codec
operator|.
name|Encoder
name|encoder
init|=
name|codec
operator|.
name|getEncoder
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|bytes
operator|=
name|runEncoderTest
argument_list|(
name|i
argument_list|,
name|initialBufferSize
argument_list|,
name|baos
argument_list|,
name|encoder
argument_list|,
name|cells
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|cycles
condition|;
name|i
operator|++
control|)
block|{
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|Codec
operator|.
name|Decoder
name|decoder
init|=
name|codec
operator|.
name|getDecoder
argument_list|(
name|bais
argument_list|)
decl_stmt|;
name|cellsDecoded
operator|=
name|CodecPerformance
operator|.
name|runDecoderTest
argument_list|(
name|i
argument_list|,
name|count
argument_list|,
name|decoder
argument_list|)
expr_stmt|;
block|}
name|verifyCells
argument_list|(
name|cells
argument_list|,
name|cellsDecoded
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
comment|// How many Cells to encode/decode on each cycle.
specifier|final
name|int
name|count
init|=
literal|100000
decl_stmt|;
comment|// How many times to do an operation; repeat gives hotspot chance to warm up.
specifier|final
name|int
name|cycles
init|=
literal|30
decl_stmt|;
name|Cell
index|[]
name|cells
init|=
name|getCells
argument_list|(
name|count
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|getRoughSize
argument_list|(
name|cells
argument_list|)
decl_stmt|;
name|int
name|initialBufferSize
init|=
literal|2
operator|*
name|size
decl_stmt|;
comment|// Multiply by 2 to ensure we don't have to grow buffer
comment|// Test KeyValue codec.
name|doCodec
argument_list|(
operator|new
name|KeyValueCodec
argument_list|()
argument_list|,
name|cells
argument_list|,
name|cycles
argument_list|,
name|count
argument_list|,
name|initialBufferSize
argument_list|)
expr_stmt|;
name|doCodec
argument_list|(
operator|new
name|CellCodec
argument_list|()
argument_list|,
name|cells
argument_list|,
name|cycles
argument_list|,
name|count
argument_list|,
name|initialBufferSize
argument_list|)
expr_stmt|;
name|doCodec
argument_list|(
operator|new
name|MessageCodec
argument_list|()
argument_list|,
name|cells
argument_list|,
name|cycles
argument_list|,
name|count
argument_list|,
name|initialBufferSize
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

