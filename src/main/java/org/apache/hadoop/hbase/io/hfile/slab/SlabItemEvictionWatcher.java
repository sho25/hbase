begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|slab
package|;
end_package

begin_comment
comment|/**  * Interface for objects that want to know when an eviction occurs.  * */
end_comment

begin_interface
interface|interface
name|SlabItemEvictionWatcher
block|{
comment|/**    * This is called as a callback by the EvictionListener in each of the    * SingleSizeSlabCaches.    *    * @param key the key of the item being evicted    * @param boolean callAssignedCache whether we should call the cache which the    *        key was originally assigned to.    */
name|boolean
name|onEviction
parameter_list|(
name|String
name|key
parameter_list|,
name|boolean
name|callAssignedCache
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

