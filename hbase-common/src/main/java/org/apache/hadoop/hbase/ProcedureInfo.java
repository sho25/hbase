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
name|security
operator|.
name|User
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
name|util
operator|.
name|EnvironmentEdgeManager
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
name|util
operator|.
name|NonceKey
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * Procedure information  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|ProcedureInfo
implements|implements
name|Cloneable
block|{
specifier|private
specifier|final
name|long
name|procId
decl_stmt|;
specifier|private
specifier|final
name|String
name|procName
decl_stmt|;
specifier|private
specifier|final
name|String
name|procOwner
decl_stmt|;
specifier|private
specifier|final
name|ProcedureState
name|procState
decl_stmt|;
specifier|private
specifier|final
name|long
name|parentId
decl_stmt|;
specifier|private
specifier|final
name|NonceKey
name|nonceKey
decl_stmt|;
specifier|private
specifier|final
name|IOException
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
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|ProcedureInfo
parameter_list|(
specifier|final
name|long
name|procId
parameter_list|,
specifier|final
name|String
name|procName
parameter_list|,
specifier|final
name|String
name|procOwner
parameter_list|,
specifier|final
name|ProcedureState
name|procState
parameter_list|,
specifier|final
name|long
name|parentId
parameter_list|,
specifier|final
name|NonceKey
name|nonceKey
parameter_list|,
specifier|final
name|IOException
name|exception
parameter_list|,
specifier|final
name|long
name|lastUpdate
parameter_list|,
specifier|final
name|long
name|startTime
parameter_list|,
specifier|final
name|byte
index|[]
name|result
parameter_list|)
block|{
name|this
operator|.
name|procId
operator|=
name|procId
expr_stmt|;
name|this
operator|.
name|procName
operator|=
name|procName
expr_stmt|;
name|this
operator|.
name|procOwner
operator|=
name|procOwner
expr_stmt|;
name|this
operator|.
name|procState
operator|=
name|procState
expr_stmt|;
name|this
operator|.
name|parentId
operator|=
name|parentId
expr_stmt|;
name|this
operator|.
name|nonceKey
operator|=
name|nonceKey
expr_stmt|;
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
comment|// If the procedure is completed, we should treat exception and result differently
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
name|result
expr_stmt|;
block|}
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"CN_IDIOM_NO_SUPER_CALL"
argument_list|,
name|justification
operator|=
literal|"Intentional; calling super class clone doesn't make sense here."
argument_list|)
specifier|public
name|ProcedureInfo
name|clone
parameter_list|()
block|{
return|return
operator|new
name|ProcedureInfo
argument_list|(
name|procId
argument_list|,
name|procName
argument_list|,
name|procOwner
argument_list|,
name|procState
argument_list|,
name|parentId
argument_list|,
name|nonceKey
argument_list|,
name|exception
argument_list|,
name|lastUpdate
argument_list|,
name|startTime
argument_list|,
name|result
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Procedure="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|procName
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" (id="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|procId
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasParentId
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", parent="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|parentId
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasOwner
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", owner="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|procOwner
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|", state="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|procState
argument_list|)
expr_stmt|;
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", startTime="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|formatTime
argument_list|(
name|now
operator|-
name|startTime
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" ago, lastUpdate="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|StringUtils
operator|.
name|formatTime
argument_list|(
name|now
operator|-
name|startTime
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|" ago"
argument_list|)
expr_stmt|;
if|if
condition|(
name|isFailed
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", exception=\""
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|this
operator|.
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"\""
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|long
name|getProcId
parameter_list|()
block|{
return|return
name|procId
return|;
block|}
specifier|public
name|String
name|getProcName
parameter_list|()
block|{
return|return
name|procName
return|;
block|}
specifier|public
name|boolean
name|hasOwner
parameter_list|()
block|{
return|return
name|procOwner
operator|!=
literal|null
return|;
block|}
specifier|public
name|String
name|getProcOwner
parameter_list|()
block|{
return|return
name|procOwner
return|;
block|}
specifier|public
name|ProcedureState
name|getProcState
parameter_list|()
block|{
return|return
name|procState
return|;
block|}
specifier|public
name|boolean
name|hasParentId
parameter_list|()
block|{
return|return
operator|(
name|parentId
operator|!=
operator|-
literal|1
operator|)
return|;
block|}
specifier|public
name|long
name|getParentId
parameter_list|()
block|{
return|return
name|parentId
return|;
block|}
specifier|public
name|NonceKey
name|getNonceKey
parameter_list|()
block|{
return|return
name|nonceKey
return|;
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
name|IOException
name|getException
parameter_list|()
block|{
if|if
condition|(
name|isFailed
argument_list|()
condition|)
block|{
return|return
name|this
operator|.
name|exception
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|String
name|getExceptionFullMessage
parameter_list|()
block|{
assert|assert
name|isFailed
argument_list|()
assert|;
specifier|final
name|IOException
name|e
init|=
name|getException
argument_list|()
decl_stmt|;
return|return
name|e
operator|.
name|getCause
argument_list|()
operator|+
literal|" - "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
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
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|boolean
name|hasClientAckTime
parameter_list|()
block|{
return|return
name|clientAckTime
operator|!=
operator|-
literal|1
return|;
block|}
annotation|@
name|InterfaceAudience
operator|.
name|Private
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
specifier|public
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
comment|/**    * Check if the user is this procedure's owner    * @param procInfo the procedure to check    * @param user the user    * @return true if the user is the owner of the procedure,    *   false otherwise or the owner is unknown.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
name|boolean
name|isProcedureOwner
parameter_list|(
specifier|final
name|ProcedureInfo
name|procInfo
parameter_list|,
specifier|final
name|User
name|user
parameter_list|)
block|{
if|if
condition|(
name|user
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|String
name|procOwner
init|=
name|procInfo
operator|.
name|getProcOwner
argument_list|()
decl_stmt|;
if|if
condition|(
name|procOwner
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|procOwner
operator|.
name|equals
argument_list|(
name|user
operator|.
name|getShortName
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

