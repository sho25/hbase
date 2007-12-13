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
name|shell
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
name|security
operator|.
name|Permission
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
name|Shell
import|;
end_import

begin_comment
comment|/**  * This is intended as a replacement for the default system manager. The goal is  * to intercept System.exit calls and make it throw an exception instead so that  * a System.exit in a jar command program does not fully terminate Shell.  *   * @see ExitException  */
end_comment

begin_class
specifier|public
class|class
name|ShellSecurityManager
extends|extends
name|SecurityManager
block|{
comment|/**    * Override SecurityManager#checkExit. This throws an ExitException(status)    * exception.    *     * @param status the exit status    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"static-access"
argument_list|)
specifier|public
name|void
name|checkExit
parameter_list|(
name|int
name|status
parameter_list|)
block|{
if|if
condition|(
name|status
operator|!=
literal|9999
condition|)
block|{
comment|// throw new ExitException(status);
comment|// I didn't figure out How can catch the ExitException in shell main.
comment|// So, I just Re-launching the shell.
name|Shell
name|shell
init|=
operator|new
name|Shell
argument_list|()
decl_stmt|;
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
name|String
operator|.
name|valueOf
argument_list|(
literal|7
argument_list|)
block|}
decl_stmt|;
try|try
block|{
name|shell
operator|.
name|main
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Override SecurityManager#checkPermission. This does nothing.    *     * @param perm the requested permission.    */
specifier|public
name|void
name|checkPermission
parameter_list|(
name|Permission
name|perm
parameter_list|)
block|{   }
block|}
end_class

end_unit

