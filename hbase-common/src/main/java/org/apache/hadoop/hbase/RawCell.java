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
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
comment|/**  * An extended version of cell that gives more power to CPs  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
specifier|public
interface|interface
name|RawCell
extends|extends
name|Cell
block|{
specifier|static
specifier|final
name|int
name|MAX_TAGS_LENGTH
init|=
operator|(
literal|2
operator|*
name|Short
operator|.
name|MAX_VALUE
operator|)
operator|+
literal|1
decl_stmt|;
comment|/**    * Allows cloning the tags in the cell to a new byte[]    * @return the byte[] having the tags    */
specifier|default
name|byte
index|[]
name|cloneTags
parameter_list|()
block|{
return|return
name|PrivateCellUtil
operator|.
name|cloneTags
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/**    * Creates a list of tags in the current cell    * @return a list of tags    */
specifier|default
name|Iterator
argument_list|<
name|Tag
argument_list|>
name|getTags
parameter_list|()
block|{
return|return
name|PrivateCellUtil
operator|.
name|tagsIterator
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/**    * Returns the specific tag of the given type    * @param type the type of the tag    * @return the specific tag if available or null    */
specifier|default
name|Optional
argument_list|<
name|Tag
argument_list|>
name|getTag
parameter_list|(
name|byte
name|type
parameter_list|)
block|{
return|return
name|PrivateCellUtil
operator|.
name|getTag
argument_list|(
name|this
argument_list|,
name|type
argument_list|)
return|;
block|}
comment|/**    * Check the length of tags. If it is invalid, throw IllegalArgumentException    * @param tagsLength the given length of tags    * @throws IllegalArgumentException if tagslength is invalid    */
specifier|public
specifier|static
name|void
name|checkForTagsLength
parameter_list|(
name|int
name|tagsLength
parameter_list|)
block|{
if|if
condition|(
name|tagsLength
operator|>
name|MAX_TAGS_LENGTH
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"tagslength "
operator|+
name|tagsLength
operator|+
literal|"> "
operator|+
name|MAX_TAGS_LENGTH
argument_list|)
throw|;
block|}
block|}
block|}
end_interface

end_unit

