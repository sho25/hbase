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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|ref
operator|.
name|Reference
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|ref
operator|.
name|WeakReference
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
name|ObjectPool
operator|.
name|ObjectFactory
import|;
end_import

begin_comment
comment|/**  * A {@code WeakReference} based shared object pool.  * The objects are kept in weak references and  * associated with keys which are identified by the {@code equals} method.  * The objects are created by {@link ObjectFactory} on demand.  * The object creation is expected to be lightweight,  * and the objects may be excessively created and discarded.  * Thread safe.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|WeakObjectPool
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
extends|extends
name|ObjectPool
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
block|{
specifier|public
name|WeakObjectPool
parameter_list|(
name|ObjectFactory
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|objectFactory
parameter_list|)
block|{
name|super
argument_list|(
name|objectFactory
argument_list|)
expr_stmt|;
block|}
specifier|public
name|WeakObjectPool
parameter_list|(
name|ObjectFactory
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|objectFactory
parameter_list|,
name|int
name|initialCapacity
parameter_list|)
block|{
name|super
argument_list|(
name|objectFactory
argument_list|,
name|initialCapacity
argument_list|)
expr_stmt|;
block|}
specifier|public
name|WeakObjectPool
parameter_list|(
name|ObjectFactory
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|objectFactory
parameter_list|,
name|int
name|initialCapacity
parameter_list|,
name|int
name|concurrencyLevel
parameter_list|)
block|{
name|super
argument_list|(
name|objectFactory
argument_list|,
name|initialCapacity
argument_list|,
name|concurrencyLevel
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Reference
argument_list|<
name|V
argument_list|>
name|createReference
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|obj
parameter_list|)
block|{
return|return
operator|new
name|WeakObjectReference
argument_list|(
name|key
argument_list|,
name|obj
argument_list|)
return|;
block|}
specifier|private
class|class
name|WeakObjectReference
extends|extends
name|WeakReference
argument_list|<
name|V
argument_list|>
block|{
specifier|final
name|K
name|key
decl_stmt|;
name|WeakObjectReference
parameter_list|(
name|K
name|key
parameter_list|,
name|V
name|obj
parameter_list|)
block|{
name|super
argument_list|(
name|obj
argument_list|,
name|staleRefQueue
argument_list|)
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|K
name|getReferenceKey
parameter_list|(
name|Reference
argument_list|<
name|V
argument_list|>
name|ref
parameter_list|)
block|{
return|return
operator|(
operator|(
name|WeakObjectReference
operator|)
name|ref
operator|)
operator|.
name|key
return|;
block|}
block|}
end_class

end_unit

