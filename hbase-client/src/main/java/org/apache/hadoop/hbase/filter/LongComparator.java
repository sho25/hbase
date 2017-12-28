begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|InvalidProtocolBufferException
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
name|exceptions
operator|.
name|DeserializationException
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
name|ComparatorProtos
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
name|ByteBufferUtils
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
comment|/**  * A long comparator which numerical compares against the specified byte array  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|LongComparator
extends|extends
name|ByteArrayComparable
block|{
specifier|private
name|long
name|longValue
decl_stmt|;
specifier|public
name|LongComparator
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|longValue
operator|=
name|value
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|long
name|that
init|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|value
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
decl_stmt|;
return|return
name|Long
operator|.
name|compare
argument_list|(
name|longValue
argument_list|,
name|that
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|ByteBuffer
name|value
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|long
name|that
init|=
name|ByteBufferUtils
operator|.
name|toLong
argument_list|(
name|value
argument_list|,
name|offset
argument_list|)
decl_stmt|;
return|return
name|Long
operator|.
name|compare
argument_list|(
name|longValue
argument_list|,
name|that
argument_list|)
return|;
block|}
comment|/**      * @return The comparator serialized using pb      */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
name|ComparatorProtos
operator|.
name|LongComparator
operator|.
name|Builder
name|builder
init|=
name|ComparatorProtos
operator|.
name|LongComparator
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setComparable
argument_list|(
name|ProtobufUtil
operator|.
name|toByteArrayComparable
argument_list|(
name|this
operator|.
name|value
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
comment|/**      * @param pbBytes A pb serialized {@link LongComparator} instance      * @return An instance of {@link LongComparator} made from<code>bytes</code>      * @throws org.apache.hadoop.hbase.exceptions.DeserializationException      * @see #toByteArray      */
specifier|public
specifier|static
name|LongComparator
name|parseFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|pbBytes
parameter_list|)
throws|throws
name|DeserializationException
block|{
name|ComparatorProtos
operator|.
name|LongComparator
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|ComparatorProtos
operator|.
name|LongComparator
operator|.
name|parseFrom
argument_list|(
name|pbBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
operator|new
name|LongComparator
argument_list|(
name|Bytes
operator|.
name|toLong
argument_list|(
name|proto
operator|.
name|getComparable
argument_list|()
operator|.
name|getValue
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * @param other      * @return true if and only if the fields of the comparator that are serialized      * are equal to the corresponding fields in other.  Used for testing.      */
name|boolean
name|areSerializedFieldsEqual
parameter_list|(
name|LongComparator
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
name|this
condition|)
return|return
literal|true
return|;
return|return
name|super
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|other
argument_list|)
return|;
block|}
block|}
end_class

end_unit

