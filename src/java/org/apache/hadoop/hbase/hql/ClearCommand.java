begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hql
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
name|io
operator|.
name|Writer
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
name|HBaseConfiguration
import|;
end_import

begin_comment
comment|/**  * Clears the console screen.  */
end_comment

begin_class
specifier|public
class|class
name|ClearCommand
extends|extends
name|BasicCommand
block|{
specifier|public
name|ClearCommand
parameter_list|(
name|Writer
name|o
parameter_list|)
block|{
name|super
argument_list|(
name|o
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReturnMsg
name|execute
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|HBaseConfiguration
name|conf
parameter_list|)
block|{
name|clear
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|private
name|void
name|clear
parameter_list|()
block|{
name|String
name|osName
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"os.name"
argument_list|)
decl_stmt|;
if|if
condition|(
name|osName
operator|.
name|length
argument_list|()
operator|>
literal|7
operator|&&
name|osName
operator|.
name|subSequence
argument_list|(
literal|0
argument_list|,
literal|7
argument_list|)
operator|.
name|equals
argument_list|(
literal|"Windows"
argument_list|)
condition|)
block|{
try|try
block|{
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|exec
argument_list|(
literal|"cmd /C cls"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
try|try
block|{
name|println
argument_list|(
literal|"Can't clear."
operator|+
name|e
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|e1
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"\033c"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|CommandType
name|getCommandType
parameter_list|()
block|{
return|return
name|CommandType
operator|.
name|SHELL
return|;
block|}
block|}
end_class

end_unit

