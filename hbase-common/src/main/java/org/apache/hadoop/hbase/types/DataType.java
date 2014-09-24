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
name|classification
operator|.
name|InterfaceStability
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
name|Order
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
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  *<p>  * {@code DataType} is the base class for all HBase data types. Data  * type implementations are designed to be serialized to and deserialized from  * byte[]. Serialized representations can retain the natural sort ordering of  * the source object, when a suitable encoding is supported by the underlying  * implementation. This is a desirable feature for use in rowkeys and column  * qualifiers.  *</p>  *<p>  * {@code DataType}s are different from Hadoop {@link Writable}s in two  * significant ways. First, {@code DataType} describes how to serialize a  * value, it does not encapsulate a serialized value. Second, {@code DataType}  * implementations provide hints to consumers about relationships between the  * POJOs they represent and richness of the encoded representation.  *</p>  *<p>  * Data type instances are designed to be stateless, thread-safe, and reused.  * Implementations should provide {@code static final} instances corresponding  * to each variation on configurable parameters. This is to encourage and  * simplify instance reuse. For instance, order-preserving types should provide  * static ASCENDING and DESCENDING instances. It is also encouraged for  * implementations operating on Java primitive types to provide primitive  * implementations of the {@code encode} and {@code decode} methods. This  * advice is a performance consideration to clients reading and writing values  * in tight loops.  *</p>  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|DataType
parameter_list|<
name|T
parameter_list|>
block|{
comment|/**    * Indicates whether this instance writes encoded {@code byte[]}'s    * which preserve the natural sort order of the unencoded value.    * @return {@code true} when natural order is preserved,    *         {@code false} otherwise.    */
specifier|public
name|boolean
name|isOrderPreserving
parameter_list|()
function_decl|;
comment|/**    * Retrieve the sort {@link Order} imposed by this data type, or null when    * natural ordering is not preserved. Value is either ascending or    * descending. Default is assumed to be {@link Order#ASCENDING}.    */
specifier|public
name|Order
name|getOrder
parameter_list|()
function_decl|;
comment|/**    * Indicates whether this instance supports encoding null values. This    * depends on the implementation details of the encoding format. All    * {@code DataType}s that support null should treat null as comparing    * less than any non-null value for default sort ordering purposes.    * @return {@code true} when null is supported, {@code false} otherwise.    */
specifier|public
name|boolean
name|isNullable
parameter_list|()
function_decl|;
comment|/**    * Indicates whether this instance is able to skip over it's encoded value.    * {@code DataType}s that are not skippable can only be used as the    * right-most field of a {@link Struct}.    */
specifier|public
name|boolean
name|isSkippable
parameter_list|()
function_decl|;
comment|/**    * Inform consumers how long the encoded {@code byte[]} will be.    * @param val The value to check.    * @return the number of bytes required to encode {@code val}.a    */
specifier|public
name|int
name|encodedLength
parameter_list|(
name|T
name|val
parameter_list|)
function_decl|;
comment|/**    * Inform consumers over what type this {@code DataType} operates. Useful    * when working with bare {@code DataType} instances.    */
specifier|public
name|Class
argument_list|<
name|T
argument_list|>
name|encodedClass
parameter_list|()
function_decl|;
comment|/**    * Skip {@code src}'s position forward over one encoded value.    * @param src the buffer containing the encoded value.    * @return number of bytes skipped.    */
specifier|public
name|int
name|skip
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
function_decl|;
comment|/**    * Read an instance of {@code T} from the buffer {@code src}.    * @param src the buffer containing the encoded value.    */
specifier|public
name|T
name|decode
parameter_list|(
name|PositionedByteRange
name|src
parameter_list|)
function_decl|;
comment|/**    * Write instance {@code val} into buffer {@code dst}.    * @param dst the buffer containing the encoded value.    * @param val the value to encode onto {@code dst}.    * @return number of bytes written.    */
specifier|public
name|int
name|encode
parameter_list|(
name|PositionedByteRange
name|dst
parameter_list|,
name|T
name|val
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

