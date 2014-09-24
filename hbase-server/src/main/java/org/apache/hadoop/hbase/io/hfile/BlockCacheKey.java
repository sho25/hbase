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
name|io
operator|.
name|hfile
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
name|io
operator|.
name|HeapSize
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|ClassSize
import|;
end_import

begin_comment
comment|/**  * Cache Key for use with implementations of {@link BlockCache}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|BlockCacheKey
implements|implements
name|HeapSize
implements|,
name|java
operator|.
name|io
operator|.
name|Serializable
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|5199992013113130534L
decl_stmt|;
specifier|private
specifier|final
name|String
name|hfileName
decl_stmt|;
specifier|private
specifier|final
name|long
name|offset
decl_stmt|;
comment|/**    * Construct a new BlockCacheKey    * @param hfileName The name of the HFile this block belongs to.    * @param offset Offset of the block into the file    */
specifier|public
name|BlockCacheKey
parameter_list|(
name|String
name|hfileName
parameter_list|,
name|long
name|offset
parameter_list|)
block|{
name|this
operator|.
name|hfileName
operator|=
name|hfileName
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|hfileName
operator|.
name|hashCode
argument_list|()
operator|*
literal|127
operator|+
call|(
name|int
call|)
argument_list|(
name|offset
operator|^
operator|(
name|offset
operator|>>>
literal|32
operator|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|instanceof
name|BlockCacheKey
condition|)
block|{
name|BlockCacheKey
name|k
init|=
operator|(
name|BlockCacheKey
operator|)
name|o
decl_stmt|;
return|return
name|offset
operator|==
name|k
operator|.
name|offset
operator|&&
operator|(
name|hfileName
operator|==
literal|null
condition|?
name|k
operator|.
name|hfileName
operator|==
literal|null
else|:
name|hfileName
operator|.
name|equals
argument_list|(
name|k
operator|.
name|hfileName
argument_list|)
operator|)
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s_%d"
argument_list|,
name|hfileName
argument_list|,
name|offset
argument_list|)
return|;
block|}
specifier|public
specifier|static
specifier|final
name|long
name|FIXED_OVERHEAD
init|=
name|ClassSize
operator|.
name|align
argument_list|(
name|ClassSize
operator|.
name|OBJECT
operator|+
name|ClassSize
operator|.
name|REFERENCE
operator|+
comment|// this.hfileName
name|Bytes
operator|.
name|SIZEOF_LONG
argument_list|)
decl_stmt|;
comment|// this.offset
comment|/**    * Strings have two bytes per character due to default Java Unicode encoding    * (hence length times 2).    */
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|ClassSize
operator|.
name|align
argument_list|(
name|FIXED_OVERHEAD
operator|+
name|ClassSize
operator|.
name|STRING
operator|+
literal|2
operator|*
name|hfileName
operator|.
name|length
argument_list|()
argument_list|)
return|;
block|}
comment|// can't avoid this unfortunately
comment|/**    * @return The hfileName portion of this cache key    */
specifier|public
name|String
name|getHfileName
parameter_list|()
block|{
return|return
name|hfileName
return|;
block|}
specifier|public
name|long
name|getOffset
parameter_list|()
block|{
return|return
name|offset
return|;
block|}
block|}
end_class

end_unit

