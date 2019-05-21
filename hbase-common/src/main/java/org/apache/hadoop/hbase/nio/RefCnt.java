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
name|nio
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
name|io
operator|.
name|ByteBuffAllocator
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
name|ByteBuffAllocator
operator|.
name|Recycler
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
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|AbstractReferenceCounted
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
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|ReferenceCounted
import|;
end_import

begin_comment
comment|/**  * Maintain an reference count integer inside to track life cycle of {@link ByteBuff}, if the  * reference count become 0, it'll call {@link Recycler#free()} once.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RefCnt
extends|extends
name|AbstractReferenceCounted
block|{
specifier|private
name|Recycler
name|recycler
init|=
name|ByteBuffAllocator
operator|.
name|NONE
decl_stmt|;
comment|/**    * Create an {@link RefCnt} with an initial reference count = 1. If the reference count become    * zero, the recycler will do nothing. Usually, an Heap {@link ByteBuff} will use this kind of    * refCnt to track its life cycle, it help to abstract the code path although it's meaningless to    * use an refCnt for heap ByteBuff.    */
specifier|public
specifier|static
name|RefCnt
name|create
parameter_list|()
block|{
return|return
operator|new
name|RefCnt
argument_list|(
name|ByteBuffAllocator
operator|.
name|NONE
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|RefCnt
name|create
parameter_list|(
name|Recycler
name|recycler
parameter_list|)
block|{
return|return
operator|new
name|RefCnt
argument_list|(
name|recycler
argument_list|)
return|;
block|}
specifier|public
name|RefCnt
parameter_list|(
name|Recycler
name|recycler
parameter_list|)
block|{
name|this
operator|.
name|recycler
operator|=
name|recycler
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
specifier|final
name|void
name|deallocate
parameter_list|()
block|{
name|this
operator|.
name|recycler
operator|.
name|free
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|final
name|ReferenceCounted
name|touch
parameter_list|(
name|Object
name|hint
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
end_class

end_unit
