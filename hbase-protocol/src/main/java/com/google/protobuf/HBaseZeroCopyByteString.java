begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|com
operator|.
name|google
operator|.
name|protobuf
package|;
end_package

begin_comment
comment|// This is a lie.
end_comment

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
comment|/**  * Helper class to extract byte arrays from {@link ByteString} without copy.  *<p>  * Without this protobufs would force us to copy every single byte array out  * of the objects de-serialized from the wire (which already do one copy, on  * top of the copies the JVM does to go from kernel buffer to C buffer and  * from C buffer to JVM buffer).  *  * @since 0.96.1  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|HBaseZeroCopyByteString
extends|extends
name|LiteralByteString
block|{
comment|// Gotten from AsyncHBase code base with permission.
comment|/** Private constructor so this class cannot be instantiated. */
specifier|private
name|HBaseZeroCopyByteString
parameter_list|()
block|{
name|super
argument_list|(
literal|null
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Should never be here."
argument_list|)
throw|;
block|}
comment|/**    * Wraps a byte array in a {@link ByteString} without copying it.    * @param array array to be wrapped    * @return wrapped array    */
specifier|public
specifier|static
name|ByteString
name|wrap
parameter_list|(
specifier|final
name|byte
index|[]
name|array
parameter_list|)
block|{
return|return
operator|new
name|LiteralByteString
argument_list|(
name|array
argument_list|)
return|;
block|}
comment|/**    * Wraps a subset of a byte array in a {@link ByteString} without copying it.    * @param array array to be wrapped    * @param offset from    * @param length length    * @return wrapped array    */
specifier|public
specifier|static
name|ByteString
name|wrap
parameter_list|(
specifier|final
name|byte
index|[]
name|array
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
operator|new
name|BoundedByteString
argument_list|(
name|array
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
comment|// TODO:
comment|// ZeroCopyLiteralByteString.wrap(this.buf, 0, this.count);
comment|/**    * Extracts the byte array from the given {@link ByteString} without copy.    * @param buf A buffer from which to extract the array.  This buffer must be    *            actually an instance of a {@code LiteralByteString}.    * @return byte[] representation    */
specifier|public
specifier|static
name|byte
index|[]
name|zeroCopyGetBytes
parameter_list|(
specifier|final
name|ByteString
name|buf
parameter_list|)
block|{
if|if
condition|(
name|buf
operator|instanceof
name|LiteralByteString
condition|)
block|{
return|return
operator|(
operator|(
name|LiteralByteString
operator|)
name|buf
operator|)
operator|.
name|bytes
return|;
block|}
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Need a LiteralByteString, got a "
operator|+
name|buf
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

