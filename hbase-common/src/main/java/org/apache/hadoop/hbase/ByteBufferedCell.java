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
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * This class is a server side extension to the Cell interface. This is used when the actual Cell  * implementation is backed by {@link ByteBuffer}. This class contain ByteBuffer backed getters for  * row, cf, qualifier, value and tags. Also getters of the position where these field bytes begin. A  * cell object can be of this type only in server side. When the object is of this type, use the  * getXXXByteBuffer() method along with getXXXPositionInByteBuffer(). If cell is backed by off heap  * ByteBuffer the call to getXXXArray() will result is temporary byte array creation and bytes copy  * resulting in lot of garbage.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|ByteBufferedCell
implements|implements
name|Cell
block|{
comment|/**    * @return The {@link ByteBuffer} containing the row bytes.    */
specifier|abstract
name|ByteBuffer
name|getRowByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return Position in the {@link ByteBuffer} where row bytes start    */
specifier|abstract
name|int
name|getRowPositionInByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return The {@link ByteBuffer} containing the column family bytes.    */
specifier|abstract
name|ByteBuffer
name|getFamilyByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return Position in the {@link ByteBuffer} where column family bytes start    */
specifier|abstract
name|int
name|getFamilyPositionInByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return The {@link ByteBuffer} containing the column qualifier bytes.    */
specifier|abstract
name|ByteBuffer
name|getQualifierByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return Position in the {@link ByteBuffer} where column qualifier bytes start    */
specifier|abstract
name|int
name|getQualifierPositionInByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return The {@link ByteBuffer} containing the value bytes.    */
specifier|abstract
name|ByteBuffer
name|getValueByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return Position in the {@link ByteBuffer} where value bytes start    */
specifier|abstract
name|int
name|getValuePositionInByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return The {@link ByteBuffer} containing the tag bytes.    */
specifier|abstract
name|ByteBuffer
name|getTagsByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return Position in the {@link ByteBuffer} where tag bytes start    */
specifier|abstract
name|int
name|getTagsPositionInByteBuffer
parameter_list|()
function_decl|;
block|}
end_class

end_unit

