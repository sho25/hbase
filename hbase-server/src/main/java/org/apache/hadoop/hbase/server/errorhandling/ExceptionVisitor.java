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
name|server
operator|.
name|errorhandling
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
name|InterfaceStability
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
name|server
operator|.
name|errorhandling
operator|.
name|impl
operator|.
name|ExceptionOrchestrator
import|;
end_import

begin_comment
comment|/**  * Simple visitor interface to update an error listener with an error notification  * @see ExceptionOrchestrator  * @param<T> Type of listener to update  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Unstable
specifier|public
interface|interface
name|ExceptionVisitor
parameter_list|<
name|T
parameter_list|>
block|{
comment|/**    * Visit the listener with the given error, possibly transforming or ignoring the error    * @param listener listener to update    * @param message error message    * @param e exception that caused the error    * @param info general information about the error    */
specifier|public
name|void
name|visit
parameter_list|(
name|T
name|listener
parameter_list|,
name|String
name|message
parameter_list|,
name|Exception
name|e
parameter_list|,
name|Object
modifier|...
name|info
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

