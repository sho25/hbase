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
name|procedure2
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
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Once a Procedure completes the ProcedureExecutor takes all the useful  * information of the procedure (e.g. exception/result) and creates a ProcedureResult.  * The user of the Procedure framework will get the procedure result with  * procedureExecutor.getResult(procId)  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ProcedureResult
block|{
specifier|private
specifier|final
name|RemoteProcedureException
name|exception
decl_stmt|;
specifier|private
specifier|final
name|long
name|lastUpdate
decl_stmt|;
specifier|private
specifier|final
name|long
name|startTime
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|result
decl_stmt|;
specifier|private
name|long
name|clientAckTime
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|ProcedureResult
parameter_list|(
specifier|final
name|long
name|startTime
parameter_list|,
specifier|final
name|long
name|lastUpdate
parameter_list|,
specifier|final
name|RemoteProcedureException
name|exception
parameter_list|)
block|{
name|this
operator|.
name|lastUpdate
operator|=
name|lastUpdate
expr_stmt|;
name|this
operator|.
name|startTime
operator|=
name|startTime
expr_stmt|;
name|this
operator|.
name|exception
operator|=
name|exception
expr_stmt|;
name|this
operator|.
name|result
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|ProcedureResult
parameter_list|(
specifier|final
name|long
name|startTime
parameter_list|,
specifier|final
name|long
name|lastUpdate
parameter_list|,
specifier|final
name|byte
index|[]
name|result
parameter_list|)
block|{
name|this
operator|.
name|lastUpdate
operator|=
name|lastUpdate
expr_stmt|;
name|this
operator|.
name|startTime
operator|=
name|startTime
expr_stmt|;
name|this
operator|.
name|exception
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|result
operator|=
name|result
expr_stmt|;
block|}
specifier|public
name|boolean
name|isFailed
parameter_list|()
block|{
return|return
name|exception
operator|!=
literal|null
return|;
block|}
specifier|public
name|RemoteProcedureException
name|getException
parameter_list|()
block|{
return|return
name|exception
return|;
block|}
specifier|public
name|boolean
name|hasResultData
parameter_list|()
block|{
return|return
name|result
operator|!=
literal|null
return|;
block|}
specifier|public
name|byte
index|[]
name|getResult
parameter_list|()
block|{
return|return
name|result
return|;
block|}
specifier|public
name|long
name|getStartTime
parameter_list|()
block|{
return|return
name|startTime
return|;
block|}
specifier|public
name|long
name|getLastUpdate
parameter_list|()
block|{
return|return
name|lastUpdate
return|;
block|}
specifier|public
name|long
name|executionTime
parameter_list|()
block|{
return|return
name|lastUpdate
operator|-
name|startTime
return|;
block|}
specifier|public
name|boolean
name|hasClientAckTime
parameter_list|()
block|{
return|return
name|clientAckTime
operator|>
literal|0
return|;
block|}
specifier|public
name|long
name|getClientAckTime
parameter_list|()
block|{
return|return
name|clientAckTime
return|;
block|}
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|protected
name|void
name|setClientAckTime
parameter_list|(
specifier|final
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|clientAckTime
operator|=
name|timestamp
expr_stmt|;
block|}
block|}
end_class

end_unit

