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
name|filter
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
name|assertTrue
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|KeyValue
operator|.
name|Type
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
name|TestCellUtil
operator|.
name|ByteBufferCellImpl
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
name|KeyOnlyFilter
operator|.
name|KeyOnlyByteBufferCell
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
name|KeyOnlyFilter
operator|.
name|KeyOnlyCell
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
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestKeyOnlyFilter
block|{
specifier|private
specifier|final
name|boolean
name|lenAsVal
decl_stmt|;
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|paramList
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
decl_stmt|;
block|{
name|paramList
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|false
block|}
argument_list|)
expr_stmt|;
name|paramList
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|true
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|paramList
return|;
block|}
specifier|public
name|TestKeyOnlyFilter
parameter_list|(
name|boolean
name|lenAsVal
parameter_list|)
block|{
name|this
operator|.
name|lenAsVal
operator|=
name|lenAsVal
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testKeyOnly
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|r
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|f
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|q
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|v
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val1"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|tags
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"tag1"
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|r
argument_list|,
name|f
argument_list|,
name|q
argument_list|,
literal|0
argument_list|,
name|q
operator|.
name|length
argument_list|,
literal|1234L
argument_list|,
name|Type
operator|.
name|Put
argument_list|,
name|v
argument_list|,
literal|0
argument_list|,
name|v
operator|.
name|length
argument_list|,
name|tags
argument_list|)
decl_stmt|;
name|ByteBuffer
name|buffer
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|)
decl_stmt|;
name|ByteBufferCellImpl
name|bbCell
init|=
operator|new
name|ByteBufferCellImpl
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|buffer
operator|.
name|remaining
argument_list|()
argument_list|)
decl_stmt|;
comment|// KV format:<keylen:4><valuelen:4><key:keylen><value:valuelen>
comment|// Rebuild as:<keylen:4><0:4><key:keylen>
name|int
name|dataLen
init|=
name|lenAsVal
condition|?
name|Bytes
operator|.
name|SIZEOF_INT
else|:
literal|0
decl_stmt|;
name|int
name|keyOffset
init|=
operator|(
literal|2
operator|*
name|Bytes
operator|.
name|SIZEOF_INT
operator|)
decl_stmt|;
name|int
name|keyLen
init|=
name|KeyValueUtil
operator|.
name|keyLength
argument_list|(
name|kv
argument_list|)
decl_stmt|;
name|byte
index|[]
name|newBuffer
init|=
operator|new
name|byte
index|[
name|keyLen
operator|+
name|keyOffset
operator|+
name|dataLen
index|]
decl_stmt|;
name|Bytes
operator|.
name|putInt
argument_list|(
name|newBuffer
argument_list|,
literal|0
argument_list|,
name|keyLen
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|putInt
argument_list|(
name|newBuffer
argument_list|,
name|Bytes
operator|.
name|SIZEOF_INT
argument_list|,
name|dataLen
argument_list|)
expr_stmt|;
name|KeyValueUtil
operator|.
name|appendKeyTo
argument_list|(
name|kv
argument_list|,
name|newBuffer
argument_list|,
name|keyOffset
argument_list|)
expr_stmt|;
if|if
condition|(
name|lenAsVal
condition|)
block|{
name|Bytes
operator|.
name|putInt
argument_list|(
name|newBuffer
argument_list|,
name|newBuffer
operator|.
name|length
operator|-
name|dataLen
argument_list|,
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|KeyValue
name|KeyOnlyKeyValue
init|=
operator|new
name|KeyValue
argument_list|(
name|newBuffer
argument_list|)
decl_stmt|;
name|KeyOnlyCell
name|keyOnlyCell
init|=
operator|new
name|KeyOnlyCell
argument_list|(
name|kv
argument_list|,
name|lenAsVal
argument_list|)
decl_stmt|;
name|KeyOnlyByteBufferCell
name|keyOnlyByteBufferedCell
init|=
operator|new
name|KeyOnlyByteBufferCell
argument_list|(
name|bbCell
argument_list|,
name|lenAsVal
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingRows
argument_list|(
name|KeyOnlyKeyValue
argument_list|,
name|keyOnlyCell
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingRows
argument_list|(
name|KeyOnlyKeyValue
argument_list|,
name|keyOnlyByteBufferedCell
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|KeyOnlyKeyValue
argument_list|,
name|keyOnlyCell
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|KeyOnlyKeyValue
argument_list|,
name|keyOnlyByteBufferedCell
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|KeyOnlyKeyValue
argument_list|,
name|keyOnlyCell
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|KeyOnlyKeyValue
argument_list|,
name|keyOnlyByteBufferedCell
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingValue
argument_list|(
name|KeyOnlyKeyValue
argument_list|,
name|keyOnlyCell
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|KeyOnlyKeyValue
operator|.
name|getValueLength
argument_list|()
operator|==
name|keyOnlyByteBufferedCell
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|keyOnlyByteBufferedCell
operator|.
name|getValueLength
argument_list|()
operator|>
literal|0
condition|)
block|{
name|assertTrue
argument_list|(
name|CellUtil
operator|.
name|matchingValue
argument_list|(
name|KeyOnlyKeyValue
argument_list|,
name|keyOnlyByteBufferedCell
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|KeyOnlyKeyValue
operator|.
name|getTimestamp
argument_list|()
operator|==
name|keyOnlyCell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|KeyOnlyKeyValue
operator|.
name|getTimestamp
argument_list|()
operator|==
name|keyOnlyByteBufferedCell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|KeyOnlyKeyValue
operator|.
name|getTypeByte
argument_list|()
operator|==
name|keyOnlyCell
operator|.
name|getTypeByte
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|KeyOnlyKeyValue
operator|.
name|getTypeByte
argument_list|()
operator|==
name|keyOnlyByteBufferedCell
operator|.
name|getTypeByte
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|KeyOnlyKeyValue
operator|.
name|getTagsLength
argument_list|()
operator|==
name|keyOnlyCell
operator|.
name|getTagsLength
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|KeyOnlyKeyValue
operator|.
name|getTagsLength
argument_list|()
operator|==
name|keyOnlyByteBufferedCell
operator|.
name|getTagsLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

