begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|quotas
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|client
operator|.
name|RegionInfo
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * An interface for concurrently storing and updating the size of a Region.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|RegionSizeStore
extends|extends
name|Iterable
argument_list|<
name|Entry
argument_list|<
name|RegionInfo
argument_list|,
name|RegionSize
argument_list|>
argument_list|>
extends|,
name|HeapSize
block|{
comment|/**    * Returns the size for the give region if one exists. If no size exists, {@code null} is    * returned.    *    * @param regionInfo The region whose size is being fetched.    * @return The size in bytes of the region or null if no size is stored.    */
name|RegionSize
name|getRegionSize
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|)
function_decl|;
comment|/**    * Atomically sets the given {@code size} for a region.    *    * @param regionInfo An identifier for a region.    * @param size The size in bytes of the region.    */
name|void
name|put
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|,
name|long
name|size
parameter_list|)
function_decl|;
comment|/**    * Atomically alter the size of a region.    *    * @param regionInfo The region to update.    * @param delta The change in size for the region, positive or negative.    */
name|void
name|incrementRegionSize
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|,
name|long
name|delta
parameter_list|)
function_decl|;
comment|/**    * Removes the mapping for the given key, returning the value if one exists in the store.    *    * @param regionInfo The key to remove from the store    * @return The value removed from the store if one exists, otherwise null.    */
name|RegionSize
name|remove
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|)
function_decl|;
comment|/**    * Returns the number of entries in the store.    *    * @return The number of entries in the store.    */
name|int
name|size
parameter_list|()
function_decl|;
comment|/**    * Returns if the store is empty.    *    * @return true if there are no entries in the store, otherwise false.    */
name|boolean
name|isEmpty
parameter_list|()
function_decl|;
comment|/**    * Removes all entries from the store.    */
name|void
name|clear
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

