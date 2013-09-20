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
name|procedure
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
comment|/**  * Task builder to build instances of a {@link ProcedureMember}'s {@link Subprocedure}s.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|SubprocedureFactory
block|{
comment|/**    * Build {@link Subprocedure} when requested.    * @param procName name of the procedure associated with this subprocedure    * @param procArgs  arguments passed from the coordinator about the procedure    * @return {@link Subprocedure} to run or<tt>null</tt> if the no operation should be run    * @throws IllegalArgumentException if the operation could not be run because of errors in the    *           request    * @throws IllegalStateException if the current runner cannot accept any more new requests    */
name|Subprocedure
name|buildSubprocedure
parameter_list|(
name|String
name|procName
parameter_list|,
name|byte
index|[]
name|procArgs
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

