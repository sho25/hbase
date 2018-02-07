begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
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
comment|/**  * Process related utilities.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ProcessUtils
block|{
specifier|private
specifier|static
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ProcessUtils
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ProcessUtils
parameter_list|()
block|{ }
specifier|public
specifier|static
name|Integer
name|getPid
parameter_list|()
block|{
comment|// JVM_PID is exported by bin/hbase run script
name|String
name|pidStr
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"JVM_PID"
argument_list|)
decl_stmt|;
comment|// in case if it is not set correctly used fallback from mxbean which is implementation specific
if|if
condition|(
name|pidStr
operator|==
literal|null
operator|||
name|pidStr
operator|.
name|trim
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|String
name|name
init|=
name|ManagementFactory
operator|.
name|getRuntimeMXBean
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|!=
literal|null
condition|)
block|{
name|int
name|idx
init|=
name|name
operator|.
name|indexOf
argument_list|(
literal|"@"
argument_list|)
decl_stmt|;
if|if
condition|(
name|idx
operator|!=
operator|-
literal|1
condition|)
block|{
name|pidStr
operator|=
name|name
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|name
operator|.
name|indexOf
argument_list|(
literal|"@"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
try|try
block|{
if|if
condition|(
name|pidStr
operator|!=
literal|null
condition|)
block|{
return|return
name|Integer
operator|.
name|valueOf
argument_list|(
name|pidStr
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|nfe
parameter_list|)
block|{
comment|// ignore
block|}
return|return
literal|null
return|;
block|}
specifier|public
specifier|static
name|Process
name|runCmdAsync
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|cmd
parameter_list|)
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Running command async: "
operator|+
name|cmd
argument_list|)
expr_stmt|;
return|return
operator|new
name|ProcessBuilder
argument_list|(
name|cmd
argument_list|)
operator|.
name|inheritIO
argument_list|()
operator|.
name|start
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

