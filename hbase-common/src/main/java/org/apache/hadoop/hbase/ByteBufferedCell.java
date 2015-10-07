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
comment|/**  * This class is a server side extension to the {@link Cell} interface. It is used when the  * Cell is backed by a {@link ByteBuffer}: i.e.<code>cell instanceof ByteBufferedCell</code>.  *  *<p>This class has getters for the row, column family, column qualifier, value and tags hosting  * ByteBuffers. It also has getters of the *position* within a ByteBuffer where these  * field bytes begin. These are needed because a single ByteBuffer may back one or many Cell  * instances -- it depends on the implementation -- so the ByteBuffer position as returned by  * {@link ByteBuffer#arrayOffset()} cannot be relied upon. Also, do not confuse these position  * methods with the getXXXOffset methods from the super Interface, {@link Cell}; dependent up on  * implementation, the Cell getXXXOffset methods can return the same value as a call to its  * equivalent position method from below BUT they can also stray; if a ByteBufferedCell, use the  * below position methods to find where a field begins.  *  *<p>Use the getXXXLength methods from Cell to find a fields length.  *  *<p>A Cell object can be of this type only on the server side.  *  *<p>WARNING: If a Cell is backed by an offheap ByteBuffer, any call to getXXXArray() will result  * in a temporary byte array creation and a bytes copy. Avoid these allocations by using the  * appropriate Cell access server-side: i.e. ByteBufferedCell when backed by a ByteBuffer and Cell  * when it is not.  */
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
specifier|public
specifier|abstract
name|ByteBuffer
name|getRowByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return Position in the {@link ByteBuffer} where row bytes start    */
specifier|public
specifier|abstract
name|int
name|getRowPositionInByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return The {@link ByteBuffer} containing the column family bytes.    */
specifier|public
specifier|abstract
name|ByteBuffer
name|getFamilyByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return Position in the {@link ByteBuffer} where column family bytes start    */
specifier|public
specifier|abstract
name|int
name|getFamilyPositionInByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return The {@link ByteBuffer} containing the column qualifier bytes.    */
specifier|public
specifier|abstract
name|ByteBuffer
name|getQualifierByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return Position in the {@link ByteBuffer} where column qualifier bytes start    */
specifier|public
specifier|abstract
name|int
name|getQualifierPositionInByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return The {@link ByteBuffer} containing the value bytes.    */
specifier|public
specifier|abstract
name|ByteBuffer
name|getValueByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return Position in the {@link ByteBuffer} where value bytes start    */
specifier|public
specifier|abstract
name|int
name|getValuePositionInByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return The {@link ByteBuffer} containing the tag bytes.    */
specifier|public
specifier|abstract
name|ByteBuffer
name|getTagsByteBuffer
parameter_list|()
function_decl|;
comment|/**    * @return Position in the {@link ByteBuffer} where tag bytes start    */
specifier|public
specifier|abstract
name|int
name|getTagsPositionInByteBuffer
parameter_list|()
function_decl|;
block|}
end_class

end_unit

