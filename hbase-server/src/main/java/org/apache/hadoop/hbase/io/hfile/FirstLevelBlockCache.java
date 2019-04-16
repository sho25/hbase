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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * In-memory BlockCache that may be backed by secondary layer(s).  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|FirstLevelBlockCache
extends|extends
name|ResizableBlockCache
extends|,
name|HeapSize
block|{
comment|/**    * Whether the cache contains the block with specified cacheKey    *    * @param cacheKey cache key for the block    * @return true if it contains the block    */
name|boolean
name|containsBlock
parameter_list|(
name|BlockCacheKey
name|cacheKey
parameter_list|)
function_decl|;
comment|/**    * Specifies the secondary cache. An entry that is evicted from this cache due to a size    * constraint will be inserted into the victim cache.    *    * @param victimCache the second level cache    * @throws IllegalArgumentException if the victim cache had already been set    */
name|void
name|setVictimCache
parameter_list|(
name|BlockCache
name|victimCache
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

