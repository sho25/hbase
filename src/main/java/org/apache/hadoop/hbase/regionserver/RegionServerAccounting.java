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
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
import|;
end_import

begin_comment
comment|/**  * RegionServerAccounting keeps record of some basic real time information about  * the Region Server. Currently, it only keeps record the global memstore size.   */
end_comment

begin_class
specifier|public
class|class
name|RegionServerAccounting
block|{
specifier|private
specifier|final
name|AtomicLong
name|atomicGlobalMemstoreSize
init|=
operator|new
name|AtomicLong
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|/**    * @return the global Memstore size in the RegionServer    */
specifier|public
name|long
name|getGlobalMemstoreSize
parameter_list|()
block|{
return|return
name|atomicGlobalMemstoreSize
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * @param memStoreSize the Memstore size will be added to     *        the global Memstore size     * @return the global Memstore size in the RegionServer     */
specifier|public
name|long
name|addAndGetGlobalMemstoreSize
parameter_list|(
name|long
name|memStoreSize
parameter_list|)
block|{
return|return
name|atomicGlobalMemstoreSize
operator|.
name|addAndGet
argument_list|(
name|memStoreSize
argument_list|)
return|;
block|}
block|}
end_class

end_unit

