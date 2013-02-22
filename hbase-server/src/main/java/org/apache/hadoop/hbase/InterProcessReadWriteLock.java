begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * An interface for a distributed reader-writer lock.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|InterProcessReadWriteLock
block|{
comment|/**    * Obtain a reader lock containing given metadata.    * @param metadata Serialized lock metadata (this may contain information    *                 such as the process owning the lock or the purpose for    *                 which the lock was acquired). Must not be null.    * @return An instantiated InterProcessReadWriteLock instance    */
specifier|public
name|InterProcessLock
name|readLock
parameter_list|(
name|byte
index|[]
name|metadata
parameter_list|)
function_decl|;
comment|/**    * Obtain a writer lock containing given metadata.    * @param metadata See documentation of metadata parameter in readLock()    * @return An instantiated InterProcessReadWriteLock instance    */
specifier|public
name|InterProcessLock
name|writeLock
parameter_list|(
name|byte
index|[]
name|metadata
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

