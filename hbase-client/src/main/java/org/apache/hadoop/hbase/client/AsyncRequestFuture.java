begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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
name|java
operator|.
name|io
operator|.
name|InterruptedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * The context used to wait for results from one submit call.  * 1) If AsyncProcess is set to track errors globally, and not per call (for HTable puts),  *    then errors and failed operations in this object will reflect global errors.  * 2) If submit call is made with needResults false, results will not be saved.  *  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|AsyncRequestFuture
block|{
specifier|public
name|boolean
name|hasError
parameter_list|()
function_decl|;
specifier|public
name|RetriesExhaustedWithDetailsException
name|getErrors
parameter_list|()
function_decl|;
specifier|public
name|List
argument_list|<
name|?
extends|extends
name|Row
argument_list|>
name|getFailedOperations
parameter_list|()
function_decl|;
specifier|public
name|Object
index|[]
name|getResults
parameter_list|()
throws|throws
name|InterruptedIOException
function_decl|;
comment|/** Wait until all tasks are executed, successfully or not. */
specifier|public
name|void
name|waitUntilDone
parameter_list|()
throws|throws
name|InterruptedIOException
function_decl|;
block|}
end_interface

end_unit

