begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|master
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Listener for regionserver events in master.  * @see HMaster#registerRegionServerOperationListener(RegionServerOperationListener)  * @see HMaster#unregisterRegionServerOperationListener(RegionServerOperationListener)  */
end_comment

begin_interface
specifier|public
interface|interface
name|RegionServerOperationListener
block|{
comment|/**    * Called before processing<code>op</code>    * @param op    * @return True if we are to proceed w/ processing.    * @exception IOException    */
specifier|public
name|boolean
name|process
parameter_list|(
specifier|final
name|RegionServerOperation
name|op
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after<code>op</code> has been processed.    * @param op The operation that just completed.    */
specifier|public
name|void
name|processed
parameter_list|(
specifier|final
name|RegionServerOperation
name|op
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

