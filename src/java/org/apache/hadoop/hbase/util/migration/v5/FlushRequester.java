begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|migration
operator|.
name|v5
package|;
end_package

begin_comment
comment|/**  * Implementors of this interface want to be notified when an HRegion  * determines that a cache flush is needed. A FlushRequester (or null)  * must be passed to the HRegion constructor so it knows who to call when it  * has a filled memcache.  */
end_comment

begin_interface
specifier|public
interface|interface
name|FlushRequester
block|{
comment|/**    * Tell the listener the cache needs to be flushed.    *     * @param region the HRegion requesting the cache flush    */
name|void
name|request
parameter_list|(
name|HRegion
name|region
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

