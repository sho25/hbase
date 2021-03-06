begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|quotas
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * An Exception that is thrown when a space quota is in violation.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|SpaceLimitingException
extends|extends
name|QuotaExceededException
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|2319438922387583600L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SpaceLimitingException
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MESSAGE_PREFIX
init|=
name|SpaceLimitingException
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|": "
decl_stmt|;
specifier|private
specifier|final
name|String
name|policyName
decl_stmt|;
specifier|public
name|SpaceLimitingException
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|super
argument_list|(
name|parseMessage
argument_list|(
name|msg
argument_list|)
argument_list|)
expr_stmt|;
comment|// Hack around ResponseConverter expecting to invoke a single-arg String constructor
comment|// on this class
if|if
condition|(
name|msg
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|SpaceViolationPolicy
name|definedPolicy
range|:
name|SpaceViolationPolicy
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|msg
operator|.
name|indexOf
argument_list|(
name|definedPolicy
operator|.
name|name
argument_list|()
argument_list|)
operator|!=
operator|-
literal|1
condition|)
block|{
name|policyName
operator|=
name|definedPolicy
operator|.
name|name
argument_list|()
expr_stmt|;
return|return;
block|}
block|}
block|}
name|policyName
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
name|SpaceLimitingException
parameter_list|(
name|String
name|policyName
parameter_list|,
name|String
name|msg
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|this
operator|.
name|policyName
operator|=
name|policyName
expr_stmt|;
block|}
specifier|public
name|SpaceLimitingException
parameter_list|(
name|String
name|policyName
parameter_list|,
name|String
name|msg
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|this
operator|.
name|policyName
operator|=
name|policyName
expr_stmt|;
block|}
comment|/**    * Returns the violation policy in effect.    *    * @return The violation policy in effect.    */
specifier|public
name|String
name|getViolationPolicy
parameter_list|()
block|{
return|return
name|this
operator|.
name|policyName
return|;
block|}
specifier|private
specifier|static
name|String
name|parseMessage
parameter_list|(
name|String
name|originalMessage
parameter_list|)
block|{
comment|// Serialization of the exception places a duplicate class name. Try to strip that off if it
comment|// exists. Best effort... Looks something like:
comment|// "org.apache.hadoop.hbase.quotas.SpaceLimitingException: NO_INSERTS A Put is disallowed due
comment|// to a space quota."
if|if
condition|(
name|originalMessage
operator|!=
literal|null
operator|&&
name|originalMessage
operator|.
name|startsWith
argument_list|(
name|MESSAGE_PREFIX
argument_list|)
condition|)
block|{
comment|// If it starts with the class name, rip off the policy too.
try|try
block|{
name|int
name|index
init|=
name|originalMessage
operator|.
name|indexOf
argument_list|(
literal|' '
argument_list|,
name|MESSAGE_PREFIX
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|originalMessage
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Failed to trim exception message"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|originalMessage
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getMessage
parameter_list|()
block|{
return|return
operator|(
name|policyName
operator|==
literal|null
condition|?
literal|"(unknown policy)"
else|:
name|policyName
operator|)
operator|+
literal|" "
operator|+
name|super
operator|.
name|getMessage
argument_list|()
return|;
block|}
block|}
end_class

end_unit

