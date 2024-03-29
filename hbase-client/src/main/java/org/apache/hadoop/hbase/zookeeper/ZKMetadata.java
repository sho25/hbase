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
name|zookeeper
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadLocalRandom
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * The metadata append to the start of data on zookeeper.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ZKMetadata
block|{
specifier|private
name|ZKMetadata
parameter_list|()
block|{   }
comment|// The metadata attached to each piece of data has the format:
comment|//<magic> 1-byte constant
comment|//<id length> 4-byte big-endian integer (length of next field)
comment|//<id> identifier corresponding uniquely to this process
comment|// It is prepended to the data supplied by the user.
comment|// the magic number is to be backward compatible
specifier|private
specifier|static
specifier|final
name|byte
name|MAGIC
init|=
operator|(
name|byte
operator|)
literal|0XFF
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAGIC_SIZE
init|=
name|Bytes
operator|.
name|SIZEOF_BYTE
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ID_LENGTH_OFFSET
init|=
name|MAGIC_SIZE
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ID_LENGTH_SIZE
init|=
name|Bytes
operator|.
name|SIZEOF_INT
decl_stmt|;
specifier|public
specifier|static
name|byte
index|[]
name|appendMetaData
parameter_list|(
name|byte
index|[]
name|id
parameter_list|,
name|byte
index|[]
name|data
parameter_list|)
block|{
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|data
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|data
return|;
block|}
name|byte
index|[]
name|salt
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextLong
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|idLength
init|=
name|id
operator|.
name|length
operator|+
name|salt
operator|.
name|length
decl_stmt|;
name|byte
index|[]
name|newData
init|=
operator|new
name|byte
index|[
name|MAGIC_SIZE
operator|+
name|ID_LENGTH_SIZE
operator|+
name|idLength
operator|+
name|data
operator|.
name|length
index|]
decl_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putByte
argument_list|(
name|newData
argument_list|,
name|pos
argument_list|,
name|MAGIC
argument_list|)
expr_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putInt
argument_list|(
name|newData
argument_list|,
name|pos
argument_list|,
name|idLength
argument_list|)
expr_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putBytes
argument_list|(
name|newData
argument_list|,
name|pos
argument_list|,
name|id
argument_list|,
literal|0
argument_list|,
name|id
operator|.
name|length
argument_list|)
expr_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putBytes
argument_list|(
name|newData
argument_list|,
name|pos
argument_list|,
name|salt
argument_list|,
literal|0
argument_list|,
name|salt
operator|.
name|length
argument_list|)
expr_stmt|;
name|pos
operator|=
name|Bytes
operator|.
name|putBytes
argument_list|(
name|newData
argument_list|,
name|pos
argument_list|,
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|newData
return|;
block|}
specifier|public
specifier|static
name|byte
index|[]
name|removeMetaData
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
block|{
if|if
condition|(
name|data
operator|==
literal|null
operator|||
name|data
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|data
return|;
block|}
comment|// check the magic data; to be backward compatible
name|byte
name|magic
init|=
name|data
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|magic
operator|!=
name|MAGIC
condition|)
block|{
return|return
name|data
return|;
block|}
name|int
name|idLength
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|data
argument_list|,
name|ID_LENGTH_OFFSET
argument_list|)
decl_stmt|;
name|int
name|dataLength
init|=
name|data
operator|.
name|length
operator|-
name|MAGIC_SIZE
operator|-
name|ID_LENGTH_SIZE
operator|-
name|idLength
decl_stmt|;
name|int
name|dataOffset
init|=
name|MAGIC_SIZE
operator|+
name|ID_LENGTH_SIZE
operator|+
name|idLength
decl_stmt|;
name|byte
index|[]
name|newData
init|=
operator|new
name|byte
index|[
name|dataLength
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|data
argument_list|,
name|dataOffset
argument_list|,
name|newData
argument_list|,
literal|0
argument_list|,
name|dataLength
argument_list|)
expr_stmt|;
return|return
name|newData
return|;
block|}
block|}
end_class

end_unit

