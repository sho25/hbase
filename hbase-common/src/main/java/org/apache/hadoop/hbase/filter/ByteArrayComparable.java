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
comment|/** Base class for byte array comparators */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
comment|// TODO Now we are deviating a lot from the actual Comparable<byte[]> that this implements, by
comment|// adding special compareTo methods. We have to clean it. Deprecate this class and replace it
comment|// with a more generic one which says it compares bytes (not necessary a byte array only)
comment|// BytesComparable implements Comparable<Byte> will work?
specifier|public
specifier|abstract
class|class
name|ByteArrayComparable
implements|implements
name|Comparable
argument_list|<
name|byte
index|[]
argument_list|>
block|{
name|byte
index|[]
name|value
decl_stmt|;
comment|/**    * Constructor.    * @param value the value to compare against    */
specifier|public
name|ByteArrayComparable
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
block|}
comment|/**    * @return The comparator serialized using pb    */
specifier|public
specifier|abstract
name|byte
index|[]
name|toByteArray
parameter_list|()
function_decl|;
comment|/**    * @param pbBytes A pb serialized {@link ByteArrayComparable} instance    * @return An instance of {@link ByteArrayComparable} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|ByteArrayComparable
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
throw|throw
operator|new
name|DeserializationException
argument_list|(
literal|"parseFrom called on base ByteArrayComparable, but should be called on derived type"
argument_list|)
throw|;
block|}
comment|/**    * @param other    * @return true if and only if the fields of the comparator that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
name|boolean
name|areSerializedFieldsEqual
parameter_list|(
name|ByteArrayComparable
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
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|getValue
argument_list|()
argument_list|,
name|other
operator|.
name|getValue
argument_list|()
argument_list|)
return|;
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
parameter_list|)
block|{
return|return
name|compareTo
argument_list|(
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**    * Special compareTo method for subclasses, to avoid    * copying byte[] unnecessarily.    * @param value byte[] to compare    * @param offset offset into value    * @param length number of bytes to compare    * @return a negative integer, zero, or a positive integer as this object    *         is less than, equal to, or greater than the specified object.    */
specifier|public
specifier|abstract
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
function_decl|;
comment|/**    * Special compareTo method for subclasses, to avoid copying bytes unnecessarily.    * @param value bytes to compare within a ByteBuffer    * @param offset offset into value    * @param length number of bytes to compare    * @return a negative integer, zero, or a positive integer as this object    *         is less than, equal to, or greater than the specified object.    */
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
comment|// For BC, providing a default implementation here which is doing a bytes copy to a temp byte[]
comment|// and calling compareTo(byte[]). Make sure to override this method in subclasses to avoid
comment|// copying bytes unnecessarily.
name|byte
index|[]
name|temp
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromBufferToArray
argument_list|(
name|temp
argument_list|,
name|value
argument_list|,
name|offset
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|compareTo
argument_list|(
name|temp
argument_list|)
return|;
block|}
block|}
end_class

end_unit

